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

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.Utils;

public class Recorder {

    public void start(final HttpRequest request) {
        startRecorder(request);
    }

    public void record(final HttpRequest request) {
    	Session session = request.session();
		if (session.isRecording())
    		session.getRecorder().record(request.getParameter("step"));
    }

    public void stop(final HttpRequest request) {
    	Session session = request.session();
    	if (session.isRecording())
    		session.getRecorder().stop();
    	session.setIsRecording(false);
    }

    private void startRecorder(final HttpRequest request) {
        Session session = request.session();
        String dir = request.getParameter("dir");
        String fileName = request.getParameter("file");
        if (fileName.indexOf(".") == -1) {
            fileName = fileName + ".sah";
        }
        net.sf.sahi.record.Recorder recorder = session.getRecorder();
        recorder.setDir(dir);
        recorder.start(Utils.concatPaths(dir, fileName));
        session.setIsRecording(true);
//        session.setVariable("sahi_record", "1");
    }
}
