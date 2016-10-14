/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

import org.testng.annotations.*;

import java.util.*;

import static org.testng.Assert.*;

public class HttpRequestTest {

	@Test
	public void testUrlParameterization() {
		HttpRequest request = new UrlReaderProvider("https://www.google.com.au/?q={QUERY}#q={QUERY}&tbs=qdr:{PERIOD}").getRequest();
		assertEquals(request.getUrl(), "https://www.google.com.au/?q={QUERY}#q={QUERY}&tbs=qdr:{PERIOD}");

		request.setUrlParameter("QUERY", "univocity");
		assertEquals(request.getUrl(), "https://www.google.com.au/?q=univocity#q=univocity&tbs=qdr:{PERIOD}");

		request.setUrlParameter("PERIOD", "w");
		assertEquals(request.getUrl(), "https://www.google.com.au/?q=univocity#q=univocity&tbs=qdr:w");

		Set<String> params = request.getUrlParameters();
		assertEquals(params.size(), 2);

		assertTrue(params.contains("QUERY"));
		assertTrue(params.contains("PERIOD"));

		request.clearUrlParameters();
		assertEquals(request.getUrl(), "https://www.google.com.au/?q={QUERY}#q={QUERY}&tbs=qdr:{PERIOD}");
	}

}