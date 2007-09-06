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