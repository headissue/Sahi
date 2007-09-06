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
