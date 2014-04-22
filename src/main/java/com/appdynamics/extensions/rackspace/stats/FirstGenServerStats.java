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
	
	private static final String flavorsUri = "/flavors/detail";

	/**
	 * Fetches metrics issuing a Http Request to the FirstGenServer url specific
	 * to the default datacenter and returns as a Map<ServerName,
	 * Map<MetricName, MetricValue>>
	 */
	@Override
	public Map<String, Map<String, Long>> getMetrics(String authToken, String url) {
		JsonNode serviceResponse = getServiceResponse(url + uri, authToken);

		List<ServerFlavor> serverFlavors = populateServerFlavors(url, authToken);

		Map<String, Map<String, Long>> serverStats = new HashMap<String, Map<String, Long>>();
		JsonNode serversNode = serviceResponse.get("servers");
		for (JsonNode server : serversNode) {
			Map<String, Long> stats = new HashMap<String, Long>();
			stats.put("Progress", server.path("progress").asLong());
			String flavorId = server.path("flavorId").asText();
			for (ServerFlavor flavor : serverFlavors) {
				if (flavor.getId().equals(flavorId)) {
					stats.put("RAM", Long.valueOf(flavor.getRam()));
					stats.put("Swap", Long.valueOf(flavor.getSwap()));
					stats.put("vCPUs", Long.valueOf(flavor.getVcpus()));
					stats.put("Disk Space", Long.valueOf(flavor.getDisk()));
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
		JsonNode serviceResponse = getServiceResponse(url + flavorsUri, authToken);
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
