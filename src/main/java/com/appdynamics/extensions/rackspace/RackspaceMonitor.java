/**
 * Copyright 2014 AppDynamics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.appdynamics.extensions.rackspace;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.rackspace.common.AccountBase;
import com.appdynamics.extensions.rackspace.common.Authenticator;
import com.appdynamics.extensions.rackspace.exception.RackspaceMonitorException;
import com.appdynamics.extensions.rackspace.stats.CloudFilesStats;
import com.appdynamics.extensions.rackspace.stats.DatabaseStats;
import com.appdynamics.extensions.rackspace.stats.FirstGenServerStats;
import com.appdynamics.extensions.rackspace.stats.LoadBalancerStats;
import com.appdynamics.extensions.rackspace.stats.NextGenServerStats;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

public class RackspaceMonitor extends AManagedMonitor {

	private static Logger LOG = Logger.getLogger("com.singularity.extensions.RackspaceMonitor");

	private static final String DEFAULT_METRIC_PREFIX = "Custom Metrics|Rackspace|";

	private static String metric_path_prefix = DEFAULT_METRIC_PREFIX;

	private SimpleHttpClient httpClient;

	public RackspaceMonitor() {
		String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
		LOG.info(msg);
		System.out.println(msg);
	}

	/**
	 * Entry point to the extension, this is main execution method which
	 * collects the metrics and uploads them to the AppDynamics Controller
	 */
	public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext arg1) throws TaskExecutionException {

		LOG.info("Starting the Rackspace Monitoring Task");

		try {
			taskArguments = checkArguments(taskArguments);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Valid task arguments in monitor.xml");
			}

			httpClient = SimpleHttpClient.builder(new HashMap<String, String>()).build();

			Authenticator authenticator = initAuthenticator(taskArguments);
			Map<String, Map<String, String>> endpoints = authenticator.getEndpoints();
			String authToken = authenticator.getAuthToken();
			String defRegion = authenticator.getDefaultRegion();

			// Fetches and prints metrics
			populateFirstGenServerStats(endpoints.get("cloudServers"), authToken, defRegion);

			populateAccountLimits(endpoints.get("cloudServersOpenStack").get(defRegion), authToken);

			populateNextGenServerStats(endpoints.get("cloudServersOpenStack"), authToken);

			populateFileStats(endpoints.get("cloudFiles"), authToken);

			populateDatabaseStats(endpoints.get("cloudDatabases"), authToken);

			populateLoadBalancerStats(endpoints.get("cloudLoadBalancers"), authToken);

			LOG.info("Completed Rackspace Monitor Task");
		} catch (Exception e) {
			LOG.error("Error while running Rackspace Monitor", e);
			throw new TaskExecutionException(e);
		}
		return new TaskOutput("Rackspace Stats uploaded succcessfully");
	}

	private Authenticator initAuthenticator(Map<String, String> taskArguments) throws RackspaceMonitorException {
		String userName = taskArguments.get("username");
		String apiKey = taskArguments.get("api-key");
		String accountBase = taskArguments.get("account-base");

		// Authenticates and retrieves the token and endPoints from response
		String authEndPointUrl = getAuthUrl(accountBase);
		Authenticator authenticator = new Authenticator(httpClient);
		authenticator.authenticate(userName, apiKey, authEndPointUrl);
		return authenticator;
	}

	private Map<String, String> checkArguments(Map<String, String> taskArguments) {
		if (taskArguments == null) {
			throw new IllegalArgumentException("No task arguments in monitor.xml");
		}
		if (argumentInvalid(taskArguments, "username") || argumentInvalid(taskArguments, "api-key") || argumentInvalid(taskArguments, "account-base")) {
			throw new IllegalArgumentException("Required task arguments missing in monitor.xml. Please provide rackspace parameters in monitor.xml");
		}
		String prefix = taskArguments.get("metric-prefix");
		if (prefix != null && prefix != "") {
			metric_path_prefix = prefix;
			if (!metric_path_prefix.endsWith("|")) {
				metric_path_prefix += "|";
			}
			taskArguments.put("metric-prefix", metric_path_prefix);
		}
		return taskArguments;

	}

	private boolean argumentInvalid(Map<String, String> taskArguments, String argumentKey) {
		String value = taskArguments.get(argumentKey);
		return value == null || "".equals(value.trim());
	}

	/**
	 * Determines the authentication endpoint url based on the region (US/UK)
	 * where the account is based. The region one of the inputs in monitor.xml
	 * 
	 * @param accountBase
	 * @return authUrl
	 * @throws RackspaceMonitorException
	 */
	private String getAuthUrl(String accountBase) throws RackspaceMonitorException {
		String authUrl;
		try {
			authUrl = AccountBase.valueOf(accountBase).getAuthUrl();
		} catch (IllegalArgumentException e) {
			LOG.error("Specify valid account base (US/UK in monitor.xml)");
			throw new RackspaceMonitorException(e);
		}
		return authUrl;

	}

	/**
	 * Populates and prints FirstGen server metrics to AppDynamics Controller,
	 * arguments being Map<Region, publicUrl>, authentication token, and default
	 * region.
	 * 
	 * @param serviceEndPoints
	 * @param authToken
	 * @param defRegion
	 */
	private void populateFirstGenServerStats(Map<String, String> serviceEndPoints, String authToken, String defRegion) {
		if (serviceEndPoints != null) {
			try {
				String serviceUrl = serviceEndPoints.get(defRegion);
				FirstGenServerStats cloudServerStats = new FirstGenServerStats(httpClient);
				Map<String, Map<String, Long>> metricsMap = cloudServerStats.getMetrics(authToken, serviceUrl);
				for (Entry<String, Map<String, Long>> serverMetrics : metricsMap.entrySet()) {
					String serverName = serverMetrics.getKey();
					Map<String, Long> serverStats = serverMetrics.getValue();
					for (Entry<String, Long> stats : serverStats.entrySet()) {
						printMetric(String.format(FirstGenServerStats.metricPath, defRegion, serverName), stats.getKey(), stats.getValue());
					}
				}
			} catch (Exception e) {
				LOG.error("Error fetching First GenServer stats ", e);
			}
		} else {
			LOG.error("Skipping fetching FirstGen Server Stats: Missing service with name 'cloudServers' in the authentication response (serviceCatalog - endPoints)");
		}
	}

	/**
	 * Fetches account limits for SecondGenServers and uploads them to
	 * AppDynamics Controller, the input arguments being endpointUrl for default
	 * region and authentication token
	 * 
	 * @param url
	 * @param authToken
	 */
	private void populateAccountLimits(String url, String authToken) {
		try {
			Map<String, Long> limitsMap = new NextGenServerStats(httpClient).getLimits(url, authToken);
			for (Entry<String, Long> limits : limitsMap.entrySet()) {
				printMetric(NextGenServerStats.limitsPath, limits.getKey(), limits.getValue());
			}
		} catch (Exception e) {
			LOG.error("Error fetching Account limits for NextGen Server", e);
		}
	}

	/**
	 * Populates and prints NextGen server metrics to AppDynamics Controller
	 * 
	 * @param serviceEndPoints
	 * @param authToken
	 */
	private void populateNextGenServerStats(Map<String, String> serviceEndPoints, String authToken) {
		if (serviceEndPoints != null) {
			for (Entry<String, String> regionEndPoint : serviceEndPoints.entrySet()) {
				try {
					NextGenServerStats cloudServerStats = new NextGenServerStats(httpClient);
					Map<String, Map<String, Long>> metricsMap = cloudServerStats.getMetrics(authToken, regionEndPoint.getValue());
					for (Entry<String, Map<String, Long>> serverMetrics : metricsMap.entrySet()) {
						String serverName = serverMetrics.getKey();
						Map<String, Long> serverStats = serverMetrics.getValue();
						for (Entry<String, Long> stats : serverStats.entrySet()) {
							printMetric(String.format(NextGenServerStats.metricPath, regionEndPoint.getKey(), serverName), stats.getKey(),
									stats.getValue());
						}
					}
				} catch (Exception e) {
					LOG.error("Error populating NextGen Server Stats for region " + regionEndPoint.getKey(), e);
				}
			}
		} else {
			LOG.error("Skipping fetching Next Gen Server Stats: Missing service with name 'cloudServersOpenStack' in the authentication response (serviceCatalog - endPoints)");
		}
	}

	/**
	 * Populates and prints Files metrics to AppDynamics Controller
	 * 
	 * @param serviceEndPoints
	 * @param authToken
	 */
	private void populateFileStats(Map<String, String> serviceEndPoints, String authToken) {
		if (serviceEndPoints != null) {
			for (Entry<String, String> regionEndPoint : serviceEndPoints.entrySet()) {
				try {
					CloudFilesStats fileStats = new CloudFilesStats(httpClient);
					Map<String, Map<String, Long>> metricsMap = fileStats.getMetrics(authToken, regionEndPoint.getValue());
					for (Entry<String, Map<String, Long>> containerMetrics : metricsMap.entrySet()) {
						String containerName = containerMetrics.getKey();
						Map<String, Long> containerStats = containerMetrics.getValue();
						for (Entry<String, Long> stats : containerStats.entrySet()) {
							printMetric(String.format(CloudFilesStats.metricPath, regionEndPoint.getKey(), containerName), stats.getKey(),
									stats.getValue());
						}
					}
				} catch (Exception e) {
					LOG.error("Error fetching File metrics for region " + regionEndPoint.getKey(), e);
				}
			}
		} else {
			LOG.error("Skipping fetching File Stats: Missing service with name 'cloudFiles' in the authentication response (serviceCatalog - endPoints)");
		}

	}

	/**
	 * Populates and prints Database metrics to AppDynamics Controller
	 * 
	 * @param serviceEndPoints
	 * @param authToken
	 */
	private void populateDatabaseStats(Map<String, String> serviceEndPoints, String authToken) {
		if (serviceEndPoints != null) {
			for (Entry<String, String> regionEndPoint : serviceEndPoints.entrySet()) {
				try {
					DatabaseStats databaseStats = new DatabaseStats(httpClient);
					Map<String, Map<String, Long>> metricsMap = databaseStats.getMetrics(authToken, regionEndPoint.getValue());
					for (Entry<String, Map<String, Long>> instanceMetrics : metricsMap.entrySet()) {
						String instanceName = instanceMetrics.getKey();
						Map<String, Long> instanceStats = instanceMetrics.getValue();
						for (Entry<String, Long> stats : instanceStats.entrySet()) {
							printMetric(String.format(DatabaseStats.metricPath, regionEndPoint.getKey(), instanceName), stats.getKey(),
									stats.getValue());
						}
					}
				} catch (Exception e) {
					LOG.error("Error fetching Database Stats for region " + regionEndPoint.getKey(), e);
				}
			}
		} else {
			LOG.error("Skipping fetching Database Stats: Missing service with name 'cloudDatabases' in the authentication response (serviceCatalog - endPoints)");
		}

	}

	/**
	 * Populates and prints LoadBalancer metrics to AppDynamics Controller
	 * 
	 * @param serviceEndPoints
	 * @param authToken
	 */
	private void populateLoadBalancerStats(Map<String, String> serviceEndPoints, String authToken) {
		if (serviceEndPoints != null) {
			for (Entry<String, String> regionEndPoint : serviceEndPoints.entrySet()) {
				try {
					LoadBalancerStats loadbalancerStats = new LoadBalancerStats(httpClient);
					Map<String, Map<String, Long>> metricsMap = loadbalancerStats.getMetrics(authToken, regionEndPoint.getValue());
					for (Entry<String, Map<String, Long>> instanceMetrics : metricsMap.entrySet()) {
						String instanceName = instanceMetrics.getKey();
						Map<String, Long> instanceStats = instanceMetrics.getValue();
						for (Entry<String, Long> stats : instanceStats.entrySet()) {
							printMetric(String.format(LoadBalancerStats.metricPath, regionEndPoint.getKey(), instanceName), stats.getKey(),
									stats.getValue());
						}
					}
				} catch (Exception e) {
					LOG.error("Error fetching load balancer metrics for region " + regionEndPoint.getKey(), e);
				}
			}
		} else {
			LOG.error("Skipping fetching Loadbalancer Stats: Missing service with name 'cloudLoadbalancers' in the authentication response (serviceCatalog - endPoints)");
		}

	}

	private void printMetric(String metricPath, String metricName, Object metricValue) {
		printMetric(getMetricPrefix() + metricPath, metricName, metricValue, MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
				MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE, MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
	}

	private void printMetric(String metricPath, String metricName, Object metricValue, String aggregation, String timeRollup, String cluster) {
		MetricWriter metricWriter = super.getMetricWriter(metricPath + metricName, aggregation, timeRollup, cluster);
		if (metricValue != null) {
			metricWriter.printMetric(String.valueOf(metricValue));
		}
	}

	private String getMetricPrefix() {
		return metric_path_prefix;
	}

	private static String getImplementationVersion() {
		return RackspaceMonitor.class.getPackage().getImplementationTitle();
	}
}
