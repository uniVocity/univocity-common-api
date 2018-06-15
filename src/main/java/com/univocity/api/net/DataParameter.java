/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.net;

import com.univocity.api.common.*;
import com.univocity.api.io.*;

import java.io.*;

//FIXME: javadoc
public class DataParameter implements Cloneable {
	private String key;
	private String value;
	private ResourceProvider<InputStream> stream;
	private String contentType = "application/octet-stream";

	public static DataParameter create(String key, String value) {
		return new DataParameter().key(key).value(value);
	}

	public static DataParameter create(final String key, String filename, final FileProvider file) {
		return create(key, filename, new ResourceProvider<InputStream>() {
			@Override
			public InputStream getResource() {
				try {
					return new FileInputStream(new File(file.getFilePath()));
				} catch (FileNotFoundException e) {
					throw new IllegalStateException("Can't find file '" + file.getFilePath() + "' associated with data parameter '" + key + "'", e);
				}
			}
		});
	}

	public static DataParameter create(String key, String filename, final InputStream inputStream) {
		return create(key, filename, new ResourceProvider<InputStream>() {
			@Override
			public InputStream getResource() {
				return inputStream;
			}
		});
	}

	public static DataParameter create(String key, String filename, ResourceProvider<InputStream> inputStreamProvider) {
		return new DataParameter().key(key).value(filename).inputStream(inputStreamProvider);
	}

	private DataParameter() {
	}


	public DataParameter key(String key) {
		Args.notBlank(key, "Data key");
		this.key = key;
		return this;
	}


	public String key() {
		return key;
	}


	public DataParameter value(String value) {
		Args.notNull(value, "Data value");
		this.value = value;
		return this;
	}


	public String value() {
		return value;
	}


	public DataParameter inputStream(final InputStream stream) {
		Args.notNull(stream, "Input stream");
		return inputStream(new ResourceProvider<InputStream>() {
			@Override
			public InputStream getResource() {
				return stream;
			}
		});
	}

	public DataParameter inputStream(ResourceProvider<InputStream> stream) {
		Args.notNull(stream, "Input stream provider");
		this.stream = stream;
		return this;
	}

	public ResourceProvider<InputStream> inputStream() {
		return stream;
	}

	public boolean hasInputStream() {
		return stream != null;
	}


	public String toString() {
		if (value != null && value.length() > 100) {
			return key + "=" + value.substring(0, 100) + "...";
		} else {
			return key + "=" + value;
		}
	}

	/**
	 * Set the Content Type header used in the MIME body (aka mimetype) when uploading files.
	 * Only useful if {@link #inputStream(InputStream)} is set.
	 * <p>Will default to {@code application/octet-stream}.</p>
	 *
	 * @param contentType the new content type
	 *
	 * @return this KeyVal
	 */
	public DataParameter contentType(String contentType) {
		if(Args.isNotBlank(contentType)) {
			this.contentType = contentType;
		}
		return this;
	}

	/**
	 * Get the current Content Type, or {@code null} if not set.
	 *
	 * @return the current Content Type.
	 */
	public String contentType() {
		return contentType;
	}

	@Override
	public DataParameter clone() {
		try {
			return (DataParameter) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Error cloning " + this, e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DataParameter that = (DataParameter) o;

		if (key != null ? !key.equals(that.key) : that.key != null)
			return false;
		if (value != null ? !value.equals(that.value) : that.value != null)
			return false;
		if (stream != null ? !stream.equals(that.stream) : that.stream != null)
			return false;
		return contentType != null ? contentType.equals(that.contentType) : that.contentType == null;
	}

	@Override
	public int hashCode() {
		int result = key != null ? key.hashCode() : 0;
		result = 31 * result + (value != null ? value.hashCode() : 0);
		result = 31 * result + (stream != null ? stream.hashCode() : 0);
		result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
		return result;
	}
}
