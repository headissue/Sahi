package com.sahi.command;

import java.io.IOException;
import java.util.Properties;

import com.sahi.config.Configuration;
import com.sahi.playback.FileScript;
import com.sahi.playback.SahiScriptHTMLAdapter;
import com.sahi.playback.ScriptFactory;
import com.sahi.playback.log.LogFileConsolidator;
import com.sahi.request.HttpRequest;
import com.sahi.response.HttpFileResponse;
import com.sahi.response.HttpResponse;
import com.sahi.response.NoCacheHttpResponse;
import com.sahi.response.SimpleHttpResponse;
import com.sahi.session.Session;
import com.sahi.test.SahiTestSuite;

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
		session.setScript(new ScriptFactory().getScript(
				Configuration.getScriptFileWithPath(fileName)));
		startPlayback(request, session);
	}	

	public void setScriptUrl(HttpRequest request){
		Session session = request.session();
		String url = request.getParameter("url");
		session.setScript(new ScriptFactory().getScript(url));
		startPlayback(request, session);
	}
	
	private void startPlayback(HttpRequest request, Session session) {
		if (session.getScript() != null)
			session.startPlayBack();
		session.setVariable("sahi_play", "1");
		session.setVariable("sahiPaused", "1");
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
		session.setScript(new FileScript(
				Configuration.getScriptFileWithPath(fileName)));
		String startUrl = request.getParameter("startUrl");
		session.setIsWindowOpen(false);
		session.startPlayBack();
		return proxyAutoResponse(startUrl, session.id());
	}	
	
	private HttpFileResponse proxyAutoResponse(String startUrl, String sessionId) {
		Properties props = new Properties();
		props.setProperty("startUrl", startUrl);
		props.setProperty("sessionId", sessionId);
		return new HttpFileResponse(Configuration.getHtdocsRoot()
				+ "spr/auto.htm", props);
	}

	
	class PlayerStopThread extends Thread {
		private final Session session;

		PlayerStopThread(Session session){
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
				waitASec();
				if (!suite.executeNext())
					consolidateLogs(session.getSuiteLogDir());
			} else {
				consolidateLogs(session.getScriptLogFile());
			}
		}
		
		private void waitASec() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
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
