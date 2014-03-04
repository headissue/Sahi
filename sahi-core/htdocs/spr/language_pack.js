/**
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
/** -- Java Recorder Start -- **/
if (_sahi.controllerMode == "java"){
	_sahi.controllerURL = "/_s_/spr/controllerAlt.htm";
	_sahi.controllerHeight = 330;
	_sahi.controllerWidth = 420;
	_sahi.recorderClass = "StepWiseRecorder";
	Sahi.prototype.getExpectPromptScript = function(s, retVal){
		return "browser." + this.getPopupDomainPrefixes() + "expectPrompt(" + this.quotedEscapeValue(s) + ", " + this.quotedEscapeValue(retVal) + ")";
	}
	Sahi.prototype.getExpectConfirmScript = function(s, retVal){
		return "browser." + this.getPopupDomainPrefixes() + "expectConfirm(" + this.quotedEscapeValue(s) + ", " + retVal + ")";
	}
	Sahi.prototype.getNavigateToScript = function(url){
		return "browser." + this.getPopupDomainPrefixes() + "navigateTo(" + this.quotedEscapeValue(url) + ");"
	}
	Sahi.prototype.getNavigateToWithSizeScript = function(url,name,size){
		return "browser." + this.getPopupDomainPrefixes() + "openWindow(" + this.quotedEscapeValue(url) + "," + this.quotedEscapeValue(name)+ ","+size+");"
	}
	Sahi.prototype.getScript = function (infoAr, el, evType, e) {
		var info = infoAr[0];
	    var accessor = this.escapeDollar(this.getAccessor1(info));
	    if (accessor == null) return null;
	    if (accessor.indexOf("_") == 0) accessor = accessor.substring(1);
	    var ev = info.event;
	    var value = info.value;
	    var type = info.type;
	    var popup = this.getPopupName();
	
	    var cmd = null;
	    if (value == null)
	        value = "";
	 // handle F12 and contextmenu
	    if (evType == "keydown") {
	    	if (e && e.keyCode >= 112 && e.keyCode <= 123 && !e.charCode){
	    		cmd =  accessor + ".keyPress(\"[" + e.keyCode + "," + 0 + "]\");";
	    	}
		    if (!cmd) return null;
	    } else { 
		    if (ev == "_click") {
		    	if (evType && evType.toLowerCase() == "contextmenu") {
		    		cmd = accessor + ".rightClick();";
		    	}
		    	else cmd = accessor + ".click();";
		    } else if (ev == "_setValue") {
		        cmd = accessor + ".setValue(" + this.quotedEscapeValue(value) + ");";
		    } else if (ev == "_setSelected") {
		        cmd = accessor + ".choose(" + this.quotedEscapeValue(value) + ");";
		    } else if (ev == "_setFile") {
		        cmd = accessor + ".setFile(" + this.quotedEscapeValue(value) + ");";
		    }
	    }
	    cmd = this.addPopupDomainPrefixes(cmd);
	    cmd = "browser." + cmd;
	    return cmd;
	};
	Sahi.prototype.escapeDollar = function (s) {
		return s;
	    if (s == null) return null;
	    return s.replace(/[$]/g, "\\$");
	};	
	Sahi.prototype.getAccessor1 = function (info) {
	    if (info == null) return null;
	    if ("" == (""+info.shortHand) || info.shortHand == null) return null;
	    var accessor = info.type + "(" + this.escapeForScript(info.shortHand) + ")";
	    if (accessor.indexOf("_") == 0) accessor = accessor.substring(1);
	    return accessor;
	};	
	_sahi.language = {
			ASSERT_EXISTS: "assertTrue(<accessor>.exists());",
			ASSERT_VISIBLE: "assertTrue(<accessor>.isVisible());",			
			ASSERT_EQUAL_TEXT: "assertEquals(<value>, <accessor>.text());",
			ASSERT_CONTAINS_TEXT: "assertTrue(<accessor>.text().contains(<value>));",
			ASSERT_EQUAL_VALUE: "assertEquals(<value>, <accessor>.value());",
			ASSERT_SELECTION: "assertEquals(<value>, <accessor>.selectedText());",
			ASSERT_CHECKED: "assertTrue(<accessor>.checked());",
			ASSERT_NOT_CHECKED: "assertFalse(<accessor>.checked());",
			POPUP: "popup(<window_name>).",
			DOMAIN: "domain(<domain>)."				
	};
	SflWrapper.convertToFn = function(s){
			var o = (typeof s == "string") ? eval("(" + s + ")") : s;
			return 'browser.flex("' + o.objectId + '").find(\"' + o.api + '\", ' + _sahi.toJSON(o.args[0]) + ')';
	};
	SflWrapper.prototype.getStep = function(action, accessorS, value){
		return "browser." + accessorS + "." + action + "(" + (value ? value : "") + ");"
	};
}
/** -- Java Recorder End -- **/

/** -- Ruby Recorder Start -- **/
if (_sahi.controllerMode == "ruby"){
	_sahi.controllerURL = "/_s_/spr/controllerAlt.htm";
	_sahi.controllerHeight = 250;
	_sahi.controllerWidth = 420;
	_sahi.recorderClass = "StepWiseRecorder";
	Sahi.prototype.getExpectPromptScript = function(s, retVal){
		return "browser." + this.getPopupDomainPrefixes() + "expect_prompt(" + this.quotedEscapeValue(s) + ", " + this.quotedEscapeValue(retVal) + ")";
	}
	Sahi.prototype.getExpectConfirmScript = function(s, retVal){
		return "browser." + this.getPopupDomainPrefixes() + "expect_confirm(" + this.quotedEscapeValue(s) + ", " + retVal + ")"
	}
	Sahi.prototype.getNavigateToScript = function(url){
		return "browser." + this.getPopupDomainPrefixes() + "navigate_to(" + this.quotedEscapeValue(url) + ")"
	}
	Sahi.prototype.getScript = function (infoAr, el, evType, e) {
		var info = infoAr[0];
	    var accessor = this.escapeDollar(this.getAccessor1(info));
	    if (accessor == null) return null;
	    if (accessor.indexOf("_") == 0) accessor = accessor.substring(1);
	    var ev = info.event;
	    var value = info.value;
	    var type = info.type;
	    var popup = this.getPopupName();
	
	    var cmd = null;
	    if (value == null)
	        value = "";
		 // handle F12 and contextmenu
	    if (evType == "keydown") {
	    	if (e && e.keyCode >= 112 && e.keyCode <= 123 && !e.charCode){
	    		cmd =  accessor + ".key_press(\"[" + e.keyCode + "," + 0 + "]\");";
	    	}
		    if (!cmd) return null;
	    } else { 	    
		    if (ev == "_click") {
		    	if (evType && evType.toLowerCase() == "contextmenu") {
		    		cmd =  accessor + ".right_click;";
		    	}
		    	else cmd = accessor + ".click";
		    } else if (ev == "_setValue") {
		        cmd = accessor + ".value = " + this.quotedEscapeValue(value);
		    } else if (ev == "_setSelected") {
		        cmd = accessor + ".choose(" + this.quotedEscapeValue(value) + ")";
		    } else if (ev == "_setFile") {
		        cmd = accessor + ".file = " + this.quotedEscapeValue(value);
		    }
	    }
	    cmd = this.addPopupDomainPrefixes(cmd);
	    cmd = "browser." + cmd;    
	    return cmd;
	};
	Sahi.prototype.escapeDollar = function (s) {
		return s;
	    if (s == null) return null;
	    return s.replace(/[$]/g, "\\$");
	};	
	Sahi.prototype.getAccessor1 = function (info) {
	    if (info == null) return null;
	    if ("" == (""+info.shortHand) || info.shortHand == null) return null;
	    var accessor = info.type + "(" + this.escapeForScript(info.shortHand) + ")";
	    if (accessor.indexOf("_") == 0) accessor = accessor.substring(1);
	    return accessor;
	};	
	_sahi.language = {
			ASSERT_EXISTS: "assert(<accessor>.exists?)",
			ASSERT_VISIBLE: "assert(<accessor>.visible?);",			
			ASSERT_EQUAL_TEXT: "assert_equal(<value>, <accessor>.text);",
			ASSERT_CONTAINS_TEXT: "assert(<accessor>.text.contains(<value>));",
			ASSERT_EQUAL_VALUE: "assert_equal(<value>, <accessor>.value);",
			ASSERT_SELECTION: "assert_equal(<value>, <accessor>.selected_text);",
			ASSERT_CHECKED: "assert(<accessor>.checked?);",
			ASSERT_NOT_CHECKED: "assert(!<accessor>.checked?);",
			POPUP: "popup(<window_name>).",
			DOMAIN: "domain(<domain>)."		
	};		
}
/** -- Ruby Recorder End -- **/

/** -- TestMaker Recorder Start -- **/
if (_sahi.controllerMode == "testmaker"){
	/* Selenium locatorBuilders start */
	eval(_sahi.sendToServer("/_s_/spr/ext/selenium/locatorBuilders.js"));
	var Log = function(){};
	Log.info = Log.warn = Log.prototype.exception = Log.prototype.error = Log.prototype.debug = function(s){};
	DummyBot = function(){this.locationStrategies=[]};
	DummyBot.prototype.findElement = function(locator){
		return _sahi._bySeleniumLocator(locator);
	}	
	LocatorBuilders.prototype.pageBot = function(){return new DummyBot();};
	/* Selenium locatorBuilders end */

	_sahi.recorderClass = "StepWiseRecorder";
	Sahi.prototype.getExpectPromptScript = function(s, retVal){
		return this.toJSON([this.getStepObj("expectPrompt", this.quotedEscapeValue(s), this.quotedEscapeValue(retVal))]);
	}
	Sahi.prototype.getExpectConfirmScript = function(s, retVal){
		return this.toJSON([this.getStepObj("expectConfirm", this.quotedEscapeValue(s), retVal)]);
	}
	Sahi.prototype.getNavigateToScript = function(url){
		return this.toJSON([this.getStepObj("navigateTo", "", this.quotedEscapeValue(url)), this.getStepObj("navigateTo", "", this.quotedEscapeValue(url), "selenium")]);
	}	
	Sahi.prototype.getStepObj = function(action, accessor, value, dialect, popupName){
		var toSend = new Object();
		toSend["popup"] = popupName ? popupName : "";
		toSend["dialect"] = dialect ? dialect : "sahi";
		toSend["action"] = action;
		toSend["accessor"] = accessor;
		toSend["value"] = value;
		return toSend;
		
	}
	Sahi.prototype.getScript = function (infoAr, el, evType, e) {
		var popupName = this.getPopupName();
		var toSendAr = new Array();
		if (evType == "keydown") {
	    	if (e && e.keyCode >= 112 && e.keyCode <= 123 && !e.charCode){
	    		for (var i=0; i<infoAr.length; i++){
	    			var info = infoAr[i];
	    			var accessor = this.escapeDollar(this.getAccessor1(info));
					action = "keyPress";
					value = this.toJSON("[" + e.keyCode + "," + 0 + "]");
					toSendAr[toSendAr.length] = this.getStepObj(action, accessor, value, "sahi", popupName);
	    		}
				return this.toJSON(toSendAr)
			} else return null;
	    } else {
			for (var i=0; i<infoAr.length; i++){
				try{
					var info = infoAr[i];
					var action = info.event.replace(/^_/, '');
					var accessor = this.escapeDollar(this.getAccessor1(info));
					var value = null;
					if (action == "setValue" || action == "setSelected" || action == "setFile"){
						var value = info.value;
					    if (value == null) value = "";
					    value = this.toJSON(value);
					}
					if (action == "click" && (evType && evType.toLowerCase() == "contextmenu")) action = "rightClick";
					toSendAr[toSendAr.length] = this.getStepObj(action, accessor, value, "sahi", popupName);
				}catch(e){}
			}	
			// Selenium accessors start
			var locators = this.getSeleniumAccessors(el);
			for (var i=0; i<locators.length; i++){
				try{
				toSendAr[toSendAr.length] = this.getStepObj(action, this.quotedEscapeValue(locators[i][0]), value, "selenium", popupName);
				} catch(e){_sahi._alert(e)}
			}
			// Selenium accessors end

			return this.toJSON(toSendAr);
	    }
	};
	Sahi.prototype.getSeleniumAccessors = function(el){
		var l = new LocatorBuilders(window);
		return l.buildAll(el);
	}
	Sahi.prototype.escapeDollar = function (s) {
		return s;
	    if (s == null) return null;
	    return s.replace(/[$]/g, "\\$");
	};	
	Sahi.prototype.getAccessor1 = function (info) {
	    if (info == null) return null;
	    if ("" == (""+info.shortHand) || info.shortHand == null) return null;
	    this.lastIdentifiedElementType = info.type;
	    var accessor = info.type + "(" + this.escapeForScript(info.shortHand) + ")";
	    if (accessor.indexOf("_") == 0) accessor = accessor.substring(1);
	    return accessor;
	};	
	Sahi.prototype.sendIdentifierInfo = function(accessors, escapedAccessor, escapedValue, popupName){
		var el = eval(this.addSahi("_" + escapedAccessor));
		var assertions = this.identify(el).assertions;
		var locators = this.getSeleniumAccessors(el);
		var selAccessors = [];
		for (var i=0; i<locators.length; i++){
			selAccessors[selAccessors.length] = this.quotedEscapeValue(locators[i][0]);
		}		
		
		var json = this.toJSON({type: this.lastIdentifiedElementType, accessors: {sahi: accessors,
		           selenium: selAccessors}, assertions: assertions, value: escapedValue});
		this.sendToServer("/_s_/dyn/Driver_setLastIdentifiedElement?element=" + encodeURIComponent(json));
	}
//	Sahi.prototype.openWin = function(){};
//	Sahi.prototype.openController = function(){};
	SflWrapper.prototype.getStep = function(action, accessorS, value){
		return _sahi.toJSON([_sahi.getStepObj(action, accessorS, value)]);
	}

	_sahi.language = {
			ASSERT_EXISTS: "assertExists__xxSAHIDIVIDERxx__<accessor>__xxSAHIDIVIDERxx__",
			ASSERT_VISIBLE: "assertVisible__xxSAHIDIVIDERxx__<accessor>__xxSAHIDIVIDERxx__",
			//ASSERT_EQUAL_TEXT: "assertEqual__xxSAHIDIVIDERxx__getText(<accessor>)__xxSAHIDIVIDERxx__<value>",
			ASSERT_CONTAINS_TEXT: "assertContainsText__xxSAHIDIVIDERxx__<value>__xxSAHIDIVIDERxx__<accessor>",
			ASSERT_EQUAL_VALUE: "assertEqual__xxSAHIDIVIDERxx__<accessor>.value__xxSAHIDIVIDERxx__<value>",
			//ASSERT_SELECTION: "assertEqual__xxSAHIDIVIDERxx__getSelectedText(<accessor>)__xxSAHIDIVIDERxx__<value>",
			//ASSERT_CHECKED: "assert__xxSAHIDIVIDERxx__<accessor>.checked__xxSAHIDIVIDERxx__",
			ASSERT_NOT_CHECKED: "assertChecked__xxSAHIDIVIDERxx__<accessor>__xxSAHIDIVIDERxx__",
			POPUP: "popup(<window_name>).",
			DOMAIN: "domain(<domain>)."		
	};		
}
/** -- TestMaker Recorder End -- **/