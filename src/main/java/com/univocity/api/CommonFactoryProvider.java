/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api;

/**
 * The <code>CommonFactoryProvider</code> is used to obtain actual implementations of interfaces and configured objects
 * from an implementation .jar. It is used internally by the {@link Builder} class.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface CommonFactoryProvider {

	/**
	 * Returns an object implementation provided by uniVocity. This is for internal use only.
	 *
	 * @param builderType the interface of a builder entry point
	 * @param args        any arguments required to initialize the builder.
	 * @param <T>         the type of builder to instantiate.
	 *
	 * @return a concrete implementation of the given type.
	 */
	<T> T build(Class<T> builderType, Object... args);
}
