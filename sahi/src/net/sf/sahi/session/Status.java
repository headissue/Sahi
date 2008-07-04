package net.sf.sahi.session;

import net.sf.sahi.util.Utils;

import java.util.HashMap;
import java.util.Map;

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

    private Status(final String name) {
        this.name = name;
        mapStatus.put(name, this);
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
    public static final Status RETRY = new Status("RETRY");
    public static final Status INITIAL = new Status("INITIAL");
}