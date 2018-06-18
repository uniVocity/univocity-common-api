/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.net;

import com.univocity.api.*;
import com.univocity.api.common.*;
import com.univocity.api.io.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

import static com.univocity.api.common.Utils.*;

/**
 * A reusable, cloneable HTTP request configuration, with support for parameterization of the URL.
 *
 * Example of a URL with 2 parameters (QUERY and PERIOD): {@code "https://www.google.com/?q={QUERY}#q={QUERY}&tbs=qdr:{PERIOD}"}
 *
 * Use {@link #setUrlParameter(String, Object)} to set the values of any parameters so that {@link #getUrl()}
 * generates the final URL.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see RequestMethod
 * @see HttpResponse
 * @see HttpResponseReader
 * @see UrlReaderProvider
 */
public class HttpRequest extends HttpMessage implements Cloneable {

	private ParameterizedString url;
	private int timeout;
	private boolean followRedirects = true;
	private List<DataParameter> data = new ArrayList<DataParameter>();
	private Charset charset;
	private boolean ignoreHttpErrors;

	private String proxyUser;
	private char[] proxyPassword;
	private RateLimiter rateLimiter;
	private SSLSocketFactory sslSocketFactory;
	private int bodySizeLimit;
	private String requestBody;
	private Charset postDataCharset = Charset.forName("UTF-8");

	/**
	 * Creates a new request for a given request URL
	 *
	 * @param url the request URL
	 * @param rateLimiter a {@link RateLimiter} to prevent the execution of excessive simultaneous requests.
	 */
	protected HttpRequest(String url, RateLimiter rateLimiter) {
		setUrl(url);
		this.rateLimiter = rateLimiter;
		this.addHeader("Accept-Encoding", "gzip");
		this.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36");
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
	public final void setUrl(String url) {
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
	 * Returns the size limit when reading the HTTP response body, effectively
	 * truncating the response if the number of characters read exceed the given
	 * limit.
	 *
	 * <b>Defaults to 0 (no limit)</b>
	 *
	 * @return the maximum number of characters to read from the response
	 */
	public final int getBodySizeLimit() {
		return bodySizeLimit;
	}

	/**
	 * Defines a size limit when reading the HTTP response body, effectively
	 * truncating the response if the number of characters read exceed the given
	 * limit.
	 *
	 * <b>Defaults to 0 (no limit)</b>
	 *
	 * @param limit the maximum number of characters to read from the response
	 */
	public final void setBodySizeLimit(int limit) {
		Args.positiveOrZero(limit, "body size limit");
		bodySizeLimit = limit;
	}

	/**
	 * Returns the plain request body to be sent by this request.
	 *
	 * @return the request body {@code String}.
	 */
	public final String getRequestBody() {
		return requestBody;
	}

	/**
	 * Defines a plain request body to be sent by this request.
	 *
	 * @param body the request body {@code String}.
	 */
	public final void setRequestBody(String body) {
		this.requestBody = body;
	}

	/**
	 * Returns the POST data character set for {@code x-www-form-urlencoded} POST data
	 *
	 * @return the character set used to encode the POST data
	 */
	public final Charset getPostDataCharset() {
		return postDataCharset;
	}

	/**
	 * Returns the name of the POST data character set for {@code x-www-form-urlencoded} POST data
	 *
	 * @return the name of the character set used to encode the POST data
	 */
	public final String getPostDataCharsetName() {
		return postDataCharset.name();
	}

	/**
	 * Sets the POST data character set for {@code x-www-form-urlencoded} POST data
	 *
	 * @param charsetName name of the character set
	 */
	public final void setPostDataCharset(String charsetName) {
		Args.notBlank(charsetName, "Charset name for POST data request");
		setPostDataCharset(Charset.forName(charsetName));
	}

	/**
	 * Sets the POST data character set for {@code x-www-form-urlencoded} POST data
	 *
	 * @param postDataCharset the character set to use
	 */
	public final void setPostDataCharset(Charset postDataCharset) {
		Args.notNull(postDataCharset, "Charset for POST data request");
		this.postDataCharset = postDataCharset;
	}

	/**
	 * reused for further requests, or to {@code close} if the connection should be discarded and closed as soon as the
	 * Sets the {@code Connection} request header to explicitly to {@code keep-alive} if the connection is meant to be
	 * response is processed.
	 *
	 * @param enableKeepAlive flag indicating whether or not the HTTP connection should be persistent (i.e kept alive).
	 */
	public final void setKeepAliveEnabled(boolean enableKeepAlive) {
		setHeader("Connection", enableKeepAlive ? "keep-alive" : "close");
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
	 * Associates a value to a header. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of comma-separated name-value pairs
	 *
	 * Existing values associated with the given header will remain.
	 *
	 * @param header the header name
	 * @param value  the header value
	 */
	public final void addHeader(String header, String value) {
		addHeader(header, value, false);
	}

	/**
	 * Associates a value to a header. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of comma-separated name-value pairs
	 *
	 * Existing values associated with the given header will remain.
	 *
	 * @param header the header name
	 * @param value  the header value
	 * @param encode flag indicating whether to encode the given value
	 */
	public final void addHeader(String header, String value, boolean encode) {
		if (encode) {
			value = Args.encode(value);
		}
		addMulti(headers, header, value);
	}

	/**
	 * Defines a header and its value. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of comma-separated name-value pairs
	 *
	 * Existing values associated with the given header will be removed.
	 *
	 * @param header the header name
	 * @param value  the header value
	 */
	public final void setHeader(String header, String value) {
		setHeader(header, value, false);
	}

	/**
	 * Defines a header and its value. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of comma-separated name-value pairs.
	 *
	 * Existing values associated with the given header will be removed.
	 *
	 * @param header the header name
	 * @param value  the new header value
	 * @param encode flag indicating whether the value should be encoded
	 */
	public final void setHeader(String header, String value, boolean encode) {
		if (encode) {
			value = Args.encode(value);
		}
		setMulti(headers, header, value);
	}

	/**
	 * Replaces any previous headers with the given values. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of comma-separated name-value pairs
	 *
	 * Existing values associated with the given headers will be removed.
	 *
	 * @param headers a map of headers names and their values. Previous values will be discarded
	 */
	public final void setHeaders(Map<String, String> headers) {
		setHeaders(headers, false);
	}

	/**
	 * Adds the given values to the existing list of headers. New header names will be added as required.
	 * All headers are transmitted after the request line (the first line of a HTTP message), in the
	 * format of comma-separated name-value pairs
	 *
	 * @param headers a map of header names and their values.
	 */
	public final void addHeaders(Map<String, String> headers) {
		addHeaders(headers, false);
	}

	/**
	 * Replaces any previous headers with the given values. All headers are transmitted after the request line
	 * (the first line of a HTTP message), in the format of comma-separated name-value pairs
	 *
	 * Existing values associated with the given header will be removed.
	 *
	 * @param headers a map of headers names and their values. Previous values will be discarded
	 * @param encode  flag indicating whether values should be encoded
	 */
	public final void setHeaders(Map<String, String> headers, boolean encode) {
		this.headers.clear();
		addHeaders(headers, encode);
	}

	/**
	 * Adds the given values to the existing list of headers.. New header names will be added as required.
	 * All headers are transmitted after the request line (the first line of a HTTP message), in the format of comma-separated name-value pairs
	 *
	 * @param headers a map of header names and their values.
	 * @param encode  flag indicating whether values should be encoded
	 */
	public final void addHeaders(Map<String, String> headers, boolean encode) {
		if (encode) {
			for (Map.Entry<String, String> e : headers.entrySet()) {
				addMulti(this.headers, e.getKey(), Args.encode(e.getValue()));
			}
		} else {
			for (Map.Entry<String, String> e : headers.entrySet()) {
				addMulti(this.headers, e.getKey(), e.getValue());
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

	/**
	 * Removes a given cookie and its value from the {@code Cookie} header of this HTTP request
	 * @param name the name of the cookie to be removed.
	 */
	public final void removeCookie(String name) {
		cookies.remove(name);
	}

	private void addMulti(Map<String, List<String>> map, String key, String value) {
		putValueCaseInsensitive(map, key, value, true);
	}

	private void setMulti(Map<String, List<String>> map, String key, String value) {
		putValueCaseInsensitive(map, key, value, false);
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
	 * Sets the {@link RequestMethod} to be used by this request.
	 * The method type identifies an action to be performed on the identified (remote) resource.
	 *
	 * <i>Defaults to {@link RequestMethod#GET}</i>
	 *
	 * @param httpMethodType the HTTP method to use
	 */
	public final void setRequestMethod(RequestMethod httpMethodType) {
		Args.notNull(httpMethodType, "HTTP method type");
		this.requestMethod = httpMethodType;
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
	 * Adds a data parameter to the body of {@link RequestMethod#POST} requests as a plain {@code String}
	 *
	 * @param dataParameter the data parameter
	 */
	public final void addDataParameter(DataParameter dataParameter){
		Args.notNull(dataParameter, "Data parameter");
		this.data.add(dataParameter);
	}

	/**
	 * Removes a given data parameter from the body of a {@link RequestMethod#POST} request
	 * @param paramName the parameter name
	 */
	public final void removeDataParameter(String paramName) {
		Args.notBlank(paramName, "Parameter name");
		Iterator<DataParameter> it = this.data.iterator();
		while (it.hasNext()) {
			DataParameter entry = it.next();
			if (paramName.equals(entry.getName())) {
				it.remove();
			}
		}
	}

	/**
	 * Returns the values associated with a parameter of the body a {@link RequestMethod#POST} request
	 *
	 * @param paramName the parameter name
	 * @return all values associated with the given parameter name.
	 */
	public final List<DataParameter> getDataParameter(String paramName) {
		Args.notBlank(paramName, "Parameter name");

		List<DataParameter> out = new ArrayList<DataParameter>(1);

		Iterator<DataParameter> it = this.data.iterator();
		while (it.hasNext()) {
			DataParameter entry = it.next();
			if (paramName.equals(entry.getName())) {
				out.add(entry);
			}
		}
		return out;
	}

	/**
	 * Adds/replaces a parameter of the body of {@link RequestMethod#POST} requests as a plain {@code String}
	 *
	 * @param dataParameter the new/updated data parameter
	 */
	public final void setDataParameter(DataParameter dataParameter) {
		Args.notNull(dataParameter, "data parameter");
		removeDataParameter(dataParameter.getName());
		this.addDataParameter(dataParameter);
	}

	/**
	 * Sets multiple parameters to the body of {@link RequestMethod#POST} requests as a plain {@code String}s.
	 * Multiple values can be associated with a parameter name. Any previous parameters will be removed.
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
	public final void setDataParameters(Map<String, String[]> params, String... keys) {
		clearDataParameters();
		addDataParameters(params, keys);
	}


	/**
	 * Adds multiple parameters to the body of {@link RequestMethod#POST} requests as a plain {@code String}s.
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
	 * Adds a parameter to the body of {@link HttpMethodType#POST} requests as a plain {@code String}
	 *
	 * @param paramName the parameter name
	 * @param value     the parameter value
	 */
	public final void addDataParameter(String paramName, Object value) {
		this.data.add(new DataParameter(paramName, value == null ? "" : String.valueOf(value)));
	}

	/**
	 * Replaces/adds a data parameter to the body of this request
	 *
	 * @param paramName the parameter name
	 * @param value     the parameter value
	 */
	public final void setDataParameter(String paramName, Object value) {
		this.removeDataParameter(paramName);
		this.data.add(new DataParameter(paramName, value == null ? "" : String.valueOf(value)));
	}


	/**
	 * Returns the data parameters sent by on the body of this request if it is a {@link RequestMethod#POST} request.
	 *
	 * @return a map of data parameters and their values.
	 */
	public final List<DataParameter> getData() {
		return data;
	}

	public final void removeHeader(String name) {
		Map.Entry<String, List<String>> entry = Utils.getEntryCaseInsensitive(headers, name);
		if (entry != null) {
			headers.remove(entry.getKey());
		}
	}

	/**
	 * Add an input stream as a request data parameter. For GETs, has no effect, but for POSTS this will upload the
	 * input stream.
	 *
	 * @param key         data key (form item name)
	 * @param filename    the name of the file to present to the remove server. Typically just the name, not path,
	 *                    component.
	 * @param inputStream the input stream to upload.
	 * @param contentType the Content Type (aka mimetype) to specify for this file.
	 *                    You must close the InputStream in a {@code finally} block.
	 *
	 * @return this Connections, for chaining
	 */
	public void data(String key, String filename, final InputStream inputStream, String contentType) {
		this.data.add(new DataParameter(key, filename, inputStream, contentType));
	}

	/**
	 * Add an input stream as a request data parameter. For GETs, has no effect, but for POSTS this will upload the
	 * input stream.
	 *
	 * @param key         data key (form item name)
	 * @param filename    the name of the file to present to the remove server. Typically just the name, not path,
	 *                    component.
	 * @param inputStreamProvider the input stream provider to upload.
	 * @param contentType the Content Type (aka mimetype) to specify for this file.
	 *                    You must close the InputStream in a {@code finally} block.
	 *
	 * @return this Connections, for chaining
	 */
	public void data(String key, String filename, ResourceProvider<InputStream> inputStreamProvider, String contentType) {
		this.data.add(new DataParameter(key, filename, inputStreamProvider, contentType));
	}

	public void data(Map<String, String> data) {
		Args.notNull(data, "Data map");
		for (Map.Entry<String, String> entry : data.entrySet()) {
			this.data.add(new DataParameter(entry.getKey(), entry.getValue()));
		}
	}

	/**
	 * Add an input stream as a request data parameter. For GETs, has no effect, but for POSTS this will upload the
	 * input stream.
	 *
	 * @param key         data key (form item name)
	 * @param filename    the name of the file to present to the remove server. Typically just the name, not path,
	 *                    component.
	 * @param inputStream the input stream to upload, that you probably obtained from a {@link java.io.FileInputStream}.
	 *
	 * @return this Connections, for chaining
	 */
	public void data(String key, String filename, final InputStream inputStream) {
		data(key, filename, inputStream, null);
	}

	/**
	 * Add an input stream as a request data parameter. For GETs, has no effect, but for POSTS this will upload the
	 * input stream.
	 *
	 * @param key         data key (form item name)
	 * @param filename    the name of the file to present to the remove server. Typically just the name, not path,
	 *                    component.
	 * @param inputStream the input stream to upload.
	 *
	 * @return this Connections, for chaining
	 */
	public void data(String key, String filename, ResourceProvider<InputStream> inputStreamProvider) {
		data(key, filename, inputStreamProvider, null);
	}

	public void data(String... keyValuePairs) {
		if (keyValuePairs.length % 2 != 0) {
			throw new IllegalArgumentException("Number of elements in sequence of key value pairs must be even");
		}
		for (int i = 0; i < keyValuePairs.length; i += 2) {
			String key = keyValuePairs[i];
			String value = keyValuePairs[i + 1];
			Args.notBlank(key, "Data key");
			Args.notNull(value, "Data value");
			this.data.add(new DataParameter(key, value));
		}
	}

	public DataParameter data(String key) {
		Args.notEmpty(key, "Data key must not be empty");
		for (DataParameter param : data) {
			if (param.getName().equals(key))
				return param;
		}
		return null;
	}

	public void data(Collection<DataParameter> data) {
		Args.notNull(data, "Data collection");
		for (DataParameter entry : data) {
			this.data.add(entry);
		}
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
	 * Get the current custom SSL socket factory, if any.
	 * @return custom SSL socket factory if set, null otherwise
	 */
	public SSLSocketFactory getSslSocketFactory() {
		return sslSocketFactory;
	}

	/**
	 * Set a custom SSL socket factory.
	 * @param sslSocketFactory SSL socket factory
	 */
	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
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

			clone.cookies = new LinkedHashMap<String, String>(this.cookies);

			clone.headers = new LinkedHashMap<String, List<String>>();
			for (Map.Entry<String, List<String>> e : this.headers.entrySet()) {
				List<String> values = e.getValue();
				if (values != null) {
					values = new ArrayList<String>(values);
				}
				clone.headers.put(e.getKey(), values);
			}

			clone.data = new ArrayList<DataParameter>();

			for (DataParameter entry : this.data) {
				clone.data.add(entry.clone());
			}

			return clone;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Could not clone", e);
		}
	}

	private void printMap(StringBuilder out, String title, Map<?, ?> map) {
		out.append("\n+----[ ").append(title).append(" ]-----");
		if (map.isEmpty()) {
			out.append("\n| N/A");
		} else {
			for (Map.Entry<?, ?> header : map.entrySet()) {
				out.append("\n| ");
				out.append(header.getKey()).append(" = ").append(header.getValue());
			}
		}
	}

	public String printDetails() {
		StringBuilder out = new StringBuilder();
		out.append("+----[ ").append(requestMethod).append(" ]-----\n| ");
		out.append(getUrl());
		printMap(out, "cookies", cookies);
		printMap(out, "headers", headers);

		out.append("\n+----[ data ]-----");
		if (data.isEmpty()) {
			out.append("\n| N/A");
		} else {
			for (DataParameter entry : data) {
				out.append("\n| ").append(entry.getName()).append(" = ");

				String value = String.valueOf(entry.getValue());
				if (value.length() > 100) {
					out.append(value, 0, 100);
					out.append("...");
				} else {
					out.append(value);
				}
			}
		}
		out.append("\n+-----------------\n");

		return out.toString();
	}

	@Override
	public String toString() {
		return requestMethod + " - " + getUrl();
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
		if (requestBody != null ? !requestBody.equals(that.requestBody) : that.requestBody != null) {
			return false;
		}
		if (requestMethod != that.requestMethod) {
			return false;
		}
		if (headers != null ? !headers.equals(that.headers) : that.headers != null) {
			return false;
		}

		return data.equals(that.data);
	}

	@Override
	public int hashCode() {
		int result = url != null ? url.hashCode() : 0;
		result = 31 * result + (requestMethod != null ? requestMethod.hashCode() : 0);
		result = 31 * result + (headers != null ? headers.hashCode() : 0);
		result = 31 * result + (data != null ? data.hashCode() : 0);
		result = 31 * result + (requestBody != null ? requestBody.hashCode() : 0);
		return result;
	}
}
