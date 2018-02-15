/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */
package com.appdynamics.extensions.rackspace.common;

public enum AccountBase {
	US("https://identity.api.rackspacecloud.com/v2.0"), UK("https://lon.identity.api.rackspacecloud.com/v2.0");

	private String authUrl;

	private AccountBase(String authUrl) {
		this.authUrl = authUrl;
	}

	public String getAuthUrl() {
		return authUrl;
	}

}
