package net.sf.sahi.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.sahi.RemoteRequestProcessor;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoContentResponse;

public class SaveAs {
    public void expect(HttpRequest request) {
    	String pattern = request.getParameter("urlPattern");
    	if (pattern.indexOf("[.]") == -1){
    		pattern = pattern.replaceAll("[.]", "[.]");
    	}
        request.session().mockResponder().add(pattern, "SaveAs_save");
    }

    public HttpResponse save(HttpRequest request) {
    	HttpResponse response = new RemoteRequestProcessor().processHttp(request);
    	byte[] data = response.data();
        try {
            File file = new File(request.fileName());
            if (file.exists()) {
            	file.delete();
            }
            file.createNewFile();
            FileOutputStream out;
            out = new FileOutputStream(file, true);
            out.write(data);
            out.close();
        } catch (IOException e) {
            System.out.println("Could not write to file");
        }
        return new NoContentResponse();
    }
}
