package net.sf.sahi.workspace;

import net.sf.sahi.util.FileUtils;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.io.IOException;

/**
 * Created by wormi on 21.03.14.
 */

public class WorkspaceBuilder {

  private static final String BROWSER_PROFILES = "userdata/browser";
  private static final String FIREFOX_PREFIX = "FF";
  // Todo create as needed
  private static final int BROWSER_PROFILE_NUMBER = 10;
  private String target;

  private static final String CERTS_ROOT = "userdata/certs";
  private static final String DOWNLOAD_ROOT = "userdata/downloads";
  final private String LOGS_ROOT = "userdata/logs";

  public WorkspaceBuilder(String target) {
    this.target = target;
  }

  public void build(){
    createWorkspaceDirectory();
    createLogRoot();
    createCertsRoot();
    createDownloadDirectory();
    try {
      copyNeededFiles();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void createWorkspaceDirectory() {
    createFolder(target);
  }

  private void createLogRoot(){
    createFolder(Utils.concatPaths(target, LOGS_ROOT));
  }

  private void createCertsRoot(){
    createFolder(Utils.concatPaths(target, CERTS_ROOT));
  }

  private void createDownloadDirectory(){
    createFolder(Utils.concatPaths(target, DOWNLOAD_ROOT));
  }

  public static void createFolder(final String  file) {
    File thisFile = new File(file);
    if (!thisFile.exists()) {
      thisFile.mkdirs();
    }
  }

  public void copyNeededFiles() throws IOException {
    copyFireFoxProfile();
  }

  private void copyFireFoxProfile() throws IOException {
    final String template = this.getClass().getResource("ff_profile_template").getPath();
    final String profile = Utils.concatPaths(target, BROWSER_PROFILES);

    File toFile = new File(profile);
    toFile.mkdirs();
    String prefix = FIREFOX_PREFIX;

    String profile0 = Utils.concatPaths(toFile.getCanonicalPath(), prefix + 0);
    if (!new File(profile0).exists()) {
      try {
        FileUtils.copyDir(template, profile0);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    for (int i = 1; i < BROWSER_PROFILE_NUMBER; i++) {
      String profileN = Utils.concatPaths(toFile.getCanonicalPath(), prefix + i);
      copyFile(profile0, profileN, "prefs.js");
    }
  }

  public static void copyFile(final String origDir, final String destDir, final String fileName) {
    try {
      final File src = new File(origDir, fileName);
      if (src.exists())
        FileUtils.copyFile(src, new File(destDir, fileName));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


}
