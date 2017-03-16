package com.univocity.api.common;

/**
 * A simple interface used to test a given {@link String}
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface StringFilter {
	boolean accept(String str);
}
