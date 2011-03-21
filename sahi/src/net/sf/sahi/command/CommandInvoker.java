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
/**
 * Authors: Dodda Rampradeep and Deepak Lewis
 */
package net.sf.sahi.command;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.util.Utils;

public class CommandInvoker {

    private static final int NORMAL_TERMINATION = 0;
    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";

    public HttpResponse execute(final HttpRequest request) throws InterruptedException {
        String command = request.getParameter(RequestConstants.COMMAND);
        boolean isSynchronous = Boolean.toString(true).equals(request.getParameter(RequestConstants.SYNC));
        String exitStatus = executeSystemCommand(command, isSynchronous);
        return new SimpleHttpResponse(exitStatus);
    }

    public String getCommandForOS(final String command) {
        String cmd = "";
        if (Utils.isWindows95()) {
            cmd = "command.com /C " + command;
        } else if (Utils.isWindows()) {
            cmd = "cmd.exe /C " + command;
        }
        return cmd;
    }

    private String executeSystemCommand(final String command, final boolean isSynchronous) throws InterruptedException {
        
        String cmd = "";
        try {
            cmd = command; // getCommandForOS(command);
            System.out.println("Executing: " + cmd);
            Process process = Runtime.getRuntime().exec(Utils.getCommandTokens(cmd));
            return (isSynchronous) ? getExitStatus(process) : SUCCESS;
        /**
        if (isSynchronous){
        process.waitFor();
        InputStream inputStream = process.getInputStream();
        String output = new String(Utils.getBytes(inputStream));
        InputStream errorStream = process.getErrorStream();
        String error = new String(Utils.getBytes(errorStream));
        return output + error;
        } else {
        return SUCCESS;
        }
        //            return (isSynchronous) ? getExitStatus(process) : SUCCESS;
         */
        } catch (Exception e) {
            e.printStackTrace();
            return FAILURE;
        }
    }

    private String getExitStatus(final Process process) throws InterruptedException {
        return (NORMAL_TERMINATION == process.waitFor()) ? SUCCESS : FAILURE;
    }
}
