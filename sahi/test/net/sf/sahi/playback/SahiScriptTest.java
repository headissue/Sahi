package net.sf.sahi.playback;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class SahiScriptTest extends TestCase {
	private static final long serialVersionUID = -3933249717685341073L;
    TestScript testScript = new TestScript("scrName", null, "scrName");

	public void testGetInclude() {
		assertEquals("prof.sah", SahiScript
				.getInclude("/*asdad*/ _include(\"prof.sah\"); //asdasd\n"));
	}

	public void testGetIncludeSingleQuote() {
		assertEquals("prof.sah", SahiScript.getInclude("_include('prof.sah')"));
	}

	public void testModify() {
        assertEquals("sahiSchedule(\"sahi_assertEqual(sahi_table(\\\"aa\\\"))\", \"scrName&n=1\")\r\n",
                testScript.modify("_assertEqual(_table(\"aa\"))"));

		assertEquals("sahi_assertEqual(sahi_table(\"aa\"))\r\n", testScript
				.modify("__assertEqual(_table(\"aa\"))"));

		assertEquals("if(sahi_table(\"aa\"))\r\n", testScript.modify("if(_table(\"aa\"))"));

		assertEquals(
				"sahiSchedule(\"sahi_setGlobal(\\\"newFinanceTypeName\\\", \'sahiTestFT\'+sahi_random(10000))\", \"scrName&n=1\")\r\n",
				testScript
						.modify("_setGlobal(\"newFinanceTypeName\", \'sahiTestFT\'+_random(10000))"));

		assertEquals("var $n = sahi_getGlobal(\"nv\");\r\n", testScript
				.modify("var $n = _getGlobal(\"nv\");\r\n"));

		assertEquals("var $n = sahi_getGlobal(\"nv\");\r\n", testScript
				.modify("var $n = sahi_getGlobal(\"nv\");\r\n"));

		assertEquals("sahi_setGlobal(\"n\", \'aa\'+sahi_random(10000));\r\n", testScript
				.modify("sahi_setGlobal(\"n\", \'aa\'+_random(10000));"));

		assertEquals("sahi_textbox(\"username\").value=\"kk\";\r\n", testScript
				.modify("_textbox(\"username\").value=\"kk\";"));

		assertEquals("sahi_textbox(\"username\").value=\"kk\";\r\n", testScript
				.modify("__textbox(\"username\").value=\"kk\";"));
		assertEquals("sahiSchedule(\"sahi_call(fn1())\", \"scrName&n=1\")\r\n", testScript
				.modify("_call(fn1())"));

        assertEquals("sahiSchedule(\"sahi_click(\"+s_v($ar[$i[1][\"COL\"]])+\")\", \"scrName&n=1\")\r\n", testScript
				.modify("_click($ar[$i[1][\"COL\"]])"));

	}

	public void testKeywordsAsASubstringFails() {
		assertEquals(
				"sahiSchedule(\"sahi_setValue(sahi_textbox (\\\"form_loginname\\\"), \\\"narayanraman\\\");\", \"scrName&n=1\")\r\n",
				testScript
						.modify("_setValue(_textbox (\"form_loginname\"), \"narayanraman\");"));
	}

	public void testModifyFunctionNames() {
		assertEquals("sahi_setGlobal(", TestScript.modifyFunctionNames("_setGlobal("));
		assertEquals("_insert  (", TestScript.modifyFunctionNames("_insert  ("));
		assertEquals("sahi_setValue (", TestScript.modifyFunctionNames("__setValue ("));
	}

	public void testGetRegExp() {
		ArrayList keywords = new ArrayList();
		keywords.add("_accessor");
		keywords.add("_alert");
		assertEquals("sahi_?(_accessor|_alert)(\\s*\\()", TestScript.getRegExp(true, keywords));
	}

	public void testGetActionRegExp() {
		ArrayList keywords = new ArrayList();
		keywords.add("_alert");
		keywords.add("_assertEqual");
		assertEquals("^(?:_alert|_assertEqual)\\s*\\(.*", TestScript.getActionRegExp(keywords));
	}

	public void testLineStartsWithActionKeyword() {
		assertTrue(TestScript.lineStartsWithActionKeyword("_alert()"));
	}

	public void xtestEfficiency() {
		long start = System.currentTimeMillis();
		TestScript.lineStartsWithActionKeyword("_alert()");

		for (int i = 0; i < 10000; i++) {
			TestScript.lineStartsWithActionKeyword("_alert()");
		}
		long t1 = System.currentTimeMillis() - start;

		start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			TestScript.lineStartsWithActionKeyword("_alert()");
		}
		long t2 = System.currentTimeMillis() - start;
		System.out.println(t1 + "\n" + t2);
	}

	public void testRegEx() {
		assertEquals("sahi_alert", "__alert".replaceAll("_?(_alert)", "sahi$1"));
		assertEquals("sahi_alert", "_alert".replaceAll("_?(_alert)", "sahi$1"));
	}

	class TestScript extends SahiScript {
        public TestScript() {
            super(null, new ArrayList(), null);
        }

        public TestScript(String fileName, ArrayList parents, String scriptName) {
            super(fileName, parents, scriptName);
        }

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
		assertEquals("sahi_log (form_login", "_log (form_login".replaceAll(
				"_?(_log|_textbox)(\\s*\\()", "sahi$1$2"));
		assertEquals("sahi_log(form_login", "_log(form_login".replaceAll(
				"_?(_log|_textbox)(\\s*\\()", "sahi$1$2"));
		assertTrue("_assertEqual(".matches("^(_assertEqual)\\s*\\("));
		assertTrue("_assertEqual           (".matches("^(_assertEqual)\\s*\\("));
	}
	
	public void testGetActionKeywords() {
		List keywords = SahiScript.getActionKeyWords();
		assertTrue(keywords.contains("_alert"));
		assertTrue(keywords.contains("_assertEqual"));
		assertTrue(keywords.contains("_assertNotEqual"));
		assertTrue(keywords.contains("_assertNotNull"));
		assertTrue(keywords.contains("_assertNull"));
		assertTrue(keywords.contains("_assertTrue"));
		assertTrue(keywords.contains("_assertNotTrue"));
		assertTrue(keywords.contains("_click"));
		assertTrue(keywords.contains("_clickLinkByAccessor"));
		assertTrue(keywords.contains("_dragDrop"));
		assertTrue(keywords.contains("_getCellText"));
		assertTrue(keywords.contains("_getSelectedText"));
		assertTrue(keywords.contains("_setSelected"));
		assertTrue(keywords.contains("_setValue"));
		assertTrue(keywords.contains("_simulateEvent"));
		assertTrue(keywords.contains("_call"));
		assertTrue(keywords.contains("_eval"));
		assertTrue(keywords.contains("_setGlobal"));
		assertTrue(keywords.contains("_wait"));
		assertTrue(keywords.contains("_popup"));
		assertTrue(keywords.contains("_highlight"));
		assertTrue(keywords.contains("_log"));
		assertTrue(keywords.contains("_navigateTo"));
	}

	public void testGetKeywords() {
		List keywords = SahiScript.getKeyWords();
		assertTrue(keywords.contains("_accessor"));
		assertTrue(keywords.contains("_alert"));
		assertTrue(keywords.contains("_assertEqual"));
		assertTrue(keywords.contains("_assertNotEqual"));
		assertTrue(keywords.contains("_assertNotNull"));
		assertTrue(keywords.contains("_assertNull"));
		assertTrue(keywords.contains("_assertTrue"));
		assertTrue(keywords.contains("_assertNotTrue"));
		assertTrue(keywords.contains("_button"));
		assertTrue(keywords.contains("_check"));
		assertTrue(keywords.contains("_checkbox"));
		assertTrue(keywords.contains("_click"));
		assertTrue(keywords.contains("_clickLinkByAccessor"));
		assertTrue(keywords.contains("_dragDrop"));
		assertTrue(keywords.contains("_getCellText"));
		assertTrue(keywords.contains("_getSelectedText"));
		assertTrue(keywords.contains("_image"));
		assertTrue(keywords.contains("_imageSubmitButton"));
		assertTrue(keywords.contains("_link"));
		assertTrue(keywords.contains("_password"));
		assertTrue(keywords.contains("_radio"));
		assertTrue(keywords.contains("_select"));
		assertTrue(keywords.contains("_setSelected"));
		assertTrue(keywords.contains("_setValue"));
		assertTrue(keywords.contains("_simulateEvent"));
		assertTrue(keywords.contains("_submit"));
		assertTrue(keywords.contains("_textarea"));
		assertTrue(keywords.contains("_textbox"));
		assertTrue(keywords.contains("_event"));
		assertTrue(keywords.contains("_call"));
		assertTrue(keywords.contains("_eval"));
		assertTrue(keywords.contains("_setGlobal"));
		assertTrue(keywords.contains("_getGlobal"));
		assertTrue(keywords.contains("_wait"));
		assertTrue(keywords.contains("_random"));
		assertTrue(keywords.contains("_savedRandom"));
		assertTrue(keywords.contains("_cell"));
		assertTrue(keywords.contains("_table"));
		assertTrue(keywords.contains("_containsText"));
		assertTrue(keywords.contains("_containsHTML"));
		assertTrue(keywords.contains("_popup"));
		assertTrue(keywords.contains("_byId"));
		assertTrue(keywords.contains("_highlight"));
		assertTrue(keywords.contains("_log"));
		assertTrue(keywords.contains("_navigateTo"));
	}
}
