/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.io;

import java.io.*;

/**
 * A {@link WriterProvider} for {@code String}s. Use this to write data directly to a {@code String}.
 * This is just a convenience class that you can use to write test cases
 * without having to deal with files or other persistent resources.
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see WriterProvider
 */
public final class StringWriterProvider extends WriterProvider {

	private StringWriter writer;
	private String string = "";

	/**
	 * Creates a new, empty {@code StringWriterProvider}
	 */
	public StringWriterProvider() {

	}

	/**
	 * Obtains a new StringWriter instance.
	 *
	 * @return a new StringWriter
	 */
	@Override
	public final StringWriter getResource() {
		string = getString();
		writer = new StringWriter();
		writer.append(string);
		return writer;
	}

	/**
	 * Clears the contents written to the string so far
	 */
	@Override
	public final void clearDestination() {
		string = "";
		writer = new StringWriter();
	}

	/**
	 * Returns the contents written to the string so far.
	 *
	 * @return the contents written to the string so far.
	 */
	public final String getString() {
		if (writer != null) {
			string = writer.toString();
			writer = null;
		}
		return string;
	}

	@Override
	public final boolean isEmpty() {
		return string.isEmpty() && writer.getBuffer().length() == 0;
	}
}
