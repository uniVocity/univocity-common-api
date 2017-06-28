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

	private Parameter[] parameters;
	private final Set<String> parameterNames = new TreeSet<String>();
	private TreeMap<String, Object> parameterValues;

	private String[] nonParameterSections;

	private final String openBracket;
	private final String closeBracket;

	private String result = null;
	private Object defaultValue = null;
	private boolean convertDefaultValueToNull = true;


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
		List<String> nonParameterSections = new ArrayList<String>();
		List<Parameter> parameters = new ArrayList<Parameter>();

		int x = 0;
		int nonParameterIndexStart = 0;
		int openBracketIndex;
		while ((openBracketIndex = string.indexOf(openBracket, x)) >= 0) {
			if (openBracketIndex != 0 && openBracketIndex == nonParameterIndexStart) {
				int errorPos = openBracketIndex;
				StringBuilder errorMsg = new StringBuilder("There needs to be at least one character separating parameters\nNo separation found after:\n'");
				errorMsg.append(string).append("'\n");
				for (int i = 0; i < errorPos; i++) {
					errorMsg.append(' ');
				}
				errorMsg.append('^');
				throw new IllegalArgumentException(errorMsg.toString());
			}
			int closeBracketIndex = string.indexOf(closeBracket, openBracketIndex);
			if (closeBracketIndex > 0) {
				x = closeBracketIndex;

				String nonParameterSection = string.substring(nonParameterIndexStart, openBracketIndex);
				if (!nonParameterSection.isEmpty()) {
					nonParameterSections.add(nonParameterSection);
				}
				nonParameterIndexStart = closeBracketIndex + 1;

				String parameterizedName = string.substring(openBracketIndex + 1, closeBracketIndex);
				Parameter parameter = new Parameter(parameterizedName, openBracketIndex, closeBracketIndex + 1);
				parameters.add(parameter);
				if (openBracketIndex == 0) {
					nonParameterSections.add("");
				}
				parameterNames.add(parameter.name);
			} else {
				x = openBracketIndex + 1;
			}
		}
		if (nonParameterIndexStart < string.length()) {
			nonParameterSections.add(string.substring(nonParameterIndexStart));
		}

		this.nonParameterSections = nonParameterSections.toArray(new String[0]);
		this.parameters = parameters.toArray(new Parameter[0]);
	}

	/**
	 * Sets multiple parameter values
	 *
	 * @param parametersAndValues map of parameter names and thier corresponding values.
	 *
	 * @throws IllegalArgumentException if a parameter name in found in the given map does not exist
	 */
	public final void set(Map<String, ?> parametersAndValues) throws IllegalArgumentException {
		if (parametersAndValues == null) {
			return;
		}
		for (Map.Entry<String, ?> e : parametersAndValues.entrySet()) {
			set(e.getKey(), e.getValue());
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

		if (convertDefaultValueToNull && value != null && defaultValue != null && value.equals(defaultValue)) {
			value = null;
		}

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

	private void invalidInput(String input) {
		clearValues();
		throw new IllegalArgumentException("The input:\n'" + input + "'\nDoes not match the parameter pattern:\n'" + string + "'");
	}

	/**
	 * <p>Parses the {@code String input} and extracts the parameter values storing them as regular parameters.</p>
	 * <p>The {@link Map} of parameters is returned as a convenience, but parameter values can also be retrieved using:
	 * <ul>
	 * <li>{@link #get(String)} - for individual parameters</li>
	 * <li>{@link #getParameterValues()} - for all of them</li>
	 * </ul>
	 * </p>
	 *
	 * @param input the input String to be parsed
	 *
	 * @return the {@link Map} of parameters to their assigned values
	 */
	public final Map<String, Object> parse(String input) {
		if (parameters.length == 0) {
			return Collections.emptyMap();
		}

		TreeSet<String> parsedParams = new TreeSet<String>();

		int valueStart = 0;

		for (int i = 0, p = 0; i < nonParameterSections.length && p < parameters.length; i++) {
			String section = nonParameterSections[i];

			valueStart = input.indexOf(section, valueStart);
			if (valueStart == -1) {
				invalidInput(input);
			}
			valueStart += section.length();

			int valueEnd;
			if (i + 1 < nonParameterSections.length) {
				String nextSection = nonParameterSections[i + 1];
				valueEnd = input.indexOf(nextSection, valueStart);
				if (valueEnd == -1) {
					invalidInput(input);
				}
			} else {
				valueEnd = input.length();
			}

			String value = input.substring(valueStart, valueEnd);

			Parameter parameter = parameters[p++];
			Object existingValue = parameterValues.get(parameter.name);
			if (existingValue != null && !existingValue.equals(value) && parsedParams.contains(parameter.name)) {
				StringBuilder sb = new StringBuilder("Multiple values ('").append(existingValue).append("' and '").append(value).append("') found for parameter '");
				sb.append(parameter.name);
				sb.append("'\n");
				sb.append(input);
				sb.append('\n');
				int errPos = input.length() - input.length() + input.indexOf(value);
				for (int j = 0; j < errPos; j++) {
					sb.append(' ');
				}
				sb.append('^');
				throw new IllegalArgumentException(sb.toString());
			}
			parsedParams.add(parameter.name);
			set(parameter.name, value);
		}
		if (nonParameterSections.length == 0) {
			if (parameters.length == 1) {
				set(parameters[0].name, input);
			}
		}
		return getParameterValues();
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
	 * Unless a default value is provided via {@link #getDefaultValue()}, parameters without values provided will not
	 * be replaced. Therefore the {@code String} "zero/{one}/{two}/{one}", with parameter "one" set to 27 will
	 * evaluate to "zero/27/{two}/27"
	 *
	 * @return the resulting {@code String} with all parameters replaced by their values.
	 */
	public final String applyParameterValues() {
		if (result == null) {
			result = string;
			for (int i = parameters.length - 1; i >= 0; i--) {
				Object parameterValue = parameterValues.get(parameters[i].name);
				if (parameterValue == null && defaultValue != null) {
					parameterValue = defaultValue;
				}
				if (parameterValue != null) {
					int openBracketIndex = parameters[i].startPosition;
					int closedBracketIndex = parameters[i].endPosition;
					result = result.substring(0, openBracketIndex) + parameterValue.toString() + result.substring(closedBracketIndex, result.length());
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

	/**
	 * Defines a default value to be used for parameters that have no value associated.
	 *
	 * Defaults to {@code null}, in which case the original parameter will appear in the result of
	 * {@link #applyParameterValues()}.
	 *
	 * @param defaultValue the default value to be used when one or more parameters have no value associated.
	 */
	public final void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
		if (defaultValue != null && !parameterValues.isEmpty()) {
			for (Map.Entry<String, Object> e : parameterValues.entrySet()) {
				if (defaultValue.equals(e.getValue())) {
					e.setValue(null);
				}
			}
		}
	}

	/**
	 * Returns the default value to be used for parameters that have no value associated.
	 *
	 * Defaults to {@code null}, in which case the original parameter will appear in the result of
	 * {@link #applyParameterValues()}.
	 *
	 * @return the default value to be used when one or more parameters have no value associated.
	 */
	public final Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Returns the index before the first parameter in this parameterized string.
	 *
	 * @return index before the first parameter, or {@code -1} if no parameters exist.
	 */
	public final int getIndexBeforeFirstParameter() {
		if (parameters.length > 0) {
			return parameters[0].startPosition;
		}
		return -1;
	}

	/**
	 * Returns the index after the last parameter in this parameterized string.
	 *
	 * @return index after the last parameter, or {@code -1} if no parameters exist.
	 */
	public final int getIndexAfterLastParameter() {
		if (parameters.length > 0) {
			return parameters[parameters.length - 1].endPosition;
		}
		return -1;
	}

	/**
	 * Returns the content before the first parameter in this parameterized string.
	 *
	 * @return text content before the first parameter, or the entire {@code String} if no parameters exist.
	 */
	public final String getContentBeforeFirstParameter() {
		if (parameters.length == 0) {
			return string;
		}
		int index = getIndexBeforeFirstParameter();
		return string.substring(0, index);
	}

	/**
	 * Returns the content after the last parameter in this parameterized string.
	 *
	 * @return text content after the last parameter, or the entire {@code String} if no parameters exist.
	 */
	public final String getContentAfterLastParameter() {
		if (parameters.length == 0) {
			return string;
		}
		int index = getIndexAfterLastParameter();
		return string.substring(index, string.length());
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
			} else {
				this.name = name;
				format = null;
			}
			this.startPosition = startPosition;
			this.endPosition = endPosition;
		}

		@Override
		public final String toString() {
			return "Parameter{" +
					"name='" + name + '\'' +
					", startPosition=" + startPosition +
					", endPosition=" + endPosition +
					", format='" + format + '\'' +
					'}';
		}

		@Override
		public final boolean equals(Object o) {
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
		public final int hashCode() {
			int result = name.hashCode();
			result = 31 * result + startPosition;
			result = 31 * result + endPosition;
			result = 31 * result + (format != null ? format.hashCode() : 0);
			return result;
		}
	}

	@Override
	public final boolean equals(Object o) {
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
	public final int hashCode() {
		int result = string != null ? string.hashCode() : 0;
		result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
		result = 31 * result + (parameterNames != null ? parameterNames.hashCode() : 0);
		result = 31 * result + (openBracket != null ? openBracket.hashCode() : 0);
		result = 31 * result + (closeBracket != null ? closeBracket.hashCode() : 0);
		result = 31 * result + (parameterValues != null ? parameterValues.hashCode() : 0);
		return result;
	}

	/**
	 * Flag indicating that values parsed from this {@code ParameterizedString} will be converted to {@code null}, when
	 * equal to the String representation of the value returned by {@link #getDefaultValue()}.
	 *
	 * <ul>
	 * <li>When {@code true} all methods that return values associated with a parameter will return {@code null} instead of the specified default value.</li>
	 * <li>When {@code false} all methods that return values associated with a parameter will default value returned by {@link #getDefaultValue()}.</li>
	 * </ul>
	 *
	 * @return {@code true} if default values should be converted to {@code null} when reading parameter values, otherwise {@code false}
	 */
	public final boolean getConvertDefaultValueToNull() {
		return convertDefaultValueToNull;
	}

	/**
	 * Defines whether values parsed from this {@code ParameterizedString} should be converted to {@code null}, when
	 * equal to the String representation of the value returned by {@link #getDefaultValue()}.
	 *
	 * <ul>
	 * <li>When {@code true} all methods that return values associated with a parameter will return {@code null} instead of the specified default value.</li>
	 * <li>When {@code false} all methods that return values associated with a parameter will default value returned by {@link #getDefaultValue()}.</li>
	 * </ul>
	 *
	 * @param convertDefaultValueToNull flag indicating whether default values should be converted to {@code null} when reading parameter values
	 */
	public final void setConvertDefaultValueToNull(boolean convertDefaultValueToNull) {
		this.convertDefaultValueToNull = convertDefaultValueToNull;
	}
}
