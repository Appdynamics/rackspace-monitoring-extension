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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DatabaseStats extends Stats {

	public static final String metricPath = "Databases |%s|%s|";

	private static final String instancesUri = "/instances";

	private enum Status {
		BUILD(1), REBOOT(2), ACTIVE(3), BACKUP(4), BLOCKED(5), RESIZE(6), SHUTDOWN(7), ERROR(8);
		private int statusInt;

		private Status(int val) {
			this.statusInt = val;
		}
	}

	/**
	 * Fetches metrics issuing a Http Request to the Databases url specific to
	 * the DataCenter and returns as a Map<InstanceName, Map<MetricName,
	 * MetricValue>>
	 */
	@Override
	public Map<String, Map<String, Object>> getMetrics(String authToken, String url) {
		JsonNode serviceResponse = getServiceResponse(url + instancesUri, authToken);
		Map<String, Map<String, Object>> instanceStats = getMetricsFromNode(serviceResponse);
		return instanceStats;
	}

	private Map<String, Map<String, Object>> getMetricsFromNode(JsonNode serviceResponse) {
		Map<String, Map<String, Object>> instanceStats = new HashMap<String, Map<String, Object>>();
		JsonNode instancesNode = serviceResponse.get("instances");

		for (JsonNode instance : instancesNode) {
			Map<String, Object> stats = new HashMap<String, Object>();
			stats.put("status", Status.valueOf(instance.path("status").asText()).statusInt);
			stats.put("flavor", instance.path("flavor").path("id").asText());
			stats.put("volume-size", instance.path("volume").path("size").asText());

			instanceStats.put(instance.path("name").asText(), stats);
		}
		return instanceStats;
	}

	public static void main(String[] args) throws IOException {
		JsonNode node = new ObjectMapper().readValue(new File(
				"/home/balakrishnav/AppDynamics/ExtensionsProject/rackspace-monitoring-extension/DBResp"), JsonNode.class);
		DatabaseStats stats = new DatabaseStats();
		Map<String, Map<String, Object>> map = stats.getMetricsFromNode(node);
		Map<String, Object> map2 = map.get("json_rack_instance");
		System.out.println((map2.get("status")));
	}

}
