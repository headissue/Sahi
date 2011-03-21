/**
 * @author dlewis
 *
 */
package net.sf.sahi.report;

import java.util.Date;

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


public class TestResult {

    String message = null;
    String debugInfo = null;
    String failureMsg = null;
    ResultType type = null;
	Date time;

    public TestResult(final String message, final ResultType type, final String debugInfo,
            final String failureMsg) {
        this.message = message;
        this.type = type;
        this.debugInfo = debugInfo;
        this.failureMsg = failureMsg;
        this.time = new Date();
    }
}
