package com.sahi.record;

import junit.framework.TestCase;

/**
 * User: nraman Date: Jun 3, 2005 Time: 12:33:33 AM
 */
public class SahiScriptFormatTest extends TestCase {
	SahiScriptFormat sahiScriptFormat = new SahiScriptFormat();

	public void testEscape() {
		assertEquals("aa\\$bb", sahiScriptFormat.escape("aa$bb"));
		assertEquals("aa\\r\\nbb", sahiScriptFormat.escape("aa\r\nbb"));
	}
	public void testSahiQuoteIfString() {
		assertEquals("123", sahiScriptFormat.sahiQuoteIfString("123"));
		assertEquals("\"abc\"", sahiScriptFormat.sahiQuoteIfString("abc"));
	}

	public void testGetAccessor() {
		assertEquals("_image(\"img_id\")", sahiScriptFormat.getAccessor(
				"document.images[0]", "img_id", "img"));
		assertEquals("_imageSubmitButton(\"img_id\")", sahiScriptFormat
				.getAccessor("", "img_id", "image"));
		assertEquals("_checkbox(\"cb1\")", sahiScriptFormat.getAccessor(
				"document.form1.cb1", "cb1", "checkbox"));
		assertEquals("_radio(\"r1\")", sahiScriptFormat.getAccessor(
				"document.form1.r1", "r1", "radio"));
		assertEquals("_link(\"Link\")", sahiScriptFormat.getAccessor(
				"document.links[12]", "Link", "link"));
		assertEquals("_textbox(\"tb1\")", sahiScriptFormat.getAccessor(
				"document.form1.tb1", "tb1", "text"));
		assertEquals("_cell(cell1)", sahiScriptFormat.getAccessor("", "cell1",
				"cell"));
		assertEquals("_select(\"sel1\")", sahiScriptFormat.getAccessor(
				"document.form1.sel1", "sel1", "select-one"));
		assertEquals("_textarea(\"ta1\")", sahiScriptFormat.getAccessor(
				"document.form1.ta1", "ta1", "textarea"));
		assertEquals("_byId(\"id1\")", sahiScriptFormat.getAccessor(
				"document.getElementById(\"id1\")", "\"id1\"", "byId"));
	}

	/*
	 * Insert into SahiScriptFormat to generate		
	 * System.out.println("assertEquals("+a(cmd)+", sahiScriptFormat.getScript("+a(event)+", "+a(jsAccessor)+", "+a(value)+", "+a(type)+", "+a(shortHand)+", "+a(popup)+"));");
	 * 	private String a(String s) {
	 * 		return s == null ? null : "\"" + s.replaceAll("\"", "\\\\\"") + "\"";
	 * }
	 * 
	 */
	public void testGetScript() {
		assertEquals("_setValue(_textbox(\"t1\"), \"some text\");", 
				sahiScriptFormat.getScript("setvalue", "top.document.forms['f1'].elements[\"t1\"]", "some text", "text", "t1", null));
		assertEquals("_click(_checkbox(\"c1\"));", 
				sahiScriptFormat.getScript("click", "top.document.forms['f1'].elements[\"c1\"][0]", "", "checkbox", "c1", null));
		assertEquals("_click(_radio(\"r1[1]\"));", 
				sahiScriptFormat.getScript("click", "top.document.forms['f1'].elements[\"r1\"][1]", "", "radio", "r1[1]", null));
		assertEquals("_setValue(_password(\"p1\"), \"mypwd\");", 
				sahiScriptFormat.getScript("setvalue", "top.document.forms['f1'].elements[\"p1\"]", "mypwd", "password", "p1", null));
		assertEquals("_setSelected(_select(\"s1\"), \"o2\");", 
				sahiScriptFormat.getScript("setselected", "top.document.getElementById('s1Id')", "o2", "select-one", "s1", null));
		assertEquals("_click(_button(\"button value\"));", 
				sahiScriptFormat.getScript("click", "top.document.getElementById('btnId')", "", "button", "button value", null));
		assertEquals("_click(_submit(\"Add\"));", 
				sahiScriptFormat.getScript("click", "top.document.getElementById('submitBtnId')", "", "submit", "Add", null));
		assertEquals("_click(_image(\"imageAlt1\"));", 
				sahiScriptFormat.getScript("click", "top.document.getElementById('imageId1')", "", "img", "imageAlt1", null));
		assertEquals("_click(_imageSubmitButton(\"Search\"));", 
				sahiScriptFormat.getScript("click", "top.document.getElementsByTagName('input')[19]", "", "image", "Search", null));
	}
	
	public void testGetScriptForAssert() {
		assertEquals("_assertNotNull(_checkbox(\"c1\"));\r\n_assertTrue(_checkbox(\"c1\").checked);", sahiScriptFormat.getScript("assert", "top.document.forms['f1'].elements[\"c1\"][0]", "true", "checkbox", "c1", null));
		assertEquals("_click(_checkbox(\"c1\"));", sahiScriptFormat.getScript("click", "top.document.forms['f1'].elements[\"c1\"][0]", "", "checkbox", "c1", null));
		assertEquals("_assertNotNull(_checkbox(\"c1\"));\r\n_assertNotTrue(_checkbox(\"c1\").checked);", sahiScriptFormat.getScript("assert", "top.document.forms['f1'].elements[\"c1\"][0]", "", "checkbox", "c1", null));
		assertEquals("_click(_radio(\"r1\"));", sahiScriptFormat.getScript("click", "top.document.forms['f1'].elements[\"r1\"][0]", "true", "radio", "r1", null));
		assertEquals("_assertNotNull(_radio(\"r1\"));\r\n_assertTrue(_radio(\"r1\").checked);", sahiScriptFormat.getScript("assert", "top.document.forms['f1'].elements[\"r1\"][0]", "true", "radio", "r1", null));
		assertEquals("_click(_radio(\"r1[1]\"));", sahiScriptFormat.getScript("click", "top.document.forms['f1'].elements[\"r1\"][1]", "true", "radio", "r1[1]", null));
		assertEquals("_assertNotNull(_radio(\"r1\"));\r\n_assertNotTrue(_radio(\"r1\").checked);", sahiScriptFormat.getScript("assert", "top.document.forms['f1'].elements[\"r1\"][0]", "", "radio", "r1", null));		
	}
}
