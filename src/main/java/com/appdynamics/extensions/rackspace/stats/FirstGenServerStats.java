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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class FirstGenServerStats extends Stats {

	public static final String metricPath = "FirstGenServers |%s|%s|";

	private static final String uri = "/servers/detail";

	/**
	 * Fetches metrics issuing a Http Request to the FirstGenServer url specific
	 * to the default datacenter and returns as a Map<ServerName,
	 * Map<MetricName, MetricValue>>
	 */
	@Override
	public Map<String, Map<String, Object>> getMetrics(String authToken, String url) {
		JsonNode serviceResponse = getServiceResponse(url + uri, authToken);

		List<ServerFlavor> serverFlavors = populateServerFlavors(url, authToken);

		Map<String, Map<String, Object>> serverStats = new HashMap<String, Map<String, Object>>();
		JsonNode serversNode = serviceResponse.get("servers");
		for (JsonNode server : serversNode) {
			Map<String, Object> stats = new HashMap<String, Object>();
			stats.put("progress", server.path("progress").asText());
			String flavorId = server.path("flavorId").asText();
			for (ServerFlavor flavor : serverFlavors) {
				if (flavor.getId().equals(flavorId)) {
					stats.put("ram", flavor.getRam());
					stats.put("swap", flavor.getSwap());
					stats.put("vcpus", flavor.getVcpus());
					stats.put("disk", flavor.getDisk());
				}
			}
			serverStats.put(server.path("name").asText(), stats);
		}
		return serverStats;
	}

	/**
	 * Fetches ServerFlavours by issuing a Http Request to determine the
	 * params(RAM, Disk, etc) based on ID
	 * 
	 * @param url
	 * @param authToken
	 * @return
	 */
	private List<ServerFlavor> populateServerFlavors(String url, String authToken) {
		JsonNode serviceResponse = getServiceResponse(url + "/flavors/detail", authToken);
		JsonNode flavors = serviceResponse.get("flavors");
		List<ServerFlavor> serverFlavors = new ArrayList<ServerFlavor>();
		for (JsonNode flavor : flavors) {
			ServerFlavor serverFlavor = new ServerFlavor();
			serverFlavor.setId(flavor.path("id").asText());
			serverFlavor.setName(flavor.path("name").asText());
			serverFlavor.setRam(flavor.path("ram").asInt());
			serverFlavor.setDisk(flavor.path("disk").asInt());
			serverFlavors.add(serverFlavor);
		}
		return serverFlavors;
	}

}
