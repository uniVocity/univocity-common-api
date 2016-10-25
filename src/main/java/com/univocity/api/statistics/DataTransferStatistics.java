/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.statistics;

import com.univocity.api.common.*;

import java.sql.*;
import java.text.*;
import java.util.concurrent.*;

import static java.lang.System.*;
import static java.util.concurrent.TimeUnit.*;

/**
 * A basic implementation of {@link DataTransferListener} which collects statistics about a given data transfer process, and
 * provides easy to use methods to report the progress and timing of the process.
 *
 * @param <S> the source of data, where data is coming from
 * @param <T> the target of data, where data is being transferred into.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see Notification
 */
public final class DataTransferStatistics<S, T> implements DataTransferListener<S, T> {

	private S source;
	private T target;
	private long totalSize = -1L;
	private long totalTransferredSoFar = -1L;
	private long startTime = -1L;
	private long endTime = -1L;
	private boolean aborted;
	private Exception abortError;
	private SimpleDateFormat dateFormat;
	private String dateMask;

	private String description;

	private Notification<DataTransferStatistics<S, T>> notification;

	/**
	 * Creates a new data transfer statistics object that doesn't notify the user of updates. Users are expected to
	 * query this object at their discretion to obtain the process' status
	 */
	public DataTransferStatistics() {
		this(0, null);
	}

	/**
	 * Creates a new data transfer statistics object that notifies the user of updates. Users can
	 * query this object at their discretion to obtain the process' status and will receive notification of
	 * any updates to this process as soon as they occur.
	 *
	 * @param notification the notification callback that will receive updates as soon as the statistics are changed.
	 */
	public DataTransferStatistics(Notification<DataTransferStatistics<S, T>> notification) {
		this(0, notification);
	}

	/**
	 * Creates a new data transfer statistics object that notifies the user of updates at given intervals, and when
	 * the process completes. Users can query this object at their discretion to obtain the process' status and will
	 * receive notification of updates to this process, after a given interval to avoid excessive processing.
	 *
	 * @param notificationInterval the minimum interval, in milliseconds, before notifying the provided callback of updates.
	 * @param notification         the notification callback that will receive updates as soon as the statistics
	 *                             are changed.
	 */
	public DataTransferStatistics(long notificationInterval, final Notification<DataTransferStatistics<S, T>> notification) {
		if (notificationInterval > 0) {
			this.notification = new TimedNotification<DataTransferStatistics<S, T>>(notificationInterval) {
				@Override
				protected void onNotification(DataTransferStatistics<S, T> n, boolean lastNotification) {
					notification.notify(n, lastNotification);
				}
			};
		} else {
			this.notification = notification;
		}
	}

	@Override
	public final void started(S source, long totalSize, T target) {
		if (isRunning()) {
			throw new IllegalStateException("Data transfer statistics already started: " + toString());
		}

		this.source = source;
		this.target = target;
		description = source + " -> " + target;
		this.totalSize = totalSize;
		startTime = currentTimeMillis();
		totalTransferredSoFar = 0L;
		endTime = -1L;
	}

	@Override
	public final void transferred(long transferred) {
		if (transferred > 0) {
			totalTransferredSoFar += transferred;
			notifyStatisticUpdates();
		}
	}

	private void notifyStatisticUpdates() {
		if (notification != null) {
			notification.notify(this, endTime > 0);
		}
	}

	@Override
	public final void completed() {
		endTime = currentTimeMillis();
		notifyStatisticUpdates();
	}

	@Override
	public final void aborted(Exception error) {
		aborted = true;
		abortError = error;
		completed();
	}

	/**
	 * Returns the transfer rate, in milliseconds.
	 *
	 * @return the number number of data elements measured by the underlying implementation transferred per millisecond.
	 */
	public final double getRate() {
		return getRate(MILLISECONDS);
	}

	/**
	 * Returns the transfer rate, using a given {@link TimeUnit}
	 *
	 * @param timeUnit the time unit used to calculate the transfer rate.
	 *
	 * @return the number number of data elements measured by the underlying implementation
	 * transferred per a given unit of time
	 */
	public final double getRate(TimeUnit timeUnit) {
		long time = getTimeElapsed();

		time = timeUnit.convert(time, MILLISECONDS);
		if (time > 0) {
			return (double) totalTransferredSoFar / (double) time;
		} else {
			return totalTransferredSoFar;
		}
	}

	/**
	 * Returns the number of milliseconds the data transfer took so far.
	 *
	 * @return the number of milliseconds the data transfer took so far.
	 */
	public final long getTimeElapsed() {
		if (endTime > 0) {
			return endTime - startTime;
		}
		return currentTimeMillis() - startTime;
	}

	/**
	 * Returns how long the data transfer took so far, using a given time unit.
	 *
	 * @param timeUnit the unit of time to use to represent the time elapsed.
	 *
	 *                 Returns how long the data transfer took so far.
	 */
	public final long getTimeElapsed(TimeUnit timeUnit) {
		return timeUnit.convert(getTimeElapsed(), MILLISECONDS);
	}

	/**
	 * Returns the total amount transferred already.
	 *
	 * @return the total amount transferred already.
	 */
	public final long getTotalTransferredSoFar() {
		return totalTransferredSoFar;
	}

	/**
	 * Returns the total size of the source to be transferred to the target, if available. If the process completed
	 * successfully, returns the amount transferred. The meaning of the amount returned depends on the underlying
	 * implementation: this can be bytes, number of records, etc. If {@code -1}, the total size is unknown ahead of
	 * time.
	 *
	 * @return the total size to transfer, if known. Otherwise, returns {@code -1}. If the process completed
	 * successfully, returns the amount transferred.
	 */
	public final long getTotalSize() {
		if (!aborted && endTime > 0) {
			return totalTransferredSoFar;
		}
		return totalSize;
	}

	/**
	 * Returns the data transfer start time, in milliseconds.
	 *
	 * @return the start time
	 */
	public final long getStartTime() {
		return startTime;
	}

	private String getTime(long time, String dateMask, String valueOnError) {
		if (this.dateMask != null && !this.dateMask.equals(dateMask)) {
			dateFormat = null;
		}
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat(dateMask);
			this.dateMask = dateMask;
		}
		try {
			return dateFormat.format(new Date(time));
		} catch (Exception ex) {
			return valueOnError;
		}
	}

	/**
	 * Returns the data transfer start time, as a formatted date.
	 *
	 * @param dateMask     a date mask following the rules specified by {@link SimpleDateFormat}
	 * @param valueOnError a default value to be returned in case of errors generating the formatted date.
	 *
	 * @return the start time
	 */
	public final String getStartTime(String dateMask, String valueOnError) {
		return getTime(startTime, dateMask, valueOnError);
	}

	/**
	 * Returns the data transfer start time, as a formatted date.
	 *
	 * @param dateMask a date mask following the rules specified by {@link SimpleDateFormat}
	 *
	 * @return the start time
	 */
	public final String getStartTime(String dateMask) {
		return getTime(startTime, dateMask, "N/A");
	}

	/**
	 * Returns the data transfer end time, in milliseconds. If it is still ongoing, returns {@code -1}
	 *
	 * @return the end time, or {@code -1} of the process is still running.
	 */
	public final long getEndTime() {
		return endTime;
	}

	/**
	 * Returns the data transfer end time, in milliseconds. If it is still ongoing, returns {@code -1}
	 *
	 * @param dateMask     a date mask following the rules specified by {@link SimpleDateFormat}
	 * @param valueOnError a default value to be returned in case of errors generating the formatted date.
	 *
	 * @return the end time, or {@code -1} of the process is still running.
	 */
	public final String getEndTime(String dateMask, String valueOnError) {
		return getTime(endTime, dateMask, valueOnError);
	}

	/**
	 * Returns the data transfer end time, in milliseconds. If it is still ongoing, returns {@code -1}
	 *
	 * @param dateMask a date mask following the rules specified by {@link SimpleDateFormat}
	 *
	 * @return the end time, or {@code -1} of the process is still running.
	 */
	public final String getEndTime(String dateMask) {
		return getTime(endTime, dateMask, "N/A");
	}

	/**
	 * Returns the percentage of data already transferred, (where 0.0 represents 0% and 1.0 represents 100%)
	 *
	 * @return the percentage of data already transferred.
	 */
	public final double getTransferPercentage() {
		if (totalSize > 0) {
			return (double) totalTransferredSoFar / (double) totalSize;
		}
		if (aborted) {
			return 0.0;
		}
		if (endTime > 0) {
			return 1.0;
		}
		return 0.0;
	}

	/**
	 * Returns the source of data
	 *
	 * @return the source of data
	 */
	public final S getSource() {
		return source;
	}

	/**
	 * Returns the destination of the data transferred from the source.
	 *
	 * @return the data destination.
	 */
	public final T getTarget() {
		return target;
	}

	/**
	 * Returns the exception, if any, that caused the interruption of data transfer process.
	 *
	 * @return the cause of the interruption of this data transfer.
	 */
	public final Exception getAbortError() {
		return abortError;
	}

	/**
	 * Returns a flag indicating whether the data transfer has been started.
	 *
	 * @return a flag indicating whether the data transfer has been started.
	 */
	public final boolean isStarted() {
		return totalTransferredSoFar != -1;
	}

	/**
	 * Returns a flag indicating whether the data transfer is running.
	 *
	 * @return a flag indicating whether the data transfer is running.
	 */
	public final boolean isRunning() {
		return isStarted() && endTime <= 0;
	}

	/**
	 * Returns a flag indicating whether the data transfer was aborted.
	 *
	 * @return a flag indicating whether the data transfer was aborted.
	 */
	public final boolean isAborted() {
		return aborted;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DataTransferStatistics<?, ?> that = (DataTransferStatistics<?, ?>) o;

		if (!source.equals(that.source)) return false;
		return target.equals(that.target);

	}

	@Override
	public final int hashCode() {
		int result = source.hashCode();
		result = 31 * result + target.hashCode();
		return result;
	}

	@Override
	public final String toString() {
		if (totalTransferredSoFar == -1L) {
			return "Not started";
		}

		StringBuilder stats = new StringBuilder();
		stats.append(getTotalTransferredSoFar());

		if (totalSize > 0) {
			stats.append(" of ");
			stats.append(totalSize);
		}

		stats.append(' ').append('(').append(NumberFormat.getPercentInstance().format(getTransferPercentage()));

		if (aborted) {
			if (abortError != null) {
				return description + stats.toString() + ") | Aborted with error " + abortError.getMessage();
			} else {
				return description + stats.toString() + ") | Aborted";
			}
		}

		stats.append(' ').append('-').append(' ');
		stats.append(NumberFormat.getNumberInstance().format(getRate(TimeUnit.SECONDS))).append("/sec)");

		if (endTime < 0) {
			return description + stats.toString() + " | Transferring ";
		} else {
			return description + stats.toString() + " | Completed ";
		}
	}
}
