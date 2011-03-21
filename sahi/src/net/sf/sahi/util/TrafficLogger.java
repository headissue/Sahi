package net.sf.sahi.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.sahi.config.Configuration;

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


public class TrafficLogger {
	static String trafficDir;
	static {
		String logsRoot = Configuration.getLogsRoot();
		trafficDir = logsRoot + "/traffic";
	}
	
	private String reqFileName;
	private File threadDir;
	private final String type;
	private final boolean log;
	private final Date time;

	public TrafficLogger(String reqFileName, String type, boolean log, final Date time) {
		this.type = type;
		this.log = log;
		this.time = time;
		if (log) init(reqFileName);
	}


	private void init(String reqFileName) {
		this.reqFileName = FileUtils.cleanFileName(reqFileName);
		this.threadDir = getThreadDir();
	}


	protected synchronized String createThreadId() {
		return new SimpleDateFormat("HH_mm_ss_SSS").format(time);
	}

	private void storeRequestHeader(byte[] bytes) {
		store(bytes, "request.header_" + type + ".txt");
	}

	private void storeRequestBody(byte[] bytes) {
		store(bytes, "request.body_" + type + ".txt");
	}

	private void storeResponseHeader(byte[] bytes) {
		store(bytes, "response.header_" + type + ".txt");
	}

	private void storeResponseBody(byte[] bytes) {
		store(bytes, "response.body"  + "_" + type + (reqFileName==null?"":"_"+reqFileName));
	}

	private void store(byte[] bytes, String fileName) {
		if (!log) return;
		if (bytes == null) return;
		File file = new File(threadDir, fileName);
		FileOutputStream out = null;
		try {
			if (!file.exists()) file.createNewFile();
			out = new FileOutputStream(file, true);
			out.write(bytes);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private File getThreadDir() {
		String threadId = createThreadId();
		File threadDir = new File(trafficDir + "/" + (new SimpleDateFormat("yyyy_MM_dd").format(time)) + "/" + threadId + (reqFileName==null?"":"_"+reqFileName));
		threadDir.mkdirs();
		return threadDir;
	}

	public static TrafficLogger getLoggerForThread(String type){
		return (TrafficLogger) ThreadLocalMap.get("logger_" + type);
	}


	public static void createLoggerForThread(String fileName, String type, boolean log, Date time) {
		TrafficLogger logger = new TrafficLogger(fileName, type, log, time);
		ThreadLocalMap.put("logger_" + type, logger);		
	}
	
	public static void storeRequestHeader(byte[] bytes, String type) {
		final TrafficLogger loggerForThread = getLoggerForThread(type);
		if (loggerForThread != null) loggerForThread.storeRequestHeader(bytes);
	}
	
	public static void storeRequestBody(byte[] bytes, String type) {
		final TrafficLogger loggerForThread = getLoggerForThread(type); 
		if (loggerForThread != null) loggerForThread.storeRequestBody(bytes);
	}	
	
	public static void storeResponseHeader(byte[] bytes, String type) {
		final TrafficLogger loggerForThread = getLoggerForThread(type); 
		if (loggerForThread != null) loggerForThread.storeResponseHeader(bytes);
	}	
	
	public static void storeResponseBody(byte[] bytes, String type) {
		final TrafficLogger loggerForThread = getLoggerForThread(type); 
		if (loggerForThread != null) loggerForThread.storeResponseBody(bytes);
	}
}