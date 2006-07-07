package net.sf.sahi.command;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.playback.FileScript;
import net.sf.sahi.playback.SahiScriptHTMLAdapter;
import net.sf.sahi.playback.ScriptFactory;
import net.sf.sahi.playback.log.LogFileConsolidator;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.session.Session;
import net.sf.sahi.test.SahiTestSuite;

import java.io.IOException;
import java.util.Properties;

public class Player {
    public void start(HttpRequest request) {
        startPlayback(request, request.session());
    }

    public void stop(HttpRequest request) {
        request.session().getRecorder().stop();
        new PlayerStopThread(request.session()).start();
    }

    public void setScriptFile(HttpRequest request) {
        Session session = request.session();
        String fileName = request.getParameter("file");
        session.setScript(new ScriptFactory().getScript(fileName));
//		session.setScript(new ScriptFactory().getScript(
//				Configuration.getScriptFileWithPath(fileName)));
        startPlayback(request, session);
    }

    public void setScriptUrl(HttpRequest request) {
        Session session = request.session();
        String url = request.getParameter("url");
        session.setScript(new ScriptFactory().getScript(url));
        startPlayback(request, session);
    }

    private void startPlayback(HttpRequest request, Session session) {
        if (session.getScript() != null)
            session.startPlayBack();
        session.setVariable("sahi_play", "1");
        session.setVariable("sahi_paused", "1");
    }

    public HttpResponse currentScript(HttpRequest request) {
        Session session = request.session();
        HttpResponse httpResponse;
        if (session.getScript() != null) {
            httpResponse = new SimpleHttpResponse("<pre>"
                    + SahiScriptHTMLAdapter.createHTML(session
                    .getScript().getOriginal()) + "</pre>");
        } else {
            httpResponse = new SimpleHttpResponse(
                    "No Script has been set for playback.");
        }
        return httpResponse;
    }

    public HttpResponse currentParsedScript(HttpRequest request) {
        Session session = request.session();
        HttpResponse httpResponse;
        if (session.getScript() != null) {
            httpResponse = new SimpleHttpResponse("<pre>"
                    + SahiScriptHTMLAdapter.createHTML(session
                    .getScript().modifiedScript()) + "</pre>");
        } else {
            httpResponse = new SimpleHttpResponse(
                    "No Script has been set for playback.");
        }
        return httpResponse;
    }


    public HttpResponse script(HttpRequest request) {
        Session session = request.session();
        String s = (session.getScript() != null) ? session.getScript()
                .modifiedScript() : "";
        return new NoCacheHttpResponse(s);
    }


    public HttpResponse auto(HttpRequest request) {
        Session session = request.session();
        String fileName = request.getParameter("file");

        final String scriptFileWithPath;
        scriptFileWithPath = fileName;
        session.setScript(new FileScript(
                scriptFileWithPath));
        String startUrl = request.getParameter("startUrl");
        session.setIsWindowOpen(false);
        session.startPlayBack();
        return proxyAutoResponse(startUrl, session.id());
    }


    public void success(HttpRequest request) {
        Session session = request.session();
        new SessionState().setVar("sahi_retries", "0", session);
        new Log().execute(request);
    }

    private HttpFileResponse proxyAutoResponse(String startUrl, String sessionId) {
        Properties props = new Properties();
        props.setProperty("startUrl", startUrl);
        props.setProperty("sessionId", sessionId);
        return new HttpFileResponse(Configuration.getHtdocsRoot()
                + "spr/auto.htm", props, false, true);
    }


    class PlayerStopThread extends Thread {
        private final Session session;

        PlayerStopThread(Session session) {
            this.session = session;
        }

        public void run() {
            stopPlay();
        }

        private void stopPlay() {
            if (session.getScript() != null)
                session.stopPlayBack();
            SahiTestSuite suite = SahiTestSuite.getSuite(session.id());
            if (suite != null) {
                suite.stop(session.getScript().getScriptName());
                waitSomeTime();
                if (!suite.executeNext())
                    consolidateLogs(session.getSuiteLogDir());
            } else {
                consolidateLogs(session.getScriptLogFile());
            }
        }

        private void waitSomeTime() {
            try {
                Thread.sleep(Configuration.getTimeBetweenTestsInSuite());
            } catch (Exception e) {

            }

        }

        private void consolidateLogs(String consolidateBy) {
            try {
                new LogFileConsolidator(consolidateBy).summarize();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
