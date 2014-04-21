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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Stats {

	private static final Logger LOG = Logger.getLogger("com.singularity.extensions.Stats");

	/**
	 * Processes the Get HttpRequest to the resource given the Authentication
	 * Token. The response is a JsonNode used further to retrieve metrics using
	 * XPath.
	 * 
	 * @param resource
	 * @param authToken
	 * @return
	 */
	public JsonNode getServiceResponse(String resource, String authToken) {
		URL url = null;
		try {
			url = new URL(resource);

		} catch (MalformedURLException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
		JsonNode jsonNode = executeGetRequest(url, authToken);
		return jsonNode;
	}

	private JsonNode executeGetRequest(URL url, String authToken) {
		HttpsURLConnection conn = null;
		InputStream responseStream = null;
		try {
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("X-Auth-Token", authToken);
		} catch (IOException e) {
			LOG.error("Failed to execute Http Request to " + url, e);
			throw new RuntimeException(e);
		}

		try {
			responseStream = conn.getInputStream();
		} catch (IOException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
		JsonNode jsonNode = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonNode = mapper.readValue(responseStream, JsonNode.class);
		} catch (JsonParseException e) {
			LOG.error(e.getMessage());
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			LOG.error(e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOG.error(e.getMessage());
			throw new RuntimeException(e);
		}
		return jsonNode;
	}

	public abstract Map<String, Map<String, Object>> getMetrics(String authToken, String url);

}
