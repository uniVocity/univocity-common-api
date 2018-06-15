/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.net;

/**
 * The set of  HTTP/1.1 methods, plus <a href="https://tools.ietf.org/html/rfc5789">RFC 5789</a>.
 * Each method type identifies an action to be performed on a the remote resource.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see HttpRequest
 * @see HttpResponse
 * @see HttpResponseReader
 * @see UrlReaderProvider
 */
public enum HttpMethodType {

	/**
	 * Requests to delete the resource identified by the Request-URI.
	 */
	DELETE,

	/**
	 * Requests to retrieve whatever information is identified by the Request-URI
	 */
	GET,

	/**
	 * Requests that a set of changes, described in the request entity, must be applied to the resource identified by
	 * the request’s URI. This set contains instructions describing how a resource currently residing on the origin
	 * server should be modified (as described in <a href="https://tools.ietf.org/html/rfc5789">RFC 5789</a>).
	 */
	PATCH,

	/**
	 * Requests that the origin server accept the entity enclosed in the request as a new subordinate of the resource
	 * identified by the Request-URI in the Request-Line
	 */
	POST,

	/**
	 * Requests for the entity enclosed in the message to be stored under the supplied Request-URI.
	 */
	PUT,

	/**
	 * Identical to {@link #GET} except that the server must not return a message-body in the response.
	 */
	HEAD,

	/**
	 * Represents a request for information about the communication options available on the request/response chain
	 * identified by the Request-URI.
	 */
	OPTIONS,

	/**
	 * Used to invoke a remote, application-layer loop-back of the request message. It is expected that the recipient
	 * of the message reflects back the message with a 200 (OK) response.
	 */
	TRACE,

	/**
	 * For use with a proxy that can dynamically switch to being a tunnel (e.g. SSL)
	 */
	CONNECT;


	//FIXME: javadoc
	public final boolean hasBody() {
		return this == POST || this == PUT || this == PATCH;
	}
}
