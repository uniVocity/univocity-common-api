/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

import java.io.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class InputReaderQueue extends InputQueue<ReaderProvider> {

	public void add(final ReaderProvider readerProvider) {
		offer(readerProvider);
	}

	public void add(final Reader reader) {
		offer(new ReaderProvider() {
			@Override
			public Reader getResource() {
				return reader;
			}
		});
	}

	@Override
	protected Reader open(ReaderProvider input) {
		return input.getResource();
	}
}
