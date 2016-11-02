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
 * A basic implementation of {@link DataTransfer} which collects statistics about a given data transfer process, and
 * provides easy to use methods to report the progress and timing of the process.
 *
 * @param <S> the source of data, where data is coming from
 * @param <T> the target of data, where data is being transferred into.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see NotificationHandler
 */
public class DataTransferStatistics<S, T> implements DataTransfer<S, T> {

	private S source;
	private T target;
	private double totalSize = -1L;
	private double totalTransferredSoFar = -1L;
	private double totalTransferredRecently = 0.0;
	private double ratePerSecond;
	private long startTime = -1L;
	private long endTime = -1L;
	private boolean aborted;
	private Exception abortError;
	private SimpleDateFormat dateFormat;
	private String dateMask;

	private String description;
	private String unitDescription;
	private double unitDivisor = 1;

	private NotificationHandler<DataTransferStatistics<S, T>> notificationHandler;

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
	 * @param notificationHandler the notification callback that will receive updates as soon as the statistics are changed.
	 */
	public DataTransferStatistics(NotificationHandler<DataTransferStatistics<S, T>> notificationHandler) {
		this(0, notificationHandler);
	}

	/**
	 * Creates a new data transfer statistics object that notifies the user of updates at given intervals, and when
	 * the process completes. Users can query this object at their discretion to obtain the process' status and will
	 * receive notification of updates to this process, after a given interval to avoid excessive processing.
	 *
	 * @param notificationInterval the minimum interval, in milliseconds, before notifying the provided callback of updates.
	 * @param handler              the notification callback that will receive updates as soon as the statistics
	 *                             are changed.
	 */
	public DataTransferStatistics(long notificationInterval, final NotificationHandler<DataTransferStatistics<S, T>> handler) {
		setNotificationHandler(notificationInterval, handler);
	}

	protected final void setNotificationHandler(final NotificationHandler<DataTransferStatistics<S, T>> handler) {
		setNotificationHandler(0, handler);
	}

	protected final void setNotificationHandler(long notificationInterval, final NotificationHandler<DataTransferStatistics<S, T>> handler) {
		if (notificationInterval > 0) {
			this.notificationHandler = new TimedNotificationHandler<DataTransferStatistics<S, T>>(notificationInterval) {
				@Override
				protected void onNotification(DataTransferStatistics<S, T> n, boolean lastNotification) {
					handler.notify(n, lastNotification);
				}
			};
		} else {
			this.notificationHandler = handler;
		}
	}

	/**
	 * Returns a description of the unit of data being transferred.
	 *
	 * @return a description of the unit of data being transferred.
	 */
	public final String getUnitDescription() {
		return unitDescription == null ? "" : unitDescription;
	}

	/**
	 * Defines a description of the unit of data being transferred.
	 *
	 * @param unitDescription the description of what is being transferred
	 */
	public final void setUnit(String unitDescription) {
		this.unitDescription = unitDescription;
	}

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
		if (unitDivisor == 0) {
			unitDivisor = 1;
		}
		this.unitDivisor = unitDivisor;
	}


	/**
	 * Returns the unit divisor currently in use. The unit divisor allows you to convert totals accumulated as bytes
	 * into kilobytes ({@code unitDivisor = 1024}) or megabytes ({@code unitDivisor = 1024*1024}) for example.
	 *
	 * @return the divisor to be applied over the totals accumulated by this class
	 */
	public final long getUnitDivisor() {
		return (long) unitDivisor;
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
		totalTransferredRecently = 0L;
		endTime = -1L;
	}

	long lastSecond = 0;

	@Override
	public final void transferred(S source, long transferred, T target) {
		if (transferred > 0 && this.source == source && this.target == target) {
			totalTransferredSoFar += transferred;
			totalTransferredRecently += transferred;
			notifyStatisticUpdates();
		}
	}

	private void updateRatePerSecond() {
		if (lastSecond <= 0) {
			lastSecond = System.currentTimeMillis();
			totalTransferredRecently = 0.0;
			return;
		}
		long time = currentTimeMillis() - lastSecond;
		if (time < 1000) {
			return;
		}

		ratePerSecond = (totalTransferredRecently / unitDivisor) / ((double) time) * 1000.0;

		totalTransferredRecently = 0.0;
		lastSecond = System.currentTimeMillis();
	}

	private void notifyStatisticUpdates() {
		if (notificationHandler != null) {
			updateRatePerSecond();
			notificationHandler.notify(this, endTime > 0);
		}
	}

	@Override
	public final void completed(S source, T target) {
		if (this.source == source && this.target == target) {
			endTime = currentTimeMillis();
			notifyStatisticUpdates();
		}
	}

	@Override
	public final void aborted(S source, T target, Exception error) {
		if (this.source == source && this.target == target) {
			aborted = true;
			abortError = error;
			completed(source, target);
		}
	}

	/**
	 * Returns the average transfer rate, in milliseconds.
	 *
	 * @return the number number of data elements measured by the underlying implementation transferred per millisecond.
	 */
	public final double getAverageRate() {
		return getAverageRate(MILLISECONDS);
	}

	/**
	 * Returns the average transfer rate, using a given {@link TimeUnit}
	 *
	 * @param timeUnit the time unit used to calculate the transfer rate.
	 *
	 * @return the number number of data elements measured by the underlying implementation
	 * transferred per a given unit of time
	 */
	public final double getAverageRate(TimeUnit timeUnit) {
		long time = getTimeElapsed();

		time = timeUnit.convert(time, MILLISECONDS);
		if (time > 0) {
			return getTotalTransferredSoFar() / (double) time;
		} else {
			return getTotalTransferredSoFar();
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
	public final double getTimeElapsed(TimeUnit timeUnit) {
		return timeUnit.convert(getTimeElapsed(), MILLISECONDS);
	}

	/**
	 * Returns the total amount transferred already.
	 *
	 * @return the total amount transferred already.
	 */
	public final double getTotalTransferredSoFar() {
		return totalTransferredSoFar / unitDivisor;
	}

	/**
	 * Returns the total size of the source to be transferred to the target, if available. If the process completed
	 * successfully, returns the amount transferred. The meaning of the amount returned depends on the underlying
	 * implementation: this can be bytes, number of records, etc. If negative, the total size is unknown ahead of
	 * time.
	 *
	 * @return the total size to transfer, if known. Otherwise, returns a negative value. If the process completed
	 * successfully, returns the amount transferred.
	 */
	public final double getTotalSize() {
		if (!aborted && endTime > 0) {
			return totalTransferredSoFar / unitDivisor;
		}
		return totalSize / unitDivisor;
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
			return getTotalTransferredSoFar() / getTotalSize();
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

	@Override
	public final boolean isStarted() {
		return totalTransferredSoFar != -1;
	}

	@Override
	public final boolean isRunning() {
		return isStarted() && endTime <= 0;
	}

	@Override
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
	public String toString() {
		if (totalTransferredSoFar == -1L) {
			return "Not started";
		}

		StringBuilder stats = new StringBuilder(" | ");
		stats.append(getFormattedTotalTransferredSoFar());
		stats.append(" of ").append(getFormattedTotalSize());
		stats.append(' ').append('(');
		stats.append(getFormattedTransferPercentage());

		if (aborted) {
			if (abortError != null) {
				return description + stats.toString() + ") | Aborted with error " + abortError.getMessage();
			} else {
				return description + stats.toString() + ") | Aborted";
			}
		}

		stats.append(' ').append('-').append(' ');
		stats.append(getFormattedRatePerSecond());
		stats.append(" (avg ").append(getFormattedAverageRate()).append(')');

		if (endTime < 0) {
			return description + stats.toString() + " | Transferring ";
		} else {
			return description + stats.toString() + " | Completed ";
		}
	}


	/**
	 * Returns a formatted {@code String} representing the percentage of data already transferred
	 *
	 * @param format         the numeric format to use to represent the percentage.
	 * @param valueIfUnknown the {@code String} to return if the percentage cannot be determined.
	 *
	 * @return the formatted percentage of data already transferred.
	 */
	public String getFormattedTransferPercentage(NumberFormat format, String valueIfUnknown) {
		if (getTotalSize() > 0) {
			return format.format(getTransferPercentage());
		} else {
			return valueIfUnknown;
		}
	}

	/**
	 * Returns the percentage of data already transferred, formatted a decimal number between 0% and 100% inclusive,
	 * or ?% if unknown
	 *
	 * @return the formatted percentage of data already transferred.
	 */
	public String getFormattedTransferPercentage() {
		return getFormattedTransferPercentage(NumberFormat.getPercentInstance(), "?%");
	}

	/**
	 * Returns the approximate transfer rate at which the data was transferred during the previous second.
	 *
	 * @return the number number of data elements measured by the underlying implementation
	 * transferred during the previous second.
	 */
	public final double getRatePerSecond() {
		updateRatePerSecond();
		if(ratePerSecond == 0.0){
			return getAverageRate();
		}
		return ratePerSecond;
	}

	/**
	 * Returns a formatted {@code String} representing the approximate rate at which the data was transferred
	 * during the previous second.
	 *
	 * @param format     the numeric format to use to represent the rate.
	 * @param rateSymbol the symbol to be displayed after the rate
	 *
	 * @return the formatted approximate rate at which the data was transferred during the previous second.
	 */
	public String getFormattedRatePerSecond(NumberFormat format, String rateSymbol) {
		return getFormattedRate(getRatePerSecond(), format, rateSymbol);
	}

	/**
	 * Returns a formatted {@code String} representing the approximate rate, per second, at which the data was being
	 * transferred to the target during the previous second.
	 *
	 * @return the formatted rate at which the data is being transferred during the previous second.
	 */
	public String getFormattedRatePerSecond() {
		return getFormattedRatePerSecond(NumberFormat.getNumberInstance(), " /sec");
	}


	private String getFormattedRate(double rate, NumberFormat format, String rateSymbol) {
		StringBuilder out = new StringBuilder();

		out.append(format.format(rate));
		if (!getUnitDescription().isEmpty()) {
			out.append(getUnitDescription());
		}

		if (rateSymbol != null && !rateSymbol.isEmpty()) {
			out.append(rateSymbol);
		}

		return out.toString();
	}

	/**
	 * Returns a formatted {@code String} representing the average rate at which the data is being transferred to the target.
	 *
	 * @param format     the numeric format to use to represent thea average rate.
	 * @param timeUnit   the unit of time to be used to calculate the average rate.
	 * @param rateSymbol the symbol to be displayed after the rate
	 *
	 * @return the formatted average rate at which the data is being transferred
	 */
	public String getFormattedAverageRate(NumberFormat format, TimeUnit timeUnit, String rateSymbol) {
		return getFormattedRate(getAverageRate(timeUnit), format, rateSymbol);
	}

	/**
	 * Returns a formatted {@code String} representing the average rate, per second, at which the data is being transferred
	 * to the target
	 *
	 * @return the formatted average rate at which the data is being transferred
	 */
	public String getFormattedAverageRate() {
		return getFormattedAverageRate(NumberFormat.getNumberInstance(), TimeUnit.SECONDS, " /sec");
	}

	/**
	 * Returns a formatted {@code String} representing the amount size of data already to be transferred to the target
	 *
	 * @param format the numeric format to use to represent the amount.
	 *
	 * @return the formatted total amount already transferred
	 */
	public String getFormattedTotalTransferredSoFar(NumberFormat format) {
		StringBuilder out = new StringBuilder();

		out.append(format.format(getTotalTransferredSoFar()));
		if (!getUnitDescription().isEmpty()) {
			out.append(' ');
			out.append(getUnitDescription());
		}
		return out.toString();
	}

	/**
	 * Returns a formatted {@code String} representing the amount size of data already to be transferred to the target
	 *
	 * @return the formatted total amount already transferred
	 */
	public String getFormattedTotalTransferredSoFar() {
		return getFormattedTotalTransferredSoFar(NumberFormat.getNumberInstance());
	}

	/**
	 * Returns a formatted {@code String} representing the total size of the source to be transferred to the target,
	 * if available.
	 *
	 * @param format         the numeric format to use to represent the total.
	 * @param valueIfUnknown the {@code String} to return if the total size cannot be determined.
	 *
	 * @return the formatted total amount to be transferred
	 */
	public String getFormattedTotalSize(NumberFormat format, String valueIfUnknown) {
		StringBuilder out = new StringBuilder();
		if (getTotalSize() > 0) {
			out.append(format.format(getTotalSize()));
		} else {
			out.append(valueIfUnknown);
		}
		if (!getUnitDescription().isEmpty()) {
			out.append(' ');
			out.append(getUnitDescription());
		}
		return out.toString();
	}

	/**
	 * Returns a formatted {@code String} representing the total size of the source to be transferred to the target,
	 * if available.
	 *
	 * @return the formatted total amount to be transferred
	 */
	public String getFormattedTotalSize() {
		return getFormattedTotalSize(NumberFormat.getNumberInstance(), "?");
	}
}
