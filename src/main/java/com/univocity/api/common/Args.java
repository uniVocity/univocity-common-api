/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

import java.io.*;
import java.net.*;
import java.util.*;

import static java.sql.Connection.*;

/**
 * Utility class used to validate arguments and configuration options passed to objects in uniVocity's API.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class Args {
	protected Args() {

	}

	/**
	 * Ensures a given argument is not null.
	 *
	 * @param o         the object to validate
	 * @param fieldName the description of the field
	 */
	public static final void notNull(Object o, String fieldName) {
		if (o == null) {
			throw new IllegalArgumentException(fieldName + " cannot be null");
		}
	}

	/**
	 * Ensures a given number is positive (and greater than zero).
	 *
	 * @param o         the number to validate
	 * @param fieldName the description of the field
	 */
	public static final void positive(Number o, String fieldName) {
		notNull(o, fieldName);
		if (((Integer) o.intValue()).compareTo(0) <= 0) {
			throw new IllegalArgumentException(fieldName + " must be positive. Got " + o);
		}
	}

	/**
	 * Ensures a given number is positive or equal to zero.
	 *
	 * @param o         the number to validate
	 * @param fieldName the description of the field
	 */
	public static final void positiveOrZero(Number o, String fieldName) {
		notNull(o, fieldName);
		if (((Double) o.doubleValue()).compareTo(0.0) < 0) {
			throw new IllegalArgumentException(fieldName + " must be a positive number or zero. Got " + o);
		}
	}

	/**
	 * Ensures a given array argument is not null/empty
	 *
	 * @param sequence  the array of objects
	 * @param fieldName the description of the field
	 * @param <T>       the type of elements in the array
	 */
	public static <T> void notEmpty(T[] sequence, String fieldName) {
		notNull(sequence, fieldName);
		if (sequence.length == 0) {
			throw new IllegalArgumentException(fieldName + " cannot be empty");
		}
		for (T element : sequence) {
			if (element == null) {
				throw new IllegalArgumentException("Illegal " + fieldName + " list. Null elements are not allowed. Got " + Arrays.toString(sequence));
			} else if (element instanceof String && element.toString().trim().isEmpty()) {
				throw new IllegalArgumentException("Illegal " + fieldName + " list. Blank elements are not allowed. Got " + Arrays.toString(sequence));
			}
		}
	}

	public static void notEmpty(Collection<?> field, String fieldName) {
		notNull(field, fieldName);
		if (field.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " cannot be empty");
		}
	}

	/**
	 * Ensures a given collection is not null/empty
	 *
	 * @param elements  the collection of objects
	 * @param fieldName the description of the field
	 * @param <T>       the type of elements in the collection
	 */
	public static <T> void noneEmpty(Collection<T> elements, String fieldName) {
		notNull(elements, fieldName);
		if (elements.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " cannot be empty");
		}
		for (T element : elements) {
			if (element == null) {
				throw new IllegalArgumentException("Illegal " + fieldName + " list. Null elements are not allowed. Got " + elements);
			} else if (element instanceof String && element.toString().trim().isEmpty()) {
				throw new IllegalArgumentException("Illegal " + fieldName + " list. Blank elements are not allowed. Got " + elements);
			}
		}
	}

	/**
	 * Ensures a given int[] array argument is not null/empty
	 *
	 * @param field     the array of objects
	 * @param fieldName the description of the field
	 */
	public static final void notEmpty(int[] field, String fieldName) {
		notNull(field, fieldName);
		if (field.length == 0) {
			throw new IllegalArgumentException(fieldName + " cannot be empty");
		}
	}

	/**
	 * Ensures a given {@code char} array argument is not null/empty
	 *
	 * @param field     the array of objects
	 * @param fieldName the description of the field
	 */
	public static final void notEmpty(char[] field, String fieldName) {
		notNull(field, fieldName);
		if (field.length == 0) {
			throw new IllegalArgumentException(fieldName + " cannot be empty");
		}
	}


	/**
	 * Ensures a given {@link CharSequence} argument is not null/empty
	 *
	 * @param o         a character sequence
	 * @param fieldName the description of the field
	 */
	public static final void notEmpty(CharSequence o, String fieldName) {
		notNull(o, fieldName);
		if (o.length() == 0) {
			throw new IllegalArgumentException(fieldName + " cannot be empty");
		}
	}

	/**
	 * Ensures a given {@link CharSequence} argument is not null/empty/blank
	 *
	 * @param o         a character sequence
	 * @param fieldName the description of the field
	 */
	public static final void notBlank(CharSequence o, String fieldName) {
		notNull(o, fieldName);
		if (o.toString().trim().isEmpty()) {
			throw new IllegalArgumentException(fieldName + " cannot be blank");
		}
	}

	/**
	 * Ensures the elements in a given array are not null/empty/blank. The array itself can be empty but not null.
	 *
	 * @param o         the array of elements to be validated.
	 * @param fieldName description of the array.
	 */
	public static final void noBlanks(Object[] o, String fieldName) {
		notNull(o, fieldName);
		for (Object e : o) {
			if (e == null) {
				throw new IllegalArgumentException("Null value in " + fieldName + ": " + Arrays.toString(o));
			}
			if (e instanceof CharSequence) {
				if (isBlank(e.toString())) {
					throw new IllegalArgumentException("Blank value in " + fieldName + ": " + Arrays.toString(o));
				}
			}
		}
	}

	/**
	 * Ensures a given {@link File} argument is not null, exists and does not point to a directory
	 *
	 * @param file      a file
	 * @param fieldName the description of the field
	 */
	public static final void validFile(File file, String fieldName) {
		notNull(file, fieldName);
		if (!file.exists()) {
			throw new IllegalArgumentException("Illegal " + fieldName + ": '" + file.getAbsolutePath() + "' it does not exist.");
		}
		if (file.isDirectory()) {
			throw new IllegalArgumentException("Illegal " + fieldName + ": '" + file.getAbsolutePath() + "' it cannot be a directory.");
		}
	}

	/**
	 * Ensures a given SQL isolation level is a valid and known JDBC value that exists int {@link java.sql.Connection}
	 *
	 * @param transactionIsolationLevel code of the transaction isolation level
	 */
	public static final void validTransactionIsolationLevel(int transactionIsolationLevel) {
		List<Integer> levels = Arrays.asList(TRANSACTION_NONE, TRANSACTION_READ_COMMITTED, TRANSACTION_READ_UNCOMMITTED, TRANSACTION_REPEATABLE_READ, TRANSACTION_SERIALIZABLE);
		if (!levels.contains(transactionIsolationLevel)) {
			throw new IllegalArgumentException("Illegal transaction isolation level: " + transactionIsolationLevel + ". Accepted isolation levels are: " + levels + " (from java.sql.Connection)");
		}
	}

	public static final String guessAndValidateName(String name, File file, String fieldName) {
		if (name != null) {
			notBlank(name, fieldName);
			return name;
		}
		validFile(file, fieldName);

		name = file.getName();
		if (name.lastIndexOf('.') != -1) {
			name = name.substring(0, name.lastIndexOf('.'));
		}

		if (name.trim().isEmpty()) {
			throw new IllegalArgumentException("Cannot derive " + fieldName + " from file " + file.getAbsolutePath());
		}

		return name;
	}


	/**
	 * Tests if a given {@code String} is null/empty/blank/
	 *
	 * @param s the string
	 *
	 * @return {@code true} if the given {@code String} is null, empty or blank, otherwise returns {@code false}
	 */
	public static final boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	/**
	 * Tests if a given {@code String} is not null/empty/blank/
	 *
	 * @param s the string
	 *
	 * @return {@code true} if the given {@code String} is not null, empty or blank, otherwise returns {@code false}
	 */
	public static final boolean isNotBlank(String s) {
		return !isBlank(s);
	}

	/**
	 * Replaces system properties between { and } in a given {@code String} with the property values, and returns the result.
	 * Unknown properties won't be replaced.
	 *
	 * @param string the {@code String} with potential system properties.
	 *
	 * @return the resulting {@code String} with all known system properties replaced.
	 */
	public static final String replaceSystemProperties(String string) {
		int offset = 0;
		int braceOpen = 0;
		while (braceOpen >= 0) {
			braceOpen = string.indexOf('{', offset);
			if (braceOpen >= 0) {
				offset = braceOpen;
				int braceClose = string.indexOf('}');
				if (braceClose > braceOpen) {
					offset = braceClose;
					String property = string.substring(braceOpen + 1, braceClose);
					String value = System.getProperty(property);
					if (value != null) {
						String beforeProperty = string.substring(0, braceOpen);
						String afterProperty = "";
						if (braceClose < string.length()) {
							afterProperty = string.substring(braceClose + 1, string.length());
						}
						string = beforeProperty + value + afterProperty;
					}
				}

			} else {
				break;
			}
		}
		return string;
	}

	/**
	 * Decodes a URL encoded value using UTF-8.
	 *
	 * @param value the value to be decoded.
	 *
	 * @return the decoded value.
	 */
	public static final String decode(Object value) {
		return decode(null, value, null);
	}

	/**
	 * Decodes a URL encoded value.
	 *
	 * @param value       the value to be decoded.
	 * @param charsetName the charset to use for decoding the given value. If {@code null}, then UTF-8 will be used.
	 *
	 * @return the decoded value.
	 */
	public static final String decode(Object value, String charsetName) {
		return decode(null, value, charsetName);
	}

	/**
	 * Decodes a URL encoded value.
	 *
	 * @param parameterName name of the parameter associated with the value
	 * @param value         the value to be decoded.
	 * @param charsetName   the charset to use for decoding the given value. If {@code null}, then UTF-8 will be used.
	 *
	 * @return the decoded value.
	 */
	public static final String decode(String parameterName, Object value, String charsetName) {
		if (value == null) {
			return null;
		}
		if (charsetName == null) {
			charsetName = "UTF-8";
		}
		String stringVal = String.valueOf(value);
		try {
			stringVal = URLDecoder.decode(stringVal, charsetName);
		} catch (Exception ex) {
			if (parameterName == null) {
				throw new IllegalStateException("Error decoding value: " + value, ex);
			} else {
				throw new IllegalStateException("Error decoding value of parameter '" + parameterName + "'. Value: " + value, ex);
			}
		}

		return stringVal;
	}

	/**
	 * Encodes a value using UTF-8 so it can be used as part of a URL. If the value is already encoded, its original {@code String}
	 * representation will be returned.
	 *
	 * @param parameterValue the value to be encoded.
	 *
	 * @return the encoded value.
	 */
	public static final String encode(Object parameterValue) {
		return encode(null, parameterValue, null);
	}

	/**
	 * Encodes a value so it can be used as part of a URL. If the value is already encoded, its original {@code String}
	 * representation will be returned.
	 *
	 * @param parameterValue the value to be encoded.
	 * @param charsetName    charset to use for encoding the given value. If {@code null}, then UTF-8 will be used.
	 *
	 * @return the encoded value.
	 */
	public static final String encode(Object parameterValue, String charsetName) {
		return encode(null, parameterValue, charsetName);
	}

	/**
	 * Encodes a value so it can be used as part of a URL. If the value is already encoded, its original {@code String}
	 * representation will be returned.
	 *
	 * @param parameterName  name of the parameter associated with the value
	 * @param parameterValue the value to be encoded.
	 * @param charsetName    charset to use for encoding the given value. If {@code null}, then UTF-8 will be used.
	 *
	 * @return the encoded value.
	 */
	public static final String encode(String parameterName, Object parameterValue, String charsetName) {
		if (parameterValue == null) {
			return null;
		}
		if (charsetName == null) {
			charsetName = "UTF-8";
		}
		String original = String.valueOf(parameterValue);
		String decoded = original;
		try {
			decoded = decode(parameterName, original, charsetName);
		} catch (Exception ex) {
			//ignore, will encode.
		}

		if (decoded.equals(original)) {
			try {
				return URLEncoder.encode(original, charsetName);
			} catch (Exception ex) {
				if (parameterName == null) {
					throw new IllegalStateException("Error encoding value: " + parameterValue, ex);
				} else {
					throw new IllegalStateException("Error encoding value of parameter '" + parameterName + "'. Value: " + parameterValue, ex);
				}
			}
		} //else value is already encoded.
		return original;
	}

	/**
	 * Converts a yyyy-MM-dd formatted string to a Calendar instance.
	 *
	 * @param s the yyyy-MM-dd formatted string
	 *
	 * @return the corresponding {@code Calendar} instance
	 */
	public static final Calendar isoDateStringToCalendar(String s) {
		if (isBlank(s)) {
			return null;
		}
		Calendar out = null;
		try {
			int firstDash = s.indexOf('-');
			int secondDash = s.indexOf('-', firstDash + 1);

			String yyyy = s.substring(0, firstDash);
			String mm = s.substring(firstDash + 1, secondDash);
			String dd = s.substring(secondDash + 1);

			if (yyyy.length() == 4 && mm.length() == 2 && dd.length() == 2) {
				int year = Integer.parseInt(yyyy);
				int month = Integer.parseInt(mm) - 1;
				int day = Integer.parseInt(dd);

				out = new GregorianCalendar(year, month, day);

				if (out.get(Calendar.YEAR) != year || out.get(Calendar.MONTH) != month || out.get(Calendar.DAY_OF_MONTH) != day) {
					out = null;
				}
			}
		} catch (Exception e) {
			//Not formatted correctly ignore any errors here;
		}

		if (out == null) {
			throw new IllegalArgumentException("Date '" + s + "' must be formatted as yyyy-MM-dd");
		}

		return out;
	}

	/**
	 * Determines whether two collections of Object[] contain the same values.
	 * @param c1 the first collection
	 * @param c2 the second collection.
	 * @return {@code true} if both collections contain the same values, {@code false} otherwise.
	 */
	public static boolean equals(Collection<Object[]> c1, Collection<Object[]> c2) {
		if(c1 == c2){
			return true;
		}
		if (c1 != null) {
			if (c2 != null) {
				if (c1.size() == c2.size()) {
					Iterator<Object[]> i1 = c1.iterator();
					Iterator<Object[]> i2 = c1.iterator();

					while (i1.hasNext()) {
						if (!Arrays.equals(i1.next(), i2.next())) {
							return false;
						}
					}
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return c2 == null;
		}
	}
}
