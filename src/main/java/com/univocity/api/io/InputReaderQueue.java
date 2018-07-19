/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.io;

import java.io.*;

/**
 * A queue of a sequence of {@link ReaderProvider} and plain {@link java.io.Reader} inputs to be processed.
 * Variables can be used to assign values to each input and provide more control and information about what the input
 * represents (e.g. date the input was produced, some user ID associated with the input, etc)
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see InputQueue
 * @see ReaderProvider
 */
public class InputReaderQueue extends InputQueue<ReaderProvider> {


	/**
	 * Adds a {@link ReaderProvider} to the input queue
	 *
	 * @param readerProvider the {@link ReaderProvider} to be added to this queue
	 */
	public void add(final ReaderProvider readerProvider) {
		offer(readerProvider);
	}

	/**
	 * Adds a {@link java.io.Reader} to the input queue.
	 *
	 * @param reader the {@link java.io.Reader} to be added to this queue.
	 */
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
