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

public class LoadBalancerStats extends Stats {

	public static final String metricPath = "LoadBalancers |%s|%s|";

	private static final String uri = "/loadbalancers";

	private enum Status {
		ACTIVE(1), BUILD(2), PENDING_UPDATE(3), PENDING_DELETE(4), SUSPENDED(5), ERROR(6), DELETED(7);
		private int statusInt;

		private Status(int val) {
			this.statusInt = val;
		}
	}

	/**
	 * Fetches metrics issuing a Http Request to the LoadBalancer url specific to
	 * the DataCenter and returns as a Map<Loadbalancer, Map<MetricName,
	 * MetricValue>>
	 */
	@Override
	public Map<String, Map<String, Object>> getMetrics(String authToken, String url) {
		JsonNode serviceResponse = getServiceResponse(url + uri, authToken);
		Map<String, Map<String, Object>> loadBalancerStats = getMetricsFromNode(serviceResponse);
		return loadBalancerStats;
	}

	private Map<String, Map<String, Object>> getMetricsFromNode(JsonNode serviceResponse) {
		Map<String, Map<String, Object>> instanceStats = new HashMap<String, Map<String, Object>>();
		JsonNode loadBalancersNode = serviceResponse.get("loadBalancers");

		for (JsonNode loadBalancer : loadBalancersNode) {
			Map<String, Object> stats = new HashMap<String, Object>();
			stats.put("status", Status.valueOf(loadBalancer.path("status").asText()).statusInt);
			stats.put("nodeCount", loadBalancer.path("nodeCount").asInt());

			instanceStats.put(loadBalancer.path("name").asText(), stats);
		}
		return instanceStats;
	}

	public static void main(String[] args) throws IOException {
		JsonNode node = new ObjectMapper().readValue(new File(
				"/home/balakrishnav/AppDynamics/ExtensionsProject/rackspace-monitoring-extension/LoadbalancersResp"), JsonNode.class);
		LoadBalancerStats stats = new LoadBalancerStats();
		Map<String, Map<String, Object>> map = stats.getMetricsFromNode(node);
		Map<String, Object> map2 = map.get("lb-site2");
		System.out.println((map2.get("status")));
		System.out.println((map2.get("nodeCount")));

	}

}
