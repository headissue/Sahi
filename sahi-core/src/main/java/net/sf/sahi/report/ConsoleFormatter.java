package net.sf.sahi.report;

import java.text.DateFormat;
import java.util.List;

public class ConsoleFormatter implements Formatter {

  public String getFileName(String scriptName) {
    return "";
  }

  public String getHeader() {
    return "";
  }

  public String getResultData(List<TestResult> listResult) {
    StringBuffer sb = new StringBuffer();
    if (listResult != null && listResult.size() > 0) {
      for (int i = 0; i < listResult.size(); i++) {
        TestResult result = listResult.get(i);
        String res = getStringResult(result);
        if (!res.isEmpty()) {
          sb.append(res).append("\n");
        }
      }
    }

    return sb.toString();
  }

  public String getStringResult(final TestResult result) {
    if (result.type.equals(ResultType.RAW)) {
      return result.message;
    }
    if (!(ResultType.FAILURE.equals(result.type) || ResultType.ERROR.equals(result.type))) {
      return "";
    }

    StringBuffer sb = new StringBuffer();
    sb.append("[").append(result.type.getName()).append(
      "] ");
    if (result.debugInfo != null) {
      sb.append(result.debugInfo);
    }
    sb.append("\t").append(DateFormat.getDateTimeInstance().format(result.time));
    if (result.message != null && !result.message.isEmpty()) {
      sb.append("\n\t").append(result.message.trim().replace("\n", "\n\t"));
    }
    if (result.failureMsg != null && !result.failureMsg.isEmpty()) {
      sb.append("\n\t").append(result.failureMsg.trim().replace("\n", "\n\t"));
    }
    return sb.toString();
  }

  public String getSummaryHeader() {
    return "";
  }

  public String getSummaryData(TestSummary summary) {
    StringBuffer sb = new StringBuffer();
    int successRate = summary.getSteps() != 0 ? ((summary.getSteps() - (summary.getFailures() + summary.getErrors())) * 100) / summary.getSteps()
      : 100;
    sb.append("[")
      .append(summary.hasFailed() ? ResultType.FAILURE.getName() : ResultType.SUCCESS.getName())
      .append("] ")
      .append(summary.getScriptName())
      .append("\n\tTests run: ").append(summary.getSteps())
      .append(", Failures: ").append(summary.getFailures())
      .append(", Errors: ").append(summary.getErrors())
      /*
      .append(" -> Rate: ").append(successRate)
      .append("%")
      */
      .append(", Time Elapsed: ")
      .append(new Double(summary.getTimeTaken()) / 1000)
      .append(" sec")
      .append("\n");
    return sb.toString();
  }

  public String getSummaryFooter() {
    return "";
  }

  public String getStartScript() {
    return "";
  }

  public String getStopScript() {
    return "";
  }

  public String getFooter() {
    return "";
  }

  public String getSuiteLogFileName() {
    return "";
  }
}
