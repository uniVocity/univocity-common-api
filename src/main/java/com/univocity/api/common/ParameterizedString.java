/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

import java.util.*;

/**
 * Utility class for handling {@code String}s with parameters. Use {@link #set(String, Object)} to set a parameter value,
 * and {@link #applyParameterValues()} to obtain a result {@code String} with the values of all known parameters replaced.
 *
 * Parameters without values provided will not be replaced, therefore the string "zero/{one}/{two}/{one}", with parameter
 * "one" set to 27 will evaluate to "zero/27/{two}/27"
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ParameterizedString implements Cloneable {

	private final String string;
	private final List<Parameter> parameters = new ArrayList<Parameter>();
	private final Set<String> parameterNames = new TreeSet<String>();
	private final String openBracket;
	private final String closeBracket;
	private TreeMap<String, Object> parameterValues;
	private String result = null;


	/**
	 * Creates a new parameterized string with custom open and closing brackets
	 *
	 * @param string       the string with parameters
	 * @param openBracket  the open bracket (before a parameter name)
	 * @param closeBracket the close bracket (after a paramter name)
	 */
	public ParameterizedString(String string, String openBracket, String closeBracket) {
		Args.notBlank(string, "Input string");
		Args.notBlank(openBracket, "Open bracket");
		Args.notBlank(closeBracket, "Close bracket");

		this.parameterValues = new TreeMap<String, Object>();
		this.openBracket = openBracket;
		this.closeBracket = closeBracket;
		this.string = string;
		collectParameters();
	}

	/**
	 * Creates a new parameterized string with parameter names enclosed within { and }
	 *
	 * @param string the string with parameters
	 */
	public ParameterizedString(String string) {
		this(string, "{", "}");
	}


	private void collectParameters() {
		int x = 0;
		int openBracketIndex;
		while ((openBracketIndex = string.indexOf(openBracket, x)) >= 0) {
			int closeBracketIndex = string.indexOf(closeBracket, openBracketIndex);
			if (closeBracketIndex > 0) {
				x = closeBracketIndex;
				String parameterizedName = string.substring(openBracketIndex + 1, closeBracketIndex);
				Parameter parameter = new Parameter(parameterizedName, openBracketIndex, closeBracketIndex + 1);
				parameters.add(parameter);
				parameterNames.add(parameter.name);
			}
			else {
				x = openBracketIndex + 1;
			}
		}
	}

	/**
	 * Sets a parameter value
	 *
	 * @param parameter the parameter name
	 * @param value     the parameter value
	 *
	 * @throws IllegalArgumentException if the parameter name does not exist
	 */
	public final void set(String parameter, Object value) throws IllegalArgumentException {
		validateParameterName(parameter);
		parameterValues.put(parameter, value);
		result = null;
	}

	/**
	 * Returns the value of a given parameter.
	 *
	 * @param parameter the parameter name
	 *
	 * @return the parameter value
	 *
	 * @throws IllegalArgumentException if the parameter name does not exist
	 */
	public final Object get(String parameter) throws IllegalArgumentException {
		validateParameterName(parameter);
		return parameterValues.get(parameter);
	}

	private void validateParameterName(String parameter) {
		Args.notBlank(parameter, "Parameter name");
		if (!parameterNames.contains(parameter)) {
			throw new IllegalArgumentException("Parameter '" + parameter + "' not found in " + string + ". Available parameters: " + parameterNames);
		}
	}

	/**
	 * Returns the original {@code String} provided in the constructor of this class, no parameters are replaced
	 *
	 * @return the {@code String} with parameters
	 */
	@Override
	public final String toString() {
		return string;
	}

	/**
	 * Applies the parameter values provided using {@link #set(String, Object)} and returns the resulting {@code String}
	 *
	 * Parameters without values provided will not be replaced, therefore the {@code String} "zero/{one}/{two}/{one}",
	 * with parameter "one" set to 27 will evaluate to "zero/27/{two}/27"
	 *
	 * @return the resulting {@code String} with all parameters replaced by their values.
	 */
	public final String applyParameterValues() {
		if (result == null) {
			result = string;
			for (int i = parameters.size() - 1; i >= 0; i--) {
				Object parameterValue = parameterValues.get(parameters.get(i).name);
				if (parameterValue != null) {
					int openBracketIndex = parameters.get(i).startPosition;
					int closedBracketIndex = parameters.get(i).endPosition;
					result = result.substring(0, openBracketIndex) + parameterValue + result.substring(closedBracketIndex, result.length());
				}
			}
		}
		return result;
	}

	/**
	 * Returns a set of all parameter names found in the input string given in the constructor of this class.
	 *
	 * @return the unmodifiable set of available parameter names.
	 */
	public final Set<String> getParameters() {
		return Collections.unmodifiableSet(parameterNames);
	}

	/**
	 * Clears the values of all parameters.
	 */
	public final void clearValues() {
		parameterValues.clear();
		result = null;
	}

	/**
	 * Tests whether a given parameter name exists in this parameterized string.
	 *
	 * @param parameterName the name of the parameter
	 *
	 * @return {@code true} if the parameter name exists in this parameterized string, otherwise {@code false}
	 */
	public final boolean contains(String parameterName) {
		return parameterNames.contains(parameterName);
	}

	/**
	 * Returns the format associated with a given parameter
	 *
	 * @param parameterName the name of the parameter
	 *
	 * @return the format of parameter as {@code String}. Returns {@code null} if the parameter does not exist or format was not set.
	 *
	 * @throws IllegalArgumentException if the parameter name does not exist
	 */
	public final String getFormat(String parameterName) throws IllegalArgumentException {
		validateParameterName(parameterName);
		for (Parameter parameter : parameters) {
			if (parameter.name.equals(parameterName)) {
				return parameter.format;
			}
		}
		return null;
	}

	/**
	 * Clones this parameterzied string object. Currenty parameter values are copied as well.
	 *
	 * @return a clone of this object.
	 */
	@Override
	public final ParameterizedString clone() {
		try {
			ParameterizedString clone = (ParameterizedString) super.clone();
			clone.parameterValues = (TreeMap<String, Object>) this.parameterValues.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Could not clone parameterized string", e);
		}
	}

	/**
	 * Returns the current parameter names and their values.
	 *
	 * @return an unmodifiable copy of the map of parameter names and their values.
	 */
	public final Map<String, Object> getParameterValues() {
		return Collections.unmodifiableMap(parameterValues);
	}

	static private final class Parameter {
		final String name;
		final int startPosition;
		final int endPosition;
		final String format;

		Parameter(String name, int startPosition, int endPosition) {
			name = name.trim();
			if (name.contains(",")) {
				this.name = name.substring(0, name.indexOf(","));
				this.format = name.substring(name.indexOf(",") + 1).trim();
				if (format.length() == 0) {
					throw new IllegalArgumentException("Expected formatting parameter after ',' in '" + name + "'");
				}
			}
			else {
				this.name = name;
				format = null;
			}
			this.startPosition = startPosition;
			this.endPosition = endPosition;
		}

		@Override
		public String toString() {
			return "Parameter{" +
					"name='" + name + '\'' +
					", startPosition=" + startPosition +
					", endPosition=" + endPosition +
					", format='" + format + '\'' +
					'}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			Parameter parameter = (Parameter) o;

			if (startPosition != parameter.startPosition) {
				return false;
			}
			if (endPosition != parameter.endPosition) {
				return false;
			}
			if (!name.equals(parameter.name)) {
				return false;
			}
			return format != null ? format.equals(parameter.format) : parameter.format == null;
		}

		@Override
		public int hashCode() {
			int result = name.hashCode();
			result = 31 * result + startPosition;
			result = 31 * result + endPosition;
			result = 31 * result + (format != null ? format.hashCode() : 0);
			return result;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ParameterizedString that = (ParameterizedString) o;

		if (string != null ? !string.equals(that.string) : that.string != null) {
			return false;
		}
		if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) {
			return false;
		}
		if (parameterNames != null ? !parameterNames.equals(that.parameterNames) : that.parameterNames != null) {
			return false;
		}
		if (openBracket != null ? !openBracket.equals(that.openBracket) : that.openBracket != null) {
			return false;
		}
		if (closeBracket != null ? !closeBracket.equals(that.closeBracket) : that.closeBracket != null) {
			return false;
		}
		return parameterValues != null ? parameterValues.equals(that.parameterValues) : that.parameterValues == null;
	}

	@Override
	public int hashCode() {
		int result = string != null ? string.hashCode() : 0;
		result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
		result = 31 * result + (parameterNames != null ? parameterNames.hashCode() : 0);
		result = 31 * result + (openBracket != null ? openBracket.hashCode() : 0);
		result = 31 * result + (closeBracket != null ? closeBracket.hashCode() : 0);
		result = 31 * result + (parameterValues != null ? parameterValues.hashCode() : 0);
		return result;
	}
}
