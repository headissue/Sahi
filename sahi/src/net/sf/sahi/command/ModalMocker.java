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

import java.util.Properties;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;

public class ModalMocker {

	public HttpResponse alert(HttpRequest requestFromBrowser) {
		return proxyAlertResponse(requestFromBrowser.getParameter("msg"));
	}

	public HttpResponse confirm(HttpRequest requestFromBrowser) {
		return proxyConfirmResponse(requestFromBrowser.getParameter("msg"));
	}

	private HttpFileResponse proxyAlertResponse(String msg) {
		Properties props = new Properties();
		props.setProperty("msg", msg);
		return new HttpFileResponse(Configuration.getHtdocsRoot()
				+ "spr/alert.htm", props, false, true);
	}

	private HttpFileResponse proxyConfirmResponse(String msg) {
		Properties props = new Properties();
		props.setProperty("msg", msg);
		return new HttpFileResponse(Configuration.getHtdocsRoot()
				+ "spr/confirm.htm", props, false, true);
	}
}
