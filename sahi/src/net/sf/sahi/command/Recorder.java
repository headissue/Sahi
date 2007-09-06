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

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.Utils;

public class Recorder {
    public void start(HttpRequest request) {
        startRecorder(request);
    }

    public void record(HttpRequest request) {
        request.session().getRecorder().record(request.getParameter("cmd"));
    }

    public void stop(HttpRequest request) {
        request.session().getRecorder().stop();
    }


    private void startRecorder(HttpRequest request) {
        Session session = request.session();
        String dir = request.getParameter("dir");
        String fileName = request.getParameter("file");
        if (fileName.indexOf(".") == -1) fileName = fileName + ".sah";
        net.sf.sahi.record.Recorder recorder = session.getRecorder();
        recorder.setDir(dir);
        recorder.start(Utils.concatPaths(dir, fileName));
        session.setVariable("sahi_record", "1");
    }
}
