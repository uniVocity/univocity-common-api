package com.univocity.api;

/**
 * The <code>CommonFactoryProvider</code> is used to obtain actual implementations of interfaces and configured objects
 * from an implementation .jar. It is used internally by the {@link Builder} class.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 *
 */
interface CommonFactoryProvider {

	/**
	 * Returns an object implementation provided by uniVocity. This is for internal use only.
	 *
	 * @param builderType the interface of a builder entry point
	 * @param args any arguments required to initialize the builder.
	 *
	 * @return a builder implementation provided by uniVocity.
	 * @param <T> the type of builder to instantiate.
	 */
	<T> T build(Class<T> builderType, Object ... args);
}
