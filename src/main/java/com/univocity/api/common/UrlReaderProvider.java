/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */
package com.univocity.api.common;

import com.univocity.api.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class UrlReaderProvider extends ReaderProvider implements Cloneable {

	private int retries = 0;
	private long retryInterval = 2000;
	private final Charset defaultEncoding;
	private HttpResponse response;
	protected HttpRequest request;
	private TreeMap<String, Object> parameterValues = new TreeMap<String, Object>();
	private FileProvider localCopyProvider;
	private String baseUrl;
	private String protocol;

	public UrlReaderProvider(String url) {
		this(url, (Charset) null);
	}

	public UrlReaderProvider(String url, String defaultEncoding) {
		this(url, Charset.forName(defaultEncoding));
	}

	public UrlReaderProvider(String url, Charset defaultEncoding) {
		Args.notBlank(url, "URL");
		this.request = new HttpRequest(url);
		this.defaultEncoding = defaultEncoding == null ? Charset.forName("UTF-8") : defaultEncoding;
	}

	public String getBaseUrl() {
		if (baseUrl == null) {
			String url;
			if (response != null && request.getFollowRedirects()) {
				url = response.getRedirectionUrl();
			} else {
				url = request.getUrl();
			}
			int index = url.indexOf("://");
			if (index >= 0) {
				protocol = url.substring(0,index+3);
				url = url.substring(index+3);
			}

			if (url.indexOf('/') >= 0) {
				baseUrl = url.substring(0, url.indexOf('/'));
			}
		}


		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;

		int index = baseUrl.indexOf("://");
		if (index >= 0) {
			protocol = baseUrl.substring(0,index+3);
		}
	}
	public HttpRequest getRequestConfiguration() {
		return request;
	}

	public String getProtocol() {
		return protocol;
	}

	public final Charset getDefaultEncoding() {
		return defaultEncoding;
	}

	public final int getRetries() {
		return retries;
	}

	public final void setRetries(int retries) {
		this.retries = retries;
	}

	public final long getRetryInterval() {
		return retryInterval;
	}

	public final void setRetryInterval(long retryInterval) {
		this.retryInterval = retryInterval;
	}

	public HttpResponse getResponse() {
		if (response == null) {
			response = Builder.build(HttpResponse.class, this);
		}
		return response;
	}

	public void setParameter(String name, Object value) {
		Args.notBlank(name,"Parameter name");
		parameterValues.put(name, value);
	}

	public Map<String, Object> getParameters() {
		return parameterValues;
	}



	public void storeLocalCopyIn(FileProvider provider) {
		this.localCopyProvider = provider;
	}

	public void storeLocalCopyIn(File file) {
		localCopyProvider = new FileProvider(file);
	}

	public void storeLocalCopyIn(File file, Charset encoding) {
		localCopyProvider = new FileProvider(file,encoding);
	}

	public void storeLocalCopyIn(File file, String encoding) {
		localCopyProvider = new FileProvider(file,encoding);
	}

	public void storeLocalCopyIn(String path) {
		localCopyProvider = new FileProvider(path);
	}

	public void storeLocalCopyIn(String path, Charset encoding) {
		localCopyProvider = new FileProvider(path, encoding);
	}

	public void storeLocalCopyIn(String path, String encoding) {
		localCopyProvider = new FileProvider(path, encoding);
	}

	public FileProvider getLocalCopyFileProvider() {
		return localCopyProvider;
	}

	@Override
	public final Reader getResource() {
		try {
			return getResponse().getContentReader();
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to open URL '" + request.getUrl() + "'", ex);
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [" + request.getUrl() + "]";
	}

	public UrlReaderProvider clone() {
		try {
			UrlReaderProvider clone = (UrlReaderProvider) super.clone();
			clone.response = null;
			clone.request = request.clone();
			clone.parameterValues = (TreeMap<String, Object>) parameterValues.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
		    throw new IllegalStateException("Unable to clone ",e);
		}
	}
}
