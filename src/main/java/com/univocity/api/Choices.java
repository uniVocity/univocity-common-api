package com.univocity.api;

import java.lang.annotation.*;

/**
 * Provides choices of what a parameter's value can be. Used by the GUI to show specific values to the user.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Choices {

	/**
	 * Defines a list of choices that will be shown to the user.
	 *
	 * @return choices selectable by user
	 */
	String[] choices() default "";

	/**
	 * Choices will be loaded from the specified text file. Each line is displayed as a choice.
	 *
	 * @return the file where the choices will be loaded from
	 */
	String file() default "";

	/**
	 * Defines the initial selected choice. Will only be used if the defaultValue has been loaded in via file or array.
	 *
	 * @return the choice that is initially selected
	 */
	String defaultValue() default "";
}
