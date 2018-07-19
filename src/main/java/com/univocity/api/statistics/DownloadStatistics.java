/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.statistics;

import com.univocity.api.common.*;
import com.univocity.api.net.*;

import java.io.*;

/**
 * A simple implementation of {@link DataTransferStatistics} to notify the progress/completion of download processes
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see DataTransferStatistics
 * @see DataTransfer
 * @see TimedNotificationHandler
 */
public abstract class DownloadStatistics extends DataTransferStatistics<UrlReaderProvider, File> {

	/**
	 * Creates a default download listener that notifies the {@link #dataDownloaded(DownloadStatistics, boolean)} every
	 * 250ms.
	 */
	public DownloadStatistics() {
		this(250);
	}

	/**
	 * Creates a default download listener that notifies the {@link #dataDownloaded(DownloadStatistics, boolean)} at a
	 * rate defined by the user
	 *
	 * @param notificationInterval how often, in milliseconds, the {@link #dataDownloaded(DownloadStatistics, boolean)}
	 *                             should be invoked.
	 */
	public DownloadStatistics(long notificationInterval) {
		setNotificationHandler(notificationInterval, new NotificationHandler<DataTransferStatistics<UrlReaderProvider, File>>() {
			@Override
			public void notify(DataTransferStatistics<UrlReaderProvider, File> notification, boolean lastNotification) {
				dataDownloaded((DownloadStatistics) notification, lastNotification);
			}
		});

		setUnit("kb", 1024L);
	}

	/**
	 * Notifies of an update on the status of a download process.
	 *
	 * @param status           an object with statistics and information about the download and its progress
	 * @param lastNotification a flag indicating whether this notification is the last one (i.e. the download
	 *                         completed or was aborted due to an error)
	 */
	protected abstract void dataDownloaded(DownloadStatistics status, boolean lastNotification);


}
