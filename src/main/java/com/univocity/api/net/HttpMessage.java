/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.net;

import com.univocity.api.common.*;

import java.util.*;

import static com.univocity.api.common.Utils.*;

/**
 * Basic details of a HTTP message (request or response)
 */
public abstract class HttpMessage {
	protected RequestMethod httpMethodType = RequestMethod.GET;
	protected LinkedHashMap<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
	protected LinkedHashMap<String, String> cookies = new LinkedHashMap<String, String>();

	/**
	 * Returns the headers and their corresponding values in this HTTP message.
	 * If multiple values are associated with the same header, they will be be separated by comma.
	 *
	 * Use {@link #getMultiHeaders()} to obtain the multiple values in a list.
	 *
	 * @return a map of headers and their values.
	 */
	public final Map<String, String> getHeaders() {
		return Utils.joinValues(headers, ", ");
	}

	/**
	 * Checks whether this HTTP message has a given header defined. The search
	 * is case insensitive.
	 *
	 * @param name the header name to look for
	 * @return {@code true} if the given header exists in this HTTP message, otherwise {@code false}
	 */
	public final boolean hasHeader(String name) {
		Map.Entry<String, List<String>> entry = getEntryCaseInsensitive(headers, name);
		return entry != null;
	}

	/**
	 * Returns the headers and their corresponding values in this HTTP message.
	 * Multiple values can be associated with the same header, use {@link #getHeaders()}
	 * to obtain them as a comma separated sequence
	 *
	 * @return a map of headers and their values.
	 */
	public final Map<String, List<String>> getMultiHeaders() {
		return Collections.unmodifiableMap(headers);
	}

	/**
	 * Returns the value(s) currently defined for a given header. If multiple values are
	 * associated with the header the resulting {@code String} will have them separated by a comma.
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
	 * Use {@link getHeaderValue(String)} to obtain the header values as a comma delimited {@code String}
	 *
	 * @return the list of values associated with the given header, or {@code null} if the header is not defined
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
	 *
	 * @return a map of cookie names and values.
	 */
	public final Map<String, String> getCookies() {
		return Collections.unmodifiableMap(cookies);
	}

	/**
	 * Returns the {@link RequestMethod} to be used by this request.
	 * The method type identifies an action to be performed on the identified (remote) resource.
	 *
	 * <i>Defaults to {@link RequestMethod#GET}</i>
	 *
	 * @return the HTTP method to use
	 */
	public final RequestMethod getHttpMethodType() {
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

	/**
	 * Returns the value associated with a given cookie name
	 * @param name the name that identifies a cookie
	 * @return the value associated with the cookie, it any.
	 */
	public final String getCookieValue(String name) {
		return cookies.get(name);
	}

	/**
	 * Verifies whether a cookie has been set
	 * @param name the name that identifies a cookie
	 * @return {@true} if a cookie with the given name has been defined, otherwise {@false}
	 */
	public final boolean hasCookie(String name) {
		return cookies.containsKey(name);
	}

	/**
	 * Verifies whether a given header has a value
	 * @param name the header name whose values will be verified
	 * @param value the value to find among the possible multiple values associated with the given header
	 * @return {@code true} if the given value is associated with the given header name, otherwise {@code false}
	 */
	public final boolean hasHeaderWithValue(String name, String value) {
		List<String> values = getHeaderValues(name);
		if (values != null) {
			for (String candidate : values) {
				if (value.equalsIgnoreCase(candidate))
					return true;
			}
		}
		return false;
	}
}
