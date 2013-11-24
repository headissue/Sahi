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
function poll() {
	try {
		
		var json = sahiSendToServer("/_s_/dyn/ControllerUI_getMessageToController");
		// var json =
		// sahiSendToServer("/_s_/dyn/Driver_getLastIdentifiedElement");
		
		if (json != "") {
			var res = eval("(" + json + ")");
			if (res) {
				if (res.command == "showAccessor") {
					var o = res.value;
					displayInfo(o.accessors, o.accessor, o.value, o.popupName, o.assertions);
				} else if (res.command == "showResult") {
					$(res.target).value = "" + res.value;
				} else if (res.command == "showSteps") {
					showSteps(res.value, res.isRecorded);
				} else if (res.command == "setAccessorProps") {
					setAccessorProps(res.value);
				}
				// logs
				if (res.logs && $("talogs")) {
					if ($("talogs").value != res.logs) {
							$("talogs").value = res.logs;
							scrollDown($("talogs"));
					}
				}
			}
		}
	} catch (e) {
	}
	window.setTimeout("poll()", 500);
}

function sendMessage(o, returnResult, addSahi) {
	if (typeof o == 'string') {
		o = {
			command : 'eval',
			value : o
		};
	}
	o.returnResult = returnResult;
	var s = util.toJSON(o);
	var context = $("prefix").value;
	if (context != "")
		context = "_sahi." + context;
	sahiSendToServer("/_s_/dyn/ControllerUI_setMessageFromController?msg="
			+ encodeURIComponent(s) + "&addSahi=" + addSahi + "&context="
			+ encodeURIComponent(context));
}
function Util() {
}
Util.escapeMap = {
	'\b' : '\\b',
	'\t' : '\\t',
	'\n' : '\\n',
	'\f' : '\\f',
	'\r' : '\\r',
	'"' : '\\"',
	'\\' : '\\\\'
};
Util.prototype.toJSON = function(el, map) {
	// try {
	if (!map)
		map = new SahiHashMap();
	var j = map.get(el);
	if (j && j == "___in_progress___") {
		return '"recursive_access"';
	}
	map.put(el, '___in_progress___');
	var v = this.toJSON2(el, map);
	map.put(el, v);
	return v;
	// } catch (e) {
	// return "error during toJSON conversion";
	// }
}
Util.prototype.toJSON2 = function(el, map) {
	if (el == null || el == undefined)
		return 'null';
	if (el instanceof RegExp)
		return el.toString();
	if (el instanceof Date) {
		return String(el);
	} else if (typeof el == 'string') {
		if (/["\\\x00-\x1f]/.test(el)) {
			return '"'
					+ el.replace(/([\x00-\x1f\\"])/g, function(a, b) {
						var c = Util.escapeMap[b];
						if (c) {
							return c;
						}
						c = b.charCodeAt();
						return '\\u00' + Math.floor(c / 16).toString(16)
								+ (c % 16).toString(16);
					}) + '"';
		}
		return '"' + el + '"';
	} else if (el instanceof Array) {
		var ar = [];
		for ( var i = 0; i < el.length; i++) {
			ar[i] = this.toJSON(el[i], map);
		}
		return '[' + ar.join(',') + ']';
	} else if (typeof el == 'number') {
		return new String(el);
	} else if (typeof el == 'boolean') {
		return String(el);
	} else if (el instanceof Object) {
		// if (el.tagName) {
		// var elInfo = this.identify(el, true);
		// if (elInfo == null || elInfo.apis == null) return null;
		// return (elInfo.apis.length > 0) ? "_sahi." +
		// this.escapeDollar(this.getAccessor1(elInfo.apis[0])) : null;
		// } else {
		var ar = [];
		for ( var k in el) {
			var v = el[k];
			if (typeof v != 'function') {
				ar[ar.length] = this.toJSON(k, map) + ':' + this.toJSON(v, map);
			}
		}
		return '{' + ar.join(',') + '}';
		// }
	}
};
SahiHashMap = function() {
	this.keys = new Array();
	this.values = new Array();
	this.put = function(k, v) {
		var i = this.getIndex(this.keys, k);
		if (i == -1)
			i = this.keys.length;
		this.keys[i] = k;
		this.values[i] = v;
	}
	this.get = function(k) {
		var i = this.getIndex(this.keys, k);
		return this.values[i];
	}
	this.getIndex = function(ar, k) {
		for ( var i = 0; i < ar.length; i++) {
			if (k === ar[i])
				return i;
		}
		return -1;
	}
}
var util = new Util();