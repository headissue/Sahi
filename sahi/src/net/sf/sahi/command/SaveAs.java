package net.sf.sahi.command;

import java.io.IOException;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.util.FileUtils;
import net.sf.sahi.util.Utils;

public class SaveAs {
    public void xexpect(HttpRequest request) {
    	String pattern = request.getParameter("urlPattern");
    	if (pattern.indexOf("[.]") == -1){
    		pattern = pattern.replaceAll("[.]", "[.]");
    	}
        request.session().mockResponder().add(pattern, "SaveAs_save");
    }

    public void saveLastDownloadedAs(HttpRequest request) {
    	String tempFileName = request.session().getVariable("download_lastFile");
    	String destination = request.getParameter("destination");
    	try {
    		System.out.println("tempDownloadDir " + net.sf.sahi.config.Configuration.tempDownloadDir());
    		System.out.println("tempFileName " + tempFileName);
			FileUtils.copyFile(Utils.concatPaths(net.sf.sahi.config.Configuration.tempDownloadDir(), tempFileName), destination);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public HttpResponse getLastDownloadedFileName(HttpRequest request){
    	String fileName = request.session().getVariable("download_lastFile");
    	if (fileName == null) fileName = "-1";
		return new SimpleHttpResponse(fileName);
    }

    public void clearLastDownloadedFileName(HttpRequest request){
    	request.session().removeVariables("download_lastFile");
    }
}
