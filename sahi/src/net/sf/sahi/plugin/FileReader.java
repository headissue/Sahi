package net.sf.sahi.plugin;

import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.request.HttpRequest;

public class FileReader {
    public HttpResponse contents(HttpRequest request){
        return new HttpFileResponse(request.getParameter("fileName"));
    }
}
