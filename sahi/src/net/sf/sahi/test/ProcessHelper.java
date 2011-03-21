package net.sf.sahi.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import net.sf.sahi.util.OSUtils;
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


public class ProcessHelper {
	private static final Logger logger = Logger.getLogger("net.sf.sahi.test.ProcessHelper");
	private String cmd;

	private ArrayList<String> pids;

	private Process process;

	private String imageName;

	public ProcessHelper(String cmd, String imageName) {
		this.cmd = cmd;
		this.imageName = imageName;
	}

	static Semaphore lock = new Semaphore(1, true);
	static long t;

	private static boolean hasProcessStarted; 

	public void execute() {
		try {
			try {
				lock.acquire();
				hasProcessStarted = false;
				t = System.currentTimeMillis();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			ArrayList<String> allPIDsBefore = getPIDs();
			logger.info(cmd);
			String[] tokens = Utils.getCommandTokens(cmd.replaceAll("%20", " "));
//			for (int i = 0; i < tokens.length; i++) {
//				System.out.println(tokens[i]);
//			}
			process = Utils.executeAndGetProcess(tokens);
			new Thread(new PIDGatherer(allPIDsBefore)).start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	class PIDGatherer implements Runnable{
		private final ArrayList<String> allPIDsBefore;
		PIDGatherer(ArrayList<String> allPIDsBefore){
			this.allPIDsBefore = allPIDsBefore;
		}
		public void run() {
			int maxTime = 30000;
			int time = 0;
			int interval = 100;
			while (!hasProcessStarted && time < maxTime){
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				time += interval;			
			}
			try{
				ArrayList<String> allPIDsAfter = getPIDs();
				pids = getNewlyAdded(allPIDsBefore, allPIDsAfter);					
			}catch(Exception e){}
			logger.info("PIDs: " + pids + "; " + (System.currentTimeMillis() - t) + " ms");		
			lock.release();
		}		
	}
	
	private ArrayList<String> getNewlyAdded(ArrayList<String> oldPIDs, ArrayList<String> newPIDs) {
		ArrayList<String> newlyAdded = new ArrayList<String>();
		Iterator<String> it1 = newPIDs.iterator();
		while (it1.hasNext()) {
			String pid = (String) it1.next();
			if (!oldPIDs.contains(pid)) {
				newlyAdded.add(pid);
			}
		}
		return newlyAdded;
	}

	ArrayList<String> getPIDs() {
		ArrayList<String> ar = new ArrayList<String>();
		try {
			String listCmd = OSUtils.getPIDListCommand().replaceAll("\\$imageName", imageName);
			Process p = Runtime.getRuntime().exec(Utils.getCommandTokens(listCmd));
			InputStream in = p.getInputStream();
			StringBuffer sb = new StringBuffer();
			byte c;
			while ((c = (byte) in.read()) != -1) {
				sb.append((char) c);
			}
			String output = sb.toString();
			StringTokenizer tokenizer = new StringTokenizer(output, "\r\n");
			while (tokenizer.hasMoreTokens()) {
				String line = tokenizer.nextToken();
				// System.out.println(line);
				line = line.replaceAll("\\s+", " ").trim();
				// System.out.println(line);
				if (line.equals(""))
					break;
				StringTokenizer spaceTokenizer = new StringTokenizer(line);
				int i=0;
				int colNo = OSUtils.getPIDListColumnNo();
				boolean hasCol = false;
				String pid = null;
				while (spaceTokenizer.hasMoreTokens()) {
					i++;
					pid = spaceTokenizer.nextToken();
					if (i == colNo) {
						hasCol = true;
						break;
					}
				}
				if (hasCol){
					ar.add(pid);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ar;
	}

	public static void main(String[] args) {

	}

	public void kill() {
		logger.info("Kill: " + pids);
		for (int i = 0; i<4; i++){
			killPIDs();
			if (waitTillPIDsGone(pids, 3000)) return;
		}
	}

	private void killPIDs() {
//		process.destroy();
		Iterator<String> it = pids.iterator();
		while (it.hasNext()) {
			String pid = (String) it.next();
			try {
				String killCmd = OSUtils.getPIDKillCommand().replaceAll("\\$pid", pid);
				Process killer = Runtime.getRuntime().exec(Utils.getCommandTokens(killCmd));
				killer.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean waitTillPIDsGone(ArrayList<String> pids2, int maxTime) {
		int time = 0;
		int interval = 0;
		while (time <= maxTime) {
			ArrayList<String> allPIDs = getPIDs();
//			System.out.println(pids2 + " " + allPIDs);
			Iterator<String> it = pids2.iterator();
			boolean allDone = true;
			while (it.hasNext()) {
				String pid = (String) it.next();
				if (allPIDs.contains(pid)) {
					allDone = false;
				}
			}
			if (allDone) return true;
			interval += 100;
			time += interval;
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
		return false;
	}

	public static void setProcessStarted() {
		hasProcessStarted = true;
	}
}
