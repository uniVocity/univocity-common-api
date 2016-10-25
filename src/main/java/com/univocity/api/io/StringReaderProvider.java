/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.io;

import java.io.*;

/**
 * A {@link ReaderProvider} for {@code String}s. Use this to read data directly from a {@code String} that
 * should be used as an input.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see ReaderProvider
 */
public class StringReaderProvider extends ReaderProvider {

	private String string;

	/**
	 * Creates a new instance with an empty {@code String}.
	 */
	public StringReaderProvider() {
		this("");
	}

	/**
	 * Creates a new instance with a given {@code String}. {@code null} will be converted to "".
	 *
	 * @param string the {@code String} to be read when {@link #getResource()} is called..
	 */
	public StringReaderProvider(String string) {
		setString(string);
	}

	/**
	 * Assigns a new {@code String} to this {@link ReaderProvider}. {@code null} will be converted to "".
	 *
	 * @param string the {@code String} to be read when {@link #getResource()} is called..
	 */
	public void setString(String string) {
		this.string = string == null ? "" : string;
	}

	/**
	 * Returns a new {@link StringReader} for reading the {@code String} provided in the constructor of this class or
	 * via the {@link #setString(String)} method.
	 *
	 * @return a new {@link StringReader}
	 */
	@Override
	public StringReader getResource() {
		return new StringReader(string);
	}
}
