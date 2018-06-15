/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

import java.util.*;

/**
 * A central place for utility methods.
 */
public class Utils {
	/**
	 * Joins the {@code String} representation of all non-null values in a given
	 * collection into a {@code String}, with a given separator between each value.
	 *
	 * @param values the values to be joined. Nulls are skipped.
	 * @param separator the separator to use between each value
	 * @return a String with all non-null values in the given collection.
	 */
	public static final String join(Iterable<?> values, String separator) {
		if (values == null) {
			return "";
		}

		StringBuilder out = new StringBuilder(64);
		for (Object value : values) {
			if (value != null) {
				if (out.length() != 0) {
					out.append(separator);
				}
				out.append(value);
			}
		}

		return out.toString();
	}

	/**
	 * Joins each collection of values in a given {@code Map} into their {@code String}
	 * representation, with a given separator between each value.
	 *
	 * @param map a map containing collections as its values
	 * @param separator the separator to be used between each value
	 * @param <K> the type of the key used in the given map
	 * @param <V> the type of the collection of values associated with each key of the map
	 * @return the resulting map where each key of the given input map is associated
	 * 			with the String representation of all non-null values in the collection
	 * 			associated with the key.
	 */
	public static final <K, V extends Iterable> Map<K, String> joinValues(Map<K, V> map, String separator) {
		if (map == null || map.isEmpty()) {
			return Collections.emptyMap();
		}

		LinkedHashMap<K, String> out = new LinkedHashMap<K, String>();
		for (Map.Entry<K, V> e : map.entrySet()) {
			out.put(e.getKey(), join(e.getValue(), separator));
		}
		return out;
	}

	/**
	 * Returns the {@code Map.Entry} stored in a map by searching for a given {@code String}
	 * key case-insensitively.
	 *
	 * @param map the map to search
	 * @param key the key to look for
	 * @param <V> the type of values stored in the map
	 * @return the {@code Map.Entry} associated with the given key, or {@code null} if not found.
	 */
	public static final <V> Map.Entry<String, V> getEntryCaseInsensitive(Map<String, V> map, String key) {
		if (key != null) {
			key = key.toLowerCase(Locale.ENGLISH);
		}

		for (Map.Entry<String, V> entry : map.entrySet()) {
			String k = entry.getKey();
			if (key == null) {
				if (k == null) {
					return entry;
				}
			} else if (k != null && key.equals(k.toLowerCase(Locale.ENGLISH))) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * Returns the value stored in a map by searching for a given {@code String}
	 * key case-insensitively.
	 *
	 * @param map the map to search
	 * @param key the key to look for
	 * @param <V> the type of values stored in the map
	 * @return the value associated with the given key, or {@code null} if not found.
	 */
	public static final <V> V getValueCaseInsensitive(Map<String, V> map, String key) {
		Map.Entry<String, V> e = getEntryCaseInsensitive(map, key);
		if (e == null) {
			return null;
		}
		return e.getValue();
	}

	//FIXME: javadoc
	public static final <V> void putValueCaseInsensitive(Map<String, List<V>> map, String key, V value, boolean add) {
		List<V> values = Utils.getValueCaseInsensitive(map, key);
		if (values == null) {
			values = new ArrayList<V>();
			map.put(key, values);
		} else if (!add) {
			values.clear();
		}
		values.add(value);
	}

}
