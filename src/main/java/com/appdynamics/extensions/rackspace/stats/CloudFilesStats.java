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

import com.fasterxml.jackson.databind.JsonNode;

public class CloudFilesStats extends Stats {

	public static final String metricPath = "Files |%s|%s|";

	private static final String queryString = "?format=json";

	/**
	 * Fetches metrics issuing a Http Request to the CloudFiles url specific to
	 * the DataCenter and returns as a Map<ContainerName, Map<MetricName,
	 * MetricValue>>
	 */
	public Map<String, Map<String, Long>> getMetrics(String authToken, String url) {

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
