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
public class StringReaderProvider extends ReaderProvider {

	private String string;

	public StringReaderProvider() {
		this("");
	}

	public StringReaderProvider(String string) {
		this.string = string;
	}

	public void setString(String string) {
		this.string = string == null ? "" : string;
	}

	@Override
	public Reader getResource() {
		return new StringReader(string);
	}
}
