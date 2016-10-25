/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.statistics;

/**
 * A callback interface used to receive notifications of data transfers from a source to a target.
 *
 * @param <S> the source of data, where data is coming from
 * @param <T> the target of data, where data is being transferred into.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface DataTransfer<S, T> {

	/**
	 * Notifies a data transfer has been started.
	 *
	 * @param source    the source of data
	 * @param totalSize the total size of the data. Depends on the underlying implementation: this can be bytes,
	 *                  number of records, etc. If -1, the size is unknown.
	 * @param target    the data target
	 */
	void started(S source, long totalSize, T target);

	/**
	 * Notifies how much data has been transferred from source to target. Can be invoked multiple times until the
	 * data transfer is completed or aborted.
	 *
	 * @param source      the source of data being read
	 * @param totalSize   the total size of the data. Depends on the underlying implementation: this can be bytes,
	 *                    number of records, etc.
	 * @param transferred the amount of data transferred so far. Depends on the underlying implementation: this can be bytes,
	 *                    number of records, etc.
	 * @param target      the target that received the amount of data
	 */
	void transferred(S source, long totalSize, long transferred, T target);

	/**
	 * Notifies that a data transfer has been finalized.
	 *
	 * @param source    the source of data being read
	 * @param totalSize the total size of the data. Depends on the underlying implementation: this can be bytes,
	 *                  number of records, etc.
	 * @param target    the target that received the amount of data given by the {@code transferred} parameter.
	 */
	void completed(S source, long totalSize, T target);

	/**
	 * Notifies that the data has been aborted.
	 *
	 * @param source      the source of data being read
	 * @param totalSize   the total size of the data. Depends on the underlying implementation: this can be bytes,
	 *                    number of records, etc.
	 * @param transferred the amount of data transferred until the process stopped. Depends on the underlying
	 *                    implementation: this can be bytes, number of records, etc.
	 * @param target      the target that received the amount of data given by the {@code transferred} parameter.
	 */
	void aborted(S source, long totalSize, long transferred, T target, Exception error);
}
