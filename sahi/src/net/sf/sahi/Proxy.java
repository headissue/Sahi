/**
 * Copyright V Narayan Raman
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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * User: nraman Date: May 13, 2005 Time: 6:52:31 PM Starts HTTP Proxy
 */
public class Proxy {
    private int port = 9999;

    public Proxy(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        try {
            new Proxy(Configuration.getPort()).startProxy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startProxy() throws IOException {
        ServerSocket server = null;
        try {
            server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(port));
            System.out.println(">>>> Sahi started. Listening on port:" + port);
            System.out.println(">>>> Configure your browser to use this server and port as its proxy");
            System.out.println(">>>> Browse any page and CTRL-ALT-DblClick on the page to bring up the Sahi Controller");
//			System.out.println(">>>> For troubleshooting, visit http://sahi.sourceforge.net/diagnostics");
            while (true) {
                try {
                    Socket client = server.accept();
                    new Thread(new ProxyProcessor(client)).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (server != null)
                server.close();
        }
    }
}
