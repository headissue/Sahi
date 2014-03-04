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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.BrowserTypesLoader;
import net.sf.sahi.util.Diagnostics;
import net.sf.sahi.util.ProxySwitcher;
import net.sf.sahi.util.Utils;

/**
 * 
 * Proxy manages Sahi's proxy server. <br/>
 * Proxy needs to know the base directory of Sahi. <br/>
 * This is configured by calling Configuration.init(String sahiBaseDir, String userDataDirectory) <br/>
 * 
<pre>
Usage:

String sahiBasePath = "D:\\path\\to\\sahi_dir";
Sting userDataDirectory = "D:\\path\\to\\userdata_dir"; // userdata_dir is in sahiBasePath/userdata by default

net.sf.sahi.config.Configuration.initJava(sahiBasePath, userDataDirectory);

proxy = new Proxy();
proxy.start(true); // true represents asynchronous. The proxy server will listen on a separate thread. 

// browser actions

proxy.stop();
</pre>
 * 
 */
public class Proxy {
	static Proxy currentInstance; 
	
    private int port = 9999;
	private ServerSocket server;
	private ExecutorService pool;

	private boolean forceClosed;

    public Proxy(int port) {
        this.port = port;
    }

    public Proxy() {
        this.port = Configuration.getPort();
    }

    public static void main(String[] args) {
    	if (args.length > 0){
    		Configuration.init(args[0], args[1]);
    	}else{
    		Configuration.init();
    	}
        final Proxy proxy = new Proxy(Configuration.getPort());
        currentInstance = proxy;
        try {
            Thread thread = new Thread(new ResetProxy());
            Runtime.getRuntime().addShutdownHook(thread);
            System.out.println("Added shutdown hook.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    	proxy.start(false);
    }

    public static void stopCurrentIntance(){
    	currentInstance.stop();
    }
    
    /**
     * Stops the proxy.
     */
    public void stop(){
    	if (server != null){
    		try {
    			forceClosed = true;
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
    /**
     * Starts the proxy.
     * @param asynch If true, starts Sahi's proxy in a separate thread.
     */
    public void start(boolean asynch){
		if (!asynch) {
			try {
				startProxy();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
	        try {
	            Runnable runnable = new Runnable(){
					public void run() {
						try {
							startProxy();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}};
				Thread thread = new Thread(runnable);
				thread.start();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
    }
    
    /**
    * Indicates whether this proxy server can receive connections from clients
    *
    * @return see above
    */
    public boolean isRunning() {
    	return server != null && !server.isClosed();
    }    

    private synchronized void startProxy() throws IOException {
        try {
        	byte[] probe = Utils.readURL("http://localhost:" + Configuration.getPort() + "/_s_/spr/probe.htm", false);
        	if (probe != null) {
        		System.out.println("---");
        		System.out.println("--- ERROR: Port " + Configuration.getPort() + " is already being used ---");
        		System.out.println("---");
        		return;
        	}
        	
            server = new ServerSocket();
            pool = Executors.newCachedThreadPool();

            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(port), 300);
            System.out.println(">>>> Sahi started. Listening on port: " + port);
            System.out.println(">>>> Configure your browser to use this server and port as its proxy");
            System.out.println(">>>> Browse any page and CTRL-ALT-DblClick on the page to bring up the Sahi Controller");
            
            BrowserTypesLoader.getAvailableBrowserTypes(true);
            
            new Thread(new Diagnostics()).start();
            while (true && !forceClosed && !server.isClosed()) {
                try {
                    Socket client = server.accept();
                    pool.execute(new ProxyProcessor(client));
                } catch (Exception e) {
                	System.out.println(e.getMessage());
//                    e.printStackTrace();
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
class ResetProxy implements Runnable {
	@Override
	public void run() {
		ProxySwitcher.revertSystemProxy(true);
	}
}
