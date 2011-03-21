package net.sf.sahi.client;

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
/**
 * BrowserCondition is used to specify conditional waits.
 * It is used in conjunction with browser.waitFor <br/><br/>
 *  
 * <pre>
   BrowserCondition condition = new BrowserCondition(browser){
		public boolean test() throws ExecutionException {
			return "populated".equals(browser.textbox("t1").value());
		}};
	browser.waitFor(condition, 5000);
 * </pre>
 * <br/>
 * The above code will make the browser wait till the textbox's value becomes "populated".<br/>
 * If it does not become "populated", the browser will wait for max 5000 ms 
 * before moving to the next step <br/>
 * <br/>
 * 
 */

public abstract class BrowserCondition {
	@SuppressWarnings("unused")
	private final Browser browser;
	public BrowserCondition(Browser browser) {
		this.browser = browser;
	}
	public abstract boolean test() throws ExecutionException;
}
