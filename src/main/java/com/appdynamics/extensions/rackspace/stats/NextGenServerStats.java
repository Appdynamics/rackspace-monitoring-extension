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

	private enum Status {

		ACTIVE(1), BUILD(2), DELETED(3), ERROR(4), HARD_REBOOT(5), MIGRATING(6), PASSWORD(7), REBOOT(8), REBUILD(9), RESCUE(10), RESIZE(11), REVERT_RESIZE(
				12), SUSPENDED(13), UNKNOWN(14), VERIFY_RESIZE(15);
		int statusInt;

		private Status(int val) {
			this.statusInt = val;
		}
	}

	/**
	 * Fetches metrics issuing a Http Request to the NextGenServer url specific to
	 * the DataCenter and returns as a Map<ServerName, Map<MetricName,
	 * MetricValue>>
	 */
	@Override
	public Map<String, Map<String, Object>> getMetrics(String authToken, String url) {

		JsonNode serviceResponse = getServiceResponse(url + uri, authToken);

		List<ServerFlavor> serverFlavors = populateServerFlavors(url, authToken);

		Map<String, Map<String, Object>> serverStats = new HashMap<String, Map<String, Object>>();
		JsonNode serversNode = serviceResponse.get("servers");
		for (JsonNode server : serversNode) {
			Map<String, Object> stats = new HashMap<String, Object>();
			stats.put("progress", server.path("progress").asInt());
			stats.put("status", Status.valueOf(server.path("status").asText()).statusInt);

			String flavorId = server.path("flavor").path("id").asText();
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
	 * Fetches absolute limits for NextGenServer and returns a Map<MetricName, MetricValue>
	 * @param url
	 * @param authToken
	 * @return
	 */
	public Map<String, String> getLimits(String url, String authToken) {
		JsonNode serviceResponse = getServiceResponse(url + limitsUri, authToken);
		Map<String, String> limits = new HashMap<String, String>();
		JsonNode limitsNode = serviceResponse.get("limits").path("absolute");
		limits.put("totalCoresUsed", limitsNode.path("totalCoresUsed").asText());
		limits.put("totalFloatingIpsUsed", limitsNode.path("totalFloatingIpsUsed").asText());
		limits.put("totalInstancesUsed", limitsNode.path("totalInstancesUsed").asText());
		limits.put("totalPrivateNetworksUsed", limitsNode.path("totalPrivateNetworksUsed").asText());
		limits.put("totalRAMUsed(GB)", limitsNode.path("totalRAMUsed").asText());
		limits.put("totalSecurityGroupsUsed", limitsNode.path("totalSecurityGroupsUsed").asText());
		limits.put("maxTotalInstances", limitsNode.path("maxTotalInstances").asText());
		limits.put("maxTotalRAMSize(MB)", limitsNode.path("maxImageMeta").asText());

		return limits;
	}

	/**
	 * Fetches ServerFlavours by issuing a Http Request to determine the
	 * params(RAM, Disk, etc) based on ID
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
