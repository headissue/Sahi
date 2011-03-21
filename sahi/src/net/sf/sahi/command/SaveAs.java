package net.sf.sahi.command;

import java.io.IOException;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.util.FileUtils;
import net.sf.sahi.util.Utils;

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


public class SaveAs {

    public void xexpect(final HttpRequest request) {
        String pattern = request.getParameter("urlPattern");
        if (pattern.indexOf("[.]") == -1) {
            pattern = pattern.replaceAll("[.]", "[.]");
        }
        request.session().mockResponder().add(pattern, "SaveAs_save");
    }

    public void saveLastDownloadedAs(final HttpRequest request) {
        String tempFileName = request.session().getVariable("download_lastFile");
        String destination = request.getParameter("destination");
        try {
            System.out.println("tempDownloadDir " + net.sf.sahi.config.Configuration.tempDownloadDir());
            System.out.println("tempFileName " + tempFileName);
            destination = net.sf.sahi.config.Configuration.getAbsoluteUserPath(destination);
            System.out.println("destination " + destination);
            FileUtils.copyFile(Utils.concatPaths(net.sf.sahi.config.Configuration.tempDownloadDir(),
            						request.session().id()+"__"+tempFileName), destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpResponse getLastDownloadedFileName(final HttpRequest request) {
        String fileName = request.session().getVariable("download_lastFile");
        if (fileName == null) {
            fileName = "-1";
        }
        return new SimpleHttpResponse(fileName);
    }

    public void clearLastDownloadedFileName(final HttpRequest request) {
        request.session().removeVariables("download_lastFile");
    }
}
