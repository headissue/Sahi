package net.sf.sahi.command;

import java.io.File;
import java.util.Properties;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
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


public class SSLManager {
    public HttpResponse execute(final HttpRequest request) {
    	File dir = new File(Configuration.getCertsPath());
    	String domains = "";
    	if (dir.exists()){
    		String[] files = dir.list();
    		StringBuffer sb = new StringBuffer();
    		for (int i=0; i<files.length; i++){
    			sb.append(files[i]);
    			sb.append("|");
    		}
    		domains = sb.toString().replace('_', '.');
    	}
        Properties props = new Properties();
        props.setProperty("domains", domains);
        props.setProperty("commonDomain", Configuration.getCommonDomain());
        return new HttpFileResponse(Utils.concatPaths(Configuration.getHtdocsRoot(), "spr/manage/ssl/manager.htm"),
        		props, true, true);
    }

    public HttpResponse success(final HttpRequest request) {
        Properties props = new Properties();
        props.setProperty("domain", request.getParameter("domain"));
        
        props.setProperty("commonDomain", Configuration.getCommonDomain());
        return new HttpFileResponse(Utils.concatPaths(Configuration.getHtdocsRoot(), "spr/manage/ssl/success.htm"),
        		props, true, true);
    }
}
