/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
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
