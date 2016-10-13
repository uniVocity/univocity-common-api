/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.exception;

/**
 * <code>IllegalConfigurationException</code> is the exception thrown by uniVocity to notify of an illegal configuration.
 *
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public final class IllegalConfigurationException extends RuntimeException {
	private static final long serialVersionUID = 8697369823358345165L;

	/**
	 * Constructs a new {@code IllegalConfigurationException} exception with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the cause of the exception.
	 */
	public IllegalConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new {@code IllegalConfigurationException} exception with the specified detail message, and no cause.
	 *
	 * @param message the detail message.
	 */
	public IllegalConfigurationException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@code IllegalConfigurationException} exception with the specified cause of error.
	 *
	 * @param cause the cause of the exception.
	 */
	public IllegalConfigurationException(Throwable cause) {
		super(cause);
	}
}
