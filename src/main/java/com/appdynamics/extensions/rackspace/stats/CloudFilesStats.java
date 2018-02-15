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

public class CloudFilesStats extends Stats {

	public static final String metricPath = "Files |%s|%s|";

	private static final String queryString = "?format=json";

	public CloudFilesStats(SimpleHttpClient httpClient) {
		super(httpClient);
	}

	/**
	 * Fetches metrics issuing a Http Request to the CloudFiles url specific to
	 * the DataCenter and returns as a Map<ContainerName, Map<MetricName,
	 * MetricValue>>
	 * 
	 * @throws RackspaceMonitorException
	 */
	public Map<String, Map<String, Long>> getMetrics(String authToken, String url) throws RackspaceMonitorException {

		Map<String, Map<String, Long>> stats = new HashMap<String, Map<String, Long>>();

		JsonNode containersNode = getServiceResponse(url + queryString, authToken);

		for (JsonNode container : containersNode) {
			Map<String, Long> containerStats = new HashMap<String, Long>();
			String containerName = container.path("name").asText();
			containerStats.put("Count", container.path("count").asLong());
			containerStats.put("Bytes", container.path("bytes").asLong());
			stats.put(containerName, containerStats);
		}
		return stats;

	}
}
