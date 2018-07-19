package com.univocity.api.common;

/**
 * A simple interface used to test a given {@code String}
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface StringFilter {

	/**
	 * Tests whether a given {@code String} conforms to a specific criteria.
	 *
	 * @param str the {@code String} to be tested
	 * @return {@code true} if the given {@code String} conforms to the filtering rules implemented by this interface,
	 *         otherwise returns {@code false}
	 */
	boolean accept(String str);
}
