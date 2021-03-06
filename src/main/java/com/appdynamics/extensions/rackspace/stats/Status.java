/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.rackspace.stats;

public enum Status {
	ACTIVE(1), 
	BUILD(2), 
	DELETED(3), 
	ERROR(4), 
	HARD_REBOOT(5), 
	MIGRATING(6), 
	PASSWORD(7), 
	REBOOT(8), 
	REBUILD(9), 
	RESCUE(10), 
	RESIZE(11), 
	REVERT_RESIZE(12),
	SUSPENDED(13),
	UNKNOWN(14),
	VERIFY_RESIZE(15),
	PENDING_UPDATE(16),
	PENDING_DELETE(17),
	BACKUP(18),
	BLOCKED(19),
	SHUTDOWN(20);
	
	int statusInt;

	private Status(int val) {
		this.statusInt = val;
	}
}
