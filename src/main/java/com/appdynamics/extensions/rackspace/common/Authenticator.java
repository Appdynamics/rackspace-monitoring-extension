/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */
package com.appdynamics.extensions.rackspace.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.http.Response;
import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.http.WebTarget;
import com.appdynamics.extensions.rackspace.exception.RackspaceMonitorException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Authenticator {

	private static final Logger LOG = Logger.getLogger("com.singularity.extensions.Authenticator");

	private static final String httpPostParam = "{\"auth\":{\"RAX-KSKEY:apiKeyCredentials\":{\"username\":\"%s\",\"apiKey\":\"%s\"}}}";

	private static final String TOKEN_URI = "/tokens";

	private String authToken;

	private String defaultRegion;

	private Map<String, Map<String, String>> endpoints;

	private final SimpleHttpClient httpClient;

	public Authenticator(final SimpleHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Authenticates by issuing a POST /tokens request and parses the response
	 * to retrieve the Authentication Token and Service EndPoints as a
	 * Map<Service, Map<Region, publicUrl>>. The Authentication Token and
	 * ServiceEndPoint Url are used for further API calls.
	 * 
	 * @param httpClient
	 * 
	 * @param userName
	 * @param apiKey
	 * @param authenticationEndPoint
	 * @throws RackspaceMonitorException
	 */
	public void authenticate(String userName, String apiKey, String authenticationEndPoint) throws RackspaceMonitorException {
		Response response = postAuthenticationRequest(userName, apiKey, authenticationEndPoint);
		try {
			JsonNode node = getAuthenticationResponeNode(response);
			int statusCode = response.getStatus();
			if (!(statusCode == 200 || statusCode == 203 || statusCode == 300)) {
				String message = response.getStatusLine() + " " + node.findValue("message").toString();
				LOG.error("Error in authentication response " + message);
				throw new RackspaceMonitorException("Error in authentication response " + message);
			}
			if (node != null) {
				parseAuthenticationResponse(node);
			} else {
				LOG.error("Json node is null after authentication, quitting monitor");
				throw new RackspaceMonitorException("Json node is null after authentication, quitting monitor");
			}

		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (Exception e) {
				// Ignore
			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Authenticated to " + authenticationEndPoint + "successfully");
		}

	}

	private Response postAuthenticationRequest(String userName, String apiKey, String authenticationEndPoint) {
		WebTarget target = httpClient.target(authenticationEndPoint + TOKEN_URI);
		target.header("Content-Type", "application/json");
		String data = String.format(httpPostParam, userName, apiKey);
		Response response = target.post(data);
		return response;
	}

	private JsonNode getAuthenticationResponeNode(Response response) throws RackspaceMonitorException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode node = mapper.readValue(response.inputStream(), JsonNode.class);
			return node;
		} catch (Exception e) {
			LOG.error(e);
			throw new RackspaceMonitorException(e);
		}
	}

	private void parseAuthenticationResponse(JsonNode node) {
		JsonNode tokenNode = node.findValue("token");
		setAuthToken(tokenNode.get("id").asText());
		setDefaultRegion(node.findValue("RAX-AUTH:defaultRegion").asText());

		JsonNode serviceCatalog = node.findValue("serviceCatalog");
		setEndpoints(new HashMap<String, Map<String, String>>());

		for (JsonNode serviceNode : serviceCatalog) {
			String serviceName = serviceNode.path("name").asText();
			JsonNode endPointsNode = serviceNode.path("endpoints");
			Map<String, String> serviceMap = new HashMap<String, String>();
			for (JsonNode endPoint : endPointsNode) {
				if (!endPoint.path("region").isMissingNode()) {
					serviceMap.put(endPoint.path("region").asText(), endPoint.path("publicURL").asText());
				} else {
					serviceMap.put(getDefaultRegion(), endPoint.path("publicURL").asText());
				}
			}
			getEndpoints().put(serviceName, serviceMap);
		}
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public Map<String, Map<String, String>> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(Map<String, Map<String, String>> endpoints) {
		this.endpoints = endpoints;
	}

	public String getDefaultRegion() {
		return defaultRegion;
	}

	public void setDefaultRegion(String defaultRegion) {
		this.defaultRegion = defaultRegion;
	}
}
