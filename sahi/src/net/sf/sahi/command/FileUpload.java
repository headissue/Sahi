/**
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
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.request.MultiPartRequest;
import net.sf.sahi.request.MultiPartSubRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.MimeType;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class FileUpload {
    public void setFile(HttpRequest request) {
        request.session().setVariable("file:" + request.getParameter("n"), request.getParameter("v"));
        request.session().mockResponder().add(request.getParameter("action").replaceAll("[.]", "[.]")+".*", "FileUpload_appendFiles");
    }

    public HttpResponse appendFiles(HttpRequest request) {
        HttpRequest rebuiltRequest = request;
        if (request.isMultipart()) {
            Session session = request.session();
            MultiPartRequest multiPartRequest;
            try {
                multiPartRequest = new MultiPartRequest(request);
            } catch (IOException e) {
                return null;
            }
            List parts = multiPartRequest.getMultiPartSubRequests();
            for (Iterator iterator = parts.iterator(); iterator.hasNext();) {
                MultiPartSubRequest part = (MultiPartSubRequest) iterator.next();
                String fileName = session.getVariable("file:" + part.name());
                System.out.println("Uploading: fileName = "+fileName);
                if (Utils.isBlankOrNull(fileName)) continue;
                part.setHeader("Content-Type", MimeType.getMimeTypeOfFile(fileName, "application/octet-stream"));
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
