/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

import org.testng.*;
import org.testng.annotations.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ParameterizedStringTest {
	@Test
	public void testParameters() {
		//checking single parameter
		ParameterizedString str = new ParameterizedString("www.google.com/{search}");
		Assert.assertTrue(str.getParameters().contains(String.valueOf("search")));
		//assertEquals(map.get("search"), new int[] {15,22});

		//checking multiple parameters
		str = new ParameterizedString("www.google.com/{search}/{testing}");
		Assert.assertTrue(str.getParameters().contains(String.valueOf("search")));
		//assertEquals(map.get("search"), new int[] {15,22});
		Assert.assertTrue(str.getParameters().contains(String.valueOf("testing")));
		//assertEquals(map.get("testing"), new int[] {24,32});

		//checking no parameters
		str = new ParameterizedString("www.google.com");
		Assert.assertTrue(str.getParameters().isEmpty());

		//checking no parameters with open brackets
		str = new ParameterizedString("www.google.com/{incomplete{");
		Assert.assertTrue(str.getParameters().isEmpty());

		//check parameters next to each other
		str = new ParameterizedString("www.google.com/{one}{two}");
		Assert.assertTrue(str.getParameters().contains(String.valueOf("one")));
		Assert.assertTrue(str.getParameters().contains(String.valueOf("two")));

		//check parameters with different brackets
		str = new ParameterizedString("www.google.com/(normal)/{curly}", "(", ")");
		Assert.assertTrue(str.getParameters().contains(String.valueOf("normal")));
		Assert.assertFalse(str.getParameters().contains(String.valueOf("curly")));
	}

	@Test
	public void testSetParameter() {
		//checking setting parameters 'backwards'
		ParameterizedString str = new ParameterizedString("www.google.com/{one}{two}");
		str.set("two", "hello");
		Assert.assertEquals(str.applyParameterValues(), "www.google.com/{one}hello");
		str.set("one", "bla");
		Assert.assertEquals(str.applyParameterValues(), "www.google.com/blahello");
		Assert.assertEquals(str.toString(), "www.google.com/{one}{two}");

		//testing setting invalid parameter
		str = new ParameterizedString("www.google.com/{one}{two}");
		try {
			str.set("fjbewsjkfbe", "error");
			Assert.fail();
		} catch (IllegalArgumentException e) {
			//ok then
		}
		//checking setting parameters 'forwards'
		str.set("one", "keyboard");
		Assert.assertEquals(str.applyParameterValues(), "www.google.com/keyboard{two}");
		str.set("two", "bla");
		Assert.assertEquals(str.applyParameterValues(), "www.google.com/keyboardbla");

		//testing multiple instances of same parameter
		str = new ParameterizedString("www.google.com/{one}{two}{one}");
		str.set("one", "pen");
		str.set("two", "hello");
		Assert.assertEquals(str.applyParameterValues(), "www.google.com/penhellopen");
		str.set("one", "mouse");
		Assert.assertEquals(str.applyParameterValues(), "www.google.com/mousehellomouse");


		str = new ParameterizedString("www.google.com/{one}//{two}//{one}");
		str.set("one", "pen");
		str.set("two", "hello");
		Assert.assertEquals(str.applyParameterValues(), "www.google.com/pen//hello//pen");

		//testing parameter at start
		str = new ParameterizedString("{one}www.google.com/{one}//{two}//{one}");
		str.set("one", "pen");
		str.set("two", "hello");
		Assert.assertEquals(str.applyParameterValues(), "penwww.google.com/pen//hello//pen");

		//testing clearing parameter values
		str.clearValues();
		Assert.assertEquals(str.applyParameterValues(), "{one}www.google.com/{one}//{two}//{one}");


		//testing 2nd change of parameter
		str = new ParameterizedString("www.google.com/{one}{two}");
		str.set("one", "firstChange");
		Assert.assertEquals(str.applyParameterValues(), "www.google.com/firstChange{two}");
		str.set("one", "secondChange");
		Assert.assertEquals(str.applyParameterValues(), "www.google.com/secondChange{two}");

		//testing setting parameter to null
		str = new ParameterizedString("www.google.com/{one}{two}");
		str.set("one", null);
		Assert.assertEquals(str.applyParameterValues(), "www.google.com/{one}{two}");

		//testing setting parameter to int
		str.set("one", 27);
		Assert.assertEquals(str.applyParameterValues(), "www.google.com/27{two}");

	}
}
