package net.sf.sahi.report;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dlewis
 */
public class ResultType {
    private String name = null;

    private static final Map mapType = new HashMap();

    public String getName() {
        return name;
    }

    private ResultType(String name) {
        this.name = name;
        mapType.put(name, this);
    }

    public static ResultType getType(String name) {
        return (ResultType) mapType.get(name.toUpperCase());
    }

    public static final ResultType FAILURE = new ResultType("FAILURE");

    public static final ResultType ERROR = new ResultType("ERROR");

    public static final ResultType INFO = new ResultType("INFO");

    public static final ResultType SUCCESS = new ResultType("SUCCESS");

    public static final ResultType CUSTOM = new ResultType("CUSTOM");

    public static final ResultType CUSTOM1 = new ResultType("CUSTOM1");

    public static final ResultType CUSTOM2 = new ResultType("CUSTOM2");

    public static final ResultType CUSTOM3 = new ResultType("CUSTOM3");

    public static final ResultType CUSTOM4 = new ResultType("CUSTOM4");

    public static final ResultType CUSTOM5 = new ResultType("CUSTOM5");
}
