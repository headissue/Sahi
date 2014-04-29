package net.sf.sahi.workspace;

import net.sf.sahi.util.FileUtils;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by wormi on 21.03.14.
 */

public class WorkspaceBuilder {

  private static final String BROWSER_PROFILES = "browser";
  private static final String FIREFOX_PREFIX = "ff/profiles/sahi";
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
    List<String> resources = new LinkedList<>();
    resources.add("linux.xml");
    File destDir = new File(Utils.concatPaths(target, USER_CONFIG_ROOT));
    copyResources(resources, destDir);
    if (new File(destDir, "linux.xml").exists()) {
      renameFile(new File(destDir, "linux.xml"), new File(destDir, "browser_types.xml"));
    }
  }

  private void renameFile(File src, File dest) throws IOException{
    if (!src.renameTo(dest)){
      throw new IOException("did not copy");
    }
  }

  /**
   * Copies the mentioned resources from the ff_profile_template directory to the browser/ff/profiles/sahi0/
   * directory.
   * @throws IOException
   */
  private void copyFireFoxProfile() throws IOException {
    List<String> resources = new LinkedList<>();
    resources.add("cert8.db");
    resources.add("prefs.js");

    String prefix = FIREFOX_PREFIX;
    File destDir = new File(Utils.concatPaths(target, BROWSER_PROFILES));
    destDir.mkdirs();
    destDir = new File(Utils.concatPaths(destDir.getCanonicalPath(), prefix + 0));

    copyResources(resources, destDir);
  }

  private void copyResources(List<String> resources, File destDir) throws IOException {
    destDir.mkdirs();
    for (Iterator<String> iterator = resources.iterator(); iterator.hasNext(); ) {
      String next = iterator.next();
      if (new File(destDir, next).exists()) continue;
      File f = getResourceFile(next, WorkspaceBuilder.class);
      copyFile(f, destDir, next);
    }
  }

  private void copyRootCaAndKey() throws  IOException {
    List<String> resources = new LinkedList<>();
    resources.add("ca.crt");
    resources.add("ca.crt.key");
    File destDir = new File(Utils.concatPaths(target, CERTS_ROOT));
    copyResources(resources, destDir);
  }



  private File getResourceFile(String res, Class location) throws IOException {
    File f = FileUtils.copyToTempFile(res, location);
    if (f == null) throw new IOException("Resource " + res + "not found");
    return f;
  }

  private void copyUserDataConfig() throws IOException {
    List<String> resources = new LinkedList<>();
    resources.add("download_contenttypes.txt");
    resources.add("download_urls.txt");
    resources.add("exclude_inject.txt");
    resources.add("jira.properties");
    resources.add("log.properties");
    resources.add("userdata.properties");
    File destDir = new File(Utils.concatPaths(target, USER_CONFIG_ROOT));
    copyResources(resources, destDir);
  }



  public static void copyFile(File origFile, File destDir, String fileName) throws IOException {
    if (!origFile.exists()) throw new IOException("source file " + origFile + "not found");
    FileUtils.copyFile(origFile, new File(destDir, fileName));
  }


}
