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

public class NextGenServerStats extends Stats {

	public static final String metricPath = "NextGenServers |%s|%s|";

	public static final String limitsPath = "NextGenServers |Limits|";

	private static final String uri = "/servers/detail";

	private static final String limitsUri = "/limits";

	/**
	 * Fetches metrics issuing a Http Request to the NextGenServer url specific
	 * to the DataCenter and returns as a Map<ServerName, Map<MetricName,
	 * MetricValue>>
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
			stats.put("Status", Long.valueOf(Status.valueOf(server.path("status").asText()).statusInt));

			String flavorId = server.path("flavor").path("id").asText();
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
	 * Fetches absolute limits for NextGenServer and returns a Map<MetricName,
	 * MetricValue>
	 * 
	 * @param url
	 * @param authToken
	 * @return
	 */
	public Map<String, Long> getLimits(String url, String authToken) {
		JsonNode serviceResponse = getServiceResponse(url + limitsUri, authToken);
		Map<String, Long> limits = new HashMap<String, Long>();
		JsonNode limitsNode = serviceResponse.get("limits").path("absolute");
		limits.put("Total Cores Used", limitsNode.path("totalCoresUsed").asLong());
		limits.put("Total Floating Ips Used", limitsNode.path("totalFloatingIpsUsed").asLong());
		limits.put("Total Instances Used", limitsNode.path("totalInstancesUsed").asLong());
		limits.put("Total Private Networks Used", limitsNode.path("totalPrivateNetworksUsed").asLong());
		limits.put("Total RAM Used(GB)", limitsNode.path("totalRAMUsed").asLong());
		limits.put("Total Security Groups Used", limitsNode.path("totalSecurityGroupsUsed").asLong());
		limits.put("Max Total Instances", limitsNode.path("maxTotalInstances").asLong());
		limits.put("Max Total RAM Size(MB)", limitsNode.path("maxImageMeta").asLong());

		return limits;
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
			serverFlavor.setSwap(flavor.path("swap").asInt());
			serverFlavor.setVcpus(flavor.path("vcpus").asInt());
			serverFlavor.setDisk(flavor.path("disk").asInt());
			serverFlavors.add(serverFlavor);
		}
		return serverFlavors;
	}

}
