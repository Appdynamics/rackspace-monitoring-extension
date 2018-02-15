/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */
package com.appdynamics.extensions.rackspace.stats;

import java.util.Map;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.http.Response;
import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.http.WebTarget;
import com.appdynamics.extensions.rackspace.exception.RackspaceMonitorException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Stats {

	private static final Logger LOG = Logger.getLogger("com.singularity.extensions.Stats");

	private final SimpleHttpClient httpClient;

	public Stats(SimpleHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Processes the Get HttpRequest to the resource given the Authentication
	 * Token. The response is a JsonNode used further to retrieve metrics using
	 * XPath.
	 * 
	 * @param resource
	 * @param authToken
	 * @return
	 * @throws RackspaceMonitorException
	 */
	public JsonNode getServiceResponse(String resource, String authToken) throws RackspaceMonitorException {
		Response response = null;
		try {
			WebTarget target = httpClient.target(resource);
			target.header("Content-Type", "application/json");
			target.header("Accept", "application/json");
			target.header("X-Auth-Token", authToken);

			response = target.get();

			JsonNode jsonNode = getAuthenticationResponeNode(response);

			int statusCode = response.getStatus();
			if (!(statusCode == 200 || statusCode == 203 || statusCode == 300)) {
				String message = response.getStatusLine() + " " + jsonNode.findValue("message").toString();
				LOG.error("Error in response " + message);
				throw new RackspaceMonitorException("Error in response " + message);
			}
			return jsonNode;
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (Exception e1) {
				// Ignore
			}
		}
	}

	private JsonNode getAuthenticationResponeNode(Response response) throws RackspaceMonitorException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode node = mapper.readValue(response.inputStream(), JsonNode.class);
			return node;
		} catch (Exception e) {
			LOG.error("Exception while mapping json content to Json Node object ", e);
			throw new RackspaceMonitorException(e);
		}
	}

	public abstract Map<String, Map<String, Long>> getMetrics(String authToken, String url) throws RackspaceMonitorException;

}
