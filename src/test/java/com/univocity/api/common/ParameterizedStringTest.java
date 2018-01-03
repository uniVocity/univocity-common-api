/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.common;

import org.testng.annotations.*;

import java.util.*;

import static org.testng.Assert.*;

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
	public void parsingNewValues() {
		String pattern = "p1={p1} p2={p2}";
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
				"Multiple values ('1' and '3') found for parameter 'param1'\n" +
				"p1=1 p2=2 p1=3\n" +
				"             ^";
		try {
			string.parse(inputException);
		} catch (IllegalArgumentException ex) {
			assertEquals(ex.toString(), expectedExceptionMessage);
		}
	}

	@Test
	public void parseLargeString() {
		String pattern = "" +
				"a: {_a};" +
				"b: {_b};" +
				"c: {_c};" +
				"d: {_d};" +
				"e: {_e};" +
				"f: {_f};" +
				"g: {_g};" +
				"h: {_h}";

		String signature = "a: first;b: second;c: third;d: 8;e: 00:50:56:c0:00:01|00:50:56:c0:00:08|06:5c:89:92:11:01|42:6e:4c:4c:03:4f|6a:00:01:80:68:e0|6a:00:01:80:68:e1|f4:5c:89:92:11:01;f: blah;g: F1F9310F-6709-36CB-AB38-436A77CD2925|85AB6672-5E1E-440B-BAF7-AE9D1474644A|9E6BAB4A-AFE3-4541-B546-3E443135DE5F|B4B9B151-D89B-4D3F-9553-3A6452889640;h: N/A";

		ParameterizedString string = new ParameterizedString(pattern);
		Map<String, Object> result = string.parse(signature);

		assertEquals(result.get("_a"), "first");
		assertEquals(result.get("_b"), "second");
		assertEquals(result.get("_c"), "third");
		assertEquals(result.get("_d"), "8");
		assertEquals(result.get("_e"), "00:50:56:c0:00:01|00:50:56:c0:00:08|06:5c:89:92:11:01|42:6e:4c:4c:03:4f|6a:00:01:80:68:e0|6a:00:01:80:68:e1|f4:5c:89:92:11:01");
		assertEquals(result.get("_f"), "blah");
		assertEquals(result.get("_g"), "F1F9310F-6709-36CB-AB38-436A77CD2925|85AB6672-5E1E-440B-BAF7-AE9D1474644A|9E6BAB4A-AFE3-4541-B546-3E443135DE5F|B4B9B151-D89B-4D3F-9553-3A6452889640");
		assertEquals(result.get("_h"), "N/A");

		string.setDefaultValue("N/A");
		assertEquals(result.get("_h"), null);

	}

	@Test
	public void parseParameterAroundDelimiters() {
		ParameterizedString string = new ParameterizedString("{% if customer and customer.id == {CUSTOMER_ID} %} stuff");
		assertEquals(string.getParameters().size(), 1);
		assertEquals(string.getParameters().iterator().next(), "CUSTOMER_ID");

		string.set("CUSTOMER_ID", 1234);

		assertEquals(string.applyParameterValues(), "{% if customer and customer.id == 1234 %} stuff");

		string = new ParameterizedString("{% if customer and {{customer.id == {CUSTOMER_ID} %} }}stuff");
		assertEquals(string.getParameters().size(), 1);
		assertEquals(string.getParameters().iterator().next(), "CUSTOMER_ID");

		string.set("CUSTOMER_ID", 1234);

		assertEquals(string.applyParameterValues(), "{% if customer and {{customer.id == 1234 %} }}stuff");

	}

	@Test
	public void testSpecialDelimiters() {
		ParameterizedString s = new ParameterizedString("Hello {{ var }}", "{{ ", " }}");

		Iterator<String> it = s.getParameters().iterator();
		assertEquals(it.next(), "var");

		s.set("var", "world");
		assertEquals(s.applyParameterValues(), "Hello world");

		s = new ParameterizedString("Hello {{ var }}! {{ var2 }}", "{{ ", " }}");
		s.set("var", "world");
		s.set("var2", "Bye now");
		assertEquals(s.applyParameterValues(), "Hello world! Bye now");

		//again without spaces in open/close brackets
		s = new ParameterizedString("Hello {{ var }}", "{{", "}}");

		it = s.getParameters().iterator();
		assertEquals(it.next(), "var");

		s.set("var", "world");
		assertEquals(s.applyParameterValues(), "Hello world");

		s = new ParameterizedString("Hello {{ var }}! {{ var2 }}", "{{", "}}");
		s.set("var", "world");
		s.set("var2", "Bye now");
		assertEquals(s.applyParameterValues(), "Hello world! Bye now");
	}

	@Test
	public void testParameterWithFormat(){
		ParameterizedString s = new ParameterizedString("Valid until: { DATE , mmm dd, yyyy }\r\n");
		assertEquals(s.getParameters().size(), 1);

		assertEquals(s.getParameters().iterator().next(), "DATE");

		assertEquals(s.getFormat("DATE"), "mmm dd, yyyy");

		s = new ParameterizedString("Valid until: { DATE , mmm\n dd, yyyy }\r\n");
		assertEquals(s.getParameters().size(), 0);
	}

	@Test
	public void testHandlingOfInvalidStuff(){
		String in = "" +
				"\tfor (var i = 0; i < emails.length; i++) {\n" +
				"\t\t\t\tvar email = emails[i].trim();\n" +
				"\t\t\t\tif (!isEmailValid(email)) {\n" +
				"\t\t\t\t\tfield.setCustomValidity(\"Invalid e-mail address.\");\n" +
				"\t\t\t\t\treturn;\n" +
				"\t\t\t\t}\n" +
				"\t\t\t}";

		ParameterizedString s = new ParameterizedString(in);
		assertEquals(s.getParameters().size(), 0);

		s = new ParameterizedString("insertAtCursor(editor, '{{ ' + fieldName + ' }}');");
		assertEquals(s.getParameters().size(), 0);
	}
}
