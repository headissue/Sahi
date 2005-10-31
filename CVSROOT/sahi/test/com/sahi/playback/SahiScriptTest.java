package com.sahi.playback;

import java.util.ArrayList;
import junit.framework.TestCase;

public class SahiScriptTest extends TestCase {
	private static final long serialVersionUID = -3933249717685341073L;

	public void testGetInclude() {
		assertEquals("prof.sah", SahiScript
				.getInclude("/*asdad*/ _include(\"prof.sah\"); //asdasd\n"));
	}

	public void testGetIncludeSingleQuote() {
		assertEquals("prof.sah", SahiScript.getInclude("_include('prof.sah')"));
	}

	public void testModify() {
		assertEquals(
			"sahiAdd(\"sahi_assertEquals(sahi_table(\\\"aa\\\"))\", \"null : 1\")\r\n",
			new TestScript().modify("_assertEquals(_table(\"aa\"))"));
		assertEquals("if(sahi_table(\"aa\"))\r\n", new TestScript()
				.modify("if(_table(\"aa\"))"));
		assertEquals(
			"sahiAdd(\"sahi_setGlobal(\\\"newFinanceTypeName\\\", \'sahiTestFT\'+sahi_random(10000))\", \"null : 1\")\r\n", 
			new TestScript()
			.modify("_setGlobal(\"newFinanceTypeName\", \'sahiTestFT\'+_random(10000))"));
		assertEquals(
			"var $n = sahi_getGlobal(\"nv\");\r\n", 
			new TestScript()
			.modify("var $n = _getGlobal(\"nv\");\r\n"));
		assertEquals(
			"var $n = sahi_getGlobal(\"nv\");\r\n", 
			new TestScript()
			.modify("var $n = sahi_getGlobal(\"nv\");\r\n"));
		assertEquals(
			"sahi_setGlobal(\"n\", \'aa\'+sahi_random(10000));\r\n", 
			new TestScript()
					.modify("sahi_setGlobal(\"n\", \'aa\'+_random(10000));"));
	}

	class TestScript extends SahiScript {
		String getFQN(String include) {
			return null;
		}

		SahiScript getNewInstance(String scriptName, ArrayList parentScriptName) {
			return null;
		}

		protected void loadScript(String url) {
		}
	}
}
