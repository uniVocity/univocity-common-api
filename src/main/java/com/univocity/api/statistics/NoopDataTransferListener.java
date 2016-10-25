/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.statistics;

/**
 * A singleton {@link DataTransferListener} that does nothing.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public final class NoopDataTransferListener implements DataTransferListener {

	private NoopDataTransferListener() {

	}

	public static final DataTransferListener instance = new NoopDataTransferListener();

	public static final <S, T> DataTransferListener<S, T> getInstance() {
		return instance;
	}

	@Override
	public void started(Object source, long totalSize, Object target) {

	}

	@Override
	public void transferred(long transferred) {

	}

	@Override
	public void completed() {

	}

	@Override
	public void aborted(Exception error) {

	}
}