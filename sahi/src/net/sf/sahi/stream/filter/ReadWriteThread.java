package net.sf.sahi.stream.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sahi.util.Utils;

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


public class ReadWriteThread implements Runnable {
	private InputStream in;
	private OutputStream out;
	private int contentLength;
	private boolean closeOut = false;
	@SuppressWarnings("unused")
	private final String name;

	public ReadWriteThread(InputStream in, OutputStream out, String name){
		this(in, out, -1, false, name);
	}

	public ReadWriteThread(InputStream in, OutputStream out, int contentLength, boolean closeOut, String name){
		this.in = in;
		this.out = out;
		this.contentLength = contentLength;
		this.closeOut = closeOut;
		this.name = name;
	}

    public void run() {
    	int SIZE = Utils.BUFFER_SIZE;
        byte[] buffer = new byte[SIZE];
        int totalBytesRead = 0, bytesRead = -1;
        try {
        	if (contentLength == 0) {
        		out.flush();
        		if (closeOut) out.close();
        		return;
        	}
            while(true) {
            	bytesRead = -1;
            	if (contentLength != -1){
            		int limit = contentLength - totalBytesRead;
            		if (limit > SIZE) limit = SIZE;
            		try{
            			bytesRead = in.read(buffer, 0, limit);
            		} catch (IOException e) {
            			e.printStackTrace();
            			out.close();
            			System.out.println("Closing and returning");
            			return;
            		}
            	}else{
            		try{
            			bytesRead = in.read(buffer);
            		} catch (IOException e) {
            			e.printStackTrace();
            		}
            	}

                if (bytesRead == -1) break;
                totalBytesRead += bytesRead;
                if (bytesRead != 0){
	                out.write(buffer, 0, bytesRead);
                }
	            out.flush();
                if (totalBytesRead == contentLength) break;
            }
        }
        catch (IOException e) {
        	try {
				in.close();
			} catch (IOException e1) {
//				e1.printStackTrace();
			}
//            e.printStackTrace();
        }
        finally {
        	try {
        		if (closeOut) out.close();
        	} catch (IOException e) {
//        		e.printStackTrace();
        	}
        }
    }
}
