/**
 * Sahi - Web Automation and Test Tool
 * 
 * Copyright  2006  V Narayan Raman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.sahi.playback;

import net.sf.sahi.util.Utils;

import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;

public class URLScript extends SahiScript {

    public URLScript(final String url) {
        super(url, new ArrayList<String>(), url);
    }

    public URLScript(final String url, final ArrayList<String> parents) {
        super(url, parents, url);
    }

    protected void loadScript(final String url) {
        setScript(new String(Utils.readURL(url)));
    }

    String getFQN(final String scriptName) {
        try {
            return new URL(new URL(path), scriptName).toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    SahiScript getNewInstance(final String scriptName, final ArrayList<String> parents) {
        String script = getFQN(scriptName);
        URLScript urlScript = new URLScript(script, parents);
        urlScript.parents = parents;
        return urlScript;
    }
}
