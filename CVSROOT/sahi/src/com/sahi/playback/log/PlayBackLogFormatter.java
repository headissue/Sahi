package com.sahi.playback.log;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class PlayBackLogFormatter extends Formatter {

	public String format(LogRecord record) {
		StringBuffer sb = new StringBuffer();
		if (PlayBackLogLevel.SUCCESS.equals(record.getLevel()) || 
				PlayBackLogLevel.INFO.equals(record.getLevel())) {
			sb.append("<div>");			
		}else {
			sb.append("<div style='background-color:red;color:white'>");
		}
		sb.append("\r\n");
		String message = formatMessage(record);
		sb.append(message);
//		sb.append("\t\t: ");
//		sb.append(record.getLevel().getLocalizedName());
		sb.append("</div>\r\n");
		return sb.toString();
	}
}
