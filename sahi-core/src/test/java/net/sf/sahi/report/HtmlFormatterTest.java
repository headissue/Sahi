package net.sf.sahi.report;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author dlewis
 */
public class HtmlFormatterTest {
  private static final long serialVersionUID = 17080619161553882L;

  private HtmlFormatter formatter = null;

  @Before
  public void setup() {
    Configuration.init();
    formatter = new HtmlFormatter();
  }


  private String expectedSummaryForEmptyList = new StringBuffer(
    "<tr class=\"SUCCESS\"><td>test</td><td>0</td>").append(
    "<td>0</td><td>0</td><td>100%</td><td>0</td></tr>").toString();

  private String expectedSummaryForAllTypes = new StringBuffer("<tr class=\"FAILURE\"><td>test</td><td>3</td>").append(
    "<td>1</td><td>0</td><td>66%</td><td>0</td></tr>").toString();


  @Test
  public void testGetFileName() {
    assertEquals("test.htm", formatter.getFileName("test"));
  }

  @Test
  @Ignore("FIXME does not seem to be up to date")
  public void xtestGetStringResultForSuccessResult() {
    String expected = "<div class=\"SUCCESS\"><a class=\"SUCCESS\">_assertNotNull(_textarea(\"t2\"));</a></div>";
    assertEquals(expected, formatter.getStringResult(ReportUtil
      .getSuccessResult()));
  }

  @Test
  @Ignore("FIXME does not seem to be up to date")
  public void xtestGetStringResultForFailureResult() {
    String expected = "<div class=\"FAILURE\"><a class=\"FAILURE\">_call(testAccessors()); Assertion Failed.</a></div>";
    assertEquals(expected, formatter.getStringResult(ReportUtil
      .getFailureResultWithoutDebugInfo()));
  }

  @Test
  @Ignore("FIXME does not seem to be up to date")
  public void xtestGetStringResultForInfoResult() {
    String expected = "<div class=\"INFO\"><a class=\"INFO\" href=\"/_s_/dyn/Log_highlight?href=blah\">_click(_link(\"Form Test\"));</a></div>";
    assertEquals(expected, formatter.getStringResult(ReportUtil
      .getInfoResult()));
  }

  @Test
  public void testGetResultDataForEmptyList() {
    assertEquals("", formatter.getResultData(null));
  }

  @Test
  public void testGetResultDataForListWithAllTypesOfResults() {
    String expected = new StringBuffer(formatter.getStringResult(ReportUtil
      .getInfoResult())).append("\n").append(
      formatter.getStringResult(ReportUtil.getSuccessResult()))
      .append("\n").append(
        formatter.getStringResult(ReportUtil.getFailureResultWithoutDebugInfo()))
      .append("\n").toString();

    assertEquals(expected, formatter.getResultData(ReportUtil
      .getListResult()));
  }

  @Test
  public void testGetHeader() {
    String expected = new StringBuffer("<head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />\n<style>\n").append(
      new String(Utils.readFileAsString(Configuration
        .getPlaybackLogCSSFileName(true)))).append(
      new String(Utils.readFileAsString(Configuration
        .getConsolidatedLogCSSFileName(true)))).append(
      "</style></head>\n").toString();
    assertEquals(expected, formatter.getHeader());
  }

  @Test
  public void testGetSummaryHeader() {
    String expected = "<table class='summary'><tr><td>Test</td><td>Total Steps</td><td>Failures</td><td>Errors</td><td>Success Rate</td><td>Time Taken (ms)</td></tr>";
    assertEquals(expected, formatter.getSummaryHeader());
  }

  @Test
  public void testGetSummaryFooter() {
    String expected = "</table>";
    assertEquals(expected, formatter.getSummaryFooter());
  }

  @Test
  public void testGetSummaryDataForEmptyList() {
    TestSummary summary = new TestSummary();
    summary.setScriptName("test");
    assertEquals(expectedSummaryForEmptyList, formatter
      .getSummaryData(summary));
  }

  @Test
  public void testGetSummaryDataForAllTypesWithoutLink() {
    assertEquals(expectedSummaryForAllTypes, formatter
      .getSummaryData(ReportUtil.getTestSummary()));
  }

  @Test
  public void testGetSummaryDataForAllTypesWithLink() {
    String expected = expectedSummaryForAllTypes.replaceFirst("test", "<a class=\"SCRIPT\" href=\"test.htm\">test</a>");
    TestSummary summary = ReportUtil.getTestSummary();
    summary.setLogFileName("test");
    summary.setAddLink(true);
    assertEquals(expected, formatter
      .getSummaryData(summary));
  }

  @Test
  public void testNewLinesConvertedToBRTag() {
    String expected = "Difference in array length:<br/>Expected Length<br/>Another line<br/>abc";
    TestResult result = new TestResult("Difference in array length:\nExpected Length\nAnother line", ResultType.INFO, "abc", "abc");
    String stringResult = formatter.getStringResult(result);
    assertTrue(stringResult.contains(expected));
  }
}
