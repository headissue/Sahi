package com.sahi.util;

import junit.framework.TestCase;

public class UtilsTest extends TestCase {
	public UtilsTest(String name) {
		super(name);
	}

	public void testConcatPaths (){
		assertEquals("a/b", Utils.concatPaths("a", "b"));
		assertEquals("a/b", Utils.concatPaths("a/", "b"));
		assertEquals("a/b", Utils.concatPaths("a/", "/b"));
		assertEquals("a/b", Utils.concatPaths("a", "/b"));
	}
}
