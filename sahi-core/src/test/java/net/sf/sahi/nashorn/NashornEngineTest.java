package net.sf.sahi.nashorn;

import org.junit.Test;
import static org.junit.Assert.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.List;

public class NashornEngineTest {

  @Test
  public void testBinding() throws Exception {
    ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine nashornEngine = mgr.getEngineByName("nashorn");
    nashornEngine.eval("var _sahi = \"something\";");
    assertEquals("something", nashornEngine.get("_sahi"));
    assertNull(mgr.get("_sahi"));
  }

  @Test
  public void testGlobaleToEngineScopel() throws Exception {
    ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine nashornEngine = mgr.getEngineByName("nashorn");
    mgr.put("_sahi", "bla");
    Object o = nashornEngine.eval("_sahi;");
    assertEquals("bla", nashornEngine.get("_sahi"));
    assertEquals("bla", o);
  }
}
