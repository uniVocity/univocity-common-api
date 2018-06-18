/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.net;

import com.univocity.api.common.*;
import com.univocity.api.io.*;

import java.io.*;

/**
 * Holds the details of a single data parameter sent in the body of POST requests.
 * Supports plain key-value pairs and file uploads
 * */
public final class DataParameter implements Cloneable {
	private String name;
	private String value;
	private ResourceProvider<InputStream> uploadStream;
	private String contentType = "application/octet-stream";

	/**
	 * Creates a new data parameter
	 * @param name the data parameter name
	 * @param value the value associated with the given data parameter
	 */
	public DataParameter(String name, String value) {
		Args.notBlank(name, "Data parameter name");
		Args.notNull(value, "Value of data parameter '" + name + "'");
		this.name = name;
		this.value = value;
	}

	/**
	 * Creates a new upload parameter to be sent on the body of a POST request.
	 *
	 * @param name the name of the data parameter
	 * @param filename the name of the file being uploaded
	 * @param uploadStream the {@code InputStream} with the contents to be uploaded
	 */
	public DataParameter(final String name, String filename, final FileProvider fileToUpload) {
		this(name, filename, fileToUpload, null);
	}

	/**
	 * Creates a new upload parameter to be sent on the body of a POST request.
	 *
	 * @param name the name of the data parameter
	 * @param filename the name of the file being uploaded
	 * @param uploadStream the {@code InputStream} with the contents to be uploaded
	 */
	public DataParameter(String name, String filename, final InputStream uploadStream) {
		this(name, filename, uploadStream, null);
	}



	/**
	 * Creates a new upload parameter to be sent on the body of a POST request.
	 *
	 * @param name the name of the data parameter
	 * @param filename the name of the file being uploaded
	 * @param uploadStream a callback provider that returns an {@code InputStream} with the contents to be uploaded
	 */
	public DataParameter(String name, String filename, ResourceProvider<InputStream> uploadStream) {
		this(name, filename, uploadStream, null);
	}

	/**
	 * Creates a new upload parameter to be sent on the body of a POST request.
	 *
	 * @param name the name of the data parameter
	 * @param filename the name of the file being uploaded
	 * @param uploadStream the file with the contents to be uploaded
	 * @param contentType the Content Type header used in the MIME body when uploading files.
	 *                    <b>Defaults to {@code application/octet-stream}.</b>
	 */
	public DataParameter(final String name, String filename, final FileProvider fileToUpload, String contentType) {
		this(name, filename, new ResourceProvider<InputStream>() {
			@Override
			public InputStream getResource() {
				try {
					return new FileInputStream(new File(fileToUpload.getFilePath()));
				} catch (FileNotFoundException e) {
					throw new IllegalStateException("Can't find file '" + fileToUpload.getFilePath() + "' associated with data parameter '" + name + "'", e);
				}
			}
		}, contentType);
	}

	/**
	 * Creates a new upload parameter to be sent on the body of a POST request.
	 *
	 * @param name the name of the data parameter
	 * @param filename the name of the file being uploaded
	 * @param uploadStream the {@code InputStream} with the contents to be uploaded
	 * @param contentType the Content Type header used in the MIME body when uploading files.
	 *                    <b>Defaults to {@code application/octet-stream}.</b>
	 */
	public DataParameter(String name, String filename, final InputStream uploadStream, String contentType) {
		this(name, filename, new ResourceProvider<InputStream>() {
			@Override
			public InputStream getResource() {
				return uploadStream;
			}
		}, contentType);
	}

	/**
	 * Creates a new upload parameter to be sent on the body of a POST request.
	 *
	 * @param name the name of the data parameter
	 * @param filename the name of the file being uploaded
	 * @param uploadStream a callback provider that returns an {@code InputStream} with the contents to be uploaded
	 * @param contentType the Content Type header used in the MIME body when uploading files.
	 *                    <b>Defaults to {@code application/octet-stream}.</b>
	 */
	public DataParameter(String name, String filename, ResourceProvider<InputStream> uploadStream, String contentType) {
		Args.notBlank(name, "Data parameter name");
		Args.notBlank(filename, "Filename associated with data parameter '" + name + "'");
		Args.notNull(uploadStream, "Input stream provider of upload file '" + filename + "' associated with data parameter '" + name + "'");
		this.name = name;
		this.value = filename;
		this.uploadStream = uploadStream;
		if (Args.isNotBlank(contentType)) {
			this.contentType = contentType;
		}
	}

	/**
	 * Returns the name of this data parameter
	 * @return the data parameter name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Returns the plain {@code String} value associated with this data parameter
	 * @return the data parameter value
	 */
	public final String getValue() {
		return value;
	}

	/**
	 * Returns the upload stream provider that produces an {@code InputStream}
	 * of the data to be uploaded when called
	 * @return the upload stream provider
	 */
	public final ResourceProvider<InputStream> getUploadStream() {
		return uploadStream;
	}

	public String toString() {
		if (value != null && value.length() > 100) {
			return name + "=" + value.substring(0, 100) + "...";
		} else {
			return name + "=" + value;
		}
	}


	/**
	 * Get the current Content Type, or {@code null} if not set.
	 *
	 * @return the current Content Type.
	 */
	public final String getContentType() {
		return contentType;
	}

	@Override
	public final DataParameter clone() {
		try {
			return (DataParameter) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Error cloning " + this, e);
		}
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DataParameter that = (DataParameter) o;

		if (name != null ? !name.equals(that.name) : that.name != null)
			return false;
		if (value != null ? !value.equals(that.value) : that.value != null)
			return false;
		if (uploadStream != null ? !uploadStream.equals(that.uploadStream) : that.uploadStream != null)
			return false;
		return contentType != null ? contentType.equals(that.contentType) : that.contentType == null;
	}

	@Override
	public final int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (value != null ? value.hashCode() : 0);
		result = 31 * result + (uploadStream != null ? uploadStream.hashCode() : 0);
		result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
		return result;
	}
}
