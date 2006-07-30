package net.sf.sahi.command;

import net.sf.sahi.RemoteRequestProcessor;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.request.MultiPartRequest;
import net.sf.sahi.request.MultiPartSubRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class FileUpload {
    public void setFile(HttpRequest request) {
        request.session().setVariable("file:" + request.getParameter("n"), request.getParameter("v"));
        request.session().mockResponder().add(request.getParameter("action").replaceAll("[.]", "[.]"), "FileUpload_appendFiles");
    }

    public HttpResponse appendFiles(HttpRequest request) {
        HttpRequest rebuiltRequest = request;
        if (request.isMultipart()) {
            Session session = request.session();
            MultiPartRequest multiPartRequest = null;
            try {
                multiPartRequest = new MultiPartRequest(request);
            } catch (IOException e) {
                return null;
            }
            Map parts = multiPartRequest.getMultiPartSubRequests();
            for (Iterator iterator = parts.values().iterator(); iterator.hasNext();) {
                MultiPartSubRequest part = (MultiPartSubRequest) iterator.next();
                String fileName = session.getVariable("file:" + part.name());
                if (Utils.isBlankOrNull(fileName)) continue;
                byte[] fileContent = Utils.readFile(fileName);
                part.setData(fileContent);
                part.setFileName(new File(fileName).getName());
            }
            rebuiltRequest = multiPartRequest.getRebuiltRequest();
            session.mockResponder().remove(request.url().replaceAll("[.]", "[.]"));
        }
        return new RemoteRequestProcessor().processHttp(rebuiltRequest);
    }
}
