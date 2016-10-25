/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.net;

import java.io.*;
import java.nio.charset.*;

/**
 * A custom {@link HttpResponse} reader which provides better control over how to handle the response body of
 * a {@link HttpResponse} object produced by a {@link UrlReaderProvider}.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface HttpResponseReader {

	/**
	 * Processes a {@link HttpResponse} obtained through a {@link HttpRequest} invocation via a {@link UrlReaderProvider}.
	 *
	 * @param response     the HTTP response object
	 * @param responseBody the body of thr HTTP response
	 * @param encoding     the character set from the {@code Content-Type} header, or if explicitly provided in the HTTP request,
	 *                     the result of {@link HttpRequest#getCharsetName()}. If no known character could be obtained,
	 *                     the default system {@code Charset} will be provided.
	 *
	 * @throws Exception in case anything goes wrong processing the response.
	 */

	void processResponse(HttpResponse response, InputStream responseBody, Charset encoding) throws Exception;
}
