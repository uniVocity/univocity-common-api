/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.io;

import java.io.*;
import java.util.*;

/**
 * A queue of a sequence of inputs to be processed. Inputs can come in all sort of formats, but must be abstracted
 * by a {@link java.io.Reader}. Variables can be used to assign values to each input and provide more control and
 * information about what the input represents (e.g. date the input was produced, some user ID associated with the
 * input, etc)
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see InputFileQueue
 * @see InputReaderQueue
 */
public abstract class InputQueue<T> extends ReaderProvider {

	private final Map<T, Map<String, Object>> variables = new HashMap<T, Map<String, Object>>();

	private final Queue<T> inputQueue = new LinkedList<T>();

	private Map<String, Object> currentVariables = null;
	private T lastEntry;

	/**
	 * Creates an empty queue.
	 */
	public InputQueue() {

	}

	/**
	 * Queries whether the input is empty (i.e. all inputs have been processed)
	 *
	 * @return {@code true} if there are no more inputs to be processed, otherwise {@code false}
	 */
	public final boolean isEmpty() {
		return inputQueue.isEmpty();
	}

	/**
	 * Returns the number of inputs to be processed that are waiting in the queue
	 *
	 * @return the queue size
	 */
	public final int size() {
		return inputQueue.size();
	}

	/**
	 * Adds an input to be processed to the end of the queue
	 *
	 * @param input the input to be processed
	 */
	protected final void offer(T input) {
		inputQueue.offer(input);
		this.lastEntry = input;
	}

	/**
	 * Assigns a variable and its value to the last entry added to this input queue
	 *
	 * @param variable the variable name associated to the last input to be processed in this input queue
	 * @param value    the value to associated to the given variable
	 */
	public void assignVariableToLastEntry(String variable, Object value) {
		if (lastEntry == null) {
			throw new IllegalArgumentException("Can't assign value '" + value + "' to variable '" + variable + "' bound to last entry of input queue. Input queue is empty.");
		}
		assignVariableToEntry(lastEntry, variable, value);
	}

	/**
	 * Assigns a set of variables and their values to the last entry added to this input queue
	 *
	 * @param variables the variable and values associated to the last input to be processed in this input queue
	 */
	public void assignVariablesToLastEntry(Map<String, Object> variables) {
		if (lastEntry == null) {
			throw new IllegalArgumentException("Can't assign variables " + variables + " to last entry of input queue. Input queue is empty.");
		}
		for (Map.Entry<String, Object> e : variables.entrySet()) {
			assignVariableToEntry(lastEntry, e.getKey(), e.getValue());
		}
	}

	private void assignVariableToEntry(T entry, String variable, Object value) {
		Map<String, Object> entryVars = variables.get(entry);
		if (entryVars == null) {
			entryVars = new HashMap<String, Object>();
			variables.put(entry, entryVars);
		}
		entryVars.put(variable, value);
	}

	/**
	 * Grabs the next input of the queue, opens it as an instanceof {@link java.io.Reader} with the help of method
	 * {@link #open(Object)}, loads the variables associated with the input, and returns the {@link java.io.Reader}.
	 *
	 * @return an instance of {@link java.io.Reader} to consume the next element of the input queue.
	 */
	@Override
	public final Reader getResource() {
		T input = inputQueue.poll();

		if (input == null) {
			throw new IllegalStateException("No input to process");
		}

		currentVariables = variables.remove(input);

		return open(input);
	}

	/**
	 * Returns the value assigned to a given variable associated with the current input being read.
	 *
	 * @param variable the variable name
	 *
	 * @return the value of the variable, or {@code null} if it doesn't exist
	 */
	public Object readVariable(String variable) {
		return getCurrentVariables().get(variable);
	}

	/**
	 * Returns the value assigned to a given variable associated with the current input being read.
	 *
	 * @param variable     the variable name
	 * @param defaultValue a default value to return in case the variable doesn't exist or its value is {@code null}
	 * @param <T>          the type of value returned by this method.
	 *
	 * @return the value of the variable, or the default value if the variable value evaluates to {@code null}
	 */
	public <T> T readVariable(String variable, T defaultValue) {
		Object out = getCurrentVariables().get(variable);
		if (out == null) {
			return defaultValue;
		}
		return (T) out;
	}

	/**
	 * Returns the value assigned to a given variable associated with the current input being read.
	 *
	 * @param variable the variable name
	 * @param type     the type of value expected to be returned. The value assigned to this
	 *                 variable will be cast to the given type.
	 * @param <T>      the type of value returned by this method.
	 *
	 * @return the value of the variable, or the default value if the variable value evaluates to {@code null}
	 */
	public <T> T readVariable(String variable, Class<T> type) {
		Object out = getCurrentVariables().get(variable);
		if (out != null) {
			return type.cast(out);
		}
		return null;
	}

	/**
	 * Returns a map of all variables assigned to the input being read
	 *
	 * @return all variables associated with the current input.
	 */
	public Map<String, Object> getCurrentVariables() {
		if (currentVariables == null) {
			return Collections.emptyMap();
		}
		return currentVariables;
	}

	/**
	 * Opens an input and returns an instanceof {@link java.io.Reader} which will be used to consume it.
	 * Used when {@link #getResource()} is called.
	 *
	 * @param input the input to be read
	 *
	 * @return {@link java.io.Reader} that can be used to consume the given input.
	 */
	protected abstract Reader open(T input);

}
