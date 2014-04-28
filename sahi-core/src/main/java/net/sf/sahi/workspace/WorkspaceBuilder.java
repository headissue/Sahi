package net.sf.sahi.workspace;

import net.sf.sahi.util.FileUtils;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by wormi on 21.03.14.
 */

public class WorkspaceBuilder {

  private static final String BROWSER_PROFILES = "browser";
  private static final String FIREFOX_PREFIX = "ff/profiles/sahi";
  // Todo create as needed
  private static final int BROWSER_PROFILE_NUMBER = 10;
  private String target;

  private static final String CERTS_ROOT = "certs";
  private static final String DOWNLOAD_ROOT = "downloads";
  private static final String USER_CONFIG_ROOT = "config";
  final private String LOGS_ROOT = "logs";

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
    } catch (URISyntaxException e) {
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

  public void copyNeededFiles() throws IOException, URISyntaxException {
    copyFireFoxProfile();
    copyUserDataConfig();
    copyRootCaAndKey();
    copyBrowserXml();
  }

  private void copyBrowserXml() throws IOException, URISyntaxException {
    final String userConfig = Utils.concatPaths(target, USER_CONFIG_ROOT);
// FIXME OS independence?
    copyFile(getPath("browser_types"), userConfig, "linux.xml");
    renameFile(new File(userConfig, "linux.xml"), new File(userConfig, "browser_types.xml"));
  }

  private void renameFile(File src, File dest) throws IOException{
    if (!src.renameTo(dest)){
      throw new IOException("did not copy");
    }
  }

  private void copyRootCaAndKey() throws URISyntaxException {
    final String template = getPath("certs");
    final String certsDir = Utils.concatPaths(target, CERTS_ROOT);

    createAndCopyDirectory(template, certsDir);
  }

  private void createAndCopyDirectory(String s, String d) {
    File fromDir = new File(s);
    File toDir = new File(d);
    toDir.mkdirs();
    for (int i = 0; i < fromDir.list().length; i++) {
      String thisFile = fromDir.list()[i];
      copyFile(s, d, thisFile);
    }
  }

  private String getPath(String res) throws URISyntaxException {
    return this.getClass().getResource(res).toURI().getPath();
  }

  private void copyUserDataConfig() throws URISyntaxException {
    final String template = getPath("userdata_template");
    final String userConfig = Utils.concatPaths(target, USER_CONFIG_ROOT);

    System.out.println("template " + template);
    System.out.println("target " + userConfig);

    if (!new File(template).exists()) {
      throw new RuntimeException("Userdata template not found at '"+template+"'");
    }

    if (!new File(template).exists()) {
      throw new RuntimeException("Userdata target not found at '"+userConfig+"'");
    }

    createAndCopyDirectory(template, userConfig);
  }

  private void copyFireFoxProfile() throws IOException, URISyntaxException {
    final String template = getPath("ff_profile_template");
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
