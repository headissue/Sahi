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

    public HttpResponse execute(HttpRequest request) throws InterruptedException {
        String command = request.getParameter(RequestConstants.COMMAND);
        boolean isSynchronous = Boolean.toString(true).equals(request.getParameter(RequestConstants.SYNC));
        String exitStatus = executeSystemCommand(command, isSynchronous);
        return new SimpleHttpResponse(exitStatus);
    }

    public String getCommandForOS(String command){
        if (Utils.isWindows95()){
            command = "command.com /C " + command;
        }else if (Utils.isWindows()){
            command = "cmd.exe /C " + command;
        }
        return command;
    }

    private String executeSystemCommand(String command, boolean isSynchronous) throws InterruptedException {
        try {
            command = getCommandForOS(command);
            System.out.println("Executing: "+command);
            Process process = Runtime.getRuntime().exec(Utils.getCommandTokens(command));
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

    private String getExitStatus(Process process) throws InterruptedException {
        return (NORMAL_TERMINATION == process.waitFor()) ? SUCCESS : FAILURE;
    }
}
