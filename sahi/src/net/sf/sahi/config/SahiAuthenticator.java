package net.sf.sahi.config;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.logging.Logger;

import net.sf.sahi.playback.RequestCredentials;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.ThreadLocalMap;

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


public class SahiAuthenticator extends Authenticator {
	private static final Logger logger = Logger.getLogger("net.sf.sahi.config.SahiAuthenticator");
	
	protected PasswordAuthentication getPasswordAuthentication() {
		logger.info("getRequestingProtocol() = " + getRequestingProtocol());
		String host = getRequestingHost();
		int port = getRequestingPort();
		if (getRequestingProtocol().startsWith("http")
				&& Configuration.isHttpProxyEnabled()
				&& Configuration.isHttpProxyAuthEnabled()
				&& host.equals(Configuration.getHttpProxyHost())
				&& (""+port).equals(Configuration.getHttpProxyPort()))
			return new PasswordAuthentication(Configuration.getHttpProxyAuthName(), 
					Configuration.getHttpProxyAuthPassword().toCharArray());
		if (getRequestingProtocol().startsWith("http")
				&& Configuration.isHttpsProxyEnabled()
				&& Configuration.isHttpsProxyAuthEnabled()
				&& host.equals(Configuration.getHttpsProxyHost())
				&& (""+port).equals(Configuration.getHttpsProxyPort()))
			return new PasswordAuthentication(Configuration.getHttpsProxyAuthName(), 
					Configuration.getHttpsProxyAuthPassword().toCharArray());
		Session session = (Session) ThreadLocalMap.get("session");
		if (session != null){
			String realm = getRequestingPrompt();
			String scheme = getRequestingScheme();
			logger.info("realm=" + realm + "; getRequestingScheme()=" + scheme);
			
			RequestCredentials credentials = session.getMatchingCredentials(realm, scheme);
			if (credentials != null){
				logger.info("Using credentials supplied: " + credentials);
				return new PasswordAuthentication(credentials.username(), 
						credentials.password().toCharArray());
			}
			logger.fine("No credentials found. Should get prompt on browser.");
		}
		return null;
	}
}