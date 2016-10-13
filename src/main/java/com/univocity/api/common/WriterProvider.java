/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

import java.io.*;

/**
 * Base abstract class to define classes that provide instances of {@link java.io.Writer}.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public abstract class WriterProvider implements ResourceProvider<Writer> {

	/**
	 * Removes any data contained in the resource being written using the instances of {@link java.io.Writer} provided by this class.
	 */
	public abstract void clearDestination();

	/**
	 * Queries whether or not the resource to be written contains any sort of content.
	 *
	 * @return a flag indicating whether or not the underlying resource contains data.
	 */
	public abstract boolean isEmpty();
}
