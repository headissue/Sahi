package net.sf.sahi.command;

import java.lang.reflect.Method;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;

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
        if (commandClass.indexOf('.')==-1) {
            commandClass = "net.sf.sahi.command."+commandClass;
        }
    }

	public HttpResponse execute() {
		Class clazz;
		try {
			clazz = Class.forName(commandClass);
			final Method method = clazz.getDeclaredMethod(commandMethod, new Class[] {HttpRequest.class});
			final Object returned = method.invoke(clazz.newInstance(), new Object[] {request});
			return returned == null ? new NoCacheHttpResponse() : (HttpResponse)returned;
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
