package net.sf.sahi.workspace;

import net.sf.sahi.util.Utils;
import org.junit.Test;

import java.io.File;
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
    String workingDirectory = Utils.concatPaths(this.getClass().getResource(".").toURI().getPath(), "tmp");
    WorkspaceBuilder workspaceBuilder = new WorkspaceBuilder(workingDirectory);
    workspaceBuilder.build();
    assertTrue(new File(workingDirectory).exists());
    assertTrue(new File(workingDirectory, "logs").exists());
    assertTrue(new File(workingDirectory, "browser/ff/profiles/sahi0").exists());
    assertTrue(new File(workingDirectory, "browser/ff/profiles/sahi0/prefs.js").exists());
    assertTrue(new File(workingDirectory, "browser/ff/profiles/sahi0/cert8.db").exists());
    assertTrue(new File(workingDirectory, "certs/ca.crt").exists());
    assertTrue(new File(workingDirectory, "certs/ca.crt.key").exists());
    assertTrue(new File(workingDirectory, "config/download_contenttypes.txt").exists());
    assertTrue(new File(workingDirectory, "config/download_urls.txt").exists());
    assertTrue(new File(workingDirectory, "config/exclude_inject.txt").exists());
    assertTrue(new File(workingDirectory, "config/jira.properties").exists());
    assertTrue(new File(workingDirectory, "config/log.properties").exists());
    assertTrue(new File(workingDirectory, "config/userdata.properties").exists());
    assertTrue(new File(workingDirectory, "config/browser_types.xml").exists());
    assertTrue(new File(workingDirectory, "phantomscript/phantom.js").exists());
  }

  @Test
  public void testCreateFolder() throws Exception {

  }

  @Test
  public void testCopyNeededFiles() throws Exception {

  }

  @Test
  public void testCopyFile() throws Exception {

  }

  @Test
  public void testEqualsInpath() throws MalformedURLException, URISyntaxException {
    URL url = new URL("http://www.www/a=b/c");
    assertEquals(new URI(url.toString()), url.toURI());
    assertEquals(new URI(url.toString()).getPath(), url.toURI().getPath());
    assertEquals("/a=b/c", url.toURI().getPath());
  }
}
