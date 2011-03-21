/**
 * User: dlewis
 * Date: Dec 6, 2006
 * Time: 3:18:18 PM
 */
package net.sf.sahi.report;

import java.util.List;

import net.sf.sahi.test.TestLauncher;

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


public class TM6Reporter extends SahiReporter {

    public TM6Reporter(String logDir) {
        super(logDir, new TM6Formatter());
    }

    public void generateSuiteReport(List<TestLauncher> tests) {
    }

    public boolean createSuiteLogFolder() {
        return false;
    }
}
