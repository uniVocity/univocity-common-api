/*******************************************************************************
 * Copyright (c) 2014 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.api.common;

/**
 * A simple interface to provide values
 *
 * @param <T> the type of the value to be returned from the {@link ValueGetter#getValue()} method
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface ValueGetter<T> {

	/**
	 * Provides a value to the caller.
	 *
	 * @return the value
	 */
	T getValue();
}
