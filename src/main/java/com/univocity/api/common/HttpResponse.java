/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface HttpResponse extends Closeable,Cloneable{

	String getRedirectionUrl();

	int getStatusCode();

	String getStatusMessage();

	String getCharset();

	String getContentType();

	URL getUrl();

	HttpMethodType getMethod();

	Map<String, String> getHeaders();

	Map<String, String> getCookies();

	void close();

	Reader getContentReader();

	void readContent(HttpResponseReader responseReader);

	int getRetriesPerformed();

	HttpResponse clone();
}
