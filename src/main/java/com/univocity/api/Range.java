package com.univocity.api;

import java.lang.annotation.*;

/**
 * Defines a range of values that a method's parameters may take. Used for the GUI to define what values are selectable
 * by the user.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Range {

	/**
	 * Defines the minimum value of method's parameter.
	 *
	 * @return the smallest acceptable value for the parameter
	 */
	int min();

	/**
	 * Defines the maximum value of method's parameter.
	 *
	 * @return the largest acceptable value for the parameter
	 */
	int max();

	/**
	 * Defines the difference between the numbers of the range. For instance, setting the increment to 1 means the range
	 * will look like (min, min+1, min+2,...max). Setting the increment to 10 will look like (min, min+10, min+20,...max).
	 *
	 * Leaving it at the default will mean that the increment will be scaled based on the difference between the min
	 * max sizes
	 *
	 * @return the difference between numbers of the range
	 */
	int increment() default -1;
}
