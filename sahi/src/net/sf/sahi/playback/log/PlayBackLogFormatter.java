/**
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
			.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/_s_/"+Configuration.getPlaybackLogCSSFileName(false)+"\"/>\r\n");
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
