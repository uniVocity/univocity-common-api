/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.net;

import com.univocity.api.exception.*;

/**
 * A {@code HttpException} is thrown when a HTTP request made using a {@link HttpRequest} object did not result
 * resulted a 2xx (i.e. 'OK') or 3xx (i.e. 'Redirect') HTTP response code
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public final class HttpException extends DataInputException {

	private static final long serialVersionUID = 615769850691615294L;
	private final int statusCode;
	private transient final HttpRequest request;
	private final String statusMessage;

	/**
	 * Creates a new HTTP status exception
	 *
	 * @param message       message with information about the error.
	 * @param statusCode    the HTTP status code returned by the remote server
	 * @param statusMessage the HTTP status message, describing the error code, if available.
	 * @param request       the {@link HttpRequest} object used to attempt to obtain a response from the remote server
	 */
	public HttpException(String message, int statusCode, String statusMessage, HttpRequest request) {
		super(message);
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
		this.request = request;
	}

	/**
	 * Returns the HTTP status message returned by the remote server, if available.
	 *
	 * @return the HTTP status message returned by the remote server, if available.
	 */
	public final String getStatusMessage() {
		return statusMessage;
	}

	/**
	 * Returns the HTTP status code returned by the remote server. Will always be less than 200 or greater/equal to 400
	 *
	 * @return the HTTP status code returned by the remote server
	 */
	public final int getStatusCode() {
		return statusCode;
	}

	/**
	 * Returns the {@link HttpRequest} used to attempt to obtain a response from the remote server
	 *
	 * @return the {@link HttpRequest} that failed.
	 */
	public final HttpRequest getRequest() {
		return request;
	}

	@Override
	public String getMessage() {
		if (statusMessage != null) {
			return super.getMessage() + ". Status = " + statusCode + " (" + statusMessage + "). Request = " + request;
		} else {
			return super.getMessage() + ". Status = " + statusCode + ", Request = " + request;
		}
	}
}
