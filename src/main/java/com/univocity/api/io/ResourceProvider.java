/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.io;

/**
 * Generic interface to define classes that provide a given resource.
 *
 * @param <T> the type of resource provided by this ResourceProvider
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see WriterProvider
 * @see ReaderProvider
 * @see InputQueue
 */
public interface ResourceProvider<T> {
	/**
	 * Returns the resource provided by this class
	 *
	 * @return the resource provided by this class
	 */
	T getResource();
}
