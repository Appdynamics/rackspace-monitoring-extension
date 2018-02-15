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

public class DatabaseStats extends Stats {

	public static final String metricPath = "Databases |%s|%s|";

	private static final String instancesUri = "/instances";

	public DatabaseStats(SimpleHttpClient httpClient) {
		super(httpClient);
	}

	/**
	 * Fetches metrics issuing a Http Request to the Databases url specific to
	 * the DataCenter and returns as a Map<InstanceName, Map<MetricName,
	 * MetricValue>>
	 * 
	 * @throws RackspaceMonitorException
	 */
	@Override
	public Map<String, Map<String, Long>> getMetrics(String authToken, String url) throws RackspaceMonitorException {
		JsonNode serviceResponse = getServiceResponse(url + instancesUri, authToken);
		Map<String, Map<String, Long>> instanceStats = getMetricsFromNode(serviceResponse);
		return instanceStats;
	}

	private Map<String, Map<String, Long>> getMetricsFromNode(JsonNode serviceResponse) {
		Map<String, Map<String, Long>> instanceStats = new HashMap<String, Map<String, Long>>();
		JsonNode instancesNode = serviceResponse.get("instances");

		for (JsonNode instance : instancesNode) {
			Map<String, Long> stats = new HashMap<String, Long>();
			stats.put("Status", Long.valueOf(Status.valueOf(instance.path("status").asText()).statusInt));
			stats.put("Volume-size", instance.path("volume").path("size").asLong());

			instanceStats.put(instance.path("name").asText(), stats);
		}
		return instanceStats;
	}

}
