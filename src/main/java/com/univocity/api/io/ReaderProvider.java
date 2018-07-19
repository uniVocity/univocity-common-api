/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.io;

import com.univocity.api.net.*;

import java.io.*;

/**
 * Base abstract class to define classes that provide instances of {@link java.io.Reader}
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see UrlReaderProvider
 * @see StringReaderProvider
 * @see InputQueue
 */
public abstract class ReaderProvider implements ResourceProvider<Reader> {

}
