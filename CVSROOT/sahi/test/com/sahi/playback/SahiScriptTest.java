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
				"sahiAdd(\"sahi_assertEqual(sahi_table(\\\"aa\\\"))\", \"null#1\")\r\n",
				new TestScript().modify("_assertEqual(_table(\"aa\"))"));
			
		assertEquals(
				"sahi_assertEqual(sahi_table(\"aa\"))\r\n",
				new TestScript().modify("__assertEqual(_table(\"aa\"))"));
			
		assertEquals("if(sahi_table(\"aa\"))\r\n", new TestScript()
				.modify("if(_table(\"aa\"))"));
		
		assertEquals(
			"sahiAdd(\"sahi_setGlobal(\\\"newFinanceTypeName\\\", \'sahiTestFT\'+sahi_random(10000))\", \"null#1\")\r\n", 
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
	
		assertEquals(
				"sahi_textbox(\"username\").value=\"kk\";\r\n", 
				new TestScript()
						.modify("_textbox(\"username\").value=\"kk\";"));

		assertEquals(
				"sahi_textbox(\"username\").value=\"kk\";\r\n", 
				new TestScript()
						.modify("__textbox(\"username\").value=\"kk\";"));
		assertEquals(
				"sahiAdd(\"sahi_call(fn1())\", \"null#1\")\r\n", 
				new TestScript()
						.modify("_call(fn1())"));

	}
	
	public void testKeywordsAsASubstringFails() {
		assertEquals(
		"sahiAdd(\"sahi_setValue(sahi_textbox (\\\"form_loginname\\\"), \\\"narayanraman\\\");\", \"null#1\")\r\n", 
		new TestScript()
				.modify("_setValue(_textbox (\"form_loginname\"), \"narayanraman\");"));		
	}

	public void testModifyFunctionNames() {
		assertEquals("sahi_setGlobal(", TestScript
				.modifyFunctionNames("_setGlobal("));
		assertEquals("_insert  (", TestScript
				.modifyFunctionNames("_insert  ("));
		assertEquals("sahi_setValue (", TestScript
				.modifyFunctionNames("__setValue ("));
	}
	
	public void testGetRegExp() {
		assertEquals("sahi_?(_accessor|_alert|_assertEqual|" +
				"_assertNotEqual|_assertNotNull|_assertNull|_assertTrue|" +
				"_assertNotTrue|_button|_check|_checkbox|_click|" +
				"_clickLinkByAccessor|_getCellText|_getSelectedText|" +
				"_image|_imageSubmitButton|_link|_password|_radio|" +
				"_select|_setSelected|_setValue|_simulateEvent|_submit|" +
				"_textarea|_textbox|_event|_call|_eval|_setGlobal|_getGlobal|" +
				"_wait|_random|_savedRandom|_cell|_table|_containsText|" +
				"_containsHTML|_popup|_byId|_highlight|_log|_navigateTo)(\\s*\\()", TestScript.getRegExp(true));
		
		assertEquals("_?(_accessor|_alert|_assertEqual|" +
				"_assertNotEqual|_assertNotNull|_assertNull|_assertTrue|" +
				"_assertNotTrue|_button|_check|_checkbox|_click|" +
				"_clickLinkByAccessor|_getCellText|_getSelectedText|" +
				"_image|_imageSubmitButton|_link|_password|_radio|" +
				"_select|_setSelected|_setValue|_simulateEvent|_submit|" +
				"_textarea|_textbox|_event|_call|_eval|_setGlobal|_getGlobal|" +
				"_wait|_random|_savedRandom|_cell|_table|_containsText|" +
				"_containsHTML|_popup|_byId|_highlight|_log|_navigateTo)(\\s*\\()", TestScript.getRegExp(false));
	}
	
	public void testGetActionRegExp() {
		assertEquals("^(?:_alert|_assertEqual|_assertNotEqual|" +
				"_assertNotNull|_assertNull|_assertTrue|" +
				"_assertNotTrue|_click|_clickLinkByAccessor|" +
				"_getCellText|_getSelectedText|_setSelected|" +
				"_setValue|_simulateEvent|_submit|_call|_eval|_setGlobal|" +
				"_wait|_popup|_highlight|_log|_navigateTo)\\s*\\(.*",
				TestScript.getActionRegExp());
	}
	
	public void testLineStartsWithActionKeyword() {
		assertTrue(TestScript.lineStartsWithActionKeyword("_alert()"));
	}
	
	
	public void xtestEfficiency() {
		long start = System.currentTimeMillis();
		TestScript.lineStartsWithActionKeyword("_alert()");
		
		for (int i=0; i<10000; i++) {
			TestScript.lineStartsWithActionKeyword("_alert()");
		}
		long t1 = System.currentTimeMillis() - start;
		
		start = System.currentTimeMillis();
		for (int i=0; i<10000; i++) {
			TestScript.lineStartsWithActionKeyword("_alert()");
		}
		long t2 = System.currentTimeMillis() - start;
		System.out.println(t1+"\n"+t2);
	}
	
	public void testRegEx() {
		assertEquals("sahi_alert", "__alert".replaceAll("_?(_alert)", "sahi$1"));
		assertEquals("sahi_alert", "_alert".replaceAll("_?(_alert)", "sahi$1"));
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
	
	public void testBrackets() {
		assertEquals("axx", "a((".replaceAll("\\(", "x"));
		assertEquals("sahi_log (form_login", "_log (form_login".replaceAll("_?(_log|_textbox)(\\s*\\()", "sahi$1$2"));
		assertEquals("sahi_log(form_login", "_log(form_login".replaceAll("_?(_log|_textbox)(\\s*\\()", "sahi$1$2"));
		assertTrue("_assertEqual(".matches("^(_assertEqual)\\s*\\("));
		assertTrue("_assertEqual           (".matches("^(_assertEqual)\\s*\\("));
	}
}
