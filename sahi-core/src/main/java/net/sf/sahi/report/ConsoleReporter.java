package net.sf.sahi.report;

import net.sf.sahi.test.TestLauncher;

import java.io.*;
import java.util.List;

public class ConsoleReporter extends SahiReporter {

  BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(System.out)) {
    @Override
    public void close() throws IOException {
      // we won't close System.out
    }
  };

  public ConsoleReporter() {
    super(new ConsoleFormatter());
  }

  @Override
  public boolean createSuiteLogFolder() {
    return true;
  }

  @Override
  public void generateSuiteReport(List<TestLauncher> tests) {
    // disabled
  }

  @Override
  protected Writer createWriter(String file) throws IOException {
    return outWriter;
  }
}
