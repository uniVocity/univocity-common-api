/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

import static org.testng.Assert.*;

import org.testng.annotations.*;


/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ParameterizedStringTest {
	@Test
	public void testParameters() {
		//checking single parameter
		ParameterizedString str = new ParameterizedString("www.google.com/{search}");
		assertTrue(str.getParameters().contains(String.valueOf("search")));
		//assertEquals(map.get("search"), new int[] {15,22});

		//checking multiple parameters
		str = new ParameterizedString("www.google.com/{search}/{testing}");
		assertTrue(str.getParameters().contains(String.valueOf("search")));
		//assertEquals(map.get("search"), new int[] {15,22});
		assertTrue(str.getParameters().contains(String.valueOf("testing")));
		//assertEquals(map.get("testing"), new int[] {24,32});

		//checking no parameters
		str = new ParameterizedString("www.google.com");
		assertTrue(str.getParameters().isEmpty());

		//checking no parameters with open brackets
		str = new ParameterizedString("www.google.com/{incomplete{");
		assertTrue(str.getParameters().isEmpty());

		//check parameters with different brackets
		str = new ParameterizedString("www.google.com/(normal)/{curly}", "(", ")");
		assertTrue(str.getParameters().contains(String.valueOf("normal")));
		assertFalse(str.getParameters().contains(String.valueOf("curly")));
	}

	@Test
	public void testSetParameter() {
		//checking setting parameters 'backwards'
		ParameterizedString str = new ParameterizedString("www.google.com/{one}/{two}");
		str.set("two", "hello");
		assertEquals(str.applyParameterValues(), "www.google.com/{one}/hello");
		str.set("one", "bla");
		assertEquals(str.applyParameterValues(), "www.google.com/bla/hello");
		assertEquals(str.toString(), "www.google.com/{one}/{two}");

		//testing setting invalid parameter
		str = new ParameterizedString("www.google.com/{one}/{two}");
		try {
			str.set("fjbewsjkfbe", "error");
			fail();
		} catch (IllegalArgumentException e) {
			//ok then
		}
		//checking setting parameters 'forwards'
		str.set("one", "keyboard");
		assertEquals(str.applyParameterValues(), "www.google.com/keyboard/{two}");
		str.set("two", "bla");
		assertEquals(str.applyParameterValues(), "www.google.com/keyboard/bla");

		//testing multiple instances of same parameter
		str = new ParameterizedString("www.google.com/{one}/{two}/{one}");
		str.set("one", "pen");
		str.set("two", "hello");
		assertEquals(str.applyParameterValues(), "www.google.com/pen/hello/pen");
		str.set("one", "mouse");
		assertEquals(str.applyParameterValues(), "www.google.com/mouse/hello/mouse");


		str = new ParameterizedString("www.google.com/{one}//{two}//{one}");
		str.set("one", "pen");
		str.set("two", "hello");
		assertEquals(str.applyParameterValues(), "www.google.com/pen//hello//pen");

		//testing parameter at start
		str = new ParameterizedString("{one}-www.google.com/{one}//{two}//{one}");
		str.set("one", "pen");
		str.set("two", "hello");
		assertEquals(str.applyParameterValues(), "pen-www.google.com/pen//hello//pen");

		//testing clearing parameter values
		str.clearValues();
		assertEquals(str.applyParameterValues(), "{one}-www.google.com/{one}//{two}//{one}");


		//testing 2nd change of parameter
		str = new ParameterizedString("www.google.com/{one}/{two}");
		str.set("one", "firstChange");
		assertEquals(str.applyParameterValues(), "www.google.com/firstChange/{two}");
		str.set("one", "secondChange");
		assertEquals(str.applyParameterValues(), "www.google.com/secondChange/{two}");

		//testing setting parameter to null
		str = new ParameterizedString("www.google.com/{one}/{two}");
		str.set("one", null);
		assertEquals(str.applyParameterValues(), "www.google.com/{one}/{two}");

		//testing setting parameter to int
		str.set("one", 27);
		assertEquals(str.applyParameterValues(), "www.google.com/27/{two}");

	}

	@Test
	public void testParseString() {
		String pattern = " == Something == \nLicense type: {LIC}\nRegistered to: {REG}\nExpiration date: {EXP}\nLicense key: {KEY}--endOfFile--";
		ParameterizedString string = new ParameterizedString(pattern);

		String expected = " == Something == \nLicense type: Evaluation\nRegistered to: My Crow Soft\nExpiration date: 12-DEC-2013\nLicense key: 234jhwf23--endOfFile--";
		string.parse(expected);

		assertEquals(string.get("LIC"), "Evaluation");
		assertEquals(string.get("REG"), "My Crow Soft");
		assertEquals(string.get("EXP"), "12-DEC-2013");
		assertEquals(string.get("KEY"), "234jhwf23");
	}

	@Test
	public void testParseStringParameterAtStart() {
		String pattern = "{startParam} non-parameter text {middleParam} {endParam}";

		String input = "BeginningOfTheInput non-parameter text middleOfTheInput endOfTheInput";

		ParameterizedString string = new ParameterizedString(pattern);
		string.parse(input);

		assertEquals(string.get("startParam"), "BeginningOfTheInput");
		assertEquals(string.get("middleParam"), "middleOfTheInput");
		assertEquals(string.get("endParam"), "endOfTheInput");
	}

	@Test
	public void testParsePath() {
		String pattern = "{rootDir}/tmp/{parentDir}/{fileName}";
		String input = "/home/user/tmp/testDirectory/testFile.txt";

		ParameterizedString string = new ParameterizedString(pattern);
		string.parse(input);

		assertEquals(string.get("rootDir"), "/home/user");
		assertEquals(string.get("parentDir"), "testDirectory");
		assertEquals(string.get("fileName"), "testFile.txt");
	}

	@Test
	public void testSingleParameter() {
		String pattern = "{param}";
		String input = "This is an input";

		ParameterizedString string = new ParameterizedString(pattern);
		string.parse(input);

		assertEquals(string.getParameterValues().toString(), "{param=This is an input}");

		string.set("param", "a different input");
		assertEquals(string.applyParameterValues(), "a different input");
	}

	@Test
	public void parsingNewValues(){
		String pattern  = "p1={p1} p2={p2}";
		String input1 = "p1=1 p2=2";
		String input2 = "p1=2 p2=1";

		ParameterizedString string = new ParameterizedString(pattern);
		string.parse(input1);
		assertEquals(string.getParameterValues().toString(), "{p1=1, p2=2}");


		string.parse(input2);
		assertEquals(string.getParameterValues().toString(), "{p1=2, p2=1}");
	}

	@Test
	public void testNoParameters() {
		String pattern = "There is no parameters";
		String input = "input";

		ParameterizedString string = new ParameterizedString(pattern);
		string.parse(input);

		assertEquals(string.getParameters().size(), 0);
		assertEquals(string.getParameterValues().size(), 0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testAdjacentParameters() {
		String pattern = "One parameter {A} two parameters together {X}{Y} should produce an exception";
		ParameterizedString string = new ParameterizedString(pattern);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNonPatternMatchInInput() {
		String pattern = "An example pattern {param1} {param2}";
		String input = "An example non-pattern 1 2 that should throw an exception";

		ParameterizedString string = new ParameterizedString(pattern);
		string.parse(input);
	}

	@Test
	public void testParamInPatternMultiple() {
		String pattern = "p1={param1} p2={param2} p1={param1}";

		ParameterizedString string = new ParameterizedString(pattern);

		String inputWorking = "p1=1 p2=2 p1=1";
		string.parse(inputWorking);

		assertEquals(string.getParameterValues().toString(), "{param1=1, param2=2}");

		String inputException = "p1=1 p2=2 p1=3";

		String expectedExceptionMessage = "java.lang.IllegalArgumentException: " +
				"Duplicate value found for parameter 'param1'\n" +
				"p1=1 p2=2 p1=3\n" +
				"             ^";
		try {
			string.parse(inputException);
		} catch (IllegalArgumentException ex) {
			assertEquals(ex.toString(), expectedExceptionMessage);
		}
	}
}
