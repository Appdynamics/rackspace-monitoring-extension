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
package com.appdynamics.extensions.rackspace.stats;

import java.util.HashMap;
import java.util.Map;

import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.rackspace.exception.RackspaceMonitorException;
import com.fasterxml.jackson.databind.JsonNode;

public class LoadBalancerStats extends Stats {

	public static final String metricPath = "LoadBalancers |%s|%s|";

	private static final String uri = "/loadbalancers";

	public LoadBalancerStats(SimpleHttpClient httpClient) {
		super(httpClient);
	}

	/**
	 * Fetches metrics issuing a Http Request to the LoadBalancer url specific
	 * to the DataCenter and returns as a Map<Loadbalancer, Map<MetricName,
	 * MetricValue>>
	 * 
	 * @throws RackspaceMonitorException
	 */
	@Override
	public Map<String, Map<String, Long>> getMetrics(String authToken, String url) throws RackspaceMonitorException {
		JsonNode serviceResponse = getServiceResponse(url + uri, authToken);
		Map<String, Map<String, Long>> loadBalancerStats = getMetricsFromNode(serviceResponse);
		return loadBalancerStats;
	}

	private Map<String, Map<String, Long>> getMetricsFromNode(JsonNode serviceResponse) {
		Map<String, Map<String, Long>> instanceStats = new HashMap<String, Map<String, Long>>();
		JsonNode loadBalancersNode = serviceResponse.get("loadBalancers");

		for (JsonNode loadBalancer : loadBalancersNode) {
			Map<String, Long> stats = new HashMap<String, Long>();
			stats.put("Status", Long.valueOf(Status.valueOf(loadBalancer.path("status").asText()).statusInt));
			stats.put("Node Count", loadBalancer.path("nodeCount").asLong());

			instanceStats.put(loadBalancer.path("name").asText(), stats);
		}
		return instanceStats;
	}
}
