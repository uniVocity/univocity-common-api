/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

import java.io.*;
import java.util.*;

/**
 * A reusable, cloneable HTTP request.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see HttpMethodType
 * @see HttpResponse
 * @see HttpResponseReader
 * @see UrlReaderProvider
 */
public class HttpRequest implements Cloneable {

	private String url;
	private int timeout = 0;
	private boolean followRedirects = false;
	private boolean validateSSL = true;
	private HttpMethodType httpMethodType = HttpMethodType.GET;
	private LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> cookies = new LinkedHashMap<String, String>();
	private List<Object[]> data = new ArrayList<Object[]>();

	private String proxyUser;
	private String proxyHost;
	private String proxyPassword;
	private int proxyPort = -1;

	/**
	 * Creates a new request for a given request URL
	 *
	 * @param url the request URL
	 */
	public HttpRequest(String url) {
		setUrl(url);
	}

	/**
	 * Modifies the current request URL
	 *
	 * @param url the new request URL
	 */
	public void setUrl(String url) {
		Args.notBlank(url, "HTTP request URL");
		this.url = url;
	}

	/**
	 * Defines a {@code User-Agent} request header, which identifies the user agent originating the request.
	 *
	 * @param userAgent the new {@code User-Agent} value
	 */
	public void setUserAgent(String userAgent) {
		setHeader("User-Agent", userAgent);
	}

	/**
	 * Defines a {@code Referer} request header that identifies the address of the web-page
	 * that linked to the resource being requested.
	 *
	 * @param referrer the new {@code Referer} value
	 */
	public void setReferrer(String referrer) {
		setHeader("Referer", referrer);
	}

	/**
	 * Returns the current  {@code User-Agent} request header, which identifies the user agent originating the request.
	 *
	 * @return the {@code User-Agent} header
	 */
	public String getUserAgent() {
		return headers.get("User-Agent");
	}

	/**
	 * Returns the current {@code Referer} request header that identifies the address of the web-page
	 * that linked to the resource being requested.
	 *
	 * @return the {@code Referer} header
	 */
	public String getReferrer() {
		return headers.get("Referer");
	}

	/**
	 * Defines a header and its value. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of colon-separated name-value pairs
	 *
	 * @param header the header name
	 * @param value  the header value
	 */
	public void setHeader(String header, String value) {
		set(headers, header, value);
	}

	/**
	 * Sets a cookie to be added to the {@code Cookie} HTTP header.
	 * All cookies are sent in this header in the format of semicolon-separated name-value pairs.
	 *
	 * @param cookie the cookie name
	 * @param value  the cookie value
	 */
	public void setCookie(String cookie, String value) {
		set(cookies, cookie, value);
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
	public void setTimeout(int timeout) {
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
	public void setHttpMethodType(HttpMethodType httpMethodType) {
		Args.notNull(httpMethodType, "HTTP method type");
		this.httpMethodType = httpMethodType;
	}

	/**
	 * Returns the request URL
	 *
	 * @return the request URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns the time limit (in milliseconds) for an initial connection to be established from this request.
	 * <i>Defaults to 0 (no timeout).</i>
	 *
	 * @return the current timeout value.
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Returns a flag indicating if the request should follow redirects. If enabled, {@link HttpResponse#getRedirectionUrl()}
	 * should return the redirection URL if this request was redirected.
	 *
	 * <i>Defaults to {@code false}.</i>
	 *
	 * return a flag indicating whether or not the request is configured to follow redirects.
	 */
	public boolean getFollowRedirects() {
		return followRedirects;
	}

	/**
	 * Configures the request to follow redirects. If enabled, {@link HttpResponse#getRedirectionUrl()} should return
	 * the redirection URL if this request was redirected.
	 *
	 * <i>Defaults to {@code false}.</i>
	 *
	 * @param followRedirects flag indicating whether or not to follow redirects.
	 */
	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	/**
	 * Returns a flag indicating whether SSL validation is enabled for HTTPS requests.
	 *
	 * <i>Defaults to {@code true}.</i>
	 *
	 * @return a flag indicating whether SSL will be validated.
	 */
	public boolean isSSLValidationEnabled() {
		return validateSSL;
	}

	/**
	 * Configures this HTTP request to enable/disable SSL validation for HTTPS requests.
	 *
	 * <i>Defaults to {@code true}.</i>
	 *
	 * @param validateSSL flag indicating whether SSL is should be validated.
	 */
	public void setSSLValidationEnabled(boolean validateSSL) {
		this.validateSSL = validateSSL;
	}

	/**
	 * Returns the {@link HttpMethodType} to be used by this request.
	 * The method type identifies an action to be performed on the identified (remote) resource.
	 *
	 * <i>Defaults to {@link HttpMethodType#GET}</i>
	 *
	 * @return the HTTP method to use
	 */
	public HttpMethodType getHttpMethodType() {
		return httpMethodType;
	}

	/**
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as a plain {@code String}
	 *
	 * @param paramName the parameter name
	 * @param value     the parameter value
	 */
	public void setDataParameter(String paramName, String value) {
		this.data.add(new Object[]{paramName, value});
	}

	/**
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as an {@link InputStream}, which will
	 * upload content in MIME multipart/form-data encoding.
	 *
	 * @param paramName    the parameter name
	 * @param dataProvider a {@link ResourceProvider} which will open the input to be uploaded when required.
	 */
	public void setDataStreamParameter(String paramName, String filename, ResourceProvider<InputStream> dataProvider) {
		this.data.add(new Object[]{paramName, filename, dataProvider});
	}

	/**
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as an {@link InputStream}, which will
	 * upload content in MIME multipart/form-data encoding.
	 *
	 * @param paramName   the parameter name
	 * @param inputStream the binary stream of the content to upload
	 */
	public void setDataStreamParameter(String paramName, String filename, final InputStream inputStream) {
		setDataStreamParameter(paramName, filename, new ResourceProvider<InputStream>() {
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
	 * @param file      the file to upload.
	 */
	public void setFileParameter(String paramName, String filename, FileProvider file) {
		this.data.add(new Object[]{paramName, filename, file});
	}

	/**
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as {@link File}.
	 *
	 * @param paramName the parameter name
	 * @param file      the file to upload.
	 */
	public void setFileParameter(String paramName, String filename, File file) {
		this.data.add(new Object[]{paramName, filename, new FileProvider(file)});
	}

	/**
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as {@link File}.
	 *
	 * @param paramName  the parameter name
	 * @param pathToFile the file or resource to upload.It can either be the path to a file in the file system or
	 *                   a resource in the classpath. The path can contain system variables enclosed within { and }
	 *                   (e.g. {@code {user.home}/myapp/log"}).
	 */
	public void setFileParameter(String paramName, String filename, String pathToFile) {
		this.data.add(new Object[]{paramName, filename, new FileProvider(pathToFile)});
	}

	/**
	 * Returns the currently defined headers and their values. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of colon-separated name-value pairs
	 *
	 * @return a map of headers and their values.
	 */
	public Map<String, String> getHeaders() {
		return Collections.unmodifiableMap(headers);
	}

	/**
	 * Returns the cookies to be added to the {@code Cookie} HTTP header.
	 * All cookies are sent in this header in the format of semicolon-separated name-value pairs.
	 *
	 * @return a map of cookie names and values.
	 */
	public Map<String, String> getCookies() {
		return Collections.unmodifiableMap(cookies);
	}

	/**
	 * Returns the data parameters sent by on the body of this request if it is a {@link HttpMethodType#POST} request.
	 *
	 * @return a map of data parameters and their values.
	 */
	public List<Object[]> getData() {
		return Collections.unmodifiableList(data);
	}

	/**
	 * Configures this request to connect through a proxy.
	 *
	 * @param host the proxy host.
	 * @param port the proxy port.
	 */
	public void setProxy(String host, int port) {
		setProxy(host, port, null, null);
	}

	/**
	 * Configures this request to connect through a proxy.
	 *
	 * @param host the proxy host.
	 * @param port the proxy port.
	 * @param user the proxy user.
	 */
	public void setProxy(String host, int port, String user) {
		setProxy(host, port, user, "");
	}

	/**
	 * Configures this request to connect through a proxy.
	 *
	 * @param host     the proxy host.
	 * @param port     the proxy port.
	 * @param user     the proxy user.
	 * @param password the proxy password
	 */
	public void setProxy(String host, int port, String user, String password) {
		Args.positive(port, "Proxy port");
		Args.notBlank(host, "Proxy host");
		this.proxyHost = host;
		this.proxyPort = port;
		this.proxyUser = user;
		this.proxyPassword = password;
	}

	/**
	 * Returns the proxy username to be used when authenticating with a proxy host
	 *
	 * @return the proxy user
	 */
	public String getProxyUser() {
		return proxyUser;
	}

	/**
	 * Returns the proxy host this request should connect to
	 *
	 * @return the proxy host
	 */
	public String getProxyHost() {
		return proxyHost;
	}

	/**
	 * Returns the proxy password to be used when connecting with a proxy host
	 *
	 * @return the proxy password
	 */
	public String getProxyPassword() {
		return proxyPassword;
	}

	/**
	 * Returns the port of the proxy host this request should connect to
	 *
	 * @return the proxy port
	 */
	public int getProxyPort() {
		return proxyPort;
	}

	/**
	 * Clones this request and all its configurations.
	 * <b>NOTE:</b>Data parameters are reused and not cloned.
	 *
	 * @return a copy of this request with all possible settings.
	 */
	public HttpRequest clone() {
		try {
			HttpRequest clone = (HttpRequest) super.clone();
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
}
