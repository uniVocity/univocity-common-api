/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.net;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A response object resulting from a HTTP request ({@link HttpRequest}) invoked from the {@link UrlReaderProvider}.
 *
 * Both text and binary content are supported.
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see HttpResponseReader
 * @see HttpRequest
 * @see RequestMethod
 * @see UrlReaderProvider
 */
public interface HttpResponse extends Closeable {

	/**
	 * Gets the redirection URL if the request has been redirected and {@link HttpRequest#getFollowRedirects()} evaluates
	 * to {@code true}
	 *
	 * @return the redirection URL if the {@link HttpRequest} has been redirected (and configured to follow redirects)
	 * otherwise returns {@code null}
	 */
	String getRedirectionUrl();

	/**
	 * Returns the integer HTTP response status code.
	 *
	 * @return the status code.
	 */
	int getStatusCode();

	/**
	 * Returns the HTTP response status message
	 *
	 * @return the status message
	 */
	String getStatusMessage();

	/**
	 * The character set from the {@code Content-Type} header, or if explicitly provided in the HTTP request,
	 * the result of {@link HttpRequest#getCharsetName()}.
	 *
	 * @return the character set to use for reading the response body.
	 */
	String getCharset();

	/**
	 * Returns the value of the {@code Content-Type} header which indicates the format of the content sent in
	 * response to the HTTP request.
	 *
	 * @return the content type.
	 */
	String getContentType();

	/**
	 * The {@link URL} object used to produce this {@code HttpResponse}
	 *
	 * @return the URL which originated the response.
	 */
	URL getUrl();

	/**
	 * Returns the headers and their corresponding values in this HTTP response.
	 * If multiple values are associated with the same header, they will be be separated by comma.
	 *
	 * Use {@link #getMultiHeaders()} to obtain the multiple values in a list.
	 *
	 * @return a map of headers and their values.
	 */
	Map<String, String> getHeaders();

	/**
	 * Returns the headers and their corresponding values in this HTTP response.
	 * Multiple values can be associated with the same header, use {@link #getHeaders()}
	 * to obtain them as a comma separated sequence
	 *
	 * @return a map of headers and their values.
	 */
	Map<String, List<String>> getMultiHeaders();

	/**
	 * Returns a map with the cookie collection listed in the {@code Set-Cookie} header of this response message.
	 *
	 * @return the cookies of this HTTP response
	 */
	Map<String, String> getCookies();


	/**
	 * Closes any resources associated with reading the request response content.
	 */
	void close();

	/**
	 * Returns a {@link java.io.Reader} instance to be used to read the content of the response body of this HTTP response.
	 *
	 * @return a {@code Reader} for the response body.
	 */
	Reader getContentReader();


	/**
	 * Returns a {@code String} with the content of the response body of this HTTP response.
	 *
	 * @return the response body as a {@code String}
	 */
	String getContent();


	/**
	 * Reads this HTTP response using a custom class (i.e. a {@link HttpResponseReader}). The response body
	 * {@link InputStream} is available for reading the response body of this message. Useful to process binary content.
	 *
	 * @param responseReader the custom response reader to process this {@code HttpResponse} object.
	 */
	void readContent(HttpResponseReader responseReader);

	/**
	 * Returns the number of retries performed by the {@link UrlReaderProvider} until a response was obtained.
	 * This number should never be greater than the number of configured retries defined by
	 * {@link UrlReaderProvider#getRetries()}
	 *
	 * @return the number of retries performed to obtain this response object.
	 */
	int getRetriesPerformed();

	/**
	 * Returns the length of the content in the body of this HTTP response, as specified in the {code Content-Length}
	 * header. If unknown, returns {@code -1}.
	 * @return length of the content in the body of this HTTP response. If unknown, returns {@code -1}.
	 */
	long getContentLength();

}
