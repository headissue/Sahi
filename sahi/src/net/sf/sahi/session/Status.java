package net.sf.sahi.session;

import net.sf.sahi.util.Utils;

import java.util.Map;
import java.util.HashMap;

/**
 * User: dlewis
 * Date: Nov 30, 2006
 * Time: 5:48:08 PM
 */
public class Status {
    private String name = null;

    private static final Map mapStatus = new HashMap();

    public String getName() {
        return name;
    }

    private Status(String name) {
        this.name = name;
    }

    private boolean equals(String name) {
        if (!Utils.isBlankOrNull(name)) {
            return this.name.equalsIgnoreCase(name);
        }
        return false;
    }

    public boolean equals(Object obj) {
        return this.equals(((Status) obj).getName());
    }

    public String toString() {
        return this.name;
    }

    public static Status getStatus(String name) {
         return (Status) mapStatus.get(name.toUpperCase());           
    }

    public static final Status RUNNING = new Status("RUNNING");

    public static final Status SUCCESS = new Status("SUCCESS");

    public static final Status FAILURE = new Status("FAILURE");

    public static final Status RETRY = new Status("RETRY");

    public static final Status INITIAL = new Status("INITIAL");
}