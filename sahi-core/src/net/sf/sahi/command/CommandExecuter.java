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
package net.sf.sahi.command;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.util.ClassLoadHelper;
import net.sf.sahi.util.Utils;

public class CommandExecuter {

	private static final Logger logger = Logger.getLogger("net.sf.sahi.command.CommandExecuter");
    private String commandMethod;
    private String commandClass;
    private final HttpRequest request;
    private static String DELIMITER = "_";

    public CommandExecuter(String cmd, HttpRequest request, boolean isPro) {
        this.request = request;
        this.commandClass = cmd;
        this.commandMethod = "execute";
        if (cmd.indexOf(DELIMITER) != -1) {
            this.commandClass = cmd.substring(0, cmd.indexOf(DELIMITER));
            this.commandMethod = cmd.substring(cmd.indexOf(DELIMITER) + 1);
        }
        if (commandClass.indexOf('.') == -1) {
            commandClass = "net.sf.sahi.command." + commandClass;
        }
    }

    public HttpResponse execute() {
        Class<?> clazz;
        try {
            clazz = ClassLoadHelper.getClass(commandClass);
            final Method method = clazz.getDeclaredMethod(commandMethod, new Class[]{HttpRequest.class});
            final Object returned = method.invoke(clazz.newInstance(), new Object[]{request});
            return returned == null ? new NoCacheHttpResponse() : (HttpResponse) returned;
        } catch (Exception e) {
        	logger.warning("commandClass = >"+commandClass + "< commandMethod = >" + commandMethod + "<");
        	logger.warning(Utils.getStackTraceString(e));
            return new NoCacheHttpResponse("SAHI_ERROR");
        }
    }

    String getCommandClass() {
        return commandClass;
    }

    String getCommandMethod() {
        return commandMethod;
    }
}
