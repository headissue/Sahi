package net.sf.sahi.nashorn;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.session.Status;
import org.junit.Before;
import org.junit.Test;


import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import static org.junit.Assert.*;

/**
 * Sahi - Web Automation and Test Tool
 * <p/>
 * Copyright  2006  V Narayan Raman
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class NashornScriptRunnerTest {

  @Before
  public void setup() {
    Configuration.init();
  }

  @Test
  public void testGetPopupNameFromStep() {
    NashornScriptRunner scriptRunner = new NashornScriptRunner("");
    assertEquals("abca", scriptRunner.getPopupNameFromStep("_sahi._popup('abca')._click()"));
    assertEquals("abca", scriptRunner.getPopupNameFromStep("_sahi._popup( 'abca')._click()"));
    assertEquals("abca", scriptRunner.getPopupNameFromStep("_sahi._popup('abca' )._click()"));
    assertEquals("abca", scriptRunner.getPopupNameFromStep("_sahi._popup ('abca')._click()"));
  }

  @Test
  public void testGetDomainFromStep() {
    NashornScriptRunner scriptRunner = new NashornScriptRunner("");
    assertEquals("a.example.com", scriptRunner.getDomainFromStep("_sahi._domain('a.example.com')._click()"));
    assertEquals("x.example.co.in", scriptRunner.getDomainFromStep("_sahi._domain( 'x.example.co.in')._click()"));
    assertEquals("a.example.com", scriptRunner.getDomainFromStep("_sahi._domain('a.example.com' )._click()"));
    assertEquals("a.example.com", scriptRunner.getDomainFromStep("_sahi._domain ('a.example.com')._click()"));
  }

  private String evaluate(String code) throws ScriptException{

    ScriptEngineManager scriptManager = new ScriptEngineManager();
    ScriptEngine nashornEngine = scriptManager.getEngineByName("nashorn");
    String lib = Configuration.getSahiJavascriptLib();
    NashornScriptRunner runner = new NashornScriptRunner(code);
    nashornEngine.put("NashornScriptRunner", runner);
    Object result;
    nashornEngine.eval(lib);
    result = nashornEngine.eval(code);
    if (result instanceof String) return (String) result;
   return ((ScriptObjectMirror) result).get("s").toString();
  }

  @Test
  public void testStubs() {
    try {
      check("_sahi.log('sadasd')");
      check("_sahi._cell('AA')");
      check("document.forms[0]");
      check("_sahi._cell('AA').parentNode.parentNode");
      check("_sahi._link('abcd').getElementsByTagName('DIV')[0]");
      check("_sahi._link('abcd').getElementsByTagName('DIV')[25]");
      check("_sahi._link('abcd').getElementsByTagName('DIV')[99]");
      check("_sahi._cell('AA').parentNode.childNodes[22].previousSibling");
      check("_sahi._cell('AA').document.forms[0].elements[11].value");
      check("_sahi._checkbox(0, _sahi._near(_sahi._spandiv(\"To: narayan.raman\")))");
      check("_sahi._textbox(0).value.substring(_sahi._textbox(0).value.indexOf('aa'), 12)");
      check("_sahi._link(/hi/)");
      check("_sahi._table('t1').rows[0].cells[1]");
    } catch (ScriptException e) {
      e.printStackTrace();
      fail();
    }
  }

  private void check(String s) throws ScriptException {
    assertEquals(s.replace('\'', '"'), evaluate(s));
  }

  @Test
  public void testAreSameShouldReturnFalseIfStringIsBlank() {
    NashornScriptRunner scriptRunner = new NashornScriptRunner("");
    assertFalse(scriptRunner.areSame("", "/.*/")); // blank should always return false
  }

  @Test
  public void testAreSame() {
    NashornScriptRunner scriptRunner = new NashornScriptRunner("");
//		assertTrue(scriptRunner.areSame("abcd", "/bc/"));
    assertTrue(scriptRunner.areSame("abcd", "/.*/"));
    assertTrue(scriptRunner.areSame("abcd", "abcd"));
    assertTrue(scriptRunner.areSame("1234", "/[\\d]*/"));
    assertTrue(scriptRunner.areSame("/abcd1234/", "/[/]abcd[\\d]*[/]/"));
    assertTrue(scriptRunner.areSame("ABCd", "/abcd/i"));
    assertTrue(scriptRunner.areSame("ABCd", "/bc/i"));
    assertTrue(scriptRunner.areSame("abcd", "/bc/"));
    assertFalse(scriptRunner.areSame("aBCd", "/bc/"));
    assertFalse(scriptRunner.areSame("abcd1234", "abcd"));
  }

  @Test
  public void testSahiException() {
    try {
      assertEquals("catched", evaluate("try { throw new SahiException('Step took too long' , 'debug') } catch(e) { 'catched'}"));
    } catch (ScriptException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testFailureIncrementsErrorCount() throws Exception {
    NashornScriptRunner scriptRunner = new NashornScriptRunner("");
    final int errorCount = scriptRunner.errorCount();
    scriptRunner.setStatus(Status.FAILURE);
    assertEquals(errorCount + 1, scriptRunner.errorCount());
  }

  @Test
  public void testErrorDoesNotIncrementErrorCount() throws Exception {
    NashornScriptRunner scriptRunner = new NashornScriptRunner("");
    final int errorCount = scriptRunner.errorCount();
    scriptRunner.setStatus(Status.ERROR);
    assertEquals(errorCount, scriptRunner.errorCount());
  }

  @Test
  public void testSetHasErrorIncrementsErrorCount() throws Exception {
    NashornScriptRunner scriptRunner = new NashornScriptRunner("");
    final int errorCount = scriptRunner.errorCount();
    scriptRunner.incrementErrors();
    assertEquals(errorCount + 1, scriptRunner.errorCount());
  }

  @Test
  public void getStackTrace() {
    Configuration.init();
    NashornScriptRunner scriptRunner = new NashornScriptRunner("");
    scriptRunner.initializeEngine();
    String result = null;
    try {
      scriptRunner.loadSahiLibary();
      scriptRunner.getEngine().put(ScriptEngine.FILENAME, "testFile");
      result = (String) scriptRunner.getEngine().eval("var call = function(){return _sahi._stackTrace()}; call" +
          "()");
    } catch (ScriptException e) {
     fail();
    }
    assertNotNull(result);
  }
}
