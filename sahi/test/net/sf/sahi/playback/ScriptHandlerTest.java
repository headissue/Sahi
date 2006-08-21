/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 10:19:59 PM
 */
package net.sf.sahi.playback;

import junit.framework.TestCase;
import net.sf.sahi.util.Utils;

public class ScriptHandlerTest extends TestCase {
	private static final long serialVersionUID = 6341354901708835100L;
	private SahiScript script;

    protected void setUp() {
        script = new MockFileScript("fileName");
    }

    public void testModify() {
        assertEquals("sahiSchedule(\"sahi_setValue ( elements['username'] , 'test'+\"+s_v($ix)+\" )\", \"fileName&n=1\")\r\n", script.modify("_setValue ( elements['username'] , 'test'+$ix )"));
    }

    public void testSeparateVariables(){
        assertEquals("_click(\"+s_v($ix)+\")", script.separateVariables("_click($ix)"));
        assertEquals("aaa \"+s_v($ix)+\" bbb", script.separateVariables("aaa $ix bbb"));
        assertEquals("aaa \"+s_v($i.x)+\" bbb", script.separateVariables("aaa $i.x bbb"));
        assertEquals("aaa \"+s_v($i.fn())+\" bbb", script.separateVariables("aaa $i.fn() bbb"));
        assertEquals("aaa \"+s_v($i[1])+\" bbb", script.separateVariables("aaa $i[1] bbb"));
        assertEquals("aaa \"+s_v($i[1].a())+\" bbb", script.separateVariables("aaa $i[1].a() bbb"));
        assertEquals("aaa \"+s_v($i[1][\"COL\"])+\" bbb", script.separateVariables("aaa $i[1][\"COL\"] bbb"));
        assertEquals("aaa \"+s_v($i[1]['COL'])+\" bbb", script.separateVariables("aaa $i[1]['COL'] bbb"));
        assertEquals("_click(_img(\"+s_v($i[1]['COL'])+\")", script.separateVariables("_click(_img($i[1]['COL'])"));
        assertEquals("_click(\"+s_v($ar[$ix])+\")", script.separateVariables("_click($ar[$ix])"));
        assertEquals("_click(\"+s_v($ar[$i[1]['COL']])+\")", script.separateVariables("_click($ar[$i[1]['COL']])"));
    }

    public void testEscape(){
    	assertEquals("\\\\", "\\".replaceAll("\\\\", "\\\\\\\\"));
        assertEquals("aaa \\\" bbb", Utils.escapeDoubleQuotesAndBackSlashes("aaa \" bbb"));
        assertEquals("aaa \\\\\\\" bbb", Utils.escapeDoubleQuotesAndBackSlashes("aaa \\\" bbb"));
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

