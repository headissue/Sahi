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

import net.sf.sahi.RemoteRequestProcessor;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.request.MultiPartRequest;
import net.sf.sahi.request.MultiPartSubRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.MimeType;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.FileNotFoundRuntimeException;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class FileUpload {
    private static Logger logger = Configuration.getLogger("net.sf.sahi.command.FileUpload");

    @SuppressWarnings("unchecked")
	public HttpResponse setFile(final HttpRequest request) {
        Session session = request.session();
		String key = "file:" + request.getParameter("n");
		ArrayList<String> list = (ArrayList<String>) session.getObject(key);
		if (list == null) list = new ArrayList<String>();
		String filePath = request.getParameter("v");
		list.add(filePath);
		session.setObject(key, list);
        session.mockResponder().add(request.getParameter("action").replaceAll("[.]", "[.]") + ".*", "FileUpload_appendFiles");
        if (new File(Configuration.getAbsoluteUserPath(filePath)).exists()) {
        	return new SimpleHttpResponse("true");
        } else {
        	return new SimpleHttpResponse("File not found: " + filePath + "; Base directory is userdata directory: " + Configuration.getUserDataDir());
        }
    }

    @SuppressWarnings("unchecked")
	public HttpResponse appendFiles(final HttpRequest request) {
        HttpRequest rebuiltRequest = request;
        if (request.isMultipart()) {
            Session session = request.session();
            MultiPartRequest multiPartRequest;
            try {
                multiPartRequest = new MultiPartRequest(request);
            } catch (IOException e) {
                return null;
            }
            List<MultiPartSubRequest> parts = multiPartRequest.getMultiPartSubRequests();
            for (Iterator<MultiPartSubRequest> iterator = parts.iterator(); iterator.hasNext();) {
                MultiPartSubRequest part = iterator.next();
                ArrayList<String> fileNames = (ArrayList<String>) session.getObject("file:" + part.name());
                if (fileNames == null || fileNames.size() == 0) {
                    continue;
                }
                String fileName = fileNames.remove(0);
                String absolutePath = Configuration.getAbsoluteUserPath(fileName);
				logger.info("Uploading: fileName = " + fileName + " resolved to " + absolutePath);
                part.setHeader("Content-Type", MimeType.getMimeTypeOfFile(fileName, "application/octet-stream"));
                byte[] fileContent = new byte[0];
                try {
                	fileContent = Utils.readFile(absolutePath);
                } catch (FileNotFoundRuntimeException e) {
                	logger.warning(Utils.getStackTraceString(e));
                }
                part.setData(fileContent);
                part.removeHeader("Content-Length");
                part.setFileName(new File(fileName).getName());
            }
            rebuiltRequest = multiPartRequest.getRebuiltRequest();
            session.mockResponder().remove(request.url().replaceAll("[.]", "[.]"));
        }
        return new RemoteRequestProcessor().processHttp(rebuiltRequest);
    }
}
