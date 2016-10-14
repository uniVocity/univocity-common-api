/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

import com.univocity.api.*;

import java.io.*;
import java.nio.charset.*;

/**
 * A {@link ReaderProvider} for URLs. This provider works with an internal {@link HttpRequest} configuration object
 * that allows you to configure a HTTP request. The actual remote invocation is performed when {@link #getResponse()}
 * is called, and the result will be provided in a {@link HttpResponse} object.
 *
 * In case of failure receiving a response, the call can be retried for a number of times using {@link #setRetries(int)}
 * and each retry can occur after a given interval, which can be defined with {@link #setRetryInterval(long)}
 *
 * The response body of a call can be stored in a local file defined using the {@link #storeLocalCopyIn(File)} method.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class UrlReaderProvider extends ReaderProvider implements Cloneable {

	private int retries = 0;
	private long retryInterval = 2000;
	private HttpResponse response;
	private HttpRequest request;
	private FileProvider localCopyProvider;

	/**
	 * Creates a new instance to read content from a given URL.
	 *
	 * @param url the URL to access.
	 */
	public UrlReaderProvider(String url) {
		this.request = new HttpRequest(url);
	}

	/**
	 * Returns the URL used to produce the current response. If {@link #getResponse()} was not invoked yet, the URL
	 * provided in the constructor of this class will be returned. If redirection was enabled
	 * (through {@link HttpRequest#getFollowRedirects()}), the redirection URL will be returned.
	 *
	 * @return the current URL
	 */
	public final String getUrl() {
		if (response != null && request.getFollowRedirects() && response.getRedirectionUrl() != null) {
			return response.getRedirectionUrl();
		} else {
			return request.getUrl();
		}
	}


	/**
	 * Returns the domain name in the current URL. If a response has been obtained (through {@link #getResponse()}) with
	 * redirection enabled, the domain name in the redirection URL will be returned.
	 *
	 * @return the current domain name.
	 */
	public final String getDomainName() {
		String url = getUrl();

		int index = url.indexOf("://");
		if (index >= 0 && index + 3 < url.length()) {
			url = url.substring(index + 3);
		}

		if (url.indexOf('/') >= 0) {
			return url.substring(0, url.indexOf('/'));
		}
		return null;
	}

	/**
	 * Returns a {@link HttpRequest} object with configuration options for executing the HTTP request.
	 *
	 * @return the request configuration.
	 */
	public final HttpRequest getRequestConfiguration() {
		return request;
	}

	/**
	 * Returns the protocol of the current URL. If a response has been obtained (through {@link #getResponse()}) with
	 * redirection enabled, the protocol of  the redirection URL will be returned.
	 *
	 * @return the current protocol.
	 */
	public final String getProtocol() {
		String url = getUrl();

		int index = url.indexOf("://");
		if (index >= 0 && index + 3 < url.length()) {
			return url.substring(0, index + 3);
		}
		return null;
	}

	/**
	 * Return the number of retries to be performed in case the HTTP request call fails.
	 *
	 * <i>Defaults to 0 (no retries)</i>
	 *
	 * @return the number of retries to perform in case of failure to obtain a response
	 */
	public final int getRetries() {
		return retries;
	}

	/**
	 * Defines a number of retries to be performed in case the HTTP request call fails.
	 *
	 * <i>Defaults to 0 (no retries)</i>
	 *
	 * @param retries the number of retries to perform in case of failure to obtain a response
	 */
	public final void setRetries(int retries) {
		this.retries = retries;
	}

	/**
	 * Returns the interval (in milliseconds) to wait before trying to execute the HTTP request after a failure.
	 *
	 * <i>Defaults to 2000 (2 seconds)</i>
	 *
	 * @return the retry interval in ms
	 */
	public final long getRetryInterval() {
		return retryInterval;
	}


	/**
	 * Defines the interval (in milliseconds) to wait before trying to execute the HTTP request after a failure.
	 *
	 * <i>Defaults to 2000 (2 seconds)</i>
	 *
	 * @param retryInterval the retry interval in ms
	 */
	public final void setRetryInterval(long retryInterval) {
		this.retryInterval = retryInterval;
	}

	/**
	 * Gets the path of the resource identified by the URL. For example, if the URL is
	 * "http://google.com/images/logo.png" then "/images/logo.png" will be returned.
	 *
	 * @return the path portion of the URL.
	 */
	public final String getPath() {
		int baseUrlLength = getProtocol().length() + getDomainName().length();
		String url = getUrl();
		if (baseUrlLength < url.length()) {
			return url.substring(baseUrlLength);
		}
		return null;
	}

	/**
	 * Returns the file name specified in URL, if it exists. For example, if the URL is
	 * "http://google.com/images/logo.png". "logo.png" will be returned
	 *
	 * @return the file name
	 */
	public final String getFileName() {
		String path = getPath();
		if (path == null) {
			return null;
		}
		int lastSlash = path.lastIndexOf('/');

		int query = Math.max(path.indexOf('?'), 0);
		int fragment = Math.max(path.indexOf('#'), 0);

		int end = Math.min(query, fragment);
		if (end == 0) {
			return path.substring(lastSlash + 1);
		} else if (end < path.length() && end > lastSlash + 1) {
			return path.substring(lastSlash + 1, end);
		}
		return null;
	}

	/**
	 * Invokes the HTTP request and returns the response as a {@link HttpResponse} object.
	 * Further calls to this method will produce the same object, and no further HTTP requests will be performed.
	 *
	 * Use the {@link #clone()} method to obtain a copy of the current {@link UrlReaderProvider} instance if you need
	 * to invoke hte HTTP request again.
	 *
	 * @return the HTTP response originated by the configured HTTP request.
	 */
	public final HttpResponse getResponse() {
		if (response == null) {
			response = Builder.build(HttpResponse.class, this);
		}
		return response;
	}

	/**
	 * Defines a file into which a copy of the response body, obtained after invoking the
	 * HTTP request via the {@link #getResponse()} method, should be stored be stored.
	 *
	 * @param provider the {@link FileProvider} defining the target file.
	 */
	public final void storeLocalCopyIn(FileProvider provider) {
		this.localCopyProvider = provider;
	}

	/**
	 * Defines a file into which a copy of the response body, obtained after invoking the
	 * HTTP request via the {@link #getResponse()} method, should be stored.
	 *
	 * @param file the target file.
	 */
	public final void storeLocalCopyIn(File file) {
		localCopyProvider = new FileProvider(file);
	}

	/**
	 * Defines a file into which a copy of the response body, obtained after invoking the
	 * HTTP request via the {@link #getResponse()} method, should be stored.
	 *
	 * @param file     the target file.
	 * @param encoding the encoding to use for the local copy.
	 */
	public final void storeLocalCopyIn(File file, Charset encoding) {
		localCopyProvider = new FileProvider(file, encoding);
	}

	/**
	 * Defines a file into which a copy of the response body, obtained after invoking the
	 * HTTP request via the {@link #getResponse()} method, should be stored.
	 *
	 * @param file     the target file.
	 * @param encoding the encoding to use for the local copy.
	 */
	public final void storeLocalCopyIn(File file, String encoding) {
		localCopyProvider = new FileProvider(file, encoding);
	}

	/**
	 * Defines a file into which a copy of the response body, obtained after invoking the
	 * HTTP request via the {@link #getResponse()} method, should be stored.
	 *
	 * @param path a path to the target file. The path can contain system variables enclosed within
	 *             { and } (e.g. {@code {user.home}/myapp/page.html"}).
	 */
	public final void storeLocalCopyIn(String path) {
		localCopyProvider = new FileProvider(path);
	}

	/**
	 * Defines a file into which a copy of the response body, obtained after invoking the
	 * HTTP request via the {@link #getResponse()} method, should be stored.
	 *
	 * @param path     a path to the target file. The path can contain system variables enclosed within
	 *                 { and } (e.g. {@code {user.home}/myapp/page.html"}).
	 * @param encoding the encoding to use for the local copy.
	 */
	public final void storeLocalCopyIn(String path, Charset encoding) {
		localCopyProvider = new FileProvider(path, encoding);
	}

	/**
	 * Defines a file into which a copy of the response body, obtained after invoking the
	 * HTTP request via the {@link #getResponse()} method, should be stored
	 *
	 * @param path     a path to the target file. The path can contain system variables enclosed within
	 *                 { and } (e.g. {@code {user.home}/myapp/page.html"}).
	 * @param encoding the encoding to use for the local copy.
	 */
	public final void storeLocalCopyIn(String path, String encoding) {
		localCopyProvider = new FileProvider(path, encoding);
	}

	/**
	 * Returns a {@link FileProvider} which indicates where in the filesystem a file with a copy of the response body,
	 * obtained after invoking the HTTP request via the {@link #getResponse()} method, should be stored.
	 *
	 * @return the local copy file configuration for storing the HTTP response body obtained when calling {@link #getResponse()}
	 */
	public final FileProvider getLocalCopyTarget() {
		return localCopyProvider;
	}

	/**
	 * Returns a {@link java.io.Reader} instance ready to process the content of the body of the HTTP response obtained
	 * after invoking {@link #getResponse()}. A HTTP request will be made to obtain the response if required.
	 *
	 * @return a new {@code Reader} that can be used to consume the body of the resulting HTTP response
	 */
	@Override
	public final Reader getResource() {
		try {
			return getResponse().getContentReader();
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to open URL '" + request.getUrl() + "'", ex);
		}
	}

	/**
	 * Prints this object as the original HTTP request URL.
	 *
	 * @return the request URL passed in the constructor of this class.
	 */
	@Override
	public final String toString() {
		return request.getUrl();
	}

	/**
	 * Clones this object with all its configuration, but without the result of the HTTP request (i.e. no response).
	 * The clone allows you to perform a new HTTP request call and obtain a fresh HTTP response through {@link #getResponse()}
	 *
	 * @return a copy of the current object and all its configurations.
	 */
	public final UrlReaderProvider clone() {
		try {
			UrlReaderProvider clone = (UrlReaderProvider) super.clone();
			clone.response = null;
			clone.request = request.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Unable to clone", e);
		}
	}
}
