/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.statistics;

import com.univocity.api.net.*;

import java.io.*;

/**
 * A list of active downloads from remote locations into local files. Updates on the progress of each individual
 * download are sent to the user on the {@link #dataDownloaded(DownloadStatistics, boolean)} method.
 *
 * By default, updates to an individual download are sent every 250 ms, and immediately in case the download
 * completes or is aborted.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see DataTransferListener
 * @see DownloadStatistics
 * @see DataTransferStatistics
 */
public abstract class DownloadListener extends DataTransferListener<UrlReaderProvider, File, DownloadStatistics> {

	private String unitDescription;
	private long unitDivisor;

	private final long notificationInterval;

	/**
	 * Creates a default download list that invokes the {@link #dataDownloaded(DownloadStatistics, boolean)} method after
	 * at least 250ms have elapsed since the last call has been made for the <strong>same</strong> download process.
	 */
	public DownloadListener() {
		this(250L);
	}

	/**
	 * Creates download list that invokes the {@link #dataDownloaded(DownloadStatistics, boolean)} method after
	 * a given interval has elapsed since the last call has been made for the <strong>same</strong> download process.
	 *
	 * @param notificationInterval how often, in milliseconds, the {@link #dataDownloaded(DownloadStatistics, boolean)}
	 *                             should be invoked for each individual download process in this list
	 */
	public DownloadListener(long notificationInterval) {
		this.notificationInterval = notificationInterval;
	}


	@Override
	protected final DownloadStatistics newDataTransfer(UrlReaderProvider source, long totalSize, File target) {
		DownloadStatistics out = new DownloadStatistics(notificationInterval) {
			@Override
			protected void dataDownloaded(DownloadStatistics status, boolean lastNotification) {
				DownloadListener.this.dataDownloaded(status, lastNotification);
			}
		};

		if (unitDescription != null) {
			out.setUnit(unitDescription, unitDivisor);
		}

		return out;
	}

	/**
	 * Notifies of an update on the status of a download process.
	 *
	 * @param status           an object with statistics and information about the download and its progress
	 * @param lastNotification a flag indicating whether this notification is the last one (i.e. the download
	 *                         completed or was aborted due to an error)
	 */
	protected abstract void dataDownloaded(DownloadStatistics status, boolean lastNotification);

	/**
	 * Defines a description of the unit of data being transferred and updates the unit divisor to amounts will be returned
	 * taking into account the division of the totals by the given divisor. This allows you to convert totals accumulated as bytes
	 * into kilobytes ({@code unitDivisor = 1024}) or megabytes ({@code unitDivisor = 1024*1024}) for example.
	 *
	 * @param unitDescription the description of what unit of data is being transferred
	 * @param unitDivisor     the divisor to be applied over the totals accumulated by this class, so that the units
	 */
	public void setUnit(String unitDescription, long unitDivisor) {
		this.unitDescription = unitDescription;
		this.unitDivisor = unitDivisor;

		for (DownloadStatistics stats : this) {
			stats.setUnit(unitDescription, unitDivisor);
		}
	}
}
