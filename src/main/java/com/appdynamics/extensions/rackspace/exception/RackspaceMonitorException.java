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
