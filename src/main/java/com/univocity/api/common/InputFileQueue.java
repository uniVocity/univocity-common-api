/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;


import com.univocity.api.*;

import java.io.*;
import java.nio.charset.*;

/**
 * A queue of a sequence of files to be processed.
 * Variables can be used to assign values to each file and provide more control and information about what the file
 * represents (e.g. date the file was produced, some user ID associated with the file, etc)
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see InputQueue
 * @see FileProvider
 */
public class InputFileQueue extends InputQueue<FileProvider> {

	/**
	 * Adds a {@code java.io.File} to the queue. The default character encoding will be used to read this file.
	 *
	 * @param file the file to be added to this input queue.
	 */
	public void addFile(File file) {
		this.addFile(file, (Charset) null);
	}

	/**
	 * Adds a {@code java.io.File} to the queue.
	 *
	 * @param file     the file to be added to this input queue.
	 * @param encoding the encoding to be used when reading from the given file
	 */
	public void addFile(File file, String encoding) {
		addFile(new FileProvider(file, encoding));
	}

	/**
	 * Adds a {@code java.io.File} to the queue.
	 *
	 * @param file     the file to be added to this input queue.
	 * @param encoding the encoding to be used when reading from the given file
	 */
	public void addFile(File file, Charset encoding) {
		this.addFile(new FileProvider(file, encoding));
	}

	/**
	 * Adds a path to a file or resource to the queue. The path can contain environment variables such as {user.home}
	 * The default character encoding will be used to read this file.
	 *
	 * @param filePath the file to be added to this input queue.
	 */
	public void addFile(String filePath) {
		this.addFile(filePath, (Charset) null);
	}

	/**
	 * Adds a path to a file or resource to the queue. The path can contain environment variables such as {user.home}
	 *
	 * @param filePath the file to be added to this input queue.
	 * @param encoding the encoding to be used when reading from the given file
	 */
	public void addFile(String filePath, String encoding) {
		addFile(new File(filePath, encoding));
	}

	/**
	 * Adds a path to a file or resource to the queue. The path can contain environment variables such as {user.home}
	 *
	 * @param filePath the file to be added to this input queue.
	 * @param encoding the encoding to be used when reading from the given file
	 */
	public void addFile(String filePath, Charset encoding) {
		addFile(new FileProvider(filePath, encoding));
	}

	/**
	 * Adds a {@link FileProvider} to the input queue.
	 */
	public void addFile(FileProvider fileProvider) {
		offer(fileProvider);
	}

	@Override
	protected Reader open(FileProvider input) {
		return Builder.build(Reader.class, input);
	}
}
