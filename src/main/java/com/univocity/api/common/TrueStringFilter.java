package com.univocity.api.common;

/**
 * Constant {@link StringFilter} that accepts anything
 */
public class TrueStringFilter implements StringFilter {

	public static final TrueStringFilter TRUE = new TrueStringFilter();

	protected TrueStringFilter() {
	}

	@Override
	public boolean accept(String str) {
		return true;
	}
}
