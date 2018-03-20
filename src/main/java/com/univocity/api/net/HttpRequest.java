/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.net;

import com.univocity.api.*;
import com.univocity.api.common.*;
import com.univocity.api.io.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

/**
 * A reusable, cloneable HTTP request configuration, with support for parameterization of the URL.
 *
 * Example of a URL with 2 parameters (QUERY and PERIOD): {@code "https://www.google.com/?q={QUERY}#q={QUERY}&tbs=qdr:{PERIOD}"}
 *
 * Use {@link #setUrlParameter(String, Object)} to set the values of any parameters so that {@link #getUrl()}
 * generates the final URL.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see HttpMethodType
 * @see HttpResponse
 * @see HttpResponseReader
 * @see UrlReaderProvider
 */
public final class HttpRequest implements Cloneable {

	private ParameterizedString url;
	private int timeout = 0;
	private boolean followRedirects = true;
	private boolean validateSsl = true;
	private HttpMethodType httpMethodType = HttpMethodType.GET;
	private LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> cookies = new LinkedHashMap<String, String>();
	private List<Object[]> data = new ArrayList<Object[]>();
	private TreeMap<String, Object> parameters = new TreeMap<String, Object>();
	private Charset charset;
	private boolean ignoreHttpErrors;

	private String proxyUser;
	private char[] proxyPassword;
	private RateLimiter rateLimiter;

	/**
	 * Creates a new request for a given request URL
	 *
	 * @param url the request URL
	 * @param rateLimiter a {@link RateLimiter} to prevent the execution of excessive simultaneous requests.
	 */
	HttpRequest(String url, RateLimiter rateLimiter) {
		setUrl(url);
		this.rateLimiter = rateLimiter;
	}

	/**
	 * Returns the {@link RateLimiter} associated with this request, if any.
	 * @return the request rate limiter, if available.
	 */
	public final RateLimiter getRateLimiter() {
		return rateLimiter;
	}

	/**
	 * Associates a {@link RateLimiter} with this request to prevent multiple simultaneous requests.
	 * @param rateLimiter the rate limiter to be used when executing this request.
	 */
	public final void setRateLimiter(RateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}

	/**
	 * Modifies the URL this HTTP request. Parameter values set in the previous URL will be re-applied on the new URL,
	 * if their names match.
	 *
	 * @param url the new request URL.
	 */
	public void setUrl(String url) {
		Args.notBlank(url, "HTTP request URL");

		Map<String, Object> oldParameters = Collections.emptyMap();
		if (this.url != null) {
			oldParameters = this.url.getParameterValues();
		}

		this.url = new ParameterizedString(url);

		for (String param : this.url.getParameters()) {
			this.url.set(param, oldParameters.get(param));
		}
	}

	/**
	 * Sets the {@code Connection} request header to explicitly to {@code keep-alive} if the connection is meant to be
	 * reused for further requests, or to {@code close} if the connection should be discarded and closed as soon as the
	 * response is processed.
	 *
	 * @param enableKeepAlive flag indicating whether or not the HTTP connection should be persistent (i.e kept alive).
	 */
	public final void setKeepAliveEnabled(boolean enableKeepAlive) {
		setHeader("Connection", enableKeepAlive ? "keep-alive" : "close");
	}

	/**
	 * Returns a flag indicating whether the connection is meant to be reused for further requests (i.e. persistent) or
	 * not, in which case the {@code Connection} request header is set to {@code close}. If the {@code Connection}
	 * header is not set, it is assumed that the connection is persistent, and this method will return {@code true}.
	 *
	 * @return flag indicating whether or not the HTTP connection is persistent (i.e kept alive).
	 */
	public final boolean isKeepAliveEnabled() {
		String value = headers.get("Connection");
		return value == null || "keep-alive".equalsIgnoreCase(value);
	}

	/**
	 * Defines a {@code User-Agent} request header, which identifies the user agent originating the request.
	 *
	 * @param userAgent the new {@code User-Agent} value
	 */
	@Choices(file = "userAgents.txt")
	@UI
	public final void setUserAgent(String userAgent) {
		setHeader("User-Agent", userAgent);
	}

	/**
	 * Defines a {@code Referer} request header that identifies the address of the web-page
	 * that linked to the resource being requested.
	 *
	 * @param referrer the new {@code Referer} value
	 */
	@UI
	public final void setReferrer(String referrer) {
		setHeader("Referer", referrer);
	}

	/**
	 * Returns the current  {@code User-Agent} request header, which identifies the user agent originating the request.
	 *
	 * @return the {@code User-Agent} header
	 */
	public final String getUserAgent() {
		return headers.get("User-Agent");
	}

	/**
	 * Returns the current {@code Referer} request header that identifies the address of the web-page
	 * that linked to the resource being requested.
	 *
	 * @return the {@code Referer} header
	 */
	public final String getReferrer() {
		return headers.get("Referer");
	}

	/**
	 * Defines a header and its value. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of colon-separated name-value pairs
	 *
	 * @param header the header name
	 * @param value  the header value
	 */
	public final void setHeader(String header, String value) {
		setHeader(header, value, false);
	}

	/**
	 * Defines a header and its value. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of colon-separated name-value pairs
	 *
	 * @param header the header name
	 * @param value  the header value
	 * @param encode flag indicating whether the value should be encoded
	 */
	public final void setHeader(String header, String value, boolean encode) {
		if (encode) {
			value = Args.encode(value);
		}
		set(headers, header, value);
	}

	/**
	 * Replaces any previous headers with the given values. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of colon-separated name-value pairs
	 *
	 * @param headers a map of headers names and their values. Previous values will be discarded
	 */
	public final void setHeaders(Map<String, String> headers) {
		setHeaders(headers, false);
	}

	/**
	 * Adds the given headers and their values to the existing list of headers. All headers are transmitted after
	 * the request line (the first line of a HTTP message), in the format of colon-separated name-value pairs
	 *
	 * @param headers a map of header names and their values.
	 */
	public final void addHeaders(Map<String, String> headers) {
		addHeaders(headers, false);
	}

	/**
	 * Replaces any previous headers with the given values. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of colon-separated name-value pairs
	 *
	 * @param headers a map of headers names and their values. Previous values will be discarded
	 * @param encode  flag indicating whether values should be encoded
	 */
	public final void setHeaders(Map<String, String> headers, boolean encode) {
		this.headers.clear();
		addHeaders(headers, encode);
	}

	/**
	 * Adds the given headers and their values to the existing list of headers. All headers are transmitted after
	 * the request line (the first line of a HTTP message), in the format of colon-separated name-value pairs
	 *
	 * @param headers a map of header names and their values.
	 * @param encode  flag indicating whether values should be encoded
	 */
	public final void addHeaders(Map<String, String> headers, boolean encode) {
		if (encode) {
			for (Map.Entry<String, String> e : headers.entrySet()) {
				set(this.headers, e.getKey(), Args.encode(e.getValue()));
			}
		} else {
			for (Map.Entry<String, String> e : headers.entrySet()) {
				set(this.headers, e.getKey(), e.getValue());
			}
		}
	}

	/**
	 * Sets a cookie to be added to the {@code Cookie} HTTP header.
	 * All cookies are sent in this header in the format of semicolon-separated name-value pairs.
	 *
	 * @param cookie the cookie name
	 * @param value  the cookie value
	 */
	public final void setCookie(String cookie, String value) {
		set(cookies, cookie, value);
	}

	/**
	 * Replaces the cookies to be added to the {@code Cookie} HTTP header.
	 * All cookies are sent in this header in the format of semicolon-separated name-value pairs.
	 *
	 * @param cookies a map of cookie names and their values. Previous values will be discarded
	 */
	public final void setCookies(Map<String, String> cookies) {
		this.cookies.clear();
		addCookies(cookies);
	}

	/**
	 * Adds the given cookies to the {@code Cookie} HTTP header.
	 * All cookies are sent in this header in the format of semicolon-separated name-value pairs.
	 *
	 * @param cookies a map of cookie names and their values.
	 */
	public final void addCookies(Map<String, String> cookies) {
		for (Map.Entry<String, String> e : cookies.entrySet()) {
			set(this.cookies, e.getKey(), e.getValue());
		}
	}

	private void set(Map<String, String> map, String key, String value) {
		if (value == null) {
			map.remove(key);
		} else {
			map.put(key, value);
		}
	}

	/**
	 * Defines a time limit (in milliseconds) for an initial connection to be established from this request.
	 * <i>Defaults to 0 (no timeout).</i>
	 *
	 * @param timeout the time limit to wait until a connection is established.
	 */
	@Range(min = 0, max = 10000)
	@UI
	public final void setTimeout(int timeout) {
		Args.positiveOrZero(timeout, "HTTP request timeout");
		this.timeout = timeout;

	}

	/**
	 * Sets the {@link HttpMethodType} to be used by this request.
	 * The method type identifies an action to be performed on the identified (remote) resource.
	 *
	 * <i>Defaults to {@link HttpMethodType#GET}</i>
	 *
	 * @param httpMethodType the HTTP method to use
	 */
	public final void setHttpMethodType(HttpMethodType httpMethodType) {
		Args.notNull(httpMethodType, "HTTP method type");
		this.httpMethodType = httpMethodType;
	}

	/**
	 * Returns the request URL, with any parameters replaced by their values (if any)
	 *
	 * @return the URL that will be used to invoke the HTTP request.
	 */
	public final String getUrl() {
		return url.applyParameterValues();
	}

	/**
	 * Assigns a value to a given parameter of the URL provided in the constructor of this class.
	 * The value will be used to modify the address that will be accessed by this HTTP request.
	 *
	 * Calling {@link #getUrl()} will return the updated target URL of this request. Parameters without values
	 * won't be replaced in the URL.
	 *
	 * @param parameterName  name of the parameter enclosed within { and } in the URL
	 * @param parameterValue value of the given parameter, to replace the parameter name in the URL
	 *
	 * @throws IllegalArgumentException if the parameter name does not exist
	 */
	public final void setUrlParameter(String parameterName, Object parameterValue) {
		setUrlParameter(parameterName, parameterValue, true);
	}

	/**
	 * Returns the value of a given parameter of the URL provided in the constructor of this class.
	 * The value will be used to modify the address that will be accessed by this HTTP request.
	 *
	 * Calling {@link #getUrl()} will return the updated target URL of this request. Parameters without values
	 * won't be replaced in the URL.
	 *
	 * @param parameterName name of the parameter enclosed within { and } in the URL
	 *
	 * @return value of the given parameter, to replace the parameter name in the URL
	 *
	 * @throws IllegalArgumentException if the parameter name does not exist
	 */
	public final Object getUrlParameter(String parameterName) {
		return this.url.get(parameterName);
	}

	/**
	 * Returns the names of all available URL parameters in an unmodifiable set.
	 *
	 * @return the available parameters in the URL
	 */
	public final Set<String> getUrlParameters() {
		return this.url.getParameters();
	}

	/**
	 * Assigns a value to a given parameter of the URL provided in the constructor of this class.
	 * The value will be used to modify the address that will be accessed by this HTTP request.
	 *
	 * Calling {@link #getUrl()} will return the updated target URL of this request. Parameters without values
	 * won't be replaced in the URL.
	 *
	 * @param parameterName  name of the parameter enclosed within { and } in the URL
	 * @param parameterValue value of the given parameter, to replace the parameter name in the URL
	 * @param encode         flag indicating whether to encode the given parameter value.
	 *                       If the value is already encoded, it won't be encoded again.
	 *
	 * @throws IllegalArgumentException if the parameter name does not exist
	 */
	public final void setUrlParameter(String parameterName, Object parameterValue, boolean encode) {
		if (encode) {
			parameterValue = Args.encode(parameterName, parameterValue, getCharsetName("UTF-8"));
		}
		this.url.set(parameterName, parameterValue);
	}

	/**
	 * Returns the name of the charset to be used when reading the response resulting from this HTTP request.
	 * This will take precedence over the charset defined in the {@code Content-Type} header of the HTTP response.
	 *
	 * @param defaultIfNull a default charset name to return if the result of {@link #getCharsetName()} is {@code null}
	 *
	 * @return the charset name
	 */
	public final String getCharsetName(String defaultIfNull) {
		String name = getCharsetName();
		if (name == null) {
			return defaultIfNull;
		}
		return name;
	}

	/**
	 * Returns the value of a given parameter of the URL provided in the constructor of this class.
	 * The value will be used to modify the address that will be accessed by this HTTP request.
	 *
	 * Calling {@link #getUrl()} will return the updated target URL of this request. Parameters without values
	 * won't be replaced in the URL.
	 *
	 * @param parameterName name of the parameter enclosed within { and } in the URL
	 * @param decode        flag indicating whether to decode the value associated with the given parameter name.
	 *
	 * @return value of the given parameter, to replace the parameter name in the URL
	 *
	 * @throws IllegalArgumentException if the parameter name does not exist
	 */
	public final String getUrlParameter(String parameterName, boolean decode) {
		Object out = this.url.get(parameterName);
		if (decode) {
			return Args.decode(parameterName, out, getCharsetName("UTF-8"));
		} else if (out != null) {
			return String.valueOf(out);
		}
		return null;
	}

	/**
	 * Clears all values assigned to all parameters of the URL provided in the constructor of this class.
	 *
	 * Calling {@link #getUrl()} after this method will return the original URL with
	 * parameters provided in the constructor.
	 */
	public final void clearUrlParameters() {
		this.url.clearValues();
	}

	/**
	 * Returns the time limit (in milliseconds) for an initial connection to be established from this request.
	 * <i>Defaults to 0 (no timeout).</i>
	 *
	 * @return the current timeout value.
	 */
	public final int getTimeout() {
		return timeout;
	}

	/**
	 * Returns a flag indicating if the request should follow redirects. If enabled, {@link HttpResponse#getRedirectionUrl()}
	 * should return the redirection URL if this request was redirected.
	 *
	 * <i>Defaults to {@code true}.</i>
	 *
	 * @return a flag indicating whether or not the request is configured to follow redirects.
	 */
	public final boolean getFollowRedirects() {
		return followRedirects;
	}

	/**
	 * Configures the request to follow redirects. If enabled, {@link HttpResponse#getRedirectionUrl()} should return
	 * the redirection URL if this request was redirected.
	 *
	 * <i>Defaults to {@code true}.</i>
	 *
	 * @param followRedirects flag indicating whether or not to follow redirects.
	 */
	@UI
	public final void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	/**
	 * Returns a flag indicating whether SSL validation is enabled for HTTPS requests.
	 *
	 * <i>Defaults to {@code true}.</i>
	 *
	 * @return a flag indicating whether SSL will be validated.
	 */
	public final boolean isSslValidationEnabled() {
		return validateSsl;
	}

	/**
	 * Configures this HTTP request to enable/disable SSL validation for HTTPS requests.
	 *
	 * <i>Defaults to {@code true}.</i>
	 *
	 * @param validateSsl flag indicating whether SSL is should be validated.
	 */
	@UI
	public final void setSslValidationEnabled(boolean validateSsl) {
		this.validateSsl = validateSsl;
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
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as a plain {@code String}
	 *
	 * @param paramName the parameter name
	 * @param value     the parameter value
	 */
	public final void addDataParameter(String paramName, Object value) {
		Args.notBlank(paramName, "Parameter name");
		this.data.add(new Object[]{paramName, value == null ? "" : String.valueOf(value)});
	}

	/**
	 * Removes a given data parameter from the body of a {@link HttpMethodType#POST} request
	 * @param paramName the parameter name
	 */
	public final void removeDataParameter(String paramName) {
		Args.notBlank(paramName, "Parameter name");
		Iterator<Object[]> it = this.data.iterator();
		while(it.hasNext()){
			Object[] entry = it.next();
			if(paramName.equals(entry[0])){
				it.remove();
			}
		}
	}

	/**
	 * Adds/replaces a parameter of the body of {@link HttpMethodType#POST} requests as a plain {@code String}
	 *
	 * @param paramName the parameter name
	 * @param value     the parameter value
	 */
	public final void setDataParameter(String paramName, Object value) {
		removeDataParameter(paramName);
		this.addDataParameter(paramName, value);
	}


	/**
	 * Adds multiple parameters to the body of {@link HttpMethodType#POST} requests as a plain {@code String}s.
	 * Multiple values can be associated with a parameter name.
	 *
	 * {@code null} or empty lists of values will cause an empty {@code String} to be associated with the parameter name.
	 *
	 * @param params the parameters and values associated with them. Multiple values can be associated with each
	 *               parameter name.
	 * @param keys   the specific keys to read from the given parameter map. Parameters will be added in the order
	 *               defined by the key sequence. If the map doesn't contain a given key, a parameter named after
	 *               the key will be created, assigning an empty {@code String} value to it.
	 *               If no keys are provided, all elements of the map will be used.
	 */
	public final void addDataParameters(Map<String, String[]> params, String... keys) {
		if (keys.length == 0) {
			for (Map.Entry<String, String[]> e : params.entrySet()) {
				addDataParameters(e.getKey(), e.getValue());
			}
		} else {
			for (String key : keys) {
				addDataParameters(key, params.get(key));
			}
		}
	}

	private void addDataParameters(String key, String[] values) {
		if (key == null) {
			return;
		}
		if (values == null || values.length == 0) {
			addDataParameter(key, "");
		} else {
			for (int i = 0; i < values.length; i++) {
				addDataParameter(key, values[i]);
			}
		}
	}

	/**
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as an {@link InputStream}, which will
	 * upload content in MIME multipart/form-data encoding.
	 *
	 * @param paramName    the parameter name
	 * @param fileName     the file name
	 * @param dataProvider a {@link ResourceProvider} which will open the input to be uploaded when required.
	 */
	public final void addDataStreamParameter(String paramName, String fileName, ResourceProvider<InputStream> dataProvider) {
		this.data.add(new Object[]{paramName, fileName, dataProvider});
	}

	/**
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as an {@link InputStream}, which will
	 * upload content in MIME multipart/form-data encoding.
	 *
	 * @param paramName   the parameter name
	 * @param fileName    the file name
	 * @param inputStream the binary stream of the content to upload
	 */
	public final void addDataStreamParameter(String paramName, String fileName, final InputStream inputStream) {
		addDataStreamParameter(paramName, fileName, new ResourceProvider<InputStream>() {
			@Override
			public InputStream getResource() {
				return inputStream;
			}
		});
	}

	/**
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as {@link File}.
	 *
	 * @param paramName the parameter name
	 * @param fileName  the file name
	 * @param file      the file to upload.
	 */
	public final void addFileParameter(String paramName, String fileName, FileProvider file) {
		this.data.add(new Object[]{paramName, fileName, file});
	}

	/**
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as {@link File}.
	 *
	 * @param paramName the parameter name
	 * @param fileName  fileName   the file name
	 * @param file      the file to upload.
	 */
	public final void addFileParameter(String paramName, String fileName, File file) {
		this.data.add(new Object[]{paramName, fileName, new FileProvider(file)});
	}

	/**
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as {@link File}.
	 *
	 * @param paramName  the parameter name
	 * @param fileName   the file name
	 * @param pathToFile the file or resource to upload.It can either be the path to a file in the file system or
	 *                   a resource in the classpath. The path can contain system variables enclosed within { and }
	 *                   (e.g. {@code {user.home}/myapp/log"}).
	 */
	public final void addFileParameter(String paramName, String fileName, String pathToFile) {
		this.data.add(new Object[]{paramName, fileName, new FileProvider(pathToFile)});
	}

	/**
	 * Adds/replaces a parameter to the body of {@link HttpMethodType#POST} requests as an {@link InputStream}, which will
	 * upload content in MIME multipart/form-data encoding.
	 *
	 * @param paramName    the parameter name
	 * @param fileName     the file name
	 * @param dataProvider a {@link ResourceProvider} which will open the input to be uploaded when required.
	 */
	public final void setDataStreamParameter(String paramName, String fileName, ResourceProvider<InputStream> dataProvider) {
		removeDataParameter(paramName);
		addDataStreamParameter(paramName, fileName, dataProvider);
	}

	/**
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as an {@link InputStream}, which will
	 * upload content in MIME multipart/form-data encoding.
	 *
	 * @param paramName   the parameter name
	 * @param fileName    the file name
	 * @param inputStream the binary stream of the content to upload
	 */
	public final void setDataStreamParameter(String paramName, String fileName, final InputStream inputStream) {
		removeDataParameter(paramName);
		addDataStreamParameter(paramName, fileName, inputStream);
	}

	/**
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as {@link File}.
	 *
	 * @param paramName the parameter name
	 * @param fileName  the file name
	 * @param file      the file to upload.
	 */
	public final void setFileParameter(String paramName, String fileName, FileProvider file) {
		removeDataParameter(paramName);
		addFileParameter(paramName, fileName, file);
	}

	/**
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as {@link File}.
	 *
	 * @param paramName the parameter name
	 * @param fileName  fileName   the file name
	 * @param file      the file to upload.
	 */
	public final void setFileParameter(String paramName, String fileName, File file) {
		removeDataParameter(paramName);
		addFileParameter(paramName, fileName, file);
	}

	/**
	 * Adds/replaces a parameter to the body of {@link HttpMethodType#POST} requests as {@link File}.
	 *
	 * @param paramName  the parameter name
	 * @param fileName   the file name
	 * @param pathToFile the file or resource to upload.It can either be the path to a file in the file system or
	 *                   a resource in the classpath. The path can contain system variables enclosed within { and }
	 *                   (e.g. {@code {user.home}/myapp/log"}).
	 */
	public final void setFileParameter(String paramName, String fileName, String pathToFile) {
		removeDataParameter(paramName);
		addFileParameter(paramName, fileName, pathToFile);
	}

	/**
	 * Returns the currently defined headers and their values. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of colon-separated name-value pairs
	 *
	 * @return a map of headers and their values.
	 */
	public final Map<String, String> getHeaders() {
		return Collections.unmodifiableMap(headers);
	}

	/**
	 * Returns the cookies to be added to the {@code Cookie} HTTP header.
	 * All cookies are sent in this header in the format of semicolon-separated name-value pairs.
	 *
	 * @return a map of cookie names and values.
	 */
	public final Map<String, String> getCookies() {
		return Collections.unmodifiableMap(cookies);
	}

	/**
	 * Returns the data parameters sent by on the body of this request if it is a {@link HttpMethodType#POST} request.
	 *
	 * @return a map of data parameters and their values.
	 */
	public final List<Object[]> getData() {
		return Collections.unmodifiableList(data);
	}

	/**
	 * Removes all data parameters set on the body of this request.
	 */
	public final void clearDataParameters() {
		data.clear();
	}

	/**
	 * Configures this request to connect through a proxy.
	 *
	 * @param proxy the proxy configuration.
	 */
	public final void setProxy(Proxy proxy) {
		setProxy(proxy, null, 0, null, null);
	}

	/**
	 * Configures this request to connect through a proxy with authentication.
	 *
	 * @param proxy the proxy configuration.
	 * @param user  the proxy user.
	 */
	public final void setProxy(Proxy proxy, String user) {
		setProxy(proxy, null, 0, user, null);
	}

	/**
	 * Configures this request to connect through a proxy with authentication.
	 *
	 * @param proxy    the proxy configuration.
	 * @param user     the proxy user.
	 * @param password the proxy password.
	 */
	public final void setProxy(Proxy proxy, String user, char[] password) {
		setProxy(proxy, null, 0, user, password);
	}


	/**
	 * Configures this request to connect through a proxy.
	 *
	 * @param host the proxy host.
	 * @param port the proxy port.
	 */
	public final void setProxy(String host, int port) {
		setProxy(host, port, null, null);
	}

	/**
	 * Configures this request to connect through a proxy.
	 *
	 * @param host the proxy host.
	 * @param port the proxy port.
	 * @param user the proxy user.
	 */
	public final void setProxy(String host, int port, String user) {
		setProxy(host, port, user, null);
	}

	/**
	 * Configures this request to connect through a proxy.
	 *
	 * @param host     the proxy host.
	 * @param port     the proxy port.
	 * @param user     the proxy user.
	 * @param password the proxy password
	 */
	public final void setProxy(String host, int port, String user, char[] password) {
		setProxy((Proxy) null, host, port, user, password);
	}

	private Proxy proxy;

	/**
	 * Configures this request to connect through a proxy.
	 *
	 * @param proxy    an optional existing proxy configuration.
	 * @param host     the proxy host.
	 * @param port     the proxy port.
	 * @param user     the proxy user.
	 * @param password the proxy password (note the char array is copied)
	 */
	private final void setProxy(Proxy proxy, String host, int port, String user, char[] password) {
		if (proxy == null) {
			Args.positive(port, "Proxy port");
			Args.notBlank(host, "Proxy host");
			this.proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(host, port));
		} else {
			this.proxy = proxy;
		}

		this.proxyUser = user;
		this.proxyPassword = password == null ? null : password.clone();
	}

	/**
	 * Configures this request to connect through a proxy.
	 *
	 * @param proxyType the type of proxy.
	 * @param host      the proxy host.
	 * @param port      the proxy port.
	 * @param user      the proxy user.
	 * @param password  the proxy password (note the char array is copied)
	 */
	@UI
	public void setProxy(Proxy.Type proxyType, String host, int port, String user, char[] password) {
		Proxy proxy = new Proxy(proxyType, new InetSocketAddress(host, port));
		setProxy(proxy, host, port, user, password);
	}

	/**
	 * Returns the username to be used when authenticating with a proxy
	 *
	 * @return the proxy user
	 */
	public final String getProxyUser() {
		return proxyUser;
	}


	/**
	 * Returns the password to be used when connecting with a proxy
	 *
	 * @return the proxy password
	 */
	public final char[] getProxyPassword() {
		return proxyPassword;
	}

	/**
	 * Returns the the proxy this request should use to connect to
	 *
	 * @return the proxy configuration
	 */
	public final Proxy getProxy() {
		return proxy;
	}

	/**
	 * Defines a charset to be used when reading the response resulting from this HTTP request.
	 * This will take precedence over the charset defined in the {@code Content-Type} header of the HTTP response.
	 *
	 * @param charset the charset name
	 */
	@UI
	public void setCharset(String charset) {
		if (charset == null) {
			this.charset = null;
		} else {
			this.charset = Charset.forName(charset);
		}
	}

	/**
	 * Defines a charset to be used when reading the response resulting from this HTTP request.
	 * This will take precedence over the charset defined in the {@code Content-Type} header of the HTTP response.
	 *
	 * @param charset the charset
	 */
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	/**
	 * Returns the charset to be used when reading the response resulting from this HTTP request.
	 * This will take precedence over the charset defined in the {@code Content-Type} header of the HTTP response.
	 *
	 * @return the charset
	 */
	public Charset getCharset() {
		return this.charset;
	}

	/**
	 * Returns the name of the charset to be used when reading the response resulting from this HTTP request.
	 * This will take precedence over the charset defined in the {@code Content-Type} header of the HTTP response.
	 *
	 * @return the charset name
	 */
	public String getCharsetName() {
		if (charset == null) {
			return null;
		}
		return this.charset.name();
	}

	/**
	 * Returns whether this HTTP request to ignore HTTP errors returned by the remote server and process the
	 * response anyway. Defaults to {@code false}.
	 *
	 * @return a flag indicating whether or not HTTP response code errors should be ignored/
	 */
	public boolean isIgnoreHttpErrors() {
		return ignoreHttpErrors;
	}

	/**
	 * Configures the this HTTP request to ignore HTTP errors returned by the remote server and process the
	 * response anyway. Defaults to {@code false}.
	 *
	 * @param ignoreHttpErrors flag indicating whether or not HTTP response code errors should be ignored/
	 */
	@UI
	public void setIgnoreHttpErrors(boolean ignoreHttpErrors) {
		this.ignoreHttpErrors = ignoreHttpErrors;
	}

	/**
	 * Clones this request and all its configurations.
	 * <b>NOTE:</b>Data parameters are reused and not cloned.
	 *
	 * @return a copy of this request with all possible settings.
	 */
	public final HttpRequest clone() {
		try {
			HttpRequest clone = (HttpRequest) super.clone();
			clone.url = this.url.clone();
			clone.parameters = (TreeMap<String, Object>) this.parameters.clone();
			clone.headers = (LinkedHashMap<String, String>) this.headers.clone();
			clone.cookies = (LinkedHashMap<String, String>) this.cookies.clone();
			clone.data = new ArrayList<Object[]>();

			for (Object[] object : this.data) {
				clone.data.add(object.clone());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Could not clone", e);
		}
	}

	@Override
	public String toString() {
		return httpMethodType + " - " + getUrl();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		HttpRequest that = (HttpRequest) o;

		if (url != null ? !url.equals(that.url) : that.url != null) {
			return false;
		}
		if (httpMethodType != that.httpMethodType) {
			return false;
		}
		if (headers != null ? !headers.equals(that.headers) : that.headers != null) {
			return false;
		}
		if (data != null ? !data.equals(that.data) : that.data != null) {
			return false;
		}
		return parameters != null ? parameters.equals(that.parameters) : that.parameters == null;
	}

	@Override
	public int hashCode() {
		int result = url != null ? url.hashCode() : 0;
		result = 31 * result + (httpMethodType != null ? httpMethodType.hashCode() : 0);
		result = 31 * result + (headers != null ? headers.hashCode() : 0);
		result = 31 * result + (data != null ? data.hashCode() : 0);
		result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
		return result;
	}
}
