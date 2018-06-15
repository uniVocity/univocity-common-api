/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.net;

import com.univocity.api.common.*;

import java.util.*;

import static com.univocity.api.common.Utils.*;

//FIXME: javadoc
public abstract class HttpMessage {
	protected HttpMethodType httpMethodType = HttpMethodType.GET;
	protected LinkedHashMap<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
	protected LinkedHashMap<String, String> cookies = new LinkedHashMap<String, String>();

	/**
	 * Returns the currently defined headers and their values. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of colon-separated name-value pairs
	 *
	 * @return a map of headers and their values.
	 */
	public final Map<String, String> getHeaders() {
		return Utils.joinValues(headers, ", ");
	}

	public final boolean hasHeader(String name) {
		Map.Entry<String, List<String>> entry = getEntryCaseInsensitive(headers, name);
		return entry != null;
	}

	/**
	 * Returns the currently defined headers and their values. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of colon-separated name-value pairs
	 *
	 * @return a map of headers and their values.
	 */
	public final Map<String, List<String>> getMultiHeaders() {
		return headers;
	}

	/**
	 * Returns the value(s) currently defined for a given header. If multiple values are
	 * associated with the header the resulting {@code String} will have them separated by a colon.
	 * Use {@link getHeaderValues(String)} to obtain the header values as a {@code List}
	 *
	 * @return the value(s) associated with the given header, or {@code null} if the header is not defined
	 */
	public final String getHeaderValue(String header) {
		List<String> values = getValueCaseInsensitive(headers, header);
		if (values == null) {
			return null;
		}
		return join(values, ", ");
	}

	/**
	 * Returns the list of values currently defined for a given header.
	 *
	 * Use {@link getHeaderValue(String)} to obtain the header values as a colon delimited {@code String}
	 *
	 * @return the list of value associated with the given header, or {@code null} if the header is not defined
	 */
	public final List<String> getHeaderValues(String header) {
		List<String> values = getValueCaseInsensitive(headers, header);
		if (values == null) {
			return null;
		}
		return values;
	}

	/**
	 * Returns the cookies to be added to the {@code Cookie} HTTP header.
	 * All cookies are sent in this header in the format of semicolon-separated name-value pairs.
	 *
	 * @return a map of cookie names and values.
	 */
	public final Map<String, String> getCookies() {
		return cookies;
	}

	/**
	 * Returns the {@link HttpMethodType} to be used by this request.
	 * The method type identifies an action to be performed on the identified (remote) resource.
	 *
	 * <i>Defaults to {@link HttpMethodType#GET}</i>
	 *
	 * @return the HTTP method to use
	 */
	public final HttpMethodType getHttpMethodType() {
		return httpMethodType;
	}

	/**
	 * Returns a flag indicating whether the connection is meant to be reused for further requests (i.e. persistent) or
	 * not, in which case the {@code Connection} request header is set to {@code close}. If the {@code Connection}
	 * header is not set, it is assumed that the connection is persistent, and this method will return {@code true}.
	 *
	 * @return flag indicating whether or not the HTTP connection is persistent (i.e kept alive).
	 */
	public final boolean isKeepAliveEnabled() {
		String value = getHeaderValue("Connection");
		return value == null || "keep-alive".equalsIgnoreCase(value);
	}

	/**
	 * Returns the current  {@code User-Agent} request header, which identifies the user agent originating the request.
	 *
	 * @return the {@code User-Agent} header
	 */
	public final String getUserAgent() {
		return getHeaderValue("User-Agent");
	}

	/**
	 * Returns the current {@code Referer} request header that identifies the address of the web-page
	 * that linked to the resource being requested.
	 *
	 * @return the {@code Referer} header
	 */
	public final String getReferrer() {
		return getHeaderValue("Referer");
	}

	public final String getCookie(String name) {
		return cookies.get(name);
	}

	public final boolean hasCookie(String name) {
		return cookies.containsKey(name);
	}

}
