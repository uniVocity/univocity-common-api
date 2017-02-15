/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.api.common;

/**
 * A simple interface to provide custom values
 *
 * @param <T> the type of the value to be returned from the {@link ValueGetter#getValue()} method
 */
public interface ValueGetter<T> {
	T getValue();
}
