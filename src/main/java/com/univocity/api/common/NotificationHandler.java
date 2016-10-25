/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

/**
 * A simple callback interface to notify the user of some action or update performed by the framework.
 *
 * @param <T> the type of notification object that will be passed onto the {@link #notify(Object, boolean)} method.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface NotificationHandler<T> {

	/**
	 * Notifies of some action or update.
	 *
	 * @param notification     the notification object with information about what happened.
	 * @param lastNotification flag indicating whether this is the last notification to be received.
	 */
	void notify(T notification, boolean lastNotification);
}
