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

package net.sf.sahi.command;

import java.lang.reflect.Method;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.util.ClassLoadHelper;

public class CommandExecuter {

    private String commandMethod;
    private String commandClass;
    private final HttpRequest request;
    private static String DELIMITER = "_";

    public CommandExecuter(String cmd, HttpRequest request) {
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
        Class clazz;
        try {
            clazz = ClassLoadHelper.getClass(commandClass);
            final Method method = clazz.getDeclaredMethod(commandMethod, new Class[]{HttpRequest.class});
            final Object returned = method.invoke(clazz.newInstance(), new Object[]{request});
            return returned == null ? new NoCacheHttpResponse() : (HttpResponse) returned;
        } catch (Exception e) {
            e.printStackTrace();
            return new NoCacheHttpResponse();
        }
    }

    String getCommandClass() {
        return commandClass;
    }

    String getCommandMethod() {
        return commandMethod;
    }
}
