package com.sahi.record;


/**
 * User: nraman Date: Jun 3, 2005 Time: 12:33:33 AM
 */
public class SahiScriptFormat implements ScriptFormat {
	public String getScript(String event, String jsAccessor, String value,
			String type, String shortHand, String popup) {
		String cmd = null;
		if (value == null)
			value = "";
		if ("load".equals(event)) {
			cmd = "_wait(2000);";
		} else if ("click".equals(event)) {
			String accessor = getAccessor(jsAccessor, shortHand, type);
			cmd = "_click(" + accessor + ");";
		} else if ("clicklink".equals(event)) {
			cmd = "_click(" + jsAccessor + "," + quote(value) +  ");";
		} else if ("setvalue".equals(event)) {
			String accessor = getAccessor(jsAccessor, shortHand, type);
			cmd = "_setValue(" + accessor + ", " + quote(value) +  ");";
		} else if ("setselected".equals(event)) {
			String accessor = getAccessor(jsAccessor, shortHand, type);
			cmd = "_setSelected(" + accessor + ", " + quote(value) +  ");";
		} else if ("assert".equals(event)) {
			String accessor = getAccessor(jsAccessor, shortHand, type);
			cmd = "_assertNotNull(" + accessor + ");\r\n";
			if ("cell".equals(type)) {
				cmd += "_assertEqual(" + quote(value) +  ", _getCellText(" + accessor
						+ "));";
			} else if ("select-one".equals(type)
					|| "select-multiple".equals(type)) {
				cmd += "_assertEqual(" + quote(value) +  ", _getSelectedText("
						+ accessor + "));";
			} else if ("text".equals(type) || "textarea".equals(type)
					|| "password".equals(type)) {
				cmd += "_assertEqual(" + quote(value) +  ", " + accessor + ".value);";
			} else if ("checkbox".equals(type) || "radio".equals(type)) {
				cmd += "_assert" + ("true".equals(value) ? "":"Not" ) +"True("+accessor + ".checked);";
			}
		} else if ("wait".equals(event)) {
			cmd = "_wait(" + value + ");";
		} else if ("mark".equals(event)) {
			cmd = "//MARK: " + value;
		} else if ("append".equals(event)) {
			cmd = value;
		}
		if (cmd != null && popup != null) {
			cmd = "_popup(\"" + popup + "\")." + cmd;
		}
//		System.out.println("assertEquals("+a(cmd)+", sahiScriptFormat.getScript("+a(event)+", "+a(jsAccessor)+", "+a(value)+", "+a(type)+", "+a(shortHand)+", "+a(popup)+"));");
		return cmd;
	}
	
//	private String a(String s) {
//		return s == null ? null : "\"" + s.replaceAll("\"", "\\\\\"") + "\"";
//	}

	private String quote(String value) {
		return "\"" + escape(value) + "\"";
	}

	protected String getAccessor(String jsAccessor, String shortHand, String type) {
		if ("".equals(shortHand)) {
			return "_accessor(\"" + jsAccessor + "\")";
		} else {
			if ("img".equals(type)) {
				return "_image(" + sahiQuoteIfString(shortHand) + ")";
			} else if ("image".equals(type)) {
				return "_imageSubmitButton(" + sahiQuoteIfString(shortHand) + ")";
			} else if ("link".equals(type)) {
				return "_link(" + sahiQuoteIfString(shortHand) + ")";
			} else if ("select-one".equals(type)
					|| "select-multiple".equals(type)) {
				return "_select(" + sahiQuoteIfString(shortHand) + ")";
			} else if ("text".equals(type)) {
				return "_textbox(" + sahiQuoteIfString(shortHand) + ")";
			} else if ("cell".equals(type)) {
				return "_cell(" + shortHand + ")";
			}else if ("byId".equals(type)) {
				return "_byId(" + shortHand + ")";
			}
			return "_" + type + "(" + sahiQuoteIfString(shortHand) + ")";
		}
	}

	String sahiQuoteIfString(String shortHand) {
		if (shortHand.matches("^[0-9]+$")) return shortHand;
		return "\"" + shortHand + "\"";
	}

	String escape(String s) {
		return s.replaceAll("[$]", "\\\\\\$")
			.replaceAll("\"", "\\\\\\\"")
			.replaceAll("\r", "\\\\r")
			.replaceAll("\n", "\\\\n");
	}
}
