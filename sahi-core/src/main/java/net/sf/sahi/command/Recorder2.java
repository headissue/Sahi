package net.sf.sahi.command;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.Utils;
import org.apache.log4j.Logger;

/**
 * Sahi - Web Automation and Test Tool
 * <p/>
 * Copyright  2006  V Narayan Raman
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public class Recorder2 {
  private Logger logger = org.apache.log4j.Logger.getLogger(Recorder2.class);

  public void addRecordedStep(HttpRequest request) {
    Session session = request.session();
    String steps = session.getVariable("CONTROLLER_RecordedSteps");
    String step = request.getParameter("step");
    steps = (steps != null) ? (steps + "\n" + step) : step;
    session.setVariable("CONTROLLER_RecordedSteps", steps);
//		String fileName = session.getVariable("CONTROLLER_recordFile");
//		if (!Utils.isBlankOrNull(fileName)){
//			Utils.writeFile(steps, fileName, true);
//		}
  }

  public HttpResponse getRecordedSteps(HttpRequest request) {
    Session session = request.session();
    String steps = session.getVariable("CONTROLLER_RecordedSteps");
    return new NoCacheHttpResponse(steps == null ? "" : steps);
  }

  public void setRecordedSteps(HttpRequest request) {
    Session session = request.session();
    String content = request.getParameter("content");
logger.debug(content);
    session.setVariable("CONTROLLER_RecordedSteps", content);
//		if (!Utils.isBlankOrNull(fileName)){
//			Utils.writeFile(content, fileName, !append);
//		}    	
  }

  public HttpResponse setFile(HttpRequest request) {
    Session session = request.session();
    String fileName = request.getParameter("file");
    if (Utils.isBlankOrNull(fileName)) {
      fileName = Utils.concatPaths(Configuration.getScriptRoots()[0], "/tmp/" + session.id() + ".sah");
    }
    session.setVariable("CONTROLLER_recordFile", fileName);
    return new SimpleHttpResponse(fileName);
  }
}
