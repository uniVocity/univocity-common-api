/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.exception;

/**
 * A {@code DataInputException} is the exception thrown to notify of errors when reading data from an input.
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class DataInputException extends RuntimeException {
	private static final long serialVersionUID = 5840516365821353625L;

	/**
	 * Constructs a new {@code DataInputException} exception with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the cause of the exception.
	 */
	public DataInputException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new {@code DataInputException} exception with the specified detail message, and no cause.
	 *
	 * @param message the detail message.
	 */
	public DataInputException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@code DataInputException} exception with the specified cause of error.
	 *
	 * @param cause the cause of the exception.
	 */
	public DataInputException(Throwable cause) {
		super(cause);
	}
}
