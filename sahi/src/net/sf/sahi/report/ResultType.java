package net.sf.sahi.report;

import net.sf.sahi.util.Utils;

/**
 * @author dlewis
 * 
 */
public class ResultType {
	private String name = null;

	public String getName() {
		return name;
	}

	private ResultType() {
	}

	private ResultType(String name) {
		this.name = name;
	}

	private boolean equals(String name) {
		if (!Utils.isBlankOrNull(name)) {
			return this.name.equalsIgnoreCase(name);
		}
		return false;
	}

	public static ResultType FAILURE = new ResultType("FAILURE");

	public static ResultType ERROR = new ResultType("ERROR");

	public static ResultType INFO = new ResultType("INFO");

	public static ResultType SUCCESS = new ResultType("SUCCESS");

	public static ResultType getType(String name) {
		ResultType returnType = null;
		if (FAILURE.equals(name)) {
			returnType = FAILURE;
		} else if (ERROR.equals(name)) {
			returnType = ERROR;
		} else if (INFO.equals(name)) {
			returnType = INFO;
		} else if (SUCCESS.equals(name)) {
			returnType = SUCCESS;
		}
		return returnType;
	}

}
