package net.sf.sahi.playback;

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


public class RequestCredentials {
	String realm;
	String username;
	String password;
	private int used = 0;
	
	public RequestCredentials(String url, String username, String password) {
		this.realm = url;
		this.username = username;
		this.password = password;

	}
	
	public boolean used(){
		used++;
		if (used > 1) {
			return true;
		}
		else { 
			return false;
		}
	}

	public String url() {
		return realm;
	}

	public String username() {
		return username;
	}

	public String password() {
		return password;
	}

	public String toString() {
		return "realm="+realm+"; username="+username+";";
	}
}
