/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.rackspace.exception;

public class RackspaceMonitorException extends Exception {

	private static final long serialVersionUID = 9130235845725476481L;

	public RackspaceMonitorException() {
	}

	public RackspaceMonitorException(String message, Throwable cause) {
		super(message, cause);
	}

	public RackspaceMonitorException(Throwable cause) {
		super(cause);
	}

	public RackspaceMonitorException(String message) {
		super(message);
	}
}
