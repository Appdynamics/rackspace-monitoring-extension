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
package com.appdynamics.extensions.rackspace.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Authenticator {

	private static final Logger LOG = Logger.getLogger("com.singularity.extensions.Authenticator");

	private static final String httpPostParam = "{\"auth\":{\"RAX-KSKEY:apiKeyCredentials\":{\"username\":\"%s\",\"apiKey\":\"%s\"}}}";

	private String authToken;

	private String accountId;

	private String defaultRegion;

	private Map<String, Map<String, String>> endpoints;

	/**
	 * Authenticates by issuing a POST /tokens request and parses the response
	 * to retrieve the Authentication Token and Service EndPoints as a
	 * Map<Service, Map<Region, publicUrl>>. The Authentication Token and
	 * ServiceEndPoint Url are used for further API calls.
	 * 
	 * @param userName
	 * @param apiKey
	 * @param authenticationEndPoint
	 */
	public void authenticate(String userName, String apiKey, String authenticationEndPoint) {
		URL url = null;
		try {
			url = new URL(authenticationEndPoint + "/tokens");
		} catch (MalformedURLException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}

		JsonNode accessNode = executePostRequest(url, userName, apiKey);

		parseAuthenticationResponse(accessNode);

	}

	private JsonNode executePostRequest(URL url, String userName, String apiKey) {
		HttpsURLConnection conn = null;
		InputStream responseStream = null;
		try {
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			String param = String.format(httpPostParam, userName, apiKey);
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(param.getBytes());
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			LOG.error("Failed to execute Http POST authentication request " + url, e);
			throw new RuntimeException(e);
		}

		JsonNode accessNode = null;
		try {
			responseStream = conn.getInputStream();
			ObjectMapper mapper = new ObjectMapper();
			accessNode = mapper.readValue(responseStream, JsonNode.class).path("access");
		} catch (JsonParseException e) {
			LOG.error("Error parsing ", e);
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			LOG.error("Error parsing ", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return accessNode;
	}

	private void parseAuthenticationResponse(JsonNode accessNode) {
		JsonNode tokenNode = accessNode.path("token");
		setAuthToken(tokenNode.path("id").asText());
		setAccountId(tokenNode.path("tenant").path("id").asText());
		setDefaultRegion(accessNode.path("user").path("RAX-AUTH:defaultRegion").asText());

		JsonNode serviceCatalog = accessNode.get("serviceCatalog");
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

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
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
