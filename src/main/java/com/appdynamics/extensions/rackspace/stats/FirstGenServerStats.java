/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */
package com.appdynamics.extensions.rackspace.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.rackspace.exception.RackspaceMonitorException;
import com.fasterxml.jackson.databind.JsonNode;

public class FirstGenServerStats extends Stats {

	public static final String metricPath = "FirstGenServers |%s|%s|";

	private static final String uri = "/servers/detail";

	private static final String flavorsUri = "/flavors/detail";

	public FirstGenServerStats(SimpleHttpClient httpClient) {
		super(httpClient);
	}

	/**
	 * Fetches metrics issuing a Http Request to the FirstGenServer url specific
	 * to the default datacenter and returns as a Map<ServerName,
	 * Map<MetricName, MetricValue>>
	 * 
	 * @throws RackspaceMonitorException
	 */
	@Override
	public Map<String, Map<String, Long>> getMetrics(String authToken, String url) throws RackspaceMonitorException {
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
	 * @throws RackspaceMonitorException
	 */
	private List<ServerFlavor> populateServerFlavors(String url, String authToken) throws RackspaceMonitorException {
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
