package net.sf.sahi.workspace;

import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by wormi on 21.03.14.
 */
public class WorkspaceBuilderTest {
  @Test
  public void testBuild() throws Exception {
    String workingDirectory = Files.createTempDir().getAbsolutePath();
    WorkspaceBuilder workspaceBuilder = new WorkspaceBuilder(workingDirectory);
    workspaceBuilder.build();
    assertTrue(new File(workingDirectory).exists());
    assertTrue(new File(workingDirectory, "logs").exists());
    assertTrue(new File(workingDirectory, "browser/ff/profiles/sahi0").exists());
    assertTrue(new File(workingDirectory, "browser/ff/profiles/sahi0/prefs.js").exists());
    assertTrue(new File(workingDirectory, "config/download_contenttypes.txt").exists());
    assertTrue(new File(workingDirectory, "config/download_urls.txt").exists());
    assertTrue(new File(workingDirectory, "config/exclude_inject.txt").exists());
    assertTrue(new File(workingDirectory, "config/jira.properties").exists());
    assertTrue(new File(workingDirectory, "config/log.properties").exists());
    assertTrue(new File(workingDirectory, "config/userdata.properties").exists());
    assertTrue(new File(workingDirectory, "config/browser_types.xml").exists());
    assertTrue(new File(workingDirectory, "phantomscript/phantom.js").exists());
    assertTrue(new File(workingDirectory, "config/domainfix.txt").exists());
  }

  @Test
  public void testCreateFolder() throws Exception {
  //TODO
  }

  @Test
  public void testCopyNeededFiles() throws Exception {
    //TODO
  }

  @Test
  public void testCopyFile() throws Exception {
    //TODO
  }

  @Test
  public void testEqualsInpath() throws MalformedURLException, URISyntaxException {
    URL url = new URL("http://www.www/a=b/c");
    assertEquals(new URI(url.toString()), url.toURI());
    assertEquals(new URI(url.toString()).getPath(), url.toURI().getPath());
    assertEquals("/a=b/c", url.toURI().getPath());
  }

  @Test
  public void doNotOverwriteBrowserTypes() throws URISyntaxException, IOException {
    String workingDirectory = Files.createTempDir().getAbsolutePath();
    WorkspaceBuilder wb = new WorkspaceBuilder(workingDirectory);
    wb.build();
    //modify browsertypes
    File browserTypes = new File(new File(workingDirectory, WorkspaceBuilder.USER_CONFIG_ROOT), WorkspaceBuilder.BROWSER_TYPES);
    long sizeOfFile= browserTypes.length();
    browserTypes.delete();
    browserTypes.createNewFile();
    long sizeOfEmptyFile= browserTypes.length();
    wb.build();
    assertEquals(sizeOfEmptyFile, browserTypes.length());
  }
}
