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

public class DatabaseStats extends Stats {

	public static final String metricPath = "Databases |%s|%s|";

	private static final String instancesUri = "/instances";

	/**
	 * Fetches metrics issuing a Http Request to the Databases url specific to
	 * the DataCenter and returns as a Map<InstanceName, Map<MetricName,
	 * MetricValue>>
	 */
	@Override
	public Map<String, Map<String, Long>> getMetrics(String authToken, String url) {
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
