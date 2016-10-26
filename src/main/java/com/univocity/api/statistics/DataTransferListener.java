/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.statistics;

/**
 * A callback interface used to receive notifications of data transfers from a source to a target.
 * Information about sizes transferred are implementation dependent and can signify bytes transferred, number of
 * records, etc.
 *
 * @param <S> the source of data, where data is coming from
 * @param <T> the target of data, where data is being transferred into.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface DataTransferListener<S, T> {

	/**
	 * Notifies a data transfer has been started.
	 *
	 * @param source    the source of data
	 * @param totalSize the total size of the data. The meaning of the
	 *                  amount provided depends on the underlying implementation: this can be bytes, number of
	 *                  records, etc. If -1, the total size is unknown ahead of time.
	 * @param target    the data target
	 */
	void started(S source, long totalSize, T target);

	/**
	 * Notifies how much data has been transferred from source to target. Can be invoked multiple times until the
	 * data transfer is completed or aborted.
	 *
	 * @param source      the source of data
	 * @param transferred the amount of data transferred since the last time this method was called. The meaning of the
	 *                    amount provided depends on the underlying implementation: this can be bytes, number of
	 *                    records, etc.
	 * @param target      the data target
	 */
	void transferred(S source, long transferred, T target);

	/**
	 * Notifies that a data transfer has been finalized.
	 *
	 * @param source the source of data
	 * @param target the data target
	 */
	void completed(S source, T target);

	/**
	 * Notifies that the data has been aborted.
	 *
	 * @param source the source of data
	 * @param target the data target
	 */
	void aborted(S source, T target, Exception error);

	/**
	 * Returns a flag indicating whether the data transfer has been started.
	 *
	 * @return a flag indicating whether the data transfer has been started.
	 */
	boolean isStarted();

	/**
	 * Returns a flag indicating whether the data transfer is running.
	 *
	 * @return a flag indicating whether the data transfer is running.
	 */
	boolean isRunning();

	/**
	 * Returns a flag indicating whether the data transfer was aborted.
	 *
	 * @return a flag indicating whether the data transfer was aborted.
	 */
	boolean isAborted();
}
