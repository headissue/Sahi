package net.sf.sahi.playback.log;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

public class PlayBackLogFormatter extends Formatter {

	public String format(LogRecord record) {
		StringBuffer sb = new StringBuffer();
		Level level = record.getLevel();
		if(PlayBackLogLevel.START.equals(level)) {
			sb.append("<style>\r\n")
			.append(new String(Utils.readFile(Configuration.getPlaybackLogCSSFileName(true))))
			.append("</style>\r\n")
			.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/_s_/"+Configuration.getPlaybackLogCSSFileName(false)+"\">\r\n");
		}

		sb.append("<div class=\""+level.getName()+"\">");
		sb.append(createLink(record.getMessage(), level.getName()));
		sb.append("</div>\r\n");
		return sb.toString();
	}

	public String getFailureIndicator(){
		return "<div class=\""+PlayBackLogLevel.FAILURE.getName();
	}

	public String getSuccessIndicator(){
		return "<div class=\""+PlayBackLogLevel.SUCCESS.getName();
	}

	public String getErrorIndicator(){
		return "<div class=\""+PlayBackLogLevel.ERROR.getName();
	}

	public String getInfoIndicator(){
		return "<div class=\""+PlayBackLogLevel.INFO.getName();
	}

	String createLink(String s, String level) {
		int ix = s.lastIndexOf("[");
		String msg = s.substring(0, ix).trim();
		String href = s.substring(ix+1, s.lastIndexOf("]"));
		return "<a class=\""+level+"\" href=\"/_s_/dyn/Log_highlight?href="+href+"\">"+msg+"</a>";

	}

}
