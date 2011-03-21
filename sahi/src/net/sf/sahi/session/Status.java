package net.sf.sahi.session;

import net.sf.sahi.util.Utils;

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


/**
 * User: dlewis
 * Date: Nov 30, 2006
 * Time: 5:48:08 PM
 */
public class Status {

    private String name = null;
    private static final Map<String, Status> mapStatus = new HashMap<String, Status>();

    public String getName() {
        return name;
    }

    private Status(final String name) {
        this.name = name;
        mapStatus.put(name, this);
    }

    public boolean isDone(){
    	return this == Status.FAILURE || this == Status.SUCCESS || this == Status.ERROR || this == Status.ABORTED;	
    }
    
    public boolean equals(final String name) {
        return !Utils.isBlankOrNull(name) && this.name.equalsIgnoreCase(name);
    }

    public boolean equals(final Object obj) {
        return obj != null && this.equals(((Status) obj).getName());
    }

    public String toString() {
        return this.name;
    }

    public static Status getStatus(final String name) {
        return (Status) mapStatus.get(name.toUpperCase());
    }
    public static final Status RUNNING = new Status("RUNNING");
    public static final Status SUCCESS = new Status("SUCCESS");
    public static final Status FAILURE = new Status("FAILURE");
    public static final Status ABORTED = new Status("ABORTED");
    public static final Status ERROR = new Status("ERROR");
    public static final Status RETRY = new Status("RETRY");
    public static final Status INITIAL = new Status("INITIAL");
    public static final Status NOT_SUPPORTED = new Status("NOT_SUPPORTED");
}