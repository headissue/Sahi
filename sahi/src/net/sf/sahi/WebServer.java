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

package net.sf.sahi;

import net.sf.sahi.config.Configuration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * User: nraman
 * Date: May 13, 2005
 * Time: 6:52:31 PM
 */
public class WebServer {
	static {
		Configuration.init("..", "../userdata/");
	}
    private int port = 10000;

    public WebServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        try {
            new WebServer(Configuration.getPort() + 1).startProxy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startProxy() throws IOException {
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
			System.out.println(">>>> Sahi demo web server started. Listening on port:"+port);
            while (true) {
                Socket client = server.accept();
                new Thread(new WebProcessor(client)).start();
            }
        } finally {
            if (server != null) server.close();
        }
    }
}