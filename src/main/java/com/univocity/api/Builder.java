/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api;

import java.util.*;
import java.util.concurrent.*;

/**
 * This is the entry point to univocity's internal implementation classes. It connects the resources in the public API to their actual implementations in univocity's jars.
 *
 * <p>In some circumstances, you might need to configure the class loader before being able to obtain instances of {@link CommonFactoryProvider} from univocity.jar.
 * If that is the case, use the {@link #setClassLoader(ClassLoader)} method before calling the {@link #build(Class, Object...)}} method.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public final class Builder {

	private static ConcurrentHashMap<Class, CommonFactoryProvider> providers = new ConcurrentHashMap<Class, CommonFactoryProvider>();

	private static ServiceLoader<CommonFactoryProvider> factoryProviderLoader = ServiceLoader.load(CommonFactoryProvider.class);

	/**
	 * Defines the class loader to be used to load uniVocity implementation classes (from univocity.jar)
	 *
	 * @param classLoader The class loader to be used to load provider classes, or <tt>null</tt> if the system class loader is to be used.
	 */
	public static final synchronized void setClassLoader(ClassLoader classLoader) {
		factoryProviderLoader = ServiceLoader.load(CommonFactoryProvider.class, classLoader);
		providers.clear();
	}

	/**
	 * Creates a new instance of the given type, using the given arguments
	 *
	 * @param builderType the type whose instance must be created
	 * @param args        the arguments to be used by the concrete implementation of the given type.
	 * @param <T>         the type of the object that will be returned by this method.
	 *
	 * @return an instance of the given type.
	 */
	public static final <T> T build(Class<T> builderType, Object... args) {
		T out = null;
		CommonFactoryProvider builder = providers.get(builderType);
		if (builder == null && !providers.containsKey(builderType)) {
			if (!providers.isEmpty()) {
				for (Map.Entry<Class, CommonFactoryProvider> e : providers.entrySet()) {
					try {
						out = e.getValue().build(builderType, args);
						providers.put(builderType, e.getValue());
						return out;
					} catch (Throwable t) {
						//ignore
					}
				}
			}

			for (CommonFactoryProvider provider : factoryProviderLoader) {
				try {
					out = provider.build(builderType, args);
					providers.put(builderType, provider);
					return out;
				} catch (Throwable t) {
					//ignore
				}
			}

			if(builderType != null) {
				providers.put(builderType, new CommonFactoryProvider() {
					@Override
					public <T> T build(Class<T> builderType, Object... args) {
						return null;
					}
				});
			}
		} else if (builder != null){
			out = builder.build(builderType, args);
		}
		if (out == null) {
			throw new IllegalStateException("Unable to load implementation of " + builderType.getName() + ". You might need to use a different classloader in order to load it from univocity's jar file");
		}
		return out;
	}
}
