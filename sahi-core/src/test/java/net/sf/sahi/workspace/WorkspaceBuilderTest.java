package net.sf.sahi.workspace;

import net.sf.sahi.util.Utils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Created by wormi on 21.03.14.
 */
public class WorkspaceBuilderTest {
  @Test
  public void testBuild() throws Exception {
    String workingDirectory = Utils.concatPaths(this.getClass().getResource(".").getPath(), "tmp");
    WorkspaceBuilder workspaceBuilder = new WorkspaceBuilder(workingDirectory);
    workspaceBuilder.build();
    assertTrue(new File(workingDirectory).exists());
    assertTrue(new File(workingDirectory, "logs").exists());
    assertTrue(new File(workingDirectory, "browser").exists());
    assertTrue(new File(workingDirectory, "browser/ff/profiles/sahi0").exists());
    assertTrue(new File(workingDirectory, "browser/ff/profiles/sahi0/prefs.js").exists());
    assertTrue(new File(workingDirectory, "browser/ff/profiles/sahi0/cert8.db").exists());
    assertTrue(new File(workingDirectory, "certs").exists());
    assertTrue(new File(workingDirectory, "certs/ca.crt").exists());
    assertTrue(new File(workingDirectory, "certs/ca.crt.key").exists());
    assertTrue(new File(workingDirectory, "config").exists());
    assertTrue(new File(workingDirectory, "config/download_contenttypes.txt").exists());
    assertTrue(new File(workingDirectory, "config/download_urls.txt").exists());
    assertTrue(new File(workingDirectory, "config/exclude_inject.txt").exists());
    assertTrue(new File(workingDirectory, "config/jira.properties").exists());
    assertTrue(new File(workingDirectory, "config/log.properties").exists());
    assertTrue(new File(workingDirectory, "config/userdata.properties").exists());
    assertTrue(new File(workingDirectory, "config/browser_types.xml").exists());
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
}
