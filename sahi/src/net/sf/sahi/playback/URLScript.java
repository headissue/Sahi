package net.sf.sahi.playback;

import net.sf.sahi.util.Utils;

import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;

public class URLScript extends SahiScript {
    public URLScript(String url) {
        super(url, new ArrayList(), url);
    }

    public URLScript(String url, ArrayList parents) {
        super(url, parents, url);
    }

    protected void loadScript(String url) {
        setScript(new String(Utils.readURL(url)));
    }

    String getFQN(String scriptName) {
        try {
            return new URL(new URL(path), scriptName).toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    SahiScript getNewInstance(String scriptName, ArrayList parents) {
        scriptName = getFQN(scriptName);
        URLScript urlScript = new URLScript(scriptName, parents);
        urlScript.parents = parents;
        return urlScript;
    }
}
