/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.statistics;

import java.util.*;
import java.util.concurrent.*;

/**
 * A basic management structure for data transfers occurring in parallel.
 *
 * @param <S> type of the supported sources of data
 * @param <T> type of the supported targets of data
 * @param <E> type of the entries managed by this list, created for each individual data transfer with a call to
 *            {@link #newDataTransfer(Object, long, Object)}
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a> *
 * @see DownloadList
 * @see DataTransferListener
 */
public abstract class DataTransferList<S, T, E extends DataTransferListener<S, T>> implements DataTransferListener<S, T>, Iterable<E> {

	private Map<T, E> active = new ConcurrentHashMap<T, E>();
	private LinkedHashSet<E> order = new LinkedHashSet<E>();

	@Override
	public synchronized void started(S source, long totalSize, T target) {
		E previousTransfer = active.get(target);
		if (previousTransfer != null && previousTransfer.isRunning()) {
			previousTransfer.aborted(source, target, null);
			order.remove(previousTransfer);
		}

		E transfer = newDataTransfer(source, totalSize, target);
		transfer.started(source, totalSize, target);
		order.add(transfer);
		active.put(target, transfer);
	}

	@Override
	public void transferred(S source, long transferred, T target) {
		E transfer = active.get(target);
		if (transfer != null) {
			transfer.transferred(source, transferred, target);
		}
	}

	@Override
	public synchronized void completed(S source, T target) {
		E transfer = active.remove(target);
		if (transfer != null) {
			order.remove(transfer);
			transfer.completed(source, target);
		}
	}

	@Override
	public synchronized void aborted(S source, T target, Exception error) {
		E transfer = active.remove(target);
		if (transfer != null) {
			order.remove(transfer);
			transfer.aborted(source, target, error);
		}
	}

	/**
	 * Creates a a new data transfer
	 *
	 * @param source
	 * @param totalSize
	 * @param target
	 *
	 * @return
	 */
	protected abstract E newDataTransfer(S source, long totalSize, T target);

	@Override
	public final boolean isStarted() {
		return true;
	}

	@Override
	public final boolean isRunning() {
		return !active.isEmpty();
	}

	@Override
	public final boolean isAborted() {
		return false;
	}

	/**
	 * Returns the number of active transfers currently maintained by this list
	 *
	 * @return the number of active data transfers
	 */
	public int size() {
		return active.size();
	}

	/**
	 * Returns a copy of the internal list of currently active transfers
	 *
	 * @return the active transfers
	 */
	public List<E> getActiveTransfers() {
		return new ArrayList<E>(order);
	}

	@Override
	public Iterator<E> iterator() {
		return order.iterator();
	}
}
