package net.sf.sahi.command;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.Utils;
import net.sf.sahi.record.*;

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
