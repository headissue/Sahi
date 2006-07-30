package net.sf.sahi.command;

import net.sf.sahi.RemoteRequestProcessor;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpModifiedResponse;
import net.sf.sahi.response.HttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

public class MockResponder {

    private HashMap map = new HashMap();

    public void add(String urlPattern, String className) {
        map.put(urlPattern, className);
    }

    public void remove(String urlPattern) {
        map.remove(urlPattern);
    }

    String getCommand(String url) {
        final Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String pattern = (String) iterator.next();
            if (url.matches(pattern)) {
                return (String) map.get(pattern);
            }
        }
        return null;
    }

    public HttpResponse getResponse(HttpRequest request) {
        final String command = getCommand(request.url());
        if (command == null)
            return null;
        return new CommandExecuter(command, request).execute();
    }

    public void remove(HttpRequest request) {
        request.session().mockResponder().remove(request.getParameter("pattern"));
    }

    public void add(HttpRequest request) {
        request.session().mockResponder().add(request.getParameter("pattern"), request.getParameter("class"));
    }

    public HttpResponse simple(HttpRequest request) throws IOException {
        Properties props = new Properties();
        props.put("url", request.url());
        return new HttpModifiedResponse(new HttpFileResponse(Configuration.getHtdocsRoot() + "spr/simpleMock.htm", props, false, false), request
                .isSSL(), request.fileExtension());
    }

    public HttpResponse fileUpload(HttpRequest request) {
        return new RemoteRequestProcessor().processHttp(request);
    }
}
