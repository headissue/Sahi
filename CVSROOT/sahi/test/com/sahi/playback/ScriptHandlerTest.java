/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 10:19:59 PM
 */
package com.sahi.playback;

import junit.framework.TestCase;
import com.sahi.util.Utils;

public class ScriptHandlerTest extends TestCase {
	private static final long serialVersionUID = 6341354901708835100L;
	private SahiScript script;

    protected void setUp() {
        script = new MockFileScript("fileName");
    }

    public void testModify() {
        assertEquals("sahiAdd(\"sahi_setValue ( elements['username'] , 'test'+\"+s_v($ix)+\" )\", \"null : 1\")\n", script.modify("_setValue ( elements['username'] , 'test'+$ix )"));
    }

    public void testSeparateVariables() {
        assertEquals("aaa \"+s_v($ix)+\" bbb", script.separateVariables("aaa $ix bbb"));
    }

    public void testObject(){
        assertEquals("aaa \"+s_v($i.x)+\" bbb", script.separateVariables("aaa $i.x bbb"));
//        assertEquals("aaa \"+s_v($i.fn())+\" bbb", scriptHandler.separateVariables("aaa $i.fn() bbb"));
    }

    public void testEscape(){
        assertEquals("aaa \\\" bbb", Utils.escapeDoubleQuotes("aaa \" bbb"));
    }

    public void testForUnderstanding(){
        assertFalse(Character.isJavaIdentifierPart('.'));
        assertFalse(Character.isUnicodeIdentifierPart('.'));
    }

    public void testModifyFunctionNames(){
//        assertEquals("sahi_setValue ( sahi_textbox('username') , 'test'+$ix )", script.modifyFunctionNames("_setValue ( _textbox('username') , 'test'+$ix )"));
//        assertEquals("sahi_setValue(sahi_textbox('username') , 'test'+$ix )", script.modifyFunctionNames("_setValue(_textbox('username') , 'test'+$ix )"));
        assertEquals("sahi_click(sahi_image(\"Link Quote Application \" + sahi_getCellText(sahi_accessor(\"top.content.creditFrameContent.document.getElementById('tblRecentlyAccessedQuotes').rows[3].cells[1]\"))));", script.modifyFunctionNames("_click(_image(\"Link Quote Application \" + _getCellText(_accessor(\"top.content.creditFrameContent.document.getElementById('tblRecentlyAccessedQuotes').rows[3].cells[1]\"))));"));
    }

    public void testStripSahiFromFunctionNames(){
        assertEquals("_setValue ( _textbox('username') , 'test'+$ix )", script.stripSahiFromFunctionNames( "sahi_setValue ( sahi_textbox('username') , 'test'+$ix )"  ));
        assertEquals("_setValue(_textbox('username') , 'test'+$ix )", script.stripSahiFromFunctionNames( "sahi_setValue(sahi_textbox('username') , 'test'+$ix )"));
    }
	private class MockFileScript extends FileScript{
		public MockFileScript(String fileName) {
			super(fileName);
		}

		protected void loadScript(String fileName) {}
	}
}

