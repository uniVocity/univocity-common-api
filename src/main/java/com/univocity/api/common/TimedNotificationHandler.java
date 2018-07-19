/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;


/**
 * A basic {@link NotificationHandler} implementation that filters out excessive notifications and only processes them
 * after a given interval has been elapsed since the last notification was processed, or if the last notification
 * is received.
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public abstract class TimedNotificationHandler<T> implements NotificationHandler<T> {

	private final long notificationInterval;
	private long previousNotification = 0;

	/**
	 * Creates a timed notification handler, which will ensure the {@link #onNotification(Object, boolean)} method is
	 * called at a given frequency.
	 *
	 * @param notificationInterval the interval, in milliseconds, between each call to
	 *                             {@link #onNotification(Object, boolean)}
	 */
	public TimedNotificationHandler(long notificationInterval) {
		Args.positive(notificationInterval, "Notification interval");
		this.notificationInterval = notificationInterval;
	}

	@Override
	public final void notify(T notification, boolean lastNotification) {
		if (notificationInterval > 0 && !lastNotification) {
			long time = System.currentTimeMillis();
			if (time - previousNotification > notificationInterval) {
				onNotification(notification, false);
				previousNotification = time;
			}
		} else {
			onNotification(notification, lastNotification);
		}
	}

	/**
	 * Invoked when a notification has been received after the notification interval has been elapsed, or if
	 * the notification is the last one to be received.
	 *
	 * @param notification     the notification object with details about the process being observed.
	 * @param lastNotification a flag indicating whether this is the last notification to be received.
	 */
	protected abstract void onNotification(T notification, boolean lastNotification);
}
