/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

/**
 * A central location to provide logger names used internally.
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class Loggers {

	/**
	 * Logger for operations involving network activity.
	 */
	public static final String NETWORKING = "NETWORKING";

	/**
	 * General logger for common operations performed by our software.
	 */
	public static final String GENERAL = "GENERAL";

	/**
	 * Logger used by all operations involving the local user-level storage.
	 */
	public static final String UNIVOCITY_LOCAL_STORE = "UNIVOCITY_LOCAL_STORE";
}
