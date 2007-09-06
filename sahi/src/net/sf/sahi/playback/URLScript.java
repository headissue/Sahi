/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
