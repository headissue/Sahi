package net.sf.sahi.playback;

import net.sf.sahi.util.Utils;

import java.util.ArrayList;
import java.io.File;

public class FileScript extends SahiScript {
    public FileScript(String fileName) {
        super(fileName, new ArrayList(), new File(fileName).getName());
    }

    public FileScript(String fileName, ArrayList parents) {
        super(fileName, parents, new File(fileName).getName());
    }

    protected void loadScript(String fileName) {
        setScript(new String(Utils.readFile(fileName)));
    }

    String getFQN(String scriptName) {
        if (scriptName.indexOf("http") == 0){
            return scriptName;
        }
        return Utils.concatPaths(new File(path).getParent(), scriptName);
    }


    SahiScript getNewInstance(String scriptName, ArrayList parents) {
        FileScript fileScript = new FileScript(getFQN(scriptName), parents);
        fileScript.parents = parents;
        return fileScript;
    }
}
