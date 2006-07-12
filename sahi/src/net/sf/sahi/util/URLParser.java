package net.sf.sahi.util;

import net.sf.sahi.config.Configuration;

public class URLParser {

    public static String logFileNamefromURI(String uri) {
        String fileName = getRelativeLogFile(uri);
        if ("".equals(fileName)) return "";
        return Configuration.appendLogsRoot(fileName);
    }

    static String getRelativeLogFile(String uri) {
        String fileName = uri.substring(uri.indexOf("/logs") + 5);
        while (fileName.startsWith("/")) fileName = fileName.substring(1);
        return fileName;
    }

    public static String fileNamefromURI(String uri) {
        return Utils.concatPaths(Configuration.getHtdocsRoot(), uri
                .substring(uri.indexOf("_s_/") + 4));
    }

    public static String scriptFileNamefromURI(String uri, String token) {
        int endIndex = uri.indexOf("?");
        endIndex = endIndex == -1 ? uri.length() : endIndex;
        String fileName = uri.substring(uri.lastIndexOf(token) + token.length(), endIndex);
        return Utils.concatPaths(Configuration.getScriptRoots()[0], fileName); //TODO FIX ME
    }

    public static String getCommandFromUri(String uri) {
        int ix1 = uri.indexOf("/dyn/");
        if (ix1 == -1) return null;
        ix1 = ix1 + 5;
        int ix2 = uri.indexOf("/", ix1);
        int ix3 = uri.indexOf("?", ix1);
        int endIx = -1;
        if (ix2 > -1 && ix3 == -1) endIx = ix2;
        else if (ix3 > -1 && ix2 == -1) endIx = ix3;
        else {
            endIx = ix3 < ix2 ? ix3 : ix2;
        }
        if (endIx == -1) endIx = uri.length();
        return uri.substring(ix1, endIx);
    }

}
