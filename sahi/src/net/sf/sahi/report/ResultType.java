/**
 * @author dlewis
 */
package net.sf.sahi.report;

import java.util.HashMap;
import java.util.Map;

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


public class ResultType {

    private String name = null;
    private static final Map<String, ResultType> mapType = new HashMap<String, ResultType>();

    public String getName() {
        return name;
    }

    private ResultType(final String name) {
        this.name = name;
        mapType.put(name, this);
    }

    public String toString(){
    	return name;
    }

    public static ResultType getType(final String name) {
        ResultType resultType = (ResultType) mapType.get(name.toUpperCase());
		if (resultType == null) return (ResultType) mapType.get("INFO");
        return resultType;
    }
    public static final ResultType FAILURE = new ResultType("FAILURE");
    public static final ResultType ERROR = new ResultType("ERROR");
    public static final ResultType INFO = new ResultType("INFO");
    public static final ResultType SUCCESS = new ResultType("SUCCESS");
    public static final ResultType SKIPPED = new ResultType("SKIPPED");
    public static final ResultType CUSTOM = new ResultType("CUSTOM");
    public static final ResultType CUSTOM1 = new ResultType("CUSTOM1");
    public static final ResultType CUSTOM2 = new ResultType("CUSTOM2");
    public static final ResultType CUSTOM3 = new ResultType("CUSTOM3");
    public static final ResultType CUSTOM4 = new ResultType("CUSTOM4");
    public static final ResultType CUSTOM5 = new ResultType("CUSTOM5");
	public static final ResultType RAW = new ResultType("RAW");
}
