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

import java.io.File;
import java.util.ArrayList;

public class FileScript extends SahiScript {
    public FileScript(String fileName) {
        super(fileName, new ArrayList(), new File(fileName).getName());
    }

    public FileScript(String fileName, ArrayList parents) {
        super(fileName, parents, new File(fileName).getName());
    }

    protected void loadScript(String fileName) {
        try {
            setScript(new String(Utils.readFile(fileName)));
        } catch (Exception e) {
            setScript("_log(\"Script: " + Utils.escapeDoubleQuotesAndBackSlashes(fileName) + " does not exist.\", \"failure\");\n");
        }
    }

    String getFQN(String scriptName) {
        if (scriptName.indexOf("http") == 0) {
            return scriptName;
        }
        return Utils.getRelativeFile(new File(path), scriptName).getAbsolutePath();
    }


    SahiScript getNewInstance(String scriptName, ArrayList parents) {
        FileScript fileScript = new FileScript(getFQN(scriptName), parents);
        fileScript.parents = parents;
        return fileScript;
    }
}
