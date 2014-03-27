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
    System.out.println(workingDirectory);
    assertTrue(new File(workingDirectory).exists());
    assertTrue(new File(workingDirectory, "userdata/logs").exists());
    assertTrue(new File(workingDirectory, "userdata/browser").exists());
    assertTrue(new File(workingDirectory, "userdata/browser/FF0").exists());
    assertTrue(new File(workingDirectory, "userdata/browser/FF0/prefs.js").exists());
    assertTrue(new File(workingDirectory, "userdata/certs").exists());
    assertTrue(new File(workingDirectory, "userdata/config").exists());
    assertTrue(new File(workingDirectory, "userdata/config/download_contenttypes.txt").exists());
    assertTrue(new File(workingDirectory, "userdata/config/download_urls.txt").exists());
    assertTrue(new File(workingDirectory, "userdata/config/exclude_inject.txt").exists());
    assertTrue(new File(workingDirectory, "userdata/config/jira.properties").exists());
    assertTrue(new File(workingDirectory, "userdata/config/log.properties").exists());
    assertTrue(new File(workingDirectory, "userdata/config/userdata.properties").exists());
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
