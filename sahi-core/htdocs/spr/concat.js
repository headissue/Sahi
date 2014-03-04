/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 */

__sahiDebug__("concat.js: start");
var Sahi = function(){
	// if triggerType is set to mousedown, textbox _setValues will be recorded AFTER button clicks. Is wrong
    this.triggerType = "click";
    this.cmds = new Array();
    this.cmdDebugInfo = new Array();

    this.cmdsLocal = new Array();
    this.cmdDebugInfoLocal = new Array();

    this.promptReturnValue = new Array();

    this.locals = [];

    this.INTERVAL = 100;
    this.ONERROR_INTERVAL = 1000;
    this.MAX_RETRIES = 5;
    this.SAHI_MAX_WAIT_FOR_LOAD = 30;
    this.STABILITY_INDEX = 5;
    this.waitForLoad = this.SAHI_MAX_WAIT_FOR_LOAD;
    this.interval = this.INTERVAL;

    this.localIx = 0;
    this.buffer = "";

    this.controller = null;
    this.lastAccessedInfo = null;
    this.execSteps = null; // from SahiScript through script.js

    this.sahiBuffer = "";

    this.real_alert = window.alert;
    this.real_confirm = window.confirm;
    this.real_prompt = window.prompt;
    this.real_print = window.print;
    this.wrapped = new Array();
    this.mockDialogs(window);
    
    this.lastQs = "";
    this.lastTime = 0;
    
    this.XHRs = [];
    this.XHRTimes = [];
    this.escapeMap = {
        '\b': '\\b',
        '\t': '\\t',
        '\n': '\\n',
        '\f': '\\f',
        '\r': '\\r',
        '"' : '\\"',
        '\\': '\\\\'
    };
    this.lastStepId = 0;
    this.strictVisibilityCheck = false;
    this.isSingleSession = false;
    this.ADs = [];
    this.controllerURL = "/_s_/spr/controller7.htm";
    this.controllerHeight = 550;
    this.controllerWidth = 480
    this.recorderClass = "Recorder";
    this.stabilityIndex = this.STABILITY_INDEX;
    this.xyoffsets = new Sahi.Dict();
    this.escapeUnicode = false;
    this.CHECK_REGEXP = /^\/.*\/i?$/;
    this.navigator = {"userAgent": navigator.userAgent, "appName": navigator.appName};
    this.textboxTypes = ["text", "number", "password", "textarea", "date", "datetime", "datetime-local", "email", "month", "number", "range", "search", "tel", "time", "url", "week" ];
    this.isOREnabled = false;
    this.browserType = "";
};
Sahi.prototype.isBlankOrNull = function (s) {
    return (s == "" || s == null);
};
Sahi.Dict = function(){
	this.keys = [];
	this.values = [];
	this.put = function (k,v) {
		this.keys.push(k);
		this.values.push(v);
	}
	this.get = function (k){
		for (var i=0; i<this.keys.length; i++) {
			if (this.keys[i] === k) return this.values[i];
		}
	}
}
Sahi.BLUR_TIMEOUT = 5000;
Sahi.DRAG_DROP_SPEED = 1;
Sahi.DRAG_DROP_SEGMENTS = 200;
Sahi.DRAG_DROP_WAVER = 1;
Sahi.DRAG_DROP_MAX_HOVER_AT_END = 5;
Sahi.prototype.storeDiagnostics = function(){
	if (this.diagnostics) return;
    this.diagnostics = new Object();
    var d = this.diagnostics;
    d["UserAgent"] = navigator.userAgent;
    d["Browser Name"] = navigator.appName;
    d["Browser Version"] = navigator.appVersion.substring(0, navigator.appVersion.indexOf(")")+1);
    d["Native XMLHttpRequest"] = typeof XMLHttpRequest != "undefined";
    d["Java Enabled"] = navigator.javaEnabled();
    d["Cookie Enabled"] =  ("" + document.cookie).indexOf("sahisid") != -1 // navigator.cookieEnabled throws an exception on IE on showModalDialogs.
	this.addDiagnostics("OS");
	this.addDiagnostics("Java");
};
Sahi.prototype.addDiagnostics = function(type){
	var s = this.sendToServer("/_s_/dyn/ControllerUI_get"+type+"Info");		
    if(s){
    	var properties = s.split("_$sahi$_;");
    	for (var i=0; i<properties.length; i++){
    		var prop = properties[i].split("_$sahi$_:");
    		if(prop.length == 2) this.diagnostics[prop[0]] = prop[1];
    	}	
    }	 
};
Sahi.prototype.getDiagnostics = function(name){
	if (!this.diagnostics) this.storeDiagnostics();
    if(name){
     	var v = this.diagnostics[name];
     	return (v != null) ? v : "";
    }
    var s = "";
 	for (var key in this.diagnostics){
    	s += key +": "+ this.diagnostics[key]+"\n";
 	}
    return s;
};
Sahi.prototype.wrap = function (fn) {
	var el = this;
	if (this.wrapped[fn] == null) {
		this.wrapped[fn] = function(){return fn.apply(el, arguments);};
	}
	return this.wrapped[fn];
};
Sahi.prototype.alertMock = function (s) {
    if (this.isPlaying()) {
        this.setServerVar("lastAlertText", s);
        return null;
    } else {
        return this._alert(s);
    }
};
Sahi.prototype.confirmMock = function (s) {
    if (this.isPlaying()) {
        var retVal = eval(this.getServerVar("confirm: "+s));
        if (retVal == null) retVal = true;
        this.setServerVar("lastConfirmText", s);
        this.setServerVar("confirm: "+s, null);
        return retVal;
    } else {
        var retVal = this.callFunction(this.real_confirm, window, s);
        if (this.isRecording()){
        	this.recordStep(this.getExpectConfirmScript(s, retVal));
        }
        return retVal;
    }
};
Sahi.prototype.getExpectPromptScript = function(s, retVal){
	return "_expectPrompt(" + this.quotedEscapeValue(s) + ", " + this.quotedEscapeValue(retVal) + ")";
}
Sahi.prototype.getExpectConfirmScript = function(s, retVal){
	return "_expectConfirm(" + this.quotedEscapeValue(s) + ", " + retVal + ");"
}
Sahi.prototype.getNavigateToScript = function(url){
	return "_navigateTo(" + this.quotedEscapeValue(url) +");";
}
Sahi.prototype.promptMock = function (s) {
    if (this.isPlaying()) {
        var retVal = this.getServerVar("prompt: "+s);//this.promptReturnValue[s];
        if (retVal == null) retVal = "";
        this.setServerVar("lastPromptText", s);
        this.setServerVar("prompt: "+s, null);
        return retVal;
    } else {
        var retVal = this.callFunction(this.real_prompt, window, s);
        this.recordStep(this.getExpectPromptScript(s, retVal));
        return retVal;
    }
};
Sahi.prototype.printMock = function () {
    if (this.isPlaying()) {
        this.setServerVar("printCalled", true);
        return null;
    } else {
        return this.callFunction(this.real_print, window);
    }
};
Sahi.prototype.mockDialogs = function (win) {
	win.alert = this.wrap(this.alertMock);
	win.confirm = this.wrap(this.confirmMock);
	win.prompt = this.wrap(this.promptMock);
	win.print = this.wrap(this.printMock);
};
var _sahi = new Sahi();
var tried = false;
var _sahi_top = window.top;
_sahi.selfWin = window;
_sahi.windowWin = window;
_sahi.parentWin = window.parent;

Sahi.prototype.self = function () {
	return _sahi.selfWin;
}
Sahi.prototype.parent = function () {
	return _sahi.parentWin;
}
Sahi.prototype.top = function () {
    //Hack for frames named "top"
	try{
		//alert(_sahi_top.location.href);
		var x = _sahi_top.location.href; // test
		return _sahi_top;
	}catch(e){
		var p = window;
		while (p != p._sahi.parent()){
			try{
				var y = p._sahi.parent().location.href; // test
				p = p._sahi.parent();
			}catch(e){
				return p;
			}
		}
		return p;
	}
};
Sahi.prototype.getKnownTags = function (src) {
	var el = src;
	while (true) {
		if (!el) return el;
		if (!el.tagName) return null;
		var tagLC = el.tagName.toLowerCase();
		if (tagLC == "html" || tagLC == "body") return null;
		for (var i=0; i<this.ADs.length; i++){
			var d = this.ADs[i];  
			if (d.tag.toLowerCase() == tagLC){
				return el;
			}
		}
		el = el.parentNode;
	}
};
Sahi.prototype.byId = function (src) {
    var s = src.id;
    if (this.isBlankOrNull(s)) return "";
    return "getElementById('" + s + "')";
};
Sahi.prototype.getLink = function (src) {
    var lnx = this.getElementsByTagName("A", window.document);
    for (var j = 0; j < lnx.length; j++) {
        if (lnx[j] == src) {
            return "links[" + j + "]";
        }
    }
    return  null;
};
Sahi.prototype.getElementsByTagName = function(tagName, doc){
	return doc.getElementsByTagName(tagName.toLowerCase());
}
Sahi.prototype.areTagNamesEqual = function(tagName1, tagName2){
	if (tagName1 == tagName2) return true;
	if (tagName1 == null || tagName2 == null) return false;
	return (tagName1.toLowerCase() == tagName2.toLowerCase());
}
Sahi.prototype.getImg = function (src) {
    var lnx = window.document.images;
    for (var j = 0; j < lnx.length; j++) {
        if (lnx[j] == src) {
            return "images[" + j + "]";
        }
    }
    return  null;
};

Sahi.prototype.getForm = function (src) {
    if (!this.isBlankOrNull(src.name) && this.nameNotAnInputElement(src)) {
        return "forms['" + src.name + "']";
    }
    var fs = window.document.forms;
    for (var j = 0; j < fs.length; j++) {
        if (fs[j] == src) {
            return "forms[" + j + "]";
        }
    }
    return null;
};
Sahi.prototype.nameNotAnInputElement = function (src) {
    return (typeof src.name != "object");
};
//Sahi.prototype.getFormElement = function (src) {
//    return this.getByTagName(src);
//};

//Sahi.prototype.getByTagName = function (src) {
//    var tagName = src.tagName.toLowerCase();
//    var els = this.getElementsByTagName(tagName, window.document);
//    return "getElementsByTagName('" + tagName + "')[" + this.findInArray(els, src) + "]";
//};

//Sahi.prototype.getTable = function (src) {
//    var tables = this.getElementsByTagName("table", window.document);
//    if (src.id && src.id != null && src == window.document.getElementById(src.id)) {
//        return "getElementById('" + src.id + "')";
//    }
//    return "getElementsByTagName('table')[" + this.findInArray(tables, src) + "]";
//};

//Sahi.prototype.getTableCell = function (src) {
//    var tables = window.document.getElementsByTagName("table");
//    var row = this.getRow(src);
//    if (row.id && row.id != null && row == window.document.getElementById(row.id)) {
//        return "getElementById('" + row.id + "').cells[" + src.cellIndex + "]";
//    }
//    var table = this.getTableEl(src);
//    if (table.id && table.id != null && table == window.document.getElementById(table.id)) {
//        return "getElementById('" + table.id + "').rows[" + this.getRow(src).rowIndex + "].cells[" + src.cellIndex + "]";
//    }
//    return "getElementsByTagName('table')[" + this.findInArray(tables, this.getTableEl(src)) + "].rows[" + this.getRow(src).rowIndex + "].cells[" + src.cellIndex + "]";
//};

//Sahi.prototype.getRow = function (src) {
//    return this.getParentNode(src, "tr");
//};

//Sahi.prototype.getTableEl = function (src) {
//    return this.getParentNode(src, "table");
//};

Sahi.prototype.getArrayElement = function (s, src) {
    var tag = src.tagName.toLowerCase();
    if (tag == "input" || tag == "textarea" || tag.indexOf("select") != -1) {
        var el2 = eval(s);
        if (el2 == src) return s;
        var ix = -1;
        if (el2 && el2.length) {
            ix = this.findInArray(el2, src);
            return s + "[" + ix + "]";
        }
    }
    return s;
};

Sahi.prototype.getEncapsulatingLink = function (src) {
	return (this.areTagNamesEqual(src.tagName, "A") || this.areTagNamesEqual(src.tagName, "AREA")) ? src : this._parentNode(src, "A");
};
Sahi.prototype.linkClick = function (e) {
    if (!e) e = window.event;
    var performDefault = true;
    var el = this.getTarget(e);
    this.lastLink = this.getEncapsulatingLink(el);
    if (this.lastLink.__sahi__prevClick) {
    	try{
    		performDefault = this.lastLink.__sahi__prevClick.apply(this.lastLink, arguments);
    	}catch(ex){}
    }
    this.lastLinkEvent = e;
    if (performDefault != false && this.lastLink.getAttribute("href") != null) {
    	window.setTimeout(function(){_sahi.navigateLink()}, 0);
    } else {
        return false;
    }
};
Sahi.prototype._dragDrop = function (draggable, droppable, offsetX, offsetY) {
	if (this.isFlexObj(draggable)) {
		droppable.setAsDroppable();
		return draggable.dragDrop();
	}
	this.fork();
    this.checkNull(draggable, "_dragDrop", 1, "draggable");
    this.checkNull(droppable, "_dragDrop", 2, "droppable");
    this.dragDropXYCommon(draggable, droppable, offsetX, offsetY, "DROP_RELATIVE");
};
Sahi.prototype.addBorder = function(el){
    el.style.border = "1px solid red";
};
Sahi.prototype.getScrollOffsetY = function(){
	if (document.body.scrollTop) return document.body.scrollTop;
	if (document.documentElement && document.documentElement.scrollTop) return document.documentElement.scrollTop;
	if (window.pageYOffset) return window.pageYOffset;
	if (window.scrollY) return window.scrollY;
	return 0;
};
Sahi.prototype.getScrollOffsetX = function(){
	if (document.body.scrollLeft) return document.body.scrollLeft;
	if (document.documentElement && document.documentElement.scrollLeft) return document.documentElement.scrollLeft;
	if (window.pageXOffset) return window.pageXOffset;
	if (window.scrollX) return window.scrollX;
	return 0;
};
Sahi.prototype._dragDropXY = function (draggable, x, y, isRelative) {
	this.fork();
	if (this.isFlexObj(draggable)) {
		return draggable.dragDropXY(x, y);
	}
    this.checkNull(draggable, "_dragDropXY", 1, "draggable");
	return this.dragDropXYCommon(draggable, null, x, y, isRelative ? "DRAG_RELATIVE" : false);
};

var SahiDTProxy = function(){
	this.data = {};
};
SahiDTProxy.prototype.setData = function(df, d){
	this.data[df] = d;
	return true;
};
SahiDTProxy.prototype.getData = function(df){
	return this.data[df];
};
SahiDTProxy.prototype.clearData = function(df){
	if (df) delete this.data[df];
	else this.data = {};
};
Sahi.DragDropper = function(draggable, droppable, offsetX, offsetY, isRelative){
	this.draggable = draggable;
	this.droppable = droppable;
	this.offsetX = offsetX;
	this.offsetY = offsetY;
	this.isRelative = isRelative;
	this.start = function() {
		//_sahi._alert(_sahi.findClientPos(this.droppable));
		this.dataTransfer = new SahiDTProxy();
		
	    _sahi.simulateMouseEvent(this.draggable, "mouseover");
	    _sahi.simulateMouseEvent(this.draggable, "mousemove");
	    
	    
	    _sahi.simulateMouseEvent(this.draggable, "mousedown");
	    _sahi.simulateMouseEvent(this.draggable, "mousemove");
	    _sahi.simulateDragEvent(this.draggable, "dragstart", this.dataTransfer);
	    _sahi.simulateDragEvent(this.draggable, "drag", this.dataTransfer);
	    var initPos = _sahi.findClientPos(this.draggable);
	    this.initPos = initPos;
	    this.initX = initPos[0];
	    this.initY = initPos[1];
	    this.destX = 0;
	    this.destY = 0;
	    this.stage = 0;
	    this.segments = Sahi.DRAG_DROP_SEGMENTS;
	    this.endHoverCount = 0;
	    this.lastMovedEl = null;
	}
	this.execute = function() {
		this.start();
		this.proceed();
	}
	this.proceed = function() {
		if (this.stage < this.segments) {
			this.stage++;
			this.move(this.stage);
			var o = this;
			window.setTimeout(function(){o.proceed()}, Sahi.DRAG_DROP_SPEED);
		} else {
			if (this.endHoverCount++ < Sahi.DRAG_DROP_MAX_HOVER_AT_END) {
				this.move(this.stage);
				var o = this;
				window.setTimeout(function(){o.proceed()}, Sahi.DRAG_DROP_SPEED);				
			} else {
				this.finish();
				_sahi.afterEval();
			}
		}
	}
	
	this.move = function(i) {
		// recalculate the dest coordinates because it shifts if a containing div scrolls.
    	// Digite: 895227
		var pos = [0,0];
    	if (this.isRelative == "DRAG_RELATIVE") {
            var pos = this.initPos;
    	} else if (this.isRelative == "DROP_RELATIVE") {
            var pos = _sahi.findClientPos(this.droppable);
    	}
        this.destX = pos[0] + (this.offsetX ? this.offsetX : 0);
        this.destY = pos[1] + (this.offsetY ? this.offsetY : 0);    	
    	var x1 = Math.floor(this.initX + i*(this.destX-this.initX)/this.segments);
    	var y1 = Math.floor(this.initY + i*(this.destY-this.initY)/this.segments + (i%2==0?Sahi.DRAG_DROP_WAVER:0)) ;    
    	var el2 = document.elementFromPoint(x1, y1);
    	if (el2 != null) {
    		try {
    			if (this.lastMovedEl != el2) {
    				if (this.lastMovedEl) {
    		    		_sahi.simulateMouseEventXY(this.lastMovedEl, "mouseout", x1, y1);
    					_sahi.simulateDragEventXY(this.lastMovedEl, "dragleave", x1, y1, this.dataTransfer);
    				}
					_sahi.simulateMouseEventXY(el2, "mouseenter", x1, y1);
					_sahi.simulateMouseEventXY(el2, "mousemove", x1, y1);
					_sahi.simulateMouseEventXY(el2, "mouseover", x1, y1);
					_sahi.simulateMouseEventXY(el2, "mousemove", x1, y1);
    				_sahi.simulateDragEventXY(el2, "dragenter", x1, y1, this.dataTransfer);
//    		    	_sahi._highlight(el2);
//    				
    				this.lastMovedEl = el2;
    			}
//    			_sahi._debug(this.initX + " " + this.destX +  " " + x1 + " " + y1);
	    		try{
	    			_sahi.simulateMouseEventXY(this.draggable, "mousemove", x1, y1);
	    			_sahi.simulateMouseEventXY(el2, "mousemove", x1, y1);
	    		}catch(e){
	    			_sahi._debug(e);
	    		}
	    		_sahi.simulateDragEventXY(this.draggable, "drag", x1, y1, this.dataTransfer);
	    		_sahi.simulateDragEventXY(el2, "dragover", x1, y1, this.dataTransfer);
	    		//_sahi._highlight(el2);
    		}catch(e){
    			_sahi._debug(e);
    		}
    	}
	}
	
	this.finish = function() {
	    var x = this.destX;
	    var y = this.destY;
	    
	    if (this.droppable) { 
	    	_sahi.simulateMouseEventXY(this.droppable, "mousemove", x, y);
	    }
	    _sahi.simulateMouseEventXY(this.draggable, "mousemove", x, y);
	    _sahi.simulateDragEventXY(this.draggable, "drag", x, y, this.dataTransfer);
	    if (this.droppable && this.lastMovedEl != this.droppable) {
	    	_sahi.simulateMouseEventXY(this.lastMovedEl, "mouseout", x, y);
			_sahi.simulateDragEventXY(this.lastMovedEl, "dragleave", x, y, this.dataTransfer);
	    	_sahi.simulateDragEventXY(this.droppable, "dragenter", x, y, this.dataTransfer);
	    	_sahi.simulateMouseEventXY(this.droppable, "mouseover", x, y);
	    }
	    _sahi.simulateDragEventXY(this.draggable, "drag", x, y, this.dataTransfer);
	    //this._alert('2');
	    if (this.droppable) { 
	    	_sahi.simulateDragEventXY(this.droppable, "dragover", x, y, this.dataTransfer);
	    	_sahi.simulateMouseEventXY(this.droppable, "mouseup", x, y); // needed
	    }
	    // draggable mouseup should be after droppable mouseup. http://gramam/extjs/examples/dd/field-to-grid-dd.html
	    // Not necessary.
	    _sahi.simulateMouseEventXY(this.draggable, "mouseup", x, y); 
	    if (this.droppable) _sahi.simulateDragEventXY(this.droppable, "drop", x, y, this.dataTransfer);
	    try {
		    _sahi.simulateDragEventXY(this.draggable, "dragend", x, y, this.dataTransfer);
		    _sahi.simulateMouseEventXY(this.draggable, "click", x, y);
		    _sahi.simulateMouseEventXY(this.draggable, "mousemove", x, y);
		    _sahi.simulateMouseEventXY(this.draggable, "mouseout", x, y);
	    } catch (e) {
	    	_sahi._debug(e);
	    	// Ignore. Can happen sometimes on IE. Digite issue 786516
	    }
	}
}
Sahi.prototype.dragDropXYCommon = function (draggable, droppable, offsetX, offsetY, isRelative) {
	new Sahi.DragDropper(draggable, droppable, offsetX, offsetY, isRelative).execute();
}
Sahi.prototype.checkNull = function (el, fnName, paramPos, paramName) {
    if (el == null || !this._exists(el)) {
    	var error = new Error("The " +
    	        (paramPos==1?"first ":paramPos==2?"second ":paramPos==3?"third ":"") +
    	        "parameter passed to " + fnName + " was not found on the browser")
    	error.isSahiError = true;
        throw error;
    }
};
Sahi.prototype.checkVisible = function (el) {
    if (this.strictVisibilityCheck && !this._isVisible(el)) {
        throw "" + el + " is not visible";
    }
};
Sahi.prototype.checkElementVisible = function (el) {
	if (!this.strictVisibilityCheck) return true;
	if (el.type && el.type == "hidden") return true;
    return this._isVisible(el);
}
Sahi.prototype._setStrictVisibilityCheck = function(b){
	this.setServerVar("strictVisibilityCheck", b);
	this.strictVisibilityCheck = b;
}
Sahi.prototype._isVisible = function (el) {
	if (this.isFlexObj(el)) return el.isVisible();
    try{
        if (el == null) return false;
        var elOrig = el;
        var display = true;
        while (true){
            display = display && this.isStyleDisplay(el);
            if (!display || el.parentNode == el || this.areTagNamesEqual(el.tagName, "BODY")) break;
            el = el.parentNode;
        }
        el = elOrig;
        var visible = true;
        while (true){
            visible = visible && this.isStyleVisible(el);
            if (!visible || el.parentNode == el || this.areTagNamesEqual(el.tagName, "BODY")) break;
            el = el.parentNode;
        }
        return display && visible;
    } catch(e){return false;}
};
Sahi.prototype._exists = function(el){
	if (this.isFlexObj(el)) return el.exists();
	if (this.isApplet(el)) return el.exists();
	return el != null;
}
Sahi.prototype.isStyleDisplay = function(el){
    var d = this._style(el, "display");
    return d==null || d != "none";
};
Sahi.prototype.isStyleVisible = function(el){
    var v = this._style(el, "visibility");
    return v==null || v != "hidden";
};
Sahi.prototype.invokeLastBlur = function(){
    if (this.lastBlurFn){
    	window.clearTimeout(this.lastBlurTimeout);
    	this.doNotRecord = true;
    	this.lastBlurFn();
    	this.doNotRecord = false;
    	this.lastBlurFn = null;
    }	
}
Sahi.prototype.setLastBlurFn = function(fn){
	if (this.lastBlurTimeout) window.clearTimeout(this.lastBlurTimeout);
	this.lastBlurFn = fn;
	this.lastBlurTimeout = window.setTimeout(this.wrap(this.invokeLastBlur), Sahi.BLUR_TIMEOUT);
}
Sahi.prototype._click = function (el, combo) {
	this.checkNull(el, "_click");
    this.checkVisible(el);
    if (this.isApplet(el))	return el.click();
    if (this.isFlexObj(el)) return el.click();
	this.simulateClick(el, false, false, combo);
};

Sahi.prototype._doubleClick = function (el, combo) {
    this.checkNull(el, "_doubleClick");
    this.checkVisible(el);
	if (this.isFlexObj(el)) return el.doubleClick();
    this.simulateDoubleClick(el, false, true, combo);
};

Sahi.prototype._rightClick = function (el, combo) {
    this.checkNull(el, "_rightClick");
    this.checkVisible(el);
	if (this.isFlexObj(el)) return el.rightClick();
    this.simulateRightClick(el, true, false, combo);
};

Sahi.prototype._mouseOver = function (el, combo) {
    this.checkNull(el, "_mouseOver");
    this.checkVisible(el);
    if (this.isFlexObj(el)) return el.mouseOver();
    this.simulateMouseEvent(el, "mousemove");
    this.simulateMouseEvent(el, "mouseover");
    
    this.setLastBlurFn(function(){
    	try{
	    	_sahi.simulateMouseEvent(el, "mousemove");
	        _sahi.simulateMouseEvent(el, "mouseout");
	    	_sahi.simulateMouseEvent(el, "blur");
    	}catch(e){}
    });    
};
Sahi.prototype._mouseDown = function (el, isRight, combo) {
	if (this.isFlexObj(el)) return el.mouseDown();
	this.simulateMouseEvent(el, "mousedown", isRight, false, combo);	
}
Sahi.prototype._mouseUp = function (el, isRight, combo) {
	if (this.isFlexObj(el)) return el.mouseUp();
	this.simulateMouseEvent(el, "mouseup", isRight, false, combo);	
}
Sahi.prototype._keyPress = function (el, val, combo) {
	var append = (el && el.type && (el.type=="text" || el.type=="password" || el.type=="textarea") && this.shouldAppend(el));
	this.simulateKeyPressEvents(el, val, combo, append);
}
Sahi.prototype.simulateKeyPressEvents = function (el, val, combo, append) {
	var origVal = el.value;
	var keyCode = 0;
	var charCode = 0;
	var c = null;
	if (typeof val == "number"){
		charCode = val;
	    keyCode = this.getKeyCode(charCode);
	    c = String.fromCharCode(charCode);
	} else if (typeof val == "object") {
		keyCode = val[0];
		charCode = val[1];
	    c = String.fromCharCode(charCode);
	} else if (typeof val == "string") {
	    charCode = val.charCodeAt(0);
	    keyCode = this.getKeyCode(charCode);
	    c = val;
	}
    var isShift = (charCode >= 65 && charCode <= 90);
    if (isShift) combo = "" + combo + "|SHIFT|";
    this.simulateKeyEvent([(isShift ? 16 : keyCode), 0], el, "keydown", combo);
    if (this.isSafariLike()) {
    	this.simulateKeyEvent([keyCode, charCode], el, "keypress", combo);    	
    } else {
    	this.simulateKeyEvent([0, charCode], el, "keypress", combo);
    }
    if (append && charCode!=10 && origVal == el.value) {
    	if (!this._isFF4Plus() || (this._isFF4Plus() && !(combo == "CTRL" || combo == "ALT")))
        el.value += c;
    }
    this.simulateKeyEvent([keyCode, 0], el, "keyup", combo);
};
Sahi.prototype._keyPressEvent = function (el, codes, combo) {
    this.checkNull(el, "_keyPressEvent", 1);
    this.checkVisible(el);        
    this.simulateKeyEvent(((typeof codes == "object") ? codes : [0, codes]), el, "keypress", combo);
};
Sahi.prototype._focus = function (el) {
    try{
    	el.focus();
    }catch(e){}
    this.simulateEvent(el, "focus");
};

Sahi.prototype._blur = function (el) {
    this.simulateEvent(el, "blur");
};
Sahi.prototype._removeFocus = Sahi.prototype._blur;
Sahi.prototype._keyDown = function (el, codes, combo) {
    this.checkNull(el, "_keyDown", 1);
    this.checkVisible(el);
    this.simulateKeyEvent(((typeof codes == "number")? [codes, 0] : codes), el, "keydown", combo);    
};
Sahi.prototype._keyUp = function (el, codes, combo) {
    this.checkNull(el, "_keyUp", 1);
    this.checkVisible(el);
    this.simulateKeyEvent(((typeof codes == "number")? [codes, 0] : codes), el, "keyup", combo);
};
Sahi.prototype._closeWindow = function (win) {
	if (!win) {
		try {
			win = _sahi_top.window;
		} catch (e) {
			win = window;
		}
	}
	if (win) {
		win.open("", "_self");
		win.close();
	}
};
Sahi.prototype._activeElement = function (win) {
	if (!win) win = this.top();
	var el = win.document.activeElement;
	var tagLC = el.tagName.toLowerCase();
	if (tagLC == "iframe" || tagLC == "frame"){
		return this._activeElement(el.contentWindow);
	}
	return el;
}
//Sahi.prototype._readFile = function (fileName) {
//	return this._evalOnRhino("_readFile("+this.quotedEscapeValue(fileName)+")");
//};
//Sahi.prototype._readCSVFile = function (fileName) {
//	return this._evalOnRhino("_readCSVFile("+this.quotedEscapeValue(fileName)+")");
//};
Sahi.prototype._getDB = function (driver, jdbcurl, username, password) {
    return new Sahi.dB(driver, jdbcurl, username, password, this);
};
Sahi.dB = function (driver, jdbcurl, username, password, sahi) {
    this.driver = driver;
    this.jdbcurl = jdbcurl;
    this.username = username;
    this.password = password;
    this.select = function (sql) {
        var qs = "driver=" + this.driver + "&jdbcurl=" + this.jdbcurl + "&username=" + this.username + "&password=" + this.password + "&sql=" + sql;
        return eval(sahi._callServer("net.sf.sahi.plugin.DBClient_select", qs));
    };
    this.update = function (sql) {
        var qs = "driver=" + this.driver + "&jdbcurl=" + this.jdbcurl + "&username=" + this.username + "&password=" + this.password + "&sql=" + sql;
        return eval(sahi._callServer("net.sf.sahi.plugin.DBClient_execute", qs));
    };
};

Sahi.prototype.isCheckboxRadioSimulationRequired = function(){
	if (this._isChrome()) {
		return this.chromeExplicitCheckboxRadioToggle;
	}
	return this.isSafariLike();
};
Sahi.prototype.simulateDoubleClick = function (el, isRight, isDouble, combo) {
    var n = el;
    var callBlur = true;
    if (this._isFF() || this.isSafariLike() || this._isOpera()) {
    	this.simulateMouseEvent(el, "mousemove");
	    this.simulateMouseEvent(el, "mouseover");
	    this.simulateMouseEvent(el, "mousedown", isRight, false, combo);
	    this.invokeLastBlur();
	    callBlur = this.isFocusableFormElement(el);
	    if (callBlur)  this.simulateEvent(el, "focus");
	    this.simulateMouseEvent(el, "mouseup", isRight, false, combo);
	    this.simulateMouseEvent(el, "click", isRight, false, combo);
	    this.simulateMouseEvent(el, "mousedown", isRight, isDouble, combo);
	    this.simulateMouseEvent(el, "mouseup", isRight, isDouble, combo);
	    this.simulateMouseEvent(el, "click", isRight, isDouble, combo);
	    this.simulateMouseEvent(el, "dblclick", isRight, isDouble, combo);
    } else if (this._isIE() && !this._isIE9PlusStrictMode()){
	    this.simulateMouseEvent(el, "mousemove");
	    this.simulateMouseEvent(el, "mouseover");
	    this.simulateMouseEvent(el, "mousedown", isRight, false, combo);
	    this.invokeLastBlur();
	    if (this._isIE()) this.simulateEvent(el, "focusin");
	    this.simulateMouseEvent(el, "focus");
	    this.simulateMouseEvent(el, "mouseup", isRight, false, combo);
	    this.simulateMouseEvent(el, "click", isRight, false, combo);
	    this.simulateMouseEvent(el, "mouseup", isRight, false, combo);	
	    this.simulateMouseEvent(el, "dblclick", isRight, isDouble, combo);
    } else if (this._isIE9PlusStrictMode()) {
    	this.simulateMouseEvent(el, "mousemove");
	    this.simulateMouseEvent(el, "mouseover");
	    this.simulateMouseEvent(el, "mousedown", isRight, false, combo);
	    this.invokeLastBlur();
	    if (this._isIE()) this.simulateEvent(el, "focusin");
	    this.simulateMouseEvent(el, "focus");
	    this.simulateMouseEvent(el, "mouseup", isRight, false, combo);
	    this.simulateMouseEvent(el, "click", isRight, false, combo);
	    this.simulateMouseEvent(el, "mousedown", isRight, isDouble, combo);
	    this.simulateMouseEvent(el, "mouseup", isRight, isDouble, combo);
	    this.simulateMouseEvent(el, "click", isRight, isDouble, combo);
	    this.simulateMouseEvent(el, "dblclick", isRight, isDouble, combo);
    } 
    if (callBlur){
		this.setLastBlurFn(function(){
	    	try{
		    	_sahi.simulateMouseEvent(el, "mousemove");
		        _sahi.simulateMouseEvent(el, "mouseout");
		        if (!(_sahi._isFF() || _sahi._isIE9PlusStrictMode() || _sahi._isOpera()) && (el.type == "checkbox" || el.type == "radio")){
		        	_sahi.simulateEvent(el, "change");
		        }
			    if (_sahi._isIE()) _sahi.simulateEvent(el, "focusout");
		        _sahi.simulateEvent(el, "blur");
	    	}catch(e){}
	    });
    }    
};
Sahi.prototype.simulateRightClick = function (el, isRight, isDouble, combo) {
    var n = el;
    var callBlur = true;
    if (this._isFF() || this.isSafariLike() || this._isOpera()) {
    	this.simulateMouseEvent(el, "mousemove");
	    this.simulateMouseEvent(el, "mouseover");
	    this.simulateMouseEvent(el, "mousedown", isRight, false, combo);
	    this.invokeLastBlur();
	    callBlur = this.isFocusableFormElement(el);
	    if (callBlur)  {
	    	this.simulateEvent(el, "focus");
	    }
	    this.simulateMouseEvent(el, "mouseup", isRight, false, combo);
	    this.simulateMouseEvent(el, "contextmenu", isRight, false, combo);
    } else if (this._isIE()){
	    this.simulateMouseEvent(el, "mousemove");
	    this.simulateMouseEvent(el, "mouseover");
	    this.simulateMouseEvent(el, "mousedown", isRight, false, combo);
	    this.invokeLastBlur();
	    this.simulateEvent(el, "focusin");
	    this.simulateMouseEvent(el, "focus");
	    this.simulateMouseEvent(el, "mouseup", isRight, false, combo);
	    this.simulateMouseEvent(el, "contextmenu", isRight, false, combo);
    }
    if (callBlur){
		this.setLastBlurFn(function(){
	    	try{
		    	_sahi.simulateMouseEvent(el, "mousemove");
		        _sahi.simulateMouseEvent(el, "mouseout");
		        if (!(_sahi._isFF() || _sahi._isIE9PlusStrictMode() || _sahi._isOpera()) && (el.type == "checkbox" || el.type == "radio")){
		        	_sahi.simulateEvent(el, "change");
		        }
			    if (_sahi._isIE()) _sahi.simulateEvent(el, "focusout");
		        _sahi.simulateEvent(el, "blur");
	    	}catch(e){}
	    });
    }    
};
Sahi.prototype.isFocusableFormElement = function(el){
	if (!this.isFormElement(el)) return false;
	if (this.isSafariLike() && (el.type == "checkbox" || el.type == "radio" || el.type == "button")) return false;
	return true;
}
Sahi.prototype.simulateClick = function (el, isRight, isDouble, combo) {
    var n = el;

    var link = this.getEncapsulatingLink(n);
    // This is required only for FF right now.
    // Definitely FF3.6
    // Need to check which other versions
    if (link != null && (this._isFF())){
    	link.__sahi__prevClick = link.onclick;
        var elWin = this.getWindow(link);
        link.onclick = function (e) {
        	// test with docWriteIFrame
        	return _sahi.wrap(_sahi.linkClick)(e ? e : elWin.event);
        };
    }
    
    this.simulateMouseEvent(el, "mousemove");
    this.simulateMouseEvent(el, "mouseover");
    this.simulateMouseEvent(el, "mousedown", isRight, false, combo);
    this.invokeLastBlur();
    var focusBlur = this.isFocusableFormElement(el);
    if (focusBlur) {
    	if (this._isIE()) this.simulateEvent(el, "focusin");
    	this.simulateEvent(el, "focus");
    }
    this.simulateMouseEvent(el, "mouseup", isRight, false, combo);
    try {
        if (this._isIE() && el && (this.areTagNamesEqual(el.tagName, "LABEL") || 
            		(link != null) ||
            		(el.type && (el.type == "submit"
                    || el.type == "reset" || el.type == "image"
                    || el.type == "checkbox" || el.type == "radio")))) {
    		if (link != null) {
    	        this.markStepDone(this.currentStepId, this.currentType);
    		}
    		el.click();
        } else {
        	if (window.opera){
        		// for opera single clicks don't simulate click event;
        		// Ignoring old comment, seems click is required now. 01 Nov 2010
        		this.simulateMouseEvent(el, "click", isRight, isDouble, combo);
        		if (this.areTagNamesEqual(el.tagName, "INPUT") && (el.type == "radio" || el.type == "checkbox")) {
    			this.simulateEvent(el, "change");
        		}
        	}
        	else {
        		var done = false;
        		if (this.isCheckboxRadioSimulationRequired()) {
                    if (this.areTagNamesEqual(el.tagName, "INPUT")) {
                        if (el.type == "radio" || el.type == "checkbox") {
                        	done = true;
                            el.checked = (el.type == "radio") ? true : !el.checked;
                    	this.simulateEvent(el, "change");
                        	this.simulateMouseEvent(el, "click", isRight, isDouble, combo);
                        } 
                    }
                }            		
                if (!done) this.simulateMouseEvent(el, "click", isRight, isDouble, combo);
        	}
        }
    } catch(e) {
    }
    if (focusBlur){
		this.setLastBlurFn(function(){
	    	try{
		    	_sahi.simulateMouseEvent(el, "mousemove");
		        _sahi.simulateMouseEvent(el, "mouseout");
		        if (!(_sahi._isFF() || _sahi._isIE9PlusStrictMode() || _sahi._isOpera()) && (el.type == "checkbox" || el.type == "radio")){
		        	_sahi.simulateEvent(el, "change");
		        }
		        if (_sahi._isIE()) _sahi.simulateEvent(el, "focusout");
		        _sahi.simulateEvent(el, "blur");
	    	}catch(e){}
	    });
    }
    if (link != null && (this._isFF())){
    	link.onclick = link.__sahi__prevClick;
    }
};
Sahi.prototype.getWebkitVersion = function(){
	var exp = /AppleWebKit\/(.*) \(/;
    exp.test(this.navigator.userAgent);
	return RegExp.$1;
}
Sahi.prototype.getChromeBrowserVersion = function(){
	var exp = /Chrome\/(.*) /;
    exp.test(this.navigator.userAgent);
	return RegExp.$1
}
Sahi.prototype.simulateMouseEvent = function (el, type, isRight, isDouble, combo) {
    var xy = this.findClientPos(el);
    var x = xy[0];
    var y = xy[1];
    this.simulateMouseEventXY(el, type, xy[0], xy[1], isRight, isDouble, combo);
};
Sahi.prototype.simulateDragEvent = function (el, type, dataTransfer, combo) {
    var xy = this.findClientPos(el);
    var x = xy[0];
    var y = xy[1];
    this.simulateDragEventXY(el, type, xy[0], xy[1], dataTransfer, combo);
}
Sahi.prototype.simulateDragEventXY = function (el, type, x, y, dataTransfer, combo) {
	var isRight = false;
	var isDouble = false;
	if (!combo) combo = "";
    var isShift = combo.indexOf("SHIFT")!=-1;
    var isCtrl = combo.indexOf("CTRL")!=-1;
    var isAlt = combo.indexOf("ALT")!=-1;
    var isMeta = combo.indexOf("META")!=-1;
    
    if (this._isIE()) {
    	var evt = el.ownerDocument.createEventObject();
        evt.clientX = x;
        evt.clientY = y;
        evt.ctrlKey = isCtrl;
        evt.altKey = isAlt;
        evt.metaKey = isMeta;            
        evt.shiftKey = isShift;
        if (type == "mousedown" || type == "mouseup" || type == "mousemove"){
        	evt.button = isRight ? 2 : 1;
        }
        //evt.dataTransfer = dataTransfer;
        el.fireEvent(this.getEventTypeName(type), evt);
        evt.cancelBubble = true;    	
	} else if (this._isFF()) {
        var evt = el.ownerDocument.createEvent("DragEvents");
        evt.initDragEvent(
        type,
        true, //can bubble
        true, //cancelable
        el.ownerDocument.defaultView, //view
        (isDouble ? 2 : 1), //detail
        x, //screen x
        y, //screen y
        x, //client x
        y, //client y
        isCtrl,
        isAlt,
        isShift,
        isMeta,
        isRight ? 2 : 0, //button
        null,//relatedTarget
        dataTransfer
        );
        el.dispatchEvent(evt);
    } else if (this._isChrome()) {
        var evt = el.ownerDocument.createEvent("HTMLEvents");
        evt.initEvent(
        type,
        true, //can bubble
        true, //cancelable
        el.ownerDocument.defaultView, //view
        (isDouble ? 2 : 1), //detail
        x, //screen x
        y, //screen y
        x, //client x
        y, //client y
        isCtrl,
        isAlt,
        isShift,
        isMeta,
        isRight ? 2 : 0, //button
        null//relatedTarget
        );
        evt.dataTransfer = dataTransfer;
        el.dispatchEvent(evt);
    }
}
Sahi.prototype.simulateMouseEventXY = function (el, type, x, y, isRight, isDouble, combo) {
	if (!combo) combo = "";
    var isShift = combo.indexOf("SHIFT")!=-1;
    var isCtrl = combo.indexOf("CTRL")!=-1;
    var isAlt = combo.indexOf("ALT")!=-1;
    var isMeta = combo.indexOf("META")!=-1;
    
    if (!this._isIE() || (this._isIE9PlusStrictMode() && !(type == "click" && isDouble))) {
        if (this.isSafariLike() || this._isIE9PlusStrictMode() || this._isOpera()) {
        	if (el.ownerDocument.createEvent) {
	            var evt = el.ownerDocument.createEvent('HTMLEvents');
		            type = type;
	            evt.initEvent(type, true, true);
	            evt.clientX = x;
	            evt.clientY = y;
	            evt.pageX = x;
	            evt.pageY = y;
	            evt.screenX = x;
	            evt.screenY = y;
	            evt.button = isRight ? 2 : 0;
	            evt.which = isRight ? 3 : 1;
	            evt.detail = isDouble ? 2 : (type == "contextmenu" ? 0 : 1);
	            evt.ctrlKey = isCtrl;
	            evt.altKey = isAlt;
	            evt.metaKey = isMeta;            
	            evt.shiftKey = isShift;
	            el.dispatchEvent(evt);
	        }
        }
        else {
            // FF
            var evt = el.ownerDocument.createEvent("MouseEvents");
            evt.initMouseEvent(
            type,
            true, //can bubble
            true, //cancelable
            el.ownerDocument.defaultView, //view
            (isDouble ? 2 : 1), //detail
            x, //screen x
            y, //screen y
            x, //client x
            y, //client y
            isCtrl,
            isAlt,
            isShift,
            isMeta,
            isRight ? 2 : 0, //button
            null//relatedTarget
            );
            el.dispatchEvent(evt);
        }
    } 
    if (this.checkForDuplicateEventsOnIE9Plus(el, type)) {
        // IE
        var evt = el.ownerDocument.createEventObject();
        evt.clientX = x;
        evt.clientY = y;
        evt.ctrlKey = isCtrl;
        evt.altKey = isAlt;
        evt.metaKey = isMeta;            
        evt.shiftKey = isShift;
        if (type == "mousedown" || type == "mouseup" || type == "mousemove"){
        	evt.button = isRight ? 2 : 1;
        }
        el.fireEvent(this.getEventTypeName(type), evt);
        evt.cancelBubble = true;
    }
};
Sahi.prototype.checkForDuplicateEventsOnIE9Plus = function(el, type){
	if (!this._isIE()) return false;
	if (!this._isIE9Plus()) return true;
	return ((el["on" + type] == null) || (el["on" + type] != null && !this._isIE9PlusStrictMode()));
}
Sahi.prototype.addOffset = function(el, origin){
	var x=origin[0];
	var y=origin[1];
    var offsets = this.xyoffsets.get(el);
    if (offsets){
    	var ox = offsets[0];
    	var width = parseInt(this._style(el, "width"));
    	if (ox < 0 && ((""+width) != "NaN")) ox = width + ox;
    	x += ox;
    	
    	var oy = offsets[1];
    	var height = parseInt(this._style(el, "height"));
    	if (oy < 0 && ((""+height) != "NaN")) oy = height + oy;    	                 
    	y += oy;
    }	
    return [x,y];
}

Sahi.pointTimer = 0;
Sahi.prototype._highlight = function (el) {
	if (this.isFlexObj(el)) return el.highlight();
	if (this.isApplet(el))	return el.highlight();
	if (Sahi.lastUnhighlight) {
		Sahi.lastUnhighlight();
		window.clearTimeout(Sahi.unhighlightTimer);
	}
    var oldBorder = el.style.border;
    var oldOutline = el.style.outline;
    el.style.border = "1px solid red";
    el.style.outline = "1px solid red";
    Sahi.lastUnhighlight = function(){el.style.border = oldBorder;el.style.outline = oldOutline;Sahi.lastUnhighlight=null;}
    Sahi.unhighlightTimer = window.setTimeout(Sahi.lastUnhighlight, 1000);
};
Sahi.prototype._position = function (el){
    return this.findPos(el);
};
Sahi.prototype.findPosX = function (obj){
    return this.findPos(obj)[0];
};
Sahi.prototype.findPosY = function (obj){
    return this.findPos(obj)[1];
};
Sahi.prototype.findClientPos = function (el){
	if (typeof el.getBoundingClientRect == "function") {
		var r = el.getBoundingClientRect();
		return this.addOffset(el, [r.left, Math.round(r.top)]); // trying clientRect for now. Remove below code later.
	} else {
		var xy = this.findPos(el);
	//	alert(xy[1] +" " + (xy[1]-this.getScrollOffsetY()));
		return [xy[0]-this.getScrollOffsetX(), xy[1]-this.getScrollOffsetY()];
	}
}
Sahi.prototype.findPos = function (el, isClient){
	var obj = el;
    var x = 0, y = 0;
    if (obj.offsetParent)
    {
        while (obj)
        {
            if (this.areTagNamesEqual(obj.tagName, "MAP")){
            	var res = this.getBlankResult();
            	obj = this.findTagHelper("#"+obj.name, this.getDomRelAr(this.getWindow(obj)), "IMG", res, "useMap").element;
            	if (obj == null) break;
            }
            var wasStatic = null;
            /*
            if (this._style(obj, "position") == "static"){
                wasStatic = obj.style.position;
                obj.style.position = "relative";
            }
             */
            x += obj.offsetLeft;
            y += obj.offsetTop;
            if (wasStatic != null) obj.style.position = wasStatic;
            obj = obj.offsetParent;
        }
    }
    else if (obj.x){
        x = obj.x;
        y = obj.y;
    }
    return this.addOffset(el, [x,y]);
};
Sahi.prototype.getWindow = function(el){
    var win;
    if (this.isSafariLike()) {
        win = this.getWin(el);
    } else {
        win = el.ownerDocument.defaultView; //FF
        if (!win) win = el.ownerDocument.parentWindow; //IE
    }
    return win;
};

Sahi.prototype.navigateLink = function () {
    var ln = this.lastLink;
    if (!ln) return;
    if (this.lastLinkEvent.getPreventDefault) {
        if (this.lastLinkEvent.getPreventDefault()) return;
    }
    if ((this._isIE() || this.isSafariLike()) && this.lastLinkEvent.returnValue == false) return;
    var win = this.getWindow(ln);
    if (ln.href.indexOf("javascript:") == 0) {
        var s = ln.href.substring(11);
        win.setTimeout(unescape(s), 0);
    } else {
        var target = ln.target;
        if (ln.target == null || ln.target == "") {
        	target = this.getBaseTarget(win);
        	if (target == null || target == "") target = "_self";
        }
        //if (!this.loaded) return; // happens if onclick caused unload via form submit. uncomment only if needed.
		var ancestor = this.getNamedWindow(win, target);
		if (ancestor){
    		if (this.isSafariLike()) {
                try {
                    ancestor._sahi.onBeforeUnLoad();
                } catch(e) {
                    //this._debug(e.message);
                }
    		}
			ancestor.location = ln.href;
		}else{
			win.open(ln.href, target);
        }
    }
};
Sahi.prototype.getNamedWindow = function (win, target){
	return this.getNamedAncestor(win, target) || this.getNamedFrame(win, target);
}
Sahi.prototype.getNamedAncestor = function (win, target){
	try{
		var w = win;
		if (target == "_self") return w;
		if (target == "_parent") return win.parent;
		if (target == "_top") return win.top;
		for (var i=0; i<100; i++){
			if (w.name == target) return w;
			if (w == w.parent) return null;
			w = w.parent;
		}
	}catch(e){}
}
Sahi.prototype.getNamedFrame = function (win, target){
	try{
	    var res = this.getBlankResult();
	    var el = this.findTagHelper(target, win, "iframe", res, "name").element;
	    if (el != null) return (el.contentWindow ? el.contentWindow : el);
	    res = this.getBlankResult();
	    el = this.findTagHelper(target, win, "frame", res, "name").element;
	    if (el != null) return el;
	}catch(e){}
}
Sahi.prototype.getBaseTarget = function (win) {
	var bs = this.getElementsByTagName("BASE", win.document);
	for (var i=bs.length-1; i>=0; i--){
		var t = bs[i].target;
		if (t && t != "") return t; 
	}
}
Sahi.prototype.getClickEv = function (el) {
    var e = new Object();
    if (this._isIE()) el.srcElement = e;
    else e.target = el;
    e.stopPropagation = this.noop;
    return e;
};

Sahi.prototype.noop = function () {
};

// api for link click end

Sahi.prototype._type = function (el, val) {
	for (var i = 0; i < val.length; i++) {
		var charCode = val.charAt(i).charCodeAt(0);
	    this.simulateKeyEvent(charCode, el, "keydown");
	    this.simulateKeyEvent(charCode, el, "keypress");
	    this.simulateKeyEvent(charCode, el, "keyup");
	}
};

Sahi.prototype._setValue = function (el, val) {
	if (val == null) return;
	if (this.isApplet(el)) return el.setValue(val);
	if (this.isFlexObj(el)) return el.setValue(val);
	this.invokeLastBlur();
	this.setValue(el, val);
};
Sahi.prototype.shouldAppend = function (el) {
	return !((this._isFF() && !this._isFF4Plus() && !this._isHTMLUnit()) || el.readOnly || el.disabled);
}
// api for set value start
Sahi.prototype.setValue = function (el, val) {
    this.checkNull(el, "_setValue", 1);
    this.checkVisible(el);
    
//    try{
//    	this.getWindow(el).focus();
//    }catch(e){}
    if (this._isIE()) this.simulateEvent(el, "focusin");
    this.simulateEvent(el, "focus");
    
    val = "" + val;
    var ua = this.navigator.userAgent.toLowerCase();
    if (ua.indexOf("windows") != -1) {
    	val = val.replace(/\r/g, '');
    	if (!this._isFF() || this._getFFVersion() >= 12) val = val.replace(/\n/g, '\r\n');
    } 
    var prevVal = el.value;
    //if (!window.document.createEvent) el.value = val;
    if (this._isFF4Plus()) this._focus(el); // test with textarea.sah

    if (el.type && (el.type == "hidden")){
    	el.value = val;    
    	return;
    } else if (el.type &&  (el.type == "range" || el.type == "date")){
    	el.value = val;
    } else if (el.type && el.type.indexOf("select") != -1) {
    } else {
        var append = (el && el.type && (this.findInArray(this.textboxTypes, el.type) != -1) && this.shouldAppend(el));
        el.value = "";
        if (typeof val == "string") {
        	var len = val.length;
        	if (el.maxLength && el.maxLength>=0 && val.length > el.maxLength) 
        		len = el.maxLength;
            for (var i = 0; i < len; i++) {
                var c = val.charAt(i);                
                this.simulateKeyPressEvents(el, c, null, append);
            }
        }
    }
    var triggerOnchange = prevVal != val;
    this.setLastBlurFn(function(){
    	try{
    		// on IE9, sequence is change, focusout, blur
    	    if (triggerOnchange) {
    	        if (!_sahi._isFF3()) 
    	        	_sahi.simulateEvent(el, "change"); 		
    	    }     		
    		if (_sahi._isIE()) _sahi.simulateEvent(el, "focusout");
    		_sahi.simulateEvent(el, "blur");
    	}catch(e){}
    });
};
Sahi.prototype._setFile2 = function (el, v, url) {
	this._setFile(el, v, url);
	if (this._isIE()){
		el.outerHTML = el.outerHTML.replace(/type=['"]?file['"]?/, "type=text");
		var idn = el.name;
		if (idn == "") idn = el.id;
		if (idn != "") {
			var el2 = this._textbox(idn);
			this._setValue(el2, v);
			this._blur(el2);
		}
	}else{
		el.type = "text";
		this._setValue(el, v);
		this._blur(el);
	}
}
Sahi.prototype._setFile = function (el, v, url) {
	if (v == null) return;
    if (!url) url = (!el.form || this.isBlankOrNull(el.form.action) || (typeof el.form.action != "string")) ? this.getWindow(el).location.href : el.form.action;
    if (url && (q = url.indexOf("?")) != -1) url = url.substring(0, q);
    if (url.indexOf("http") != 0) {
        var loc = window.location;
        if (url.indexOf("/") == 0){
            url = loc.protocol+ "//" +  loc.hostname + (loc.port ? (':'+loc.port) : '') + url;
        }else{
            var winUrl = loc.href;
            url = winUrl.substring(0, winUrl.lastIndexOf ('/') + 1) + url;
        }
    }
    var msg = this._callServer("FileUpload_setFile", "n=" + el.name + "&v=" + this.encode(v) + "&action=" + this.encode(url));
    if (msg != "true") {
    	throw new Error(msg);
    }
};

Sahi.prototype.simulateEvent = function (target, evType) {
//    if (!this._isIE() || this._isIE9PlusStrictMode()) { // fix for tramada IE9 onchange not triggered.
	var useCreateEvent = !this._isIE() || this._isIE9PlusStrictMode();
	var useCreateEventObject = this._isIE();
    if (useCreateEvent) {
        var evt = new Object();
        evt.type = evType;
        evt.button = 0;
        evt.bubbles = true;
        evt.cancelable = true;
        if (!target) return;
        var event = target.ownerDocument.createEvent("HTMLEvents");
        event.initEvent(evt.type, evt.bubbles, evt.cancelable);
        target.dispatchEvent(event);
    } 
    if (useCreateEventObject) {
        var evt = target.ownerDocument.createEventObject();
        evt.type = evType;
        evt.bubbles = true;
        evt.cancelable = true;
        evt.cancelBubble = true;
        target.fireEvent(this.getEventTypeName(evType), evt);
    }
};
Sahi.prototype.getKeyCode = function (charCode){
	return (charCode >= 97 && charCode <= 122) ? charCode - 32 : charCode;
}
Sahi.prototype.simulateKeyEvent = function (codes, target, evType, combo) {
	var keyCode = codes[0];
	var charCode = codes[1];
	if (!combo) combo = "";
    var isShift = combo.indexOf("SHIFT")!=-1;
    var isCtrl = combo.indexOf("CTRL")!=-1;
    var isAlt = combo.indexOf("ALT")!=-1;
    var isMeta = combo.indexOf("META")!=-1;

    if (!this._isIE() || this._isIE9PlusStrictMode()) { // FF chrome safari opera
        if (this.isSafariLike() || window.opera || this._isIE9PlusStrictMode()) {
        	if (target.ownerDocument.createEvent) {
            var event = target.ownerDocument.createEvent('HTMLEvents');
            
            var bubbles = true;
            var cancelable = true;
            var evt = event;
            if (!window.opera){
            	// this may not have any effect.
	            evt.bubbles = bubbles;
	            evt.cancelable = cancelable;
            }
            evt.ctrlKey = isCtrl;
            evt.altKey = isAlt;
            evt.metaKey = isMeta;
            evt.charCode = charCode;
            evt.keyCode =  (evType == "keypress") ? charCode : keyCode;
            evt.shiftKey = isShift;
            evt.which = evt.keyCode;
            evt.initEvent(evType, bubbles, cancelable); // don't use evt.bubbles etc. because it may be readonly and never be set to true. Chrome enter on extjs.
            target.dispatchEvent(evt);
        	}
        } else { //FF
            var evt = new Object();
            evt.type = evType;
            evt.bubbles = true;
            evt.cancelable = true;
            evt.ctrlKey = isCtrl;
            evt.altKey = isAlt;
            evt.metaKey = isMeta;
        	evt.keyCode = keyCode;            	
        	evt.charCode = charCode;
            evt.shiftKey = isShift;

            if (!target) return;
            var event = target.ownerDocument.createEvent("KeyEvents");
            event.initKeyEvent(evt.type, evt.bubbles, evt.cancelable, target.ownerDocument.defaultView,
            evt.ctrlKey, evt.altKey, evt.shiftKey, evt.metaKey, evt.keyCode, evt.charCode);
            target.dispatchEvent(event);
        }
    } 
    if (this._isIE()) { // IE
        var evt = target.ownerDocument.createEventObject();
        evt.type = evType;
        evt.bubbles = true;
        evt.cancelable = true;
        var xy = this.findClientPos(target);
        evt.clientX = xy[0];
        evt.clientY = xy[1];
        evt.ctrlKey = isCtrl;
        evt.altKey = isAlt;
        evt.metaKey = isMeta;
        evt.keyCode = (this._isIE() && evType == "keypress") ? charCode : keyCode;           	
        evt.shiftKey = isShift; //c.toUpperCase().charCodeAt(0) == evt.charCode;
        evt.shiftLeft = isShift;
        evt.cancelBubble = true;
        evt.target = target;
        target.fireEvent(this.getEventTypeName(evType), evt);
    }
};
Sahi.prototype.getEventTypeName = function (type) {
	return ((typeof MooTools) == "object") ? type : ("on" + type); 
};
Sahi.prototype._simulateMouseEvent = Sahi.prototype.simulateMouseEvent;
Sahi.prototype._simulateMouseEventXY = Sahi.prototype.simulateMouseEventXY;
Sahi.prototype._simulateKeyEvent = Sahi.prototype.simulateKeyEvent;
Sahi.prototype.selectOption = function(el, val, isCTRL){
	var combo = isCTRL ? "CTRL" : null;
	var optionEl = this._option(val, this._in(el));
	if (!optionEl) throw new Error("Option not found: " + val);
	if (this._isIE()){
		this.simulateMouseEvent(el, "mousedown", false, false, combo);
		this.simulateMouseEvent(el, "mouseup", false, false, combo);
	    optionEl.selected = true;
		this.simulateMouseEvent(el, "change");
		this.simulateMouseEvent(el, "click", false, false, combo);				
	}else if (this._isFF()){
    optionEl.selected = true;
		this.simulateMouseEvent(optionEl, "mousedown", false, false, combo);
		this.simulateMouseEvent(optionEl, "mouseup", false, false, combo);
		this.simulateMouseEvent(el, "change");
		this.simulateMouseEvent(optionEl, "click", false, false, combo);		
	}else {
		optionEl.selected = true;
		this.simulateMouseEvent(el, "change");
	}
}
Sahi.prototype._setSelected = function (el, val, append) {
	if (val == null) return;
    this.checkNull(el, "_setSelected");
    this.checkVisible(el);
    if (this.isApplet(el)) return el.setSelected(val);
	if (this.isFlexObj(el)) return el.set(val);
	// reset _under related params so that option does not use _under
	this.xyoffsets = new Sahi.Dict();
//	this.alignY = this.alignX = null;
	
	
	var l = el.options.length;
    var optionEl = null;
	if (el.type == "select-one"){
		this.simulateMouseEvent(el, "mousedown");
		if (this._isIE()) this.simulateEvent(el, "focusin");
		this.simulateEvent(el, "focus");
		this.simulateMouseEvent(el, "mouseup");
		this.simulateMouseEvent(el, "click");
    	this.selectOption(el, val);
        return;
    } else {
    if (!this.isArray(val)) val = [val];
	    if (!append){
    	for (var i = 0; i < l; i++) {
    		el.options[i].selected = false;
    	}    	
    }
    for (var i=0; i<val.length; i++){
			var isCTRL = (i > 0 || append); // use ctrl for first option if append is true.
			this.selectOption(el, val[i], isCTRL);
    }
    }
    this.setLastBlurFn(function(){
    	try{
    		if (_sahi._isIE()) _sahi.simulateEvent(el, "focusout");
    		_sahi.simulateEvent(el, "blur");
    	}catch(e){}
    });
};

// api for set value end
Sahi.prototype._check = function (el) {
    this.checkNull(el, "_check");
    if (el.checked) return;
    this._click(el);
}
Sahi.prototype._uncheck = function (el) {
    this.checkNull(el, "_uncheck");
    if (!el.checked) return;
    this._click(el);
}
Sahi.prototype._wait = function (i, condn) {
	return condn ? eval(condn) : false;
};
Sahi.prototype._accessor = function (n) {
    return eval(n);
};
Sahi.prototype._byId = function (id) {
    return this.findElementById(this.top(), id);
};
Sahi.prototype._byText = function (text, tag) {
    var res = this.getBlankResult();
    return this.tagByText({relations:[], window:this.top(), positionals:[]}, text, tag, res).element;
};
Sahi.prototype._byXPath = function (xpath, inEl) {
	inEl = (inEl && inEl.type == "dom" && inEl.relation == "_in") ? inEl.element : inEl;
	var doc = inEl ? this.getWindow(inEl).document : this.top().document;
	var prefix = "";
	if (inEl){
		var tagName = inEl.tagName;
		var ix = this.findInArray(this.getElementsByTagName(tagName, doc), inEl);
		prefix = "//" + tagName.toLowerCase() + "["+(ix+1)+"]";
	}
//	if (!inEl) inEl = this.top().document;
	var res = doc.evaluate(prefix + xpath, doc, null, 0, null);
	switch(res.resultType) {
		case 1: return res.numberValue;
		case 2: return res.stringValue;
		case 3: return res.booleanValue;
	}
	var el = res.iterateNext();
	return el;
//	var els = new Array();
//	while (true) {
//    	var el = res.iterateNext();
//        if (!el) break;
//        els.push(el);
//    }
//    return els;
}
Sahi.prototype._byClassName = function (className, tagName, inEl) {
	var inEl = this.getDomRelAr(arguments);
    var res = this.getBlankResult();
    var el = this.findTagHelper(className, inEl, tagName, res, "className").element;
    return el;
};
Sahi.prototype.byName = function (name, tagName, inEl) {
	var inEl = this.getDomRelAr(arguments);
    var res = this.getBlankResult();
    var el = this.findTagHelper(name, inEl, tagName, res, "name").element;
    return el;
};
Sahi.prototype._spandiv = function (id, inEl) {
	var el = this._span.apply(this, arguments);
	if (el == null) el = this._div.apply(this, arguments);
	return el;
};
Sahi.prototype.tagByText = function (win, id, tagName, res) {
    var o = this.getArrayNameAndIndex(id);
    var ix = o.index;
    var fetch = o.name;
    var els = this.getElementsByTagName(tagName, this.getDoc(win));
    for (var i = 0; i < els.length; i++) {
        var el = els[i];
        var text = this._getText(el);

        if (this.isTextMatch(text, fetch)) {
            res.cnt++;
            if (res.cnt == ix || ix == -1) {
                res.element = this.innerMost(el, id, tagName);
                res.found = true;
                return res;
            }
        }
    }
    return this.recurseInFrames(this.tagByText, win, res, arguments);
};
Sahi.prototype.isTextMatch = function(sample, pattern){
    if (pattern instanceof RegExp)
        return sample.match(pattern);
    return (sample == pattern);
};
Sahi.prototype.innerMost = function(el, re, tagName){
    for (var i=0; i < el.childNodes.length; i++){
        var child = el.childNodes[i];
        var text = this._getText(child);
        if (text && this.contains(text, re)){
            var inner = this.innerMost(child, re, tagName);
            if (this.areTagNamesEqual(inner.nodeName, tagName)) return inner;
        }
    }
    return el;
};
Sahi.prototype._simulateEvent = function (el, ev) {
    if (this._isIE()) {
        var newFn = (eval("el.on" + ev.type)).toString();
        newFn = newFn.replace("anonymous()", "s_anon(s_ev)", "g").replace("event", "s_ev", "g");
        eval(newFn);
        s_anon(ev);
    } else {
        eval("el.on" + ev.type + "(ev);");
    }
};
Sahi.prototype._setGlobal = function (name, value) {
    //this._debug("SET name="+name+" value="+value);
    this.setServerVar(name, value, true);
};
Sahi.prototype._getGlobal = function (name) {
    var value = this.getServerVar(name, true);
    //this._debug("GET name="+name+" value="+value);
    return value;
};
Sahi.prototype._set = function (name, value) {
    this.locals[name] = value;
};
Sahi.prototype._get = function (name) {
    var value = this.locals[name];
    return value;
};
Sahi.prototype._assertNotNull = function (n, s) {
    if (n == null) throw new SahiAssertionException(1, s);
	if (this.isFlexObj(n) && !n.exists()) throw new SahiAssertionException(1, s);
    return true;
};
Sahi.prototype._assertExists = Sahi.prototype._assertNotNull;
Sahi.prototype._assertNull = function (n, s) {
    if (n != null) throw new SahiAssertionException(2, s);
    return true;
};
Sahi.prototype._assertNotExists = Sahi.prototype._assertNull;
Sahi.prototype._assertTrue = function (n, s) {
    if (n != true) throw new SahiAssertionException(5, s);
    return true;
};
Sahi.prototype._assert = Sahi.prototype._assertTrue;
Sahi.prototype._assertNotTrue = function (n, s) {
    if (n) throw new SahiAssertionException(6, s);
    return true;
};
Sahi.prototype._assertFalse = Sahi.prototype._assertNotTrue;


Sahi.prototype._assertEqual = function (expected, actual, s) {
    if (this.isArray(expected) && this.isArray(actual))
        return this._assertEqualArrays(expected, actual, s);
	if (!this.areEqualParams(this.trim(actual), this.checkRegex(this.trim(expected)))) 
		throw new SahiAssertionException(3, (s ? s : "") + "\nExpected:" + this.toJSON(expected) + "\nActual:" + this.toJSON(actual) + "");
	return true;
};

Sahi.prototype._extract = function(str, pattern, onlyGroups){ 
	var match = str.match(this.checkRegex(pattern));
	if (match == null) return false; //in case of no matches
	if(onlyGroups) match.splice(0,1);
	return match;	
};

Sahi.prototype.isArray = function (obj) {
	return Object.prototype.toString.call(obj) === '[object Array]';
}
Sahi.prototype._assertEqualArrays = function (expected, actual, s) {
    var compareResult = this.compareArrays(expected,actual);
	if (compareResult != "equal") throw new SahiAssertionException(3,(s ? s : "") + "\n"+compareResult);
	return true;	
};

Sahi.prototype._assertNotEqual = function (expected, actual, s) {
	if (this.areEqualParams(this.trim(actual), this.checkRegex(this.trim(expected))))  
    	throw new SahiAssertionException(4, s);
    return true;
};
Sahi.prototype._assertContainsText = function (expected, el, s) {
    if (!this._containsText(el, expected)) 
    	throw new SahiAssertionException(3, (s ? s : "") + "\nExpected:" + this.toJSON(expected) + " to be part of " + this.toJSON(this._getText(el)) + "");
    return true;
};
Sahi.prototype._assertNotContainsText = function (expected, el, s) {
    if (this._containsText(el, expected)) 
    	throw new SahiAssertionException(3, (s ? s : "") + "\nExpected:" + this.toJSON(expected) + " not to be part of " + this.toJSON(this._getText(el)) + "");
    return true;
};
Sahi.prototype._imageCompareScoreExtractorFn = function(s){
	var ar = s.split(" ");
    return parseInt(ar[ar.length-1]);	
}
Sahi.prototype._getSelectedText = function (el) {
	return this.getSelectBoxText(el, true);
}
Sahi.prototype.getSelectBoxText = function (el, selectedOnly) {
	if (selectedOnly && el.type == "select-one") return this._getText(el.options[el.selectedIndex]);
	var ar = [];
	var opts = el.options;
    var l = el.options.length;
    for (var i=0; i<l; i++){
    	var opt = opts[i];
    	if (!selectedOnly || opt.selected){
    		ar.push(this._getText(opt));
    	}
    }
    if (ar.length > 0) return ar;
};
//Sahi.prototype._option = function (el, val) {
//    var o = this.getArrayNameAndIndex(id);
//    var imgIx = o.index;
//
//    var opts = el.options;
//    var l = opts.length;
//    var optionEl = null;
//    if (typeof val == "string" || val instanceof RegExp){
//        for (var i = 0; i < l; i++) {
//        	var opt = opts[i];
//            if (this.areEqual(opt, "sahiText", val) ||
//            	this.areEqual(opt, "value", val) ||
//                this.areEqual(opt, "id", val)) {
//                optionEl = opt;
//            }
//        }
//    } else if (typeof val == "number" && opts.length > val){
//        optionEl = opts[val];
//    }    
//    return optionEl;
//};
Sahi.prototype._getText = function (el) {
	if (this.isApplet(el))	return el.getText();
	if (this.isFlexObj(el)) return el.getText();
    this.checkNull(el, "_getText");
    if (el && el.type){
    	if ((el.type=="text" || el.type=="password" || (el.type=="button" && this.areTagNamesEqual(el.tagName, "INPUT")) || el.type=="textarea" || el.type=="submit") && el.value) return el.value;
//    	if (el.type=="select-one" || el.type == "select-multiple") return this._getSelectedText(el);
    }
    return this.trim(this._getTextNoTrim(el));
};
Sahi.prototype._getValue = function (el) {
	if (this.isFlexObj(el)) return el.getValue();
	if (this.isApplet(el))	return el.getValue();
	return el.value;
};
Sahi.prototype._getAttribute = function (el, attr) {
	return el[attr];
};
Sahi.prototype._getTextNoTrim = function (el) {
    this.checkNull(el, "_getTextNoTrim");
    if (el.tagName) {
    	if (el.tagName.toLowerCase() == "option") return el.text.replace(/\u00A0/g, ' ');
    	else if (el.type=="select-one" || el.type == "select-multiple") {
    		return this.getSelectBoxText(el, false);
    	}
    }
    if (this._isIE() || this.isSafariLike()) return el.innerText;
    var html = el.innerHTML;
    if (!html) return el.textContent; // text nodes
    if (html.indexOf("<br") == -1 && html.indexOf("<BR") == -1) return el.textContent;
    if (document.createElement){
    	var x = document.createElement(el.tagName);
    	x.innerHTML = el.innerHTML.replace(/<br[\/]*>/ig, " ");
    	return x.textContent;
    }
    return el.textContent;
};
Sahi.prototype._getCellText = Sahi.prototype._getText;
Sahi.prototype.getRowIndexWith = function (txt, tableEl) {
    var r = this.getRowWith(txt, tableEl);
    return (r == null) ? -1 : r.rowIndex;
};
Sahi.prototype.getRowWith = function (txt, tableEl) {
    for (var i = 0; i < tableEl.rows.length; i++) {
        var r = tableEl.rows[i];
        for (var j = 0; j < r.cells.length; j++) {
            if (this.areEqualParams(this._getText(r.cells[j]),  this.checkRegex(txt))) {
                return r;
            }
        }
    }
    return null;
};
Sahi.prototype.getColIndexWith = function (txt, tableEl) {
    for (var i = 0; i < tableEl.rows.length; i++) {
        var r = tableEl.rows[i];
        for (var j = 0; j < r.cells.length; j++) {
            if (this.areEqualParams(this._getText(r.cells[j]), this.checkRegex(txt))) {
                return j;
            }
        }
    }
    return -1;
};
Sahi.prototype._alert = function (s) {
    return this.callFunction(this.real_alert, window, s);
};
Sahi.prototype._lastAlert = function () {
    var v = this.getServerVar("lastAlertText");
    return v;
};
Sahi.prototype._clearLastAlert = function () {
	this.setServerVar("lastAlertText", null);
};
Sahi.prototype._clearLastConfirm = function () {
	this.setServerVar("lastConfirmText", null);
};
Sahi.prototype._clearLastPrompt = function () {
	this.setServerVar("lastPromptText", null);
};
Sahi.prototype._eval = function (s) {
	this.xyoffsets = new Sahi.Dict();
//	this.alignY = this.alignX = null;
    return eval(s);
};
Sahi.prototype._call = function (s) {
    return s;
};
Sahi.prototype._random = function (n) {
    return Math.floor(Math.random() * (n + 1));
};
Sahi.prototype._savedRandom = function (id, min, max) {
    if (min == null) min = 0;
    if (max == null) max = 10000;
    var r = this.getServerVar("srandom" + id);
    if (r == null || r == "") {
        r = min + this._random(max - min);
        this.setServerVar("srandom" + id, r);
    }
    return r;
};
Sahi.prototype._resetSavedRandom = function (id) {
    this.setServerVar("srandom" + id, "");
};


Sahi.prototype._expectConfirm = function (text, value) {
    this.setServerVar("confirm: "+text, value);
};
Sahi.prototype._saveDownloadedAs = function(filePath){
    this._callServer("SaveAs_saveLastDownloadedAs", "destination="+this.encode(filePath));
};
Sahi.prototype._lastDownloadedFileName = function(){
    var fileName = this._callServer("SaveAs_getLastDownloadedFileName");
    if (fileName == "-1") return null;
    return fileName;
};
Sahi.prototype._clearLastDownloadedFileName = function(){
    this._callServer("SaveAs_clearLastDownloadedFileName");
};
Sahi.prototype._saveFileAs = function(filePath){
    this._callServer("SaveAs_saveTo", filePath);
};
Sahi.prototype.callFunction = function(fn, obj, args){
    if (fn.apply){
        return fn.apply(obj, [args]);
    }else{
        return fn(args);
    }
};
Sahi.prototype._lastConfirm = function () {
    var v = this.getServerVar("lastConfirmText");
    return v;
};
Sahi.prototype._lastPrompt = function () {
    var v = this.getServerVar("lastPromptText");
    return v;
};

Sahi.prototype._expectPrompt = function (text, value) {
    this.setServerVar("prompt: "+text, value);
};
Sahi.prototype._prompt = function (s) {
	this.fork(60000);
    var r = this.callFunction(this.real_prompt, window, s);
    this.afterEval();
    return r;
};
Sahi.prototype._confirm = function (s) {
	this.fork(60000);
    var r = this.callFunction(this.real_confirm, window, s);
    this.afterEval();
    return r;
};
Sahi.prototype._print = function (s){
    return this.callFunction(this.real_print, window, s);
};
Sahi.prototype._printCalled = function (){
    return this.getServerVar("printCalled");
};
Sahi.prototype._clearPrintCalled = function (){
    return this.setServerVar("printCalled", null);
};

Sahi.prototype.arrayIndexOf = function(array,value){
	for(var itr=0; itr < array.length; itr++ ){
		if(array[itr] === value) return itr;
	}
	return -1;
};
Sahi.prototype._cell = function (id, row, col) {
    if (id == null) return null;
    if (row == null && col == null) {
        return this.findCell(id, this.getDomRelAr(arguments));
    }
    if (row != null && (this.arrayIndexOf(["_in","_near","_under","_above","_leftOf","_rightOf","_rowOf","_colOf"],row.relation) != -1)){
    	return this.findCell(id, this.getDomRelAr(arguments));
    }

    var rowIx = row;
    var colIx = col;
    if (typeof row == "string" || row instanceof RegExp) {
        rowIx = this.getRowIndexWith(row, id);
        if (rowIx == -1) return null;
    }
    if (typeof col == "string" || col instanceof RegExp) {
        colIx = this.getColIndexWith(col, id);
        if (colIx == -1) return null;
    }
    if (id.rows[rowIx] == null) return null;
    return id.rows[rowIx].cells[colIx];
};
Sahi.prototype.x_row = function (tableEl, rowIx) {
    if (typeof rowIx == "string") {
        return this.getRowWith(rowIx, tableEl);
    }
    if (typeof rowIx == "number") {
        return tableEl.rows[rowIx];
    }
    return null;
};
Sahi.prototype._containsHTML = function (el, htm) {
    return this.contains(el.innerHTML, htm)
};
Sahi.prototype._containsText = function (el, txt) {
    return this.contains(this._getText(el), txt)
};
Sahi.prototype.contains = function (orig, substr) {
	substr = this.checkRegex(substr);
    if (substr instanceof RegExp)
        return orig.match(substr) != null;
    return orig.indexOf(substr) != -1;
}
	
Sahi.prototype._contains = function (parent, child) {
	if (parent == null) return false;
	var c = child;
    while (true){
    	if (c == parent) return true;
    	if (c == null || c == c.parentNode) return false;
    	c = c.parentNode;
    }
};
Sahi.prototype._popup = function (n) {
    if (this.top().name == n || this.getTitle() == n) {
        return this.top();
    }
    throw new SahiNotMyWindowException(n);
};
Sahi.prototype._domain = function (n) {
    if (document.domain == n) {
        return this.top();
    }
    throw new SahiNotMyDomainException(n);
};
Sahi.prototype._log = function (s, type) {
    if (!type) type = "info";
    this.logPlayBack(s, type);
};
Sahi.prototype._navigateTo = function (url, force) {
    if (force || this.top().location.href != url){
        //this.top().location.href = url;
        window.setTimeout("_sahi.top().location.href = '"+url.replace(/'/g, "\\'")+"'", 0); // for _navigateTo(relUrl) from controller
    }
};
Sahi.prototype._callServer = function (cmd, qs) {
    return this.sendToServer("/_s_/dyn/" + cmd + (qs == null ? "" : ("?" + qs)));
};
Sahi.prototype._removeMock = function (pattern) {
    return this._callServer("MockResponder_remove", "pattern=" + pattern);
};
Sahi.prototype._addMock = function (pattern, clazz) {
    if (clazz == null) clazz = "MockResponder_simple";
    return this._callServer("MockResponder_add", "pattern=" + pattern + "&class=" + clazz);
};
Sahi.prototype._mockImage = function (pattern, clazz) {
    if (clazz == null) clazz = "MockResponder_mockImage";
    return this._callServer("MockResponder_add", "pattern=" + pattern + "&class=" + clazz);
};
Sahi.prototype._debug = function (s) {
    return this._callServer("Debug_toOut", "msg=Debug: " + this.encode(s));
};
Sahi.prototype._debugToErr = function (s) {
    return this._callServer("Debug_toErr", "msg=" + this.encode(s));
};
Sahi.prototype._debugToFile = function (s, file) {
    if (file == null) return;
    return this._callServer("Debug_toFile", "msg=" + this.encode(s) + "&file=" + this.encode(file));
};
Sahi.prototype._enableKeepAlive = function () {
    this.sendToServer('/_s_/dyn/Configuration_enableKeepAlive');
};
Sahi.prototype._disableKeepAlive = function () {
    this.sendToServer('/_s_/dyn/Configuration_disableKeepAlive');
};
Sahi.prototype.getWin = function (el) {
    if (el == null) return this.self();
    if (el.nodeName.indexOf("document") != -1) return this.getFrame1(this.top(), el);
    return this.getWin(el.parentNode);
};
// finds window to which a document belongs
Sahi.prototype.getFrame1 = function (win, doc) {
    if (win.document == doc) return win;
    var frs = win.frames;
    for (var j = 0; j < frs.length; j++) {
        var sub = this.getFrame1(frs[j], doc);
        if (sub != null) {
            return sub;
        }
    }
    return null;
};
Sahi.prototype.areEqual2 = function (el, param, value) {
    if (param == "sahiText") {
        var str = this._getTextNoTrim(el);
        if (value instanceof RegExp){
        	str = this.trim(str);
            return str != null && str.match(value) != null;
        }
        if (str.length - value.length > 1000) return false;
        return (this.trim(str) == this.trim(value));
    }
    else {
    	return this.areEqualParams(el[param], value);
    }
};
Sahi.prototype.areEqualParams = function(actual, input){
	if (input instanceof RegExp)
        return actual != null && (typeof actual == "string") && actual.match(input) != null;
    return (actual == input);
}
Sahi.prototype.areEqual = function (el, param, value) {
	if (param == "associative_array") {
		var retVal = true;
		for (var k in value) {
			retVal = retVal && this.areEqual(el, k, value[k]);
			if (!retVal) return false; 
		}
		return true;
	}
	if (typeof param == "function"){
		return this.areEqualParams(this.callFunction(param, this, el), value);
	}
	if (param == null || param.indexOf("|") == -1)
		return this.areEqual2(el, param, value);
    var params = param.split("|");
    for (var i=0; i<params.length; i++){
        var param = params[i];
        if (this.areEqual2(el, param, value)) return true;
    }
    return false;
};
Sahi.prototype.findLink = function (id, inEl) {
	var inEl = inEl ? inEl : this.top();
    var res = this.getBlankResult();
    var retVal = this.findImageHelper(id, inEl, res, "sahiText", false).element;
    if (retVal != null) return retVal;

    res = this.getBlankResult();
    return this.findImageHelper(id, inEl, res, "id", false).element;
};
Sahi.prototype.findImage = function (id, inEl) {
	inEl = inEl ? inEl : this.top();
    var res = this.getBlankResult();
    var retVal = this.findImageHelper(id, inEl, res, "title|alt", true).element;
    if (retVal != null) return retVal;

    res = this.getBlankResult();
    retVal = this.findImageHelper(id, inEl, res, "id", true).element;
    if (retVal != null) return retVal;

    retVal = this.findImageHelper(id, inEl, res, this.getImageSrc, true).element;
    return retVal;
};
Sahi.prototype.getImageSrc = function(el){
	var src = el.src;
	return src.substring(src.lastIndexOf("/")+1);
};
Sahi.prototype.findImageHelper = function (id, win, res, param, isImg) {
    if ((typeof id) == "number") {
        res.cnt = 0;
        res = this.findImageByIx(id, win, res, isImg);
        return res;
    } else {
        var o = this.getArrayNameAndIndex(id);
        var imgIx = o.index;
        var fetch = o.name;
        var doc = this.getDoc(win);
	    var imgs = isImg ? this.getElementsByTagName("IMG", doc) : this.getElementsByTagName("A", doc);
        for (var i = 0; i < imgs.length; i++) {
            if (this.areEqual(imgs[i], param, fetch)) {
                res.cnt++;
                if (res.cnt == imgIx || imgIx == -1) {
                    res.element = imgs[i];
                    res.found = true;
                    return res;
                }
            }
        }
    }
    return this.recurseInFrames(this.findImageHelper, win, res, arguments);
};

Sahi.prototype.findImageByIx = function (ix, win, res, isImg) {
    var doc = this.getDoc(win);
    var imgs = isImg ? this.getElementsByTagName("IMG", doc) : this.getElementsByTagName("A", doc);
//    var imgs = isImg ? win.document.images : win.document.getElementsByTagName("A");
    if (imgs[ix - res.cnt]) {
        res.element = imgs[ix - res.cnt];
        res.found = true;
        return res;
    }
    res.cnt += imgs.length;
    return this.recurseInFrames(this.findImageByIx, win, res, arguments);
};

Sahi.prototype.findLinkIx = function (id, toMatch) {
    var res = this.getBlankResult();
    if (id == null || id == "") {
        var retVal = this.findImageIxHelper(id, toMatch, this.top(), res, null, false).cnt;
        if (retVal != -1) return retVal;
    }

    res = this.getBlankResult();
    var retVal = this.findImageIxHelper(id, toMatch, this.top(), res, "sahiText", false).cnt;
    if (retVal != -1) return retVal;

    res = this.getBlankResult();
    return this.findImageIxHelper(id, toMatch, this.top(), res, "id", false).cnt;
};
Sahi.prototype.findImageIx = function (id, toMatch) {
    var res = this.getBlankResult();
    if (id == null || id == "") {
        var retVal = this.findImageIxHelper(id, toMatch, this.top(), res, null, true).cnt;
        if (retVal != -1) return retVal;
    }

    res = this.getBlankResult();
    var retVal = this.findImageIxHelper(id, toMatch, this.top(), res, this.getImageSrc, true).cnt;
    if (retVal != -1) return retVal;

    res = this.getBlankResult();
    var retVal = this.findImageIxHelper(id, toMatch, this.top(), res, "title|alt", true).cnt;
    if (retVal != -1) return retVal;

    res = this.getBlankResult();
    return this.findImageIxHelper(id, toMatch, this.top(), res, "id", true).cnt;
};
Sahi.prototype.findImageIxHelper = function (id, toMatch, win, res, param, isImg) {
    if (res && res.found) return res;

    var imgs = isImg ? win.document.images : this.getElementsByTagName("A", win.document);
    for (var i = 0; i < imgs.length; i++) {
        if (param == null || this.areEqual(imgs[i], param, id)) {
            res.cnt++;
            if (imgs[i] == toMatch) {
                res.found = true;
                return res;
            }
        }
    }
    return this.recurseInFrames(this.findImageIxHelper, win, res, arguments);
};
Sahi.prototype.findElementById = function (win, id) {
    var res = null;
    if (win.document.getElementById(id) != null) {
        return win.document.getElementById(id);
    }
    return this.recurseInFrames(this.findElementById, win, res, arguments);
};
Sahi.prototype.findFormElementByIndex = function (ix, win, type, res, tagName) {
    var els = this.getElementsByTagName(tagName, this.getDoc(win));
    els = this.isWithinBounds(els, win);
    for (var j = 0; j < els.length; j++) {
        var el = els[j];
        if (el != null && this.areEqualTypes(this.getElementType(el), type) && this.checkElementVisible(el)) {
            res.cnt++;
            if (res.cnt == ix) {
                res.element = el;
                res.found = true;
                return res;
            }
        }
    }
    return this.recurseInFrames(this.findFormElementByIndex, win, res, arguments);
};

Sahi.prototype.getElementType = function (el) {
	var t1 = el.getAttribute("type");
	if (el.type == "text" && el.type != t1) {
		if (this.findInArray(this.textboxTypes, t1) == -1) return "text";
		return t1;
	}
	return el.type;
}

Sahi.prototype.findElementHelper = function (id, win, type, res, param, tagName) {
    if ((typeof id) == "number") {
        res = this.findFormElementByIndex(id, win, type, res, tagName);
        if (res.found) return res;
    } else {
    	// for elements with name like usernames[]
    	if (param != "associative_array") {
	    	var doc = this.getDoc(win);
	        var els = this.getElementsByTagName(tagName, doc);
	        els = this.isWithinBounds(els, win);
	        for (var j = 0; j < els.length; j++) {
	        	var el = els[j];
	            if (this.areEqualTypes(this.getElementType(el), type) && this.areEqual(el, param, id) && this.checkElementVisible(el)) {
	                res.element = el;
	                res.found = true;
	                return res;
	            }
	        }
    	}

        // normal
        var o = this.getArrayNameAndIndex(id);
        var ix = o.index;
        var fetch = o.name;
        els = this.getElementsByTagName(tagName, this.getDoc(win));
        els = this.isWithinBounds(els, win);
        for (var j = 0; j < els.length; j++) {
        	var el = els[j];
        	if (this.areEqualTypes(this.getElementType(el), type) && this.areEqual(el, param, fetch) && this.checkElementVisible(el)) {
                res.cnt++;
                if (res.cnt == ix || ix == -1) {
                    res.element = el;
                    res.found = true;
                    return res;
                }
            }
        }


    }
    return this.recurseInFrames(this.findElementHelper, win, res, arguments);
};
Sahi.prototype.findElementIxHelper = function (id, type, toMatch, win, res, param, tagName) {
    if (res && res.found) return res;
    var els = this.getElementsByTagName(tagName, this.getDoc(win));
    for (var j = 0; j < els.length; j++) {
        if (this.areEqualTypes(this.getElementType(els[j]), type) && this.areEqual(els[j], param, id) && this.checkElementVisible(els[j])) {
            res.cnt++;
            if (els[j] == toMatch) {
                res.found = true;
                return res;
            }
        }
    }
    return this.recurseInFrames(this.findElementIxHelper, win, res, arguments);
};
Sahi.prototype.areEqualTypes = function (type1, type2) {
    if (type1 == type2) return true;
    return (type1.indexOf("select") != -1 && type2.indexOf("select") != -1);
};
Sahi.prototype.findCell = function (id, inEl) {
    var res = this.getBlankResult();
    res = this.findTagHelper(id, inEl, "td", res, "sahiText").element;
    if (res != null) return res;
    var res = this.getBlankResult();
    res = this.findTagHelper(id, inEl, "td", res, "id").element;
    if (res != null) return res;
    res = this.getBlankResult();
    return this.findTagHelper(id, inEl, "td", res, "className").element;
};
Sahi.prototype.getBlankResult = function () {
    var res = new Object();
    res.cnt = -1;
    res.found = false;
    res.element = null;
    return res;
};
Sahi.prototype.getArrayNameAndIndex = function (id) {
    var o = new Object();
    if (!(id instanceof RegExp)) {
    	if (typeof id == "object") {
    		o.index = (id.sahiIndex != null) ? id.sahiIndex : -1;
    		o.name = {};
    		for (var k in id) {
    			if (k != "sahiIndex") o.name[k] = this.checkRegex(id[k]);
    		}
    		return o;
    	} else {
	    	var m = id.match(/(.*)\[([0-9]*)\]$/);
	    	if (m){
		        o.name = this.checkRegex(m[1]);
		        o.index = m[2];
		        return o;
	    	}
    	}
    }
	o.name = this.checkRegex(id);
	o.index = -1;
    return o;
};
Sahi.prototype.checkRegex = function(s){
	return ((typeof s) == "string" && s.match(this.CHECK_REGEXP)) ?  eval(s) : s;
};
Sahi.prototype.findInForms = function (id, win, type) {
    var fms = win.document.forms;
    if (fms == null) return null;
    for (var j = 0; j < fms.length; j++) {
        var el = this.findInForm(id, fms[j], type);
        if (el != null) return el;
    }
    return null;
};
Sahi.prototype.findInForm = function (name, fm, type) {
    var els = fm.elements;
    var matchedEls = new Array();
    for (var i = 0; i < els.length; i++) {
        var el = els[i];
        if (el.name == name && el.type && this.areEqualTypes(this.getElementType(el), type)) {
            matchedEls[matchedEls.length] = el;
        }
        else if ((el.type == "button" || el.type == "submit") && el.value == name && el.type == type) {
            matchedEls[matchedEls.length] = el;
        }
    }
    return (matchedEls.length > 0) ? (matchedEls.length == 1 ? matchedEls[0] : matchedEls ) : null;
};
Sahi.prototype.findTable = function (id, inEl) {
	var inEl = this.getDomRelAr(arguments);
//	if (!inEl) inEl = this.top();
    var res = this.getBlankResult();
    return this.findTagHelper(id, inEl, "table", res, "id").element;
};
Sahi.prototype._iframe = function (id, inEl) {
	var inEl = this.getDomRelAr(arguments);

	var res = this.getBlankResult();
    var el = this.findTagHelper(id, inEl, "iframe", res, "id").element;
    if (el != null) return el;

    res = this.getBlankResult();
    el = this.findTagHelper(id, inEl, "iframe", res, "name").element;
    if (el != null) return el;
};
// used from lib.js
Sahi.prototype.getArgsAr = function (args, start, end) {
	if (start == null) start = 0;
	if (end == null) end = args.length;
	var ar = []
	for (var i=start; i<end; i++) {
		ar.push(args[i]);
	}
	return ar;
}
Sahi.prototype._count = function (apiType, id, inEl) {
	var upper = 2048;
	var lower = 0;
	var origArgs = this.getArgsAr(arguments, 2);
	var fn = this[apiType];
	if (fn.apply(this, [id].concat(origArgs)) == null) return 0;
	var j=20;
	while (true && j-- >= 0) {
		var diff = Math.floor((upper-lower)/2);
		if (diff == 0) return lower + 1;
		var lookAt = lower + diff;
		if (typeof id == "object" && !(id instanceof RegExp)) {
			var id2 = id;
			id2.sahiIndex = lookAt;
		} else {
			var id2 = id + "[" + lookAt + "]";
		}
		var args = [id2].concat(origArgs);
		var el = fn.apply(this, args);
		if (el == null) {
			upper = lookAt;
		} else {
			lower = lookAt;
		}
		if (upper == lower) return lower + 1;
	}
	return 0;
};
// used on browser
Sahi.prototype._collect = function (apiType, id, inEl) {
	var els = [];
	var origArgs = this.getArgsAr(arguments, 2);
	var fn = this[apiType];	
	for (var i=0; i<2048; i++) {
		if (typeof id == "object" && !(id instanceof RegExp)) {
			var id2 = id;
			id2.sahiIndex = i;
		} else {
			var id2 = id + "[" + i + "]";
		}
		var args = [id2].concat(origArgs);
		var el = fn.apply(this, args);
		if (el == null) break;
		els.push(el);
	}
	return els;
}
Sahi.prototype._rte = Sahi.prototype._iframe;
Sahi.prototype.findResByIndexInList = function (ix, win, type, res) {
    var tags = this.getElementsByTagName(type, this.getDoc(win));
    tags = this.isWithinBounds(tags, win);
    if (tags[ix - res.cnt]) {
        res.element = tags[ix - res.cnt];
        res.found = true;
        return res;
    }
    res.cnt += tags.length;
    return this.recurseInFrames(this.findResByIndexInList, win, res, arguments);
};

Sahi.prototype.is_defined = function(variable){ //Check if variable is defined
	return !(typeof variable === "undefined");
};

Sahi.prototype.getBoundedRectangle = function (positionals){
	return {
				calcXleft : function(alignX){
					this.xleft = ( ((!_sahi.is_defined(this.xleft)) || (alignX > this.xleft)) ? alignX : this.xleft) ;
				},
				calcXright : function(alignXOuter){
					this.xright = ( ((!_sahi.is_defined(this.xright)) || (alignXOuter < this.xright)) ? alignXOuter : this.xright) ;
				},
				calcYtop : function(alignY){
					this.ytop =  ( ((!_sahi.is_defined(this.ytop)) || (alignY > this.ytop)) ? alignY : this.ytop) ;
				},
				calcYbottom : function(alignYOuter){
					this.ybottom = ( ((!_sahi.is_defined(this.ybottom)) || (alignYOuter < this.ybottom)) ? alignYOuter : this.ybottom) ;
				},
				calculateRectangle : function(positionals){ //calculate rectangle coordinates based on positional relations
					this.isCompleteDoc = (positionals.length == 0);
					for (var itr=0; itr<positionals.length; itr++){
						var positional = positionals[itr]; 
						if(positional.relation == "_under"){
							this.calcXleft(positional.alignX);
							this.calcXright(positional.alignXOuter);
							this.calcYtop(positional.limitY);
							if(typeof positional.limitUnder !== "undefined"){this.calcYbottom(positional.limitUnder);}
						} else if (positional.relation == "_above"){
							this.calcXleft(positional.alignX);
							this.calcXright(positional.alignXOuter);
							this.calcYbottom(positional.limitY);
							if(typeof positional.limitTop !== "undefined"){this.calcYtop(positional.limitTop);}							
						} else if(positional.relation == "_leftOf"){
							this.calcYtop(positional.alignY)
							this.calcYbottom(positional.alignYOuter)
							this.calcXright(positional.limitX);
						} else if(positional.relation == "_rightOf"){
							this.calcYtop(positional.alignY)
							this.calcYbottom(positional.alignYOuter)	
							this.calcXleft(positional.limitX);
						} else if(positional.relation == "_rowOf"){
							this.calcYtop(positional.alignY)
							this.calcYbottom(positional.alignYOuter)
						} else if (positional.relation == "_colOf"){
							this.calcXleft(positional.alignX);
							this.calcXright(positional.alignXOuter);
						}
					}
					return this;
		
				},
				hasNoRectanlge : function(){  
					return !(
							( (!_sahi.is_defined(this.xleft)) || (!_sahi.is_defined(this.xright)) || (this.xleft<this.xright) ) &&
								( (!_sahi.is_defined(this.ytop)) || (!_sahi.is_defined(this.ybottom)) || (this.ytop<this.ybottom) ) );
				},
				contains : function(xy){ //if xy coordinates lie within the rectangle 
					return (
							(((!_sahi.is_defined(this.xleft))) || (this.xleft <= xy[0])) &&
								(((!_sahi.is_defined(this.xright))) || (this.xright >= xy[0])) &&
									(((!_sahi.is_defined(this.ytop))) || (this.ytop <= xy[1])) &&
										(((!_sahi.is_defined(this.ybottom))) || (this.ybottom >= xy[1]))  );
									
						
					
				}
	}.calculateRectangle(positionals);
	
};

Sahi.prototype.isWithinBounds = function (tags, relPos){
	var rectangle = this.getBoundedRectangle(relPos.positionals);
	var filtered = [];
	if(rectangle.isCompleteDoc) { return tags; } //Incase no positionals were passed so entire doc is the rectangle so all tags returned
	if(rectangle.hasNoRectanlge()) { return filtered; } //Incase no rectanlge is formed using positionals

	for (var itr=0; itr<tags.length; itr++){
		if ( rectangle.contains(this.getLeftTop(tags[itr])) ){
			filtered.push(tags[itr]);
		}
	}
	return filtered;
};

Sahi.prototype.withinOffset = function(actual, left, right, offset){
	return actual >= (left - offset) && actual < (right + offset); 
//	return Math.abs(a - b) <= offset; 
};

Sahi.prototype.findTagHelper = function (id, win, type, res, param) {
    if ((typeof id) == "number") {
        res.cnt = 0;
        res = this.findResByIndexInList(id, win, type, res);
        return res;
    } else {
    	//this._alert(1);
        var o = this.getArrayNameAndIndex(id);
        var ix = o.index;
        var fetch = o.name;
        var tags = this.getElementsByTagName(type, this.getDoc(win));
        tags = this.isWithinBounds(tags, win);
        if (tags) {
            for (var i = 0; i < tags.length; i++) {
                if (this.areEqual(tags[i], param, fetch)) {
                	var el = tags[i];
                	if ((param == "sahiText" && (this.innerMost(el, fetch, type) != el)) || !this.checkElementVisible(el)){
                		continue;
                	}
                    res.cnt++;
                    if (res.cnt == ix || ix == -1) {
                        res.element = el;
                        res.found = true;
                        return res;
                    }
                }
            }
        }
    }
    // window based search without relations
    return this.recurseInFrames(this.findTagHelper, win, res, arguments);
};
Sahi.prototype.recurseInFrames = function(fn, win, res, paramsAr) {
	paramsAr = this.getArgsAr(paramsAr);
	var ix = this.findInArray(paramsAr, win);
    var frs = win.window.frames;
    if (frs) {
        for (var j = 0; j < frs.length; j++) {
            try{
            	var win2 = {relations:[], window:frs[j], positionals:win.positionals};
            	paramsAr[ix] = win2;
            	res = fn.apply(this, paramsAr);
            }catch(diffDomain){}
            if (res && res.found) return res;
        }
    }
    return res;	
};

Sahi.prototype.findTagIxHelper = function (id, toMatch, win, type, res, param) {
    if (res && res.found) return res;

    var tags = this.getElementsByTagName(type, this.getDoc(win));
    if (tags) {
        for (var i = 0; i < tags.length; i++) {
            if ((param == null || this.areEqual(tags[i], param, id)) && this.checkElementVisible(tags[i])) {
                res.cnt++;
                if (tags[i] == toMatch) {
                    res.found = true;
                    return res;
                }
            }
        }
    }
    return this.recurseInFrames(this.findTagIxHelper, win, res, arguments);
};
Sahi.prototype.canSimulateClick = function (el) {
    return (el.click || el.dispatchEvent);
};
Sahi.prototype.recordStep = function (step, OREntry, fromController) {
	var regExp = /_flex[(]|flex[(]/; 
	var showFlexSteps = this.isInsideFlex && regExp.test(step);
	if (showFlexSteps == false) return;
	if (!fromController) this.showStepsInController(step, true);
	var recordQS = 'step=' + this.encode(step) + (OREntry ? "&orname=" + this.encode(OREntry["name"]) + "&orvalue=" 
	               + this.encode(OREntry["value"]) : "");
	this.sendToServer('/_s_/dyn/' + this.recorderClass + '_record?' + recordQS, true);
}
Sahi.prototype.isRecording = function () {
    if (this.topSahi()._isRecording == null)
        this.topSahi()._isRecording = this.sendToServer("/_s_/dyn/SessionState_isRecording") == "1";
    return this.topSahi()._isRecording;
};
Sahi.prototype.createCookie = function (name, value, days, path, domain, secure){
    var expires = "";
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toGMTString();
    }
    var s = name + "=" + value + expires;
    s += "; path=" + (path ? path : "/");
    if (domain) s += "; domain=" + domain;
    if (secure) s += "; secure=" + secure;
    window.document.cookie = s;
};
Sahi.prototype._createCookie = Sahi.prototype.createCookie;
Sahi.prototype.readCookie = function (name){
	return this.sendToServer("/_s_/dyn/Cookies_read?name=" + name);
};
Sahi.prototype._cookie = Sahi.prototype.readCookie;
Sahi.prototype.eraseCookie = function (name, path){
	return this.sendToServer("/_s_/dyn/Cookies_delete?name=" + name + (path ? ("&path=" + encodeURIComponent(path)) : ""));
};
Sahi.prototype._deleteCookie = Sahi.prototype.eraseCookie;
Sahi.prototype._event = function (type, keyCode) {
    this.type = type;
    this.keyCode = keyCode;
};
var SahiAssertionException = function (msgNum, msgText) {
	_sahi.lastAssertStatus = "failure";
    this.messageNumber = msgNum;
    this.messageText = msgText;
    this.exceptionType = "SahiAssertionException";
};
var SahiNotMyWindowException = function (n) {
    this.name = "SahiNotMyWindowException";
    if (n){
        this.message = "Window with name ["+n+"] not found";
    }else{
        this.message = "Base window not found";
    }
};
var SahiNotMyDomainException = function (n) {
    this.name = "SahiNotMyDomainException";
    if (n){
        this.message = "Window with domain ["+n+"] not found";
    }else{
        this.message = "Base domain not found!";
    }
};
Sahi.prototype.onEv = function (e) {
    if (e.handled == true) return; //FF
//    if (this.doNotRecord || this.getServerVar("sahiEvaluateExpr") == true) return;
    if (this.doNotRecord) return;
    var targ = this.getKnownTags(this.getTarget(e));
    if (targ.id && targ.id.indexOf("_sahi_ignore_") != -1) return;
    if (e.type == this.triggerType) {
        if (targ.type) {
            var type = targ.type;
            if (type == "text" || type == "textarea" || type == "password"
                || type == "select-one" || type == "select-multiple") return;
        }
    }
	var elInfo = this.identify(targ);
	var ids = elInfo.apis; 
	if (ids.length == 0) return;
	var OREntry = this.isOREnabled ? this.getOREntry(ids) : null;
	var script = this.getScript(ids, targ, e.type, e);
	if (script == null) return;
	if (this.hasEventBeenRecorded(script)) return; //IE
	this.recordStep(script, OREntry);
	//this.sendIdsToController(elInfo, "RECORD");
    e.handled = true;
    //this.showInController(ids[0]);
};
Sahi.prototype.showStepsInController = function (s, isRecorded) {
    try {
        var c = this.getController();
        if (c) {
        	c.showSteps(s, isRecorded);
        }
    } catch(ex2) {
        // throw ex2;
    }
};
Sahi.prototype.showInController = function (info) {
	this.showStepsInController(this.getScript([info]));
};
Sahi.prototype.hasEventBeenRecorded = function (qs) {
    var now = (new Date()).getTime();
    if ((qs == this.lastQs) && ((now - this.lastTime) < 800)) {
    	this.lastTime = now;
    	return true;
    }
    this.lastQs = qs;
    this.lastTime = now;
    return false;
};
Sahi.prototype.getPopupName = function () {
    var n = null;
    if (this.isPopup()) {
        n = this.top().name;
        if (!n || n == "") {
            try{
                n = this.getTitle();
            }catch(e){}
        }
    }
    return n ? n : "";
};
Sahi.prototype._title = function(){
	return this.getTitle();
}
Sahi.prototype.getTitle = function(){
	return this.trim(this.top().document.title);	
}
Sahi.prototype.isPopup = function () {
    if (this.top().opener == null) return false;
    if (_sahi.top().opener.closed) return true;
    try{
        var x = _sahi.top().opener._sahi;
    }catch(openerFromDiffDomain){
        return true;
    }
    if (_sahi.top().opener._sahi != null && _sahi.top().opener._sahi.top() != window._sahi.top()){
        return true;
    }
    return false;
};
Sahi.prototype.addWait = function (time) {
    var val = parseInt(time);
    if (("" + val) == "NaN" || val < 200) throw new Error();
    this.showInController(new AccessorInfo("", "", "", "wait", time));
    //    this.sendToServer('/_s_/dyn/Recorder_record?event=wait&value='+val);
};
Sahi.prototype.mark = function (s) {
    this.showInController(new AccessorInfo("", "", "", "mark", s));
};
Sahi.prototype.doAssert = function (s, v) {
    try {
    	var el = eval(this.addSahi(s));
    	if ((typeof el) == "string" || (typeof el) == "boolean" || (typeof el) == "number"){
    		var steps = "_assertEqual(" + this.quoted(v)+ ", " + s + ");";
    		this.addPopupDomainPrefixes(steps);
    		this.showStepsInController(steps);
    	}
    	else if (el){    		
    		if (this.isFlexObj(el)) {
    			var steps = [this.language.ASSERT_EXISTS, this.language.ASSERT_EQUAL_VALUE].join("\n");
	    		steps = steps.replace(/<accessor>/g, s);
	    		steps = steps.replace(/<value>/g, this.toJSON(v));
	    		steps = steps.replace(/<popup>/g, this.getPopupDomainPrefixes())
	    		this.showStepsInController(steps);
    		} else {
	    		var elInfo = this.identify(el);
	    		var ids = elInfo.apis; 
	    		if (ids.length == 0) return;
	    		var OREntry = this.isOREnabled ? this.getOREntry(ids) : null;
	    		
	    		var assertions = elInfo.assertions;
	    		var steps = assertions.join("\n");
	    		steps = steps.replace(/<accessor>/g, s);
	    		steps = steps.replace(/<value>/g, this.toJSON(v));
	    		steps = steps.replace(/<popup>/g, this.getPopupDomainPrefixes())
	    		this.showStepsInController(steps);
	    		this.lastOREntry = OREntry;
    		}
    	}
        //this.showInController(lastAccessedInfo);
        //      this.sendToServer('/_s_/dyn/Recorder_record?'+getSahiPopUpQS()+this.getAccessorInfoQS(this.top()._lastAccessedInfo, true));
    } catch(ex) {
        this.handleException(ex);
    }
};
Sahi.prototype.getTarget = function (e) {
    var targ;
    if (!e) e = window.event;
    var evType = e.type;
    if (e.target) targ = e.target;
    else if (e.srcElement) targ = e.srcElement;
    if (targ.nodeType == 3) // defeat Safari bug
        targ = targ.parentNode;  
    return targ;
};
var AccessorInfo = function (accessor, shortHand, type, event, value, valueType, relationStr) {
    this.accessor = accessor;
    this.shortHand = shortHand;
    this.type = type;
    this.event = event;
    this.value = value;
    this.valueType = valueType;
    this.relationStr = relationStr;
};

Sahi.prototype.getAccessorInfoQS = function (ai, isAssert) {
    if (ai == null || ai.event == null) return;
    var s = "event=" + (isAssert ? "assert" : ai.event);
    s += "&accessor=" + this.encode(this.convertUnicode(ai.accessor));
    s += "&shorthand=" + this.encode(this.convertUnicode(ai.shortHand));
    s += "&type=" + ai.type;
    if (ai.value) {
        s += "&value=" + this.encode(this.convertUnicode(ai.value));
    }
    return s;
};
Sahi.prototype.addHandlersToAllFrames = function (win) {
    var fs = win.frames;
	try{
		this.addHandlers(win);
	}catch(e){}
    if (fs && fs.length > 0) {
        for (var i = 0; i < fs.length; i++) {
        	try{
        		this.addHandlersToAllFrames(fs[i]);
        	}catch(e){}
        }
    }
};
Sahi.prototype.docEventHandler = function (e) {
    if (!e) e = window.event;
    var t = this.getKnownTags(this.getTarget(e));
    if (t) this.attachEvents(t);
};
Sahi.prototype.addHandlers = function (win) {
    if (!win) win = this.self();
    var doc = win.document;
    this.addWrappedEvent(doc, "keyup", this.docEventHandler);
    this.addWrappedEvent(doc, "mousemove", this.docEventHandler);
};
Sahi.prototype.attachEvents = function (el) {
	if (el.hasAttached) return;
    var tagName = el.tagName.toLowerCase();
    if (this.isFormElement(el)) {
        this.attachFormElementEvents(el);
    } else {
        this.attachImageEvents(el);
    }
    el.hasAttached = true;
};
Sahi.prototype.attachFormElementEvents = function (el) {
    var type = el.type;
    var wrapped = this.wrappedOnEv; 
    if (el.onchange == wrapped || el.onblur == wrapped || el.onclick == wrapped) return;
    if (type == "text" || type == "file" || type == "textarea" || type == "password") {
        this.addEvent(el, "change", wrapped);
        this.addEvent(el, "keydown", wrapped);
    } else if (type == "select-one" || type == "select-multiple") {
        this.addEvent(el, "change", wrapped);
    } else if (type == "button" || type == "submit" || type == "reset" || type == "checkbox" || type == "radio" || type == "image") {
        this.addEvent(el, this.triggerType, wrapped);
    }
    this.addEvent(el, "contextmenu", wrapped);
};
Sahi.prototype.attachLinkEvents = function (el) {
    this.addWrappedEvent(el, this.triggerType, this.onEv);
};
Sahi.prototype.attachImageEvents = function (el) {
    this.addWrappedEvent(el, this.triggerType, this.onEv);
    this.addWrappedEvent(el, "contextmenu", this.onEv);
};
Sahi.prototype.addWrappedEvent = function (el, ev, fn) {
	this.addEvent(el, ev, this.wrap(fn));
};
Sahi.prototype.addEvent = function (el, ev, fn) {
    if (!el) return;
    if (el.attachEvent) {
        el.attachEvent("on" + ev, fn);
    } else if (el.addEventListener) {
        el.addEventListener(ev, fn, true);
    }
};
Sahi.prototype.removeEvent = function (el, ev, fn) {
    if (!el) return;
    if (el.attachEvent) {
        el.detachEvent("on" + ev, fn);
    } else if (el.removeEventListener) {
        el.removeEventListener(ev, fn, true);
    }
};
Sahi.prototype.setRetries = function (i) {
    this.sendToServer("/_s_/dyn/Player_setRetries?retries="+i);
    //this.setServerVar("sahi_retries", i);
};
Sahi.prototype.getRetries = function () {
    var i = parseInt(this.sendToServer("/_s_/dyn/Player_getRetries"));
    return ("" + i != "NaN") ? i : 0;
};
Sahi.prototype.getExceptionString = function (e)
{
    var stack = e.isSahiError ? "" : ("\n" + (e.stack ? e.stack : "No trace available"));
    return e.name + ": " + e.message + stack;
};

Sahi.prototype.onError = function (msg, url, lno) {
    try {
        var debugInfo = "Javascript error on page";
        if (!url) url = "";
        if (!lno) lno = "";
        var jsMsg = msg + " (" + url + ":" + lno + ")";
        if (msg && msg.indexOf("Access to XPConnect service denied") == -1) { //FF hack
            this.setJSError(jsMsg, lno);
        }
        if (this.prevOnError && this.prevOnError != this.onError){
        	this.prevOnError(msg, url, lno);
        }
    } catch(swallow) {
    }
};
Sahi.prototype.setJSError = function (msg, lno) {
    this.__jsError = {'message':msg, 'lineNumber':lno};
};
Sahi.prototype.openWin = function (e) {
    try {
        if (!e) e = window.event;
        this.controller = window.open("", "sahiControl", this.getWinParams(e));
        var diffDom = false;
        try {
            var checkDiffDomain = this.controller.document.domain;
        } catch(domainInaccessible) {
            diffDom = true;
        }
        if (diffDom || !this.controller.isWinOpen) {
            this.controller = window.open(this.controllerURL, "sahiControl", this.getWinParams(e));
        }
        if (this.controller) this.controller.opener = window;
        if (e) this.controller.focus();
    } catch(ex) {
        this.handleException(ex);
    }
};
Sahi.prototype.openController = Sahi.prototype.openWin;
Sahi.prototype.closeController = function(){
    var controlWin = this.getController();
    if (controlWin && !controlWin.closed) {
    	controlWin.close();
    }
};

Sahi.prototype.getWinParams = function (e) {
    var positionParams = "";
    
    var x = e ? e.screenX - 40 : window.screen.width - this.controllerWidth - 50;
    var y = e ? e.screenY - 60 : 100;
    
    if (this._isIE()) positionParams = ",left=" + x + ",top=" + y;
    else positionParams = ",screenX=" + x + ",screenY=" + y;
    
    return "height="+ this.controllerHeight +"px,width="+ this.controllerWidth +"px,resizable=yes,toolbar=no,status=no" + positionParams;
};
Sahi.prototype.getController = function () {
    var controller = this.topSahi().controller;
    if (controller && !controller.closed) return controller;
};
Sahi.prototype.xopenControllerWindow = function (e) {
    if (!e) e = window.event;
    if (!this.isHotKeyPressed(e)) return true;
    this.topSahi().openWin(e);
    return true;
};
Sahi.prototype.openControllerWindow = function (e) {
    if (!e) e = window.event;
    if (!this.isHotKeyPressed(e)) return true;
    if (this._isChrome()) {
        window.setTimeout(function(){_sahi.topSahi().openWin(e)}, 100);
    } else {
        this.topSahi().openWin(e);
    }
    return true;
};
Sahi.prototype.isHotKeyPressed = function (e) {
    return ((this.hotKey == "SHIFT" && e.shiftKey)
        || (this.hotKey == "CTRL" && e.ctrlKey)
        || (this.hotKey == "ALT" && e.altKey)
        || (this.hotKey == "META" && e.metaKey));
};
Sahi.prototype.mouseOver = function (e) {
    if (!e) e = window.event;
    try {
        if (this.getTarget(e) == null) return;
        if (!e.ctrlKey || this.isInsideFlex) return;
        this.showCoords(e);
//        var controlWin = this.getController();
//        if (controlWin) {
            var el = this.getTarget(e);
            this.__lastMousedOverElement = el;
            if (this.__queuedMouseOverTimer) window.clearTimeout(this.__queuedMouseOverTimer);
            this.__queuedMouseOverTimer = window.setTimeout(this.wrap(this.queuedMouseOver), 50);
//        }
    } catch(ex) {
        // throw ex;
    }
};
Sahi.prototype.queuedMouseOver = function(){
	var el = this.__lastMousedOverElement;
	try{
		this.identifyAndDisplay(el);
		var controlWin = this.getController();
        if (controlWin && !controlWin.closed) {
			controlWin.clearUpDownHistory();
		}
	}catch(e){
	}
};
Sahi.prototype.identifyAndDisplay = function(el){
    var elInfo = this.identify(this.getKnownTags(el));
    if (elInfo == null || elInfo.apis == null) return;
    if (elInfo.apis.length > 0) acc = elInfo.apis[0];
    else acc = null;
    if (acc) {
		var accessors = [];
		for ( var i = 0; i < elInfo.apis.length; i++) {
			accessors[i] = this.escapeDollar(this.getAccessor1(elInfo.apis[i]));
		}
		this.sendIdentifierInfo(accessors, 
    			this.escapeDollar(this.getAccessor1(acc)), 
    			this.escapeValue(acc.value), 
    			this.getPopupDomainPrefixes(el), 
    			elInfo.assertions);
    }
}
Sahi.prototype.sendIdentifierInfo = function(accessors, escapedAccessor, escapedValue, popupName, assertions){
    var controlWin = this.getController();
	controlWin.displayInfo(accessors, escapedAccessor, escapedValue, popupName, assertions);	
}
Sahi.prototype.showCoords = function(e) {
	var x = e.clientX;
	var y = e.clientY;
    try {
        var controlWin = this.getController();
        if (controlWin && !controlWin.closed) {
            controlWin.showCoords(x, y);
        }
    } catch(ex) {
    }
	
}
Sahi.prototype.escapeDollar = function (s) {
    if (s == null) return null;
    return s.replace(/[$]/g, "\\$");
};
Sahi.prototype.getAccessor1 = function (info) {
    if (info == null) return null;
    if ("" == (""+info.shortHand) || info.shortHand == null) return null;
    return info.type + "(" + this.escapeForScript(info.shortHand) + (info.relationStr ? (", " + info.relationStr) : "") + ")"; 
};
Sahi.prototype.escapeForScript = function (s) {
    return this.quoteIfString(s);
};
Sahi.prototype.schedule = function (cmd, debugInfo) {
    if (!this.cmds) return;
    var i = this.cmds.length;
    this.cmds[i] = cmd;
    this.cmdDebugInfo[i] = debugInfo;
};
Sahi.prototype.instant = function (cmd, debugInfo) {
    if (!this.cmds) return;
    var i = this.cmdsLocal.length;
    this.cmdsLocal[i] = cmd;
    this.cmdDebugInfoLocal[i] = debugInfo;
};
Sahi.prototype.play = function () {
	this.execNextStep(false, this.INTERVAL);
};
Sahi.prototype._setXHRReadyStatesToWaitFor = function(s){
	this.setWaitForXHRReadyStates(s);
	this.sendToServer("/_s_/dyn/SessionState_setXHRReadyStatesToWaitFor?states="+this.encode(s));
}
Sahi.prototype.setWaitForXHRReadyStates = function(s){
	this.waitWhenXHRReadyState1 = s.indexOf("1") != -1;
	this.waitWhenXHRReadyState2 = s.indexOf("2") != -1;
	this.waitWhenXHRReadyState3 = s.indexOf("3") != -1;
}
Sahi.prototype.showOpenXHRs = function (){
    var xs = this.XHRs;
    var s = "";
    for (var i=0; i<xs.length; i++){
        var xsi = xs[i];
        if (xsi){
        	try{
        		if (xsi.readyState!=4){
        			s += "this.XHRs[" + i + "] " + xsi + ": xsi.readyState="+xsi.readyState + "\n";
        		}
        	}catch(e){
        		s += e;
        	}
        }
    }	
    return s;
}
Sahi.prototype.areXHRsDone = function (){
    var xs = this.XHRs;
    var maxTime = this.SAHI_MAX_WAIT_FOR_LOAD * this.INTERVAL;
    var now = new Date().getTime();
    for (var i=0; i<xs.length; i++){
        var xsi = xs[i];
        //this.d("xsi.readyState="+xsi.readyState)
        if (xsi){
        	var t = this.XHRTimes[i];
        	if (t == -1) continue;
        	if (now - t > maxTime) {
        		// AJAX request has been around for more than 2 minutes. Consider as Comet
            	this.XHRTimes[i] = -1;
        		continue;
        	}
        	if (xsi.readyState==2 || xsi.readyState==3){
//        		this._debug("xsi.readyState="+xsi.readyState);
        	}
        	if (xsi.readyState==1 && this.waitWhenXHRReadyState1) {
        		return false;
        	}
        	if (xsi.readyState==2 && this.waitWhenXHRReadyState2) {
        		return false;
        	}
        	if (xsi.readyState==3){
        		if (this.waitWhenXHRReadyState3) return false;
        		try{
        			var m = xsi.responseText;
        		}catch(e){return false;}
        	}
        }
    }
    return true;
};
Sahi.prototype.areFlexAppsLoaded = function (win) {
	var els = [];
	if (this._isIE()) {
		els = win.document.getElementsByTagName("OBJECT");
	} else {
		els = win.document.getElementsByTagName("EMBED");
	}
	for (var i=0; i<els.length; i++) {
		var el = els[i];
		try {
			var id = el._sahi_getFlexId();
			var fl = this.getFlexWrapper(el);
			try {
				if (fl.currentCursorID() != 0) return false;
			} catch (e3){}
			if (this.flexWaitCondition && !this.flexWaitCondition(fl)) return false;
		}catch (e) {
			//this._debug("" + e);
			try {
				if (el.PercentLoaded() != 100) return false;
			} catch (e2){}
		}
	}
	
    var frs = win.frames;
    if (frs && win.frames) {
    	var loaded = true;
        for (var j = 0; j < frs.length; j++) {
        	try {
	    		loaded = this.areFlexAppsLoaded(frs[j]);
	    		if (!loaded) return false;
	    	}catch (e) {    		
	    	}
        }
    }  
    return true;
};

Sahi.prototype.d = function(s){
    this.updateControlWinDisplay(s);
};
Sahi.prototype.areWindowsLoaded = function (win) {
    try {
        if (win.location.href == "about:blank") return true;
    } catch(e) {
        return true;
        // diff domain
    }
    try {
        var fs = win.frames;
        if (!fs || fs.length == 0) {
            try {
                return (this._isIE() && (win.document.readyState == "complete")) || this.loaded;
            } catch(e) {
                //this.d("**********");
                return true;
                //diff domain; don't bother
            }
        } else {
            if (win.name == "listIframe") this.d("fs.length="+fs.length);
            for (var i = 0; i < fs.length; i++) {
                //this.d("" + i + ") " +fs[i].name);
                try{
                    if (""+fs[i].location != "about:blank" && !fs[i]._sahi.areWindowsLoaded(fs[i])) return false;
                }catch(e){
                    // skip if error. can happen for ""+fs[i].location if diff domain.
                }
            }
            if (win.document && this.getElementsByTagName("frameset", win.document).length == 0)
                return (this._isIE() && (win.document.readyState == "complete")) || this.loaded;
            else return true;
        }
    }
    catch(ex) {
        //this.d("2 to " + ex);
        //this._debugToErr("3 pr " + ex.prototype);
        return true;
        //for diff domains.
    }
};
var _isLocal = false;
Sahi._timer = null

Sahi.prototype.execNextStep = function (isStep, interval) {
    if (isStep || !this.isPlaying()) return;
    if (Sahi._timer) window.clearTimeout(Sahi._timer);
    Sahi._timer = window.setTimeout("try{_sahi.ex();}catch(ex){}", interval);
};
Sahi.prototype.hasErrors = function () {
    var i = this.sendToServer("/_s_/dyn/Player_hasErrors");
    return i == "true";
};
Sahi.prototype.getCurrentStep = function () {
    var wasOpened = 1;
    var windowName = "";
    var windowTitle = "";
    var windowURL = "";
    try{
        wasOpened = (this.top().opener == null || (this.top().opener._sahi.top() == this.top())) ? 0 : 1;
    }catch(e){}
    try{
        windowName = this.top().name;
    }catch(e){}
    try{
        windowTitle = this.getTitle();
    }catch(e){}
    try{
        windowURL = this.top().location.href;
    }catch(e){}
    var v = this.sendToServer("/_s_/dyn/Player_getCurrentStep?derivedName="+this.getPopupName()
    	+ "&wasOpened="+wasOpened+"&windowName="+this.encode(windowName)
        + "&windowTitle="+this.encode(windowTitle)
        + "&windowURL="+this.encode(windowURL) 
        + "&domain=" + this.encode(this.getDomainContext()));
    var dec = this.decode(v);
    //this.d(dec);
    return eval("(" + dec + ")");
};
Sahi.prototype.getDomainContext = function(){
	return (this.top() == _sahi_top) ? "" : document.domain;
}
Sahi.prototype.markStepDone = function(stepId, type, failureMsg){
	// duplicate checking needed for IE8 link clicks
	if (this.lastStepInfo && stepId == this.lastStepInfo[0] && type == this.lastStepInfo[1] && failureMsg == this.lastStepInfo[2]) return;
	this.lastStepInfo = [stepId, type, failureMsg];
    var qs = "stepId=" + stepId + (failureMsg ? ("&failureMsg=" + this.encode(failureMsg)) : "") + "&type=" + type;
    this.sendToServer("/_s_/dyn/Player_markStepDone?"+qs);
};
Sahi.prototype.markStepInProgress = function(stepId, type, timeout){
    var qs = "stepId=" + stepId + "&type=" + type;
    if (timeout) qs += "&timeout=" + timeout;
    this.sendToServer("/_s_/dyn/Player_markStepInProgress?"+qs);
};

Sahi.prototype.skipTill = function(n){
    var stepId = -1;
    var lastStepId = -1;
    while(true){
        var stepInfo = this.getCurrentStep();
        var stepId = stepInfo['stepId'];
        if (stepId == null) return;
        if (lastStepId == stepId){
            continue;
        }
        lastStepId = stepId;
        var type = stepInfo['type'];
        if (type == "STOP") {
            this.showStopPlayingMessage();
            this.topSahi()._isPlaying = false;
            return;
        }
        var step = stepInfo['step'];
        if (step == null || step == 'null') continue;
        if (stepId < n){
            this.markStepDone(stepId, "skipped");
        }else{
            break;
        }
    }
};
// This is needed for non Sahi drivers.
Sahi.prototype.ping = function () {
	if (this.controllerMode == "sahi") return;
	if (!this.pinger){
		this.pinger = this.createRequestObject();
		this.pinger.onreadystatechange = function(){
			 if(this.readyState==4){
			    	try{
				    	if ( this.status == 200 ){
				    		var s = this.responseText;
				    		var obj = eval("(" + s + ")");
//				    		alert(s);
				    		_sahi.setState(obj);
				    		window.setTimeout("_sahi.ping()", 1000);
				    	}else{
				    		//_sahi._alert( "HTTP "+req.status+".  An error was encountered: "+ req.statusText );
				    	}	
			    	}catch(e){
			    		//throw(e);
			    	}
				}	
		}	
	}
	this.pinger.open("GET", "/_s_/dyn/SessionState_ping", true);
	this.pinger.send(null);	
}
Sahi.prototype.setState = function(o){
	if (this.topSahi()._isRecording != o.isRecording){
		if (o.isRecording)
			this.startRecording();
		else
			this.stopRecording();
	}
	this.topSahi()._isRecording = o.isRecording;
	
	this.topSahi()._isPaused = o.isPaused;
	
	var wasPlaying = this.topSahi()._isPlaying;
	this.topSahi()._isPlaying = o.isPlaying;	
	if (o.isPlaying && wasPlaying != o.isPlaying){
		this.play();
	}
}
//Sahi.prototype.checkExecution = function(){
//	//this._debug("checkExecution " + (new Date() - this.exLastTimeStamp));
//	if (new Date() - this.exLastTimeStamp > 5000) {
//		this.execNextStep(this.exIsStep, this.interval);
//	}
//}
Sahi.prototype.ex = function (isStep) {
//	this.exLastTimeStamp = new Date();
	this.exIsStep = isStep;
    var stepId = -1;
    try{
        if (this.isPaused() && !isStep) return;
        //this._debug(this.areWindowsLoaded(this.top()));
        if (!this.byPassWaitMechanism && ((!this.areWindowsLoaded(this.top()) || !this.areXHRsDone() || !this.areFlexAppsLoaded(this.top())) && this.waitForLoad > 0)){
            this.stabilityIndex = 0; 
        }
        if (this.stabilityIndex < this.STABILITY_INDEX){
        	this.stabilityIndex = this.stabilityIndex + 1;
        	this.waitForLoad  = this.waitForLoad - 1;
            if (!this._isIE() && this.waitForLoad % 20 == 0){
                this.check204Response(this.top());
            }
            this.execNextStep(isStep, this.interval);
            return;        	
        }
        if (this.__jsError){
            var msg = this.getJSErrorMessage(this.__jsError);
            this._log(msg, "custom1");
            this.d(this.__jsError.message);
            this.__jsError = null;
        }
        var stepInfo = this.getCurrentStep();
        var type = stepInfo['type'];
        if (type == "NO_SCRIPT") {
        	return;
        }
        if (type == "STOP") {
            this.showStopPlayingMessage();
            if (!this.isSingleSession) {
            this.topSahi()._isPlaying = false;
            //this.stopPlaying();
            return;
            }
        }
        var step = stepInfo['step'];
        // this._debug(this.getPopupName() + "::" + type+"::"+ new Date());
        if (type == "WAIT"){
        	if (step != null && step != 'null'){
        		var stepId = stepInfo['stepId']
        		this.updateControlWinDisplay(step, stepId);
        		try{
        			var res = eval(step);
        		}catch(e){}
        		if (res) this.markStepDone(stepId, "info");
        	}
			this.execNextStep(isStep, this.interval);
			return;
        }
        if (step == null || step == 'null'){
            this.execNextStep(isStep, this.interval);
            return;
        }
        stepId = stepInfo['stepId'];
        if (this.lastStepId == stepId){
            this.execNextStep(isStep, this.interval);
            return;
        }
        var debugInfo = stepInfo['debugInfo'];
        var origStep = stepInfo['origStep'];
        if (type == 'JSERROR'){
        	var m = (stepInfo.message) ? stepInfo.message : "Logs may have details.";
            this.updateControlWinDisplay("Error in script: "+origStep+"\n" + m);
            return;
        }
        var type = (step.indexOf("_sahi._assert") != -1) ? "success" : (type == "NO_LOG" ? "NO_LOG" : "info");
        this.markStepInProgress(stepId, type);
        this.updateControlWinDisplay(origStep, stepId);
        this.reAttachEvents();
    	this.xyoffsets = new Sahi.Dict();
//    	this.alignY = this.alignX = null;
    	if (step.indexOf("_sahi._assert") != -1) this.lastAssertStatus = "success";
    	this.currentStepId = stepId;
    	this.currentType = type; 
//    	this._alert(this.loaded);
        if (this.getServerVar("screenCapture")) {
        	this._takeScreenShot();
//        	this.checkForScrollAndCapture(stepId);
        }
        if (step.indexOf("_assertSnapShot") != -1) {
        	// img compare takes much longer than normal steps. should not auto proceed
        	this.markStepInProgress(stepId, type, 30000);        
        	// Force no retrying
        	this.setRetries(this.MAX_RETRIES + 1);
        	
        }
        this.forked = false;
        eval(step);
        if (!this.forked) {
        	this.afterEval();
        }
//        if (this.getServerVar("screenCapture")) this.checkForScrollAndCapture(stepId);
    }catch(e){
        var retries = this.getRetries();
        if (retries < this.MAX_RETRIES) {
            retries = retries + 1;
            this.setRetries(retries);
            this.interval = this.ONERROR_INTERVAL; //100 * (2^retries);
            this.execNextStep(isStep, this.interval);
            return;
        } else {
            if (e instanceof SahiAssertionException){
                var failureMsg = "Assertion Failed. " + (e.messageText ? e.messageText : "");
                this.setRetries(0);
                this.markStepDone(stepId, "failure", failureMsg);
                this.execNextStep(isStep, this.interval);
            } else {
                if (this.isPlaying()) {
                    var msg = this.getJSErrorMessage(e);
                    this.markStepDone(stepId, "error", msg);
                }
                this.execNextStep(isStep, this.interval);
            }
        }
    }
};
Sahi.prototype.fork = function(timeout){
//	this.markStepInProgress(this.currentStepId, this.currentType, timeout?timeout:3000); 
	this.forked = true;
}
Sahi.prototype.afterEval = function () {
    this.lastStepId = this.currentStepId;
    this.markStepDone(this.currentStepId, this.currentType);
    this.waitForLoad = this.SAHI_MAX_WAIT_FOR_LOAD;
    this.interval = this.INTERVAL;
    this.execNextStep(this.exIsStep, this.interval);
}
Sahi.prototype.getWinDimensions = function(){
	var winW = 630, winH = 460;
	if (this._isIE() && document.body && document.body.clientWidth) {
		winW = document.body.clientWidth;
		winH = document.body.clientHeight;
	} else if (this._isFF()) {
		winW = document.body.offsetWidth;
		if (window.innerWidth < winW) winW = window.innerWidth;
		winH = window.innerHeight;
	} else if (this._isChrome()) {
		winW = document.body.offsetWidth;
		winH = document.body.offsetHeight;
	} else {
		if (document.body && document.body.offsetWidth) {
			winW = document.body.offsetWidth;
			winH = document.body.offsetHeight;
		}
		if (document.compatMode=='CSS1Compat' &&
		    document.documentElement &&
		    document.documentElement.offsetWidth ) {
			winW = document.documentElement.offsetWidth;
			winH = document.documentElement.offsetHeight;
		}
		if (window.innerWidth && window.innerHeight) {
			winW = window.innerWidth;
			winH = window.innerHeight;
		}	
	}
	return [winW, winH];
}
Sahi.prototype.xgetWinDimensions = function(){
	var winW = 630, winH = 460;
	if (document.body && document.body.offsetWidth) {
		winW = document.body.offsetWidth;
		winH = document.body.offsetHeight;
	}
	if (document.compatMode=='CSS1Compat' &&
	    document.documentElement &&
	    document.documentElement.offsetWidth ) {
		winW = document.documentElement.offsetWidth;
		winH = document.documentElement.offsetHeight;
	}
	if (window.innerWidth && window.innerHeight) {
		winW = window.innerWidth;
		winH = window.innerHeight;
	}	
	return [winW, winH];
}
Sahi.prototype.getWinPosition = function(){
//	var ADDONS_BAR_HEIGHT = 28;
	var ADDONS_BAR_HEIGHT = 8;
	if (this._isFF() || this._isSafari() || this._isChrome()) {
		var l = window.screenX + (window.outerWidth - window.innerWidth) - 8;
		var t = window.screenY + (window.outerHeight - window.innerHeight) - ADDONS_BAR_HEIGHT;
		return [l, t];
	}
	if (this._isIE()) {
		var l = window.screenLeft + 2;
		var t = window.screenTop + 2;
		return [l, t];
	}
	return [window.screenLeft, window.screenTop];
}
Sahi.prototype.checkForScrollAndCapture = function(stepId){
	var p = this.getWinPosition();
	var l = p[0];
	var t = p[1];
	var d = this.getWinDimensions();
	var w = d[0];
	var h = d[1];
	
	window.scrollTo(0,0);
	var i=0;
	//this._debug("t="+t+" l="+l+" h="+h +" w=" + w);
//	this.takeHorSnapShots(stepId, i, t, l, h, w);
	this.takeSnapShot(stepId, i, t, l, h, w);
	i++;
	var innerHeight = this._isIE() ? parseInt(document.body.clientHeight) : parseInt(window.innerHeight);
	var scrollHeight = parseInt(document.body.scrollHeight);
	while((scrollHeight-innerHeight) > innerHeight){
		window.scrollBy(0,innerHeight);
		scrollHeight -= innerHeight;
//		this.takeHorSnapShots(stepId, i, t, l, h, w);
		this.takeSnapShot(stepId, i, t, l, h, w);
		i++;
	}
	if((scrollHeight-innerHeight < innerHeight)&&(scrollHeight-innerHeight>0)){
		window.scrollBy(0,scrollHeight-innerHeight);
		scrollHeight -= innerHeight;
		//this._alert(scrollHeight);
		var t2 = t + innerHeight - scrollHeight;
//		this.takeHorSnapShots(stepId, i, t, l, h, w);
		this.takeSnapShot(stepId, i, t2, l, scrollHeight, w);
		i++;
	}
	return this.stitchSnapShots(stepId, i, "V");
};
Sahi.prototype.takeHorSnapShots = function(stepId, vertIx, t, l, h, w){
	stepId = stepId + "temp" + vertIx;
	window.scrollTo(0, this.getScrollOffsetY());
	var i=0;
	//this._debug("t="+t+" l="+l+" h="+h +" w=" + w);
	this.takeSnapShot(stepId, i, t, l, h, w);
	i++;
	var innerWidth = (this._isIE()) ? parseInt(document.body.clientWidth) : parseInt(window.innerWidth);
	var scrollWidth = parseInt(document.body.scrollWidth);
	while((scrollWidth-innerWidth) > innerWidth){
		window.scrollBy(innerWidth, 0);
		scrollWidth -= innerWidth;
		this.takeSnapShot(stepId, i, t, l, h, w);
		i++;
	}
	if((scrollWidth-innerWidth < innerWidth)&&(scrollWidth-innerWidth>0)){
		window.scrollBy(scrollWidth-innerWidth, 0);
		scrollWidth -= innerWidth;
		//this._alert(scrollWidth);
		var l2 = l + innerWidth - scrollWidth;
		this.takeSnapShot(stepId, i, t, l2, h, scrollWidth);
		i++;
	}
	return this.stitchSnapShots(stepId, i, "H");
}
Sahi.prototype.stitchSnapShots = function(stepId, index, dir) {
	return this.sendToServer("/_s_/dyn/Player_stitchSnapShots?stepId="+stepId+"&finalIx="+index+"&dir="+dir);
};
Sahi.prototype.getJSErrorMessage2 = function(msg, lineNumber){
	if (_sahi.controllerMode == "sahi"){
	    var url = "/_s_/dyn/Log_getBrowserScript?href="+this._scriptPath()+"&n="+lineNumber;
	    msg += "\n<a href='"+url+"'><b>Click for browser script</b></a>";
	}
    return msg;
};
Sahi.prototype.getJSErrorMessage = function(e){
    var msg = this.getExceptionString(e);
    var lineNumber = (e.lineNumber) ? e.lineNumber : -1;
    return e.isSahiError ? msg : this.getJSErrorMessage2(msg, lineNumber);
};
Sahi.prototype.check204Response = function(win){
	if (win._sahi.loaded != true) {
		var was204 = this.sendToServer("/_s_/dyn/Player_check204");
//		this._alert(was204+ " " + (typeof was204));
		if (was204 == "true") win._sahi.loaded = true;
	}
    var frs = win.frames;
    if (frs) {
        for (var j = 0; j < frs.length; j++) {
        	try{this.check204Response(frs[j]);}catch(e){}
        }
    }  
};
Sahi.prototype.xcanEvalInBase = function (cmd) {
    return  (this.top().opener == null && !this.isForPopup(cmd)) || (this.top().opener && this.top().opener._sahi.top() == this.top());
};
Sahi.prototype.xisForPopup = function (cmd) {
    return cmd.indexOf("_sahi._popup") == 0;
};
Sahi.prototype.xcanEval = function (cmd) {
    return (this.top().opener == null && !this.isForPopup(cmd)) // for base window
        || (this.top().opener && this.top().opener._sahi.top() == this.top()) // for links in firefox
        || (this.top().opener != null && this.isForPopup(cmd));
    // for popups
};
Sahi.prototype.pause = function () {
    this.topSahi()._isPaused = true;
    this.setServerVar("sahi_paused", 1);
};
Sahi.prototype.unpause = function () {
    // TODO
    this.topSahi()._isPaused = false;
    this.setServerVar("sahi_paused", 0);
    this.topSahi()._isPlaying = true;
};
Sahi.prototype.isPaused = function () {
    if (this.topSahi()._isPaused == null)
        this.topSahi()._isPaused = this.getServerVar("sahi_paused") == 1;
    return this.topSahi()._isPaused;
};
Sahi.prototype.topSahi = function(){
	//alert(this.top());
	return this.top()._sahi;
}
Sahi.prototype.updateControlWinDisplay = function (s, i) {
    try {
        var controlWin = this.getController();
        if (controlWin && !controlWin.closed) {
            // controller2.js checks if this i has already been displayed.
            controlWin.displayLogs(s.replace(/_sahi[.]/g, ""), i);
            if (i != null) controlWin.displayStepNum(i);
        }
    } catch(ex) {
    }
};
Sahi.prototype.setCurrentIndex = function (i) {
    this.startFromStep = i;
    return;
    if (_isLocal) {
        this.setServerVar("this.localIx", i);
    }
    else this.setServerVar("this.ix", i);
};
Sahi.prototype.xgetCurrentIndex = function () {
    if (this.cmdsLocal.length > 0) {
        var i = parseInt(this.getServerVar("this.localIx"));
        var localIx = ("" + i != "NaN") ? i : 0;
        if (this.cmdsLocal.length == localIx) {
            this.cmdsLocal = new Array();
            this.setServerVar("this.localIx", 0);
            _isLocal = false;
        } else {
            return localIx;
        }
    }
    var i = parseInt(this.getServerVar("this.ix"));
    return ("" + i != "NaN") ? i : 0;
};
Sahi.prototype.isPlaying = function () {
    if (this.topSahi()._isPlaying == null){
        this.topSahi()._isPlaying = this.sendToServer("/_s_/dyn/SessionState_isPlaying") == "1";
    }
    return this.topSahi()._isPlaying;
};
Sahi.prototype.playManual = function (ix) {
    this.skipTill(ix);
    //this.setCurrentIndex(ix);
    this.unpause();
    this.ex();
};
Sahi.prototype.startPlaying = function () {
    this.sendToServer("/_s_/dyn/Player_start");
};
Sahi.prototype.stepWisePlay = function () {
    this.sendToServer("/_s_/dyn/Player_stepWisePlay");
};
Sahi.prototype.showStopPlayingMessage = function () {
    this.updateControlWinDisplay("--Stopped Playback: " + (this.hasErrors() ? "FAILURE" : "SUCCESS") + "--");
};
Sahi.prototype.stopPlaying = function () {
    this.sendToServer("/_s_/dyn/Player_stop");
    this.showStopPlayingMessage();
    this.topSahi()._isPlaying = false;
};
Sahi.prototype.startRecording = function () {
    this.topSahi()._isRecording = true;
    this.addHandlersToAllFrames(this.top());
    //this.sendToServer("/_s_/dyn/Recorder_start");
};
Sahi.prototype.stopRecording = function () {
    this.topSahi()._isRecording = false;
    this.sendToServer("/_s_/dyn/Recorder_stop");
//    this.setServerVar("sahi_record", 0);
};
Sahi.prototype.getLogQS = function (msg, type, debugInfo, failureMsg) {
    var qs = "msg=" + this.encode(msg) + "&type=" + type
        + (debugInfo ? "&debugInfo=" + this.encode(debugInfo) : "")
        + (failureMsg ? "&failureMsg=" + this.encode(failureMsg) : "");
    return qs;
};
Sahi.prototype.logPlayBack = function (msg, type, debugInfo, failureMsg) {
    this.sendToServer("/_s_/dyn/TestReporter_logTestResult?"+this.getLogQS(msg, type, debugInfo, failureMsg));
};

Sahi.prototype.compareArrays = function (a1,a2) {
	if (a1 == null || a2 == null) return "One of the arrays is null";
	if (a1.length != a2.length && typeof(a1) == typeof(a2)) return "Difference in length of arrays:\nExpected Length:["+a1.length+"]\nActual Length:["+a2.length+"]";
	for(var i=0;i<a1.length;i++){
		//if ((typeof a1[i]) != (typeof a2[i])) return "Type mis-match error at index " + i;
		if ((typeof a1[i]) == "object" && (typeof a2[i]) == "object" && a1[i].length && a2[i].length){
			if (this.compareArrays(a1[i], a2[i])!="equal") return "Expected:" + this.toJSON(a1[i]) + "\nActual:" + this.toJSON(a2[i]) + " at index "+i;
		} else if (!this.areEqualParams(this.trim(a2[i]), this.checkRegex(this.trim(a1[i])))) {
			return "Expected:" + this.toJSON(a1[i]) + "\nActual:" + this.toJSON(a2[i]) + " at index "+i;
		}
	}
	return "equal"
};

Sahi.prototype.trim = function (s) {
    if (s == null) return s;
    if ((typeof s) != "string") return s;
    s = s.replace(/\xA0/g, ' ').replace(/\s\s*/g, ' ');
    var ws = /\s/;
    var t1 = (ws.test(s.charAt(0))) ? 1 : 0;
    var t2 = (ws.test(s.charAt(s.length-1))) ? s.length-1 : s.length;
    return s.slice(t1, t2);
};
Sahi.prototype.list = function (el) {
	if (this.isFlexObj(el)) return el.introspect() + "\n" + el.listProperties();
    var s = "";
    var f = "";
    var j = 0;
    if (typeof el == "array"){
        for (var i=0; i<el.length; i++) {
            s += i + "=" + el[i];
        }
    }
    if (typeof el == "object") {
        for (var i in el) {
            try {
                if (el[i] && el[i] != el) {
                    if (("" + el[i]).indexOf("function") == 0) {
                        f += i + "\n";
                    } else {
                        if (typeof el[i] == "object" && el[i] != el.parentNode) {
                            s += i + "={{" + el[i] + "}};\n";
                        }
                        s += i + "=" + el[i] + ";\n";
                        j++;
                    }
                }
            } catch(e) {
                s += "" + i + "\n";
            }
        }
    } else {
        s += el;
    }
    return s + "\n\n-----Functions------\n\n" + f;
};

Sahi.prototype.findInArray = function (ar, el) {
    var len = ar.length;
    for (var i = 0; i < len; i++) {
        if (ar[i] == el) return i;
    }
    return -1;
};
Sahi.prototype._isIE = function () {return this.navigator.appName == "Microsoft Internet Explorer";}
Sahi.prototype._isIE8 = function () {return /MSIE 8[.]/.test(this.navigator.userAgent);};
Sahi.prototype._isIE9 = function () {return /MSIE 9[.]/.test(this.navigator.userAgent);};
Sahi.prototype._isIE9Plus = function () {return this._isIE() && !(/MSIE [0-8][.]/.test(this.navigator.userAgent));};
Sahi.prototype._isIE9PlusStrictMode = function () {return this._isIE() && document.documentMode>=9;};
Sahi.prototype._isFF2 = function () {return /Firefox\/2[.]|Iceweasel\/2[.]|Shiretoko\/2[.]/.test(this.navigator.userAgent);};
Sahi.prototype._isFF3 = function () {return /Firefox\/3[.]|Iceweasel\/3[.]|Shiretoko\/3[.]/.test(this.navigator.userAgent);};
Sahi.prototype._isFF4 = function () {return /Firefox\/4[.]|Iceweasel\/4[.]|Shiretoko\/4[.]/.test(this.navigator.userAgent);};
Sahi.prototype._isFF5 = function () {return /Firefox\/5[.]|Iceweasel\/5[.]|Shiretoko\/5[.]/.test(this.navigator.userAgent);};
Sahi.prototype._isFF4Plus = function () {return (this._isFF() && !this._isFF2() && !this._isFF3());};
Sahi.prototype._isFF = function () {return /Firefox|Iceweasel|Shiretoko/.test(this.navigator.userAgent);};
Sahi.prototype._isChrome = function () {return /Chrome/.test(this.navigator.userAgent);};
Sahi.prototype._isSafari = function () {return /Safari/.test(this.navigator.userAgent) && !(/Chrome/.test(this.navigator.userAgent));};
Sahi.prototype._isOpera = function () {return /Opera/.test(this.navigator.userAgent);};
Sahi.prototype.isSafariLike = function () {return /Konqueror|Safari|KHTML/.test(this.navigator.userAgent);};
Sahi.prototype._isHTMLUnit = function() {return /HTMLUnit/.test(this.navigator.userAgent);}
Sahi.prototype.createRequestObject = function () {
    var obj;
    if (window.XMLHttpRequest){
        // If IE7, Mozilla, Safari, etc: Use native object
        obj = new XMLHttpRequest()
    }else {
        if (window.ActiveXObject){
            // ...otherwise, use the ActiveX control for IE5.x and IE6
            obj = new ActiveXObject("Microsoft.XMLHTTP");
        }
    }
    return obj;
};
Sahi.prototype._getFFVersion = function(){
	var m = navigator.userAgent.match(/(Firefox|Iceweasel|Shiretoko)[/]([0-9]+)/);
	return (m && m.length == 3) ? parseInt(m[2]) : -1;
}
Sahi.prototype.getServerVar = function (name, isGlobal) {
    var v = this.sendToServer("/_s_/dyn/SessionState_getVar?name=" + this.encode(name) + "&isglobal="+(isGlobal?1:0));
    return eval("(" + this.decode(v) + ")");
};
Sahi.prototype.setServerVar = function (name, value, isGlobal) {
    this.sendToServer("/_s_/dyn/SessionState_setVar?name=" + this.encode(name) + "&value=" + this.encode(this.toJSON(value)) + "&isglobal="+(isGlobal?1:0));
};
Sahi.prototype.getVarRemember = function (name, isGlobal) {
    var v = this.sendToServer("/_s_/dyn/SessionState_getVarRemember?name=" + this.encode(name) + "&isglobal="+(isGlobal?1:0));
    return eval("(" + this.decode(v) + ")");
};
Sahi.prototype.setVarRemember = function (name, value, isGlobal) {
    this.sendToServer("/_s_/dyn/SessionState_setVarRemember?name=" + this.encode(name) + "&value=" + this.encode(this.toJSON(value)) + "&isglobal="+(isGlobal?1:0));
};
Sahi.prototype.getVarStatic = function (name, isGlobal) {
    var v = this.sendToServer("/_s_/dyn/SessionState_getVarStatic?name=" + this.encode(name) + "&isglobal="+(isGlobal?1:0));
    return eval("(" + this.decode(v) + ")");
};
Sahi.prototype.setVarStatic = function (name, value, isGlobal) {
    this.sendToServer("/_s_/dyn/SessionState_setVarStatic?name=" + this.encode(name) + "&value=" + this.encode(this.toJSON(value)) + "&isglobal="+(isGlobal?1:0));
};
Sahi.prototype.logErr = function (msg) {
    //    return;
    this.sendToServer("/_s_/dyn/Log?msg=" + this.encode(msg) + "&type=err");
};

Sahi.prototype.getParentNode = function (el, tagName, occurrence, maxPossible) {
    if (!occurrence) occurrence = 1;
    var cnt = 0;
    var parent = el.parentNode;
    var maxParent = parent;
    while (parent && !this.areTagNamesEqual(parent.tagName, "body") && !this.areTagNamesEqual(parent.tagName.toLowerCase(), "html")) {
        if (this.areTagNamesEqual(tagName, "ANY") || this.areTagNamesEqual(parent.tagName, tagName)) {
            cnt++;
            if (occurrence == cnt) return parent;
        }
        maxParent = parent;
        parent = parent.parentNode;
    }
    if (maxPossible) return maxParent;
    return null;
};
Sahi.prototype.sendToServer = function (url, isAsync) {
    try {
        var rand = (new Date()).getTime() + Math.floor(Math.random() * (10000));
        var http = this.createRequestObject();
        url = url + (url.indexOf("?") == -1 ? "?" : "&") + "t=" + rand;
        url = url + "&sahisid=" + encodeURIComponent(this.sid);
        var post = url.substring(url.indexOf("?") + 1);
        url = url.substring(0, url.indexOf("?"));
        http.open("POST", url, isAsync===true); //needed for IE
        http.send(post);
        return http.responseText;
    } catch(ex) {
        this.handleException(ex)
    }
};
var s_v = function (v) {
    var type = typeof v;
    if (type == "number") return v;
    else if (type == "string") return "\"" + v.replace(/\r/g, '\\r').replace(/\n/g, '\\n').replace(/"/g, '\\"') + "\"";
    else return v;
};
Sahi.prototype.quoted = function (s) {
    return '"' + s.replace(/"/g, '\\"') + '"';
};
Sahi.prototype.handleException = function (e) {
    //  alert(e);
    //  throw e;
};
Sahi.prototype.convertUnicode = function (s) {
	return _sahi.escapeUnicode ? this.unicode(s) : s;
}
Sahi.prototype.unicode = function (source) {
	if (source == null) return null;
    var result = '';
    for (var i = 0; i < source.length; i++) {
        if (source.charCodeAt(i) > 127)
            result += this.addSlashU(source.charCodeAt(i).toString(16));
        else result += source.charAt(i);
    }
    return result;
};
Sahi.prototype.addSlashU = function (num) {
    var buildU
    switch (num.length) {
        case 1:
            buildU = "\\u000" + num
            break
        case 2:
            buildU = "\\u00" + num
            break
        case 3:
            buildU = "\\u0" + num
            break
        case 4:
            buildU = "\\u" + num
            break
    }
    return buildU;
};
Sahi.prototype.reAttachEvents = function () {
	if (!this.areWindowsLoaded(this.top())) return;
    this.reAttachSahi(this.top());
    if (this.isRecording()) 
        this.addHandlersToAllFrames(this.top());    
}

if (_sahi.top() == window){
    window.setInterval(_sahi.wrap(_sahi.reAttachEvents), 500);
}
Sahi.prototype.reAttachSahi = function (win) {
    try{
        if (!win._sahi) {
			this.reAttachSahiToWin(win);
        }
    }catch(e){}
    var fs = win.frames;
    if (fs && fs.length > 0) {
        for (var i = 0; i < fs.length; i++) {
            try{
            	if (this._isHTMLUnit() && fs[i].location.href && fs[i].location.href.indexOf(".gif") != -1) continue;
            	this.reAttachSahi(fs[i]);
            }catch(e){}
        }
    } 
}
Sahi.prototype.reAttachSahiToWin = function(win){
	this.mockDialogs(win);
    this.activateHotKey(win);
}
Sahi.prototype.onBeforeUnLoad = function () {
    this.loaded = false;
};
Sahi.prototype.onWindowLoad = function (e){
    try {
        this.loaded = true;
        this.activateHotKey();
    } catch(ex) {
        this.handleException(ex);
    }
    if (this.self() == this.top()) {
    	__sahiDebug__("onWindowLoad: this.self() == this.top(), play()");
        this.play();
        this.ping();
    }
    if (this.isRecording()) {
    	__sahiDebug__("init: isRecording() addHandlersToAllFrames");
    	this.addHandlersToAllFrames(this.top());
    }
}
Sahi.onWindowLoad = function(e){
    eval("_sahi.onWindowLoad()");
};

Sahi.prototype.init = function (e) {
	__sahiDebug__("init: start");
	if (this.initTimer) window.clearTimeout(this.initTimer);
    if (this.initialized) return;
    this.initialized = true;	
    try {
        this.activateHotKey();
    } catch(ex) {
        this.handleException(ex);
    }
    this.prepareADs();
    this.makeLibFunctionsAvailable();
    try {
//        if (self == this.top() && self.parent == this.top()) {
// Replaced above condition on addition of domain support. check.
    	__sahiDebug__("init: in try");
        if (this.self() == this.top()) {
        	__sahiDebug__("init: self == this.top(), play()");
            //this.ping();
        }
        if (this.isRecording()) {
        	__sahiDebug__("init: isRecording() addHandlersToAllFrames");
        	this.addHandlersToAllFrames(this.top());
        }
    } catch(ex) {
        //      throw ex;
        this.handleException(ex);
    }
	__sahiDebug__("init: before isHTMLUnit");
    
    this.wrappedOnEv = this.wrap(this.onEv);
//	this.isHTMLUnit = this._isHTMLUnit();
//    alert("Cookies: " + document.domain + " " + document.cookie);
//    this.listen();    
	__sahiDebug__("init: end");
};
Sahi.prototype.activateHotKey = function (win) {
    if (!win) win = this.self();
    try {
        var doc = win.document;
        this.addWrappedEvent(doc, "dblclick", this.reAttachEvents);
        this.addWrappedEvent(doc, "dblclick", this.openControllerWindow);
        this.addWrappedEvent(doc, "mousemove", this.mouseOver);
//        if (this.isSafariLike()) {
//            var prev = doc.ondblclick;
//            doc.ondblclick = function(e) {
//                if (prev != null) prev(e);
//                _sahi.openControllerWindow(e);
//            };
//        }
    } catch(ex) {
        this.handleException(ex);
    }
};
Sahi.prototype.xisFirstExecutableFrame = function () {
    var fs = this.top().frames;
    for (var i = 0; i < fs.length; i++) {
        if (this.self() == this.top().frames[i]) return true;
        if ("" + (typeof this.top().frames[i].location) != "undefined") { // = undefined when previous frames are not accessible due to some reason (may be from diff domain)
            return false;
        }
    }
    return false;
};
Sahi.prototype.getOREntry = function (infoAr) {
	var info = infoAr[0];
    var s = this.escapeDollar(this.getAccessor1(info));
    return this.getOREntry2(s);
}
Sahi.prototype.getOREntry2 = function(s){
	if (s.charAt(0) == '$') return {name: s, value:s};
	var ORName = "$_" + s.toUpperCase().replace(/_/g, '').replace(/[^a-zA-Z0-9]/g, '_').replace(/_+/g, '_').replace(/^_/, '').replace(/_$/, '')
		.replace(/_[^_]*_NEAR_[^_]*_/g, '_').replace(/_[^_]*_IN_[^_]*_/g, '_');
	return {name : ORName, value : s}
}
/*
ESC 27
Enter 13
home 33,34,35,36
arrows 37,38,39,40
*/
Sahi.prototype.isRecordabeKeyDown = function (el, e){
	return false; // records in wrong sequence causing failures. return false for now.
	if (!e || (e.charCode != 0 && e.charCode != null)) return false;
	var  k = e.keyCode;
	return ((k >= 112 && k <= 123)
		|| (k >= 33 && k <= 40)
		|| k == 27
		|| k == 13);
		
}
Sahi.prototype.getScript = function (infoAr, el, evType, e) {
	var info = infoAr[0];
    var accessor = this.escapeDollar(this.getAccessor1(info));
    if (accessor == null) return null;
    var ev = info.event;
    var value = info.value;
    var type = info.type;
    if (evType != null) evType = evType.toLowerCase();
    
    var cmd = null;
    if (value == null)
        value = "";

    // handle F12 and contextmenu
    if (evType == "keydown" || evType == "contextmenu") {
    	if (evType == "keydown") {
    		//this._alert(this.isRecordabeKeyDown(el, e));
	    	if (this.isRecordabeKeyDown(el, e)){
	    		cmd = "_keyPress(" + accessor + ", [" + e.keyCode + "," + 0 + "]);";
	    		//this._alert(cmd);
	    	}
    	} else {
    		cmd = "_rightClick(" + accessor + ");";
    	}
	    if (!cmd) return null;
    } else {
	    if (ev == "_click") {
	    	cmd = "_click(" + accessor + ");";
	    } else if (ev == "_setValue") {
	        cmd = "_setValue(" + accessor + ", " + this.quotedEscapeValue(value) + ");";
	    } else if (ev == "_setSelected") {
	        cmd = "_setSelected(" + accessor + ", " + this.toJSON(value) + ");";
	    } else if (ev == "wait") {
	        cmd = "_wait(" + value + ");";
	    } else if (ev == "mark") {
	        cmd = "//MARK: " + value;
	    } else if (ev == "_setFile") {
	    	if (!this.isBlankOrNull(value)) {
	    		cmd = "_setFile2(" + accessor + ", " + this.quotedEscapeValue(value) + ");";
	    	} else {
	    		return null;
	    	}
	    }
    }
    return this.addPopupDomainPrefixes(cmd);
};
Sahi.prototype.addPopupDomainPrefixes = function(cmd){
	return this.getPopupDomainPrefixes() + cmd;
}
Sahi.prototype.getPopupDomainPrefixes = function(){
    var popup = this.getPopupName();
    var domain = this.getDomainContext();	
    var prefix = "";
    if (prefix != null && domain != null && domain != "") {
    	prefix = this.language.DOMAIN.replace(/<domain>/g, this.quoted(domain)); //"_domain(\"" + domain + "\").";
    }	
    if (prefix != null && popup != null && popup != "") {
    	prefix += this.language.POPUP.replace(/<window_name>/g, this.quoted(popup));
    }
    return prefix;
}
Sahi.prototype.quotedEscapeValue = function (s) {
    return this.quoted(this.escapeValue(s));
};

Sahi.prototype.escapeValue = function (s) {
    if (s == null || typeof s != "string") return s;
    return this.convertUnicode(s.replace(/\r/g, "").replace(/\\/g, "\\\\").replace(/\n/g, "\\n"));
};

Sahi.prototype.escape = function (s) {
    if (s == null) return s;
    return this.encode(s);
};

Sahi.prototype.saveCondition = function (key, a) {
    this.setServerVar(key, a ? "true" : "false");
    //this.resetCmds();
};
Sahi.prototype.resetCmds = function(){
    this.cmds = new Array();
    this.cmdDebugInfo = new Array();
    this.scriptScope();
};
Sahi.prototype.handleSet = function(varName, value){
    this.setServerVar(varName, value);
    //this.resetCmds();
};
Sahi.prototype.quoteIfString = function (shortHand) {
//    if (("" + shortHand).match(/^[0-9]+$/)) return shortHand;
    if (typeof shortHand == "number") return shortHand;
    return this.quotedEscapeValue(shortHand);
};


Sahi.prototype._execute = function (command, sync) {
    var is_sync = sync ? "true" : "false";
    var status = this._callServer("CommandInvoker_execute", "command=" + this.encode(command) + "&sync=" + is_sync);
    if ("success" != status) {
        throw new Error("Execute Command Failed!");
    }
};

_sahi.activateHotKey();

Sahi.prototype._style = function (el, style) {
    var value = el.style[this.toCamelCase(style)];

    if (!value){
        if (el.ownerDocument && el.ownerDocument.defaultView) // FF
            value = el.ownerDocument.defaultView.getComputedStyle(el, "").getPropertyValue(style);
        else if (el.currentStyle)
            value = el.currentStyle[this.toCamelCase(style)];
    }

    return value;
};

Sahi.prototype.toCamelCase = function (s) {
    var exp = /-([a-z])/
    for (;exp.test(s); s = s.replace(exp, RegExp.$1.toUpperCase()));
    return s;
};
Sahi.init = function(e){
    eval("_sahi.init()");
};
Sahi.onBeforeUnLoad = function(e){
    _sahi.onBeforeUnLoad(e);
};
// ff xhr start
if (!_sahi._isIE()){
	if ((XMLHttpRequest.prototype.__sahiModified__ == null)){ 
	    XMLHttpRequest.prototype._sahi_openOld = XMLHttpRequest.prototype.open;
	    XMLHttpRequest.prototype.__sahiModified__ = true;
	    XMLHttpRequest.prototype.open = function(method, url, async, username, password){
//	        var opened = this._sahi_openOld(method, url, async, username, password);
	        var opened = this._sahi_openOld.apply(this, arguments);
	        url = ""+url;
	        if (url.indexOf("/_s_/") == -1){
	            try{
	                if (!_sahi.isComet(url)){
	                	var xs = _sahi.topSahi().XHRs;
	                	var j = xs.length;
	                    xs[j] = this;
	                    _sahi.topSahi().XHRTimes[j] = new Date().getTime();
	                }
	            }catch(e){
	                _sahi._debug("concat.js: Diff domain: Could not add XHR to list for automatic monitoring "+e);
	            }
	            // Add sahi-isxhr if same domain
	            // Repurcussions: https://developer.mozilla.org/en/http_access_control
            	if (url.indexOf("://") == -1 || url.indexOf(location.host) != -1) {
            		this.setRequestHeader("sahi-isxhr", "true");
            	}
	        }
	        return opened;
	    }
	    new_ActiveXObject = function(s){ // Some custom implementation of ActiveXObject
	        return new ActiveXObject(s);
	    }
	}
}else{
    new_ActiveXObject = function(s,s1,s2,s3,s4,s5,s6){ // ,s1,s2,s3,s4,s5,s6 Fix for ACIWorldWide
        var lower = s.toLowerCase();
        if (lower.indexOf("microsoft.xmlhttp")!=-1 || lower.indexOf("msxml2.xmlhttp")!=-1){
            return new SahiXHRWrapper(s, true);
        }else{
            return new ActiveXObject(s,s1,s2,s3,s4,s5,s6);
        }
    }
}
// ff xhr end
SahiXHRWrapper = function (s, isActiveX){
	try{
		this.xhr = isActiveX ? new ActiveXObject(s) : new real_XMLHttpRequest();
	}catch(e){
		if (_sahi._isIE() && window.ActiveXObject){
			this.xhr = new ActiveXObject("Microsoft.XMLHTTP");
		}
	}
    var xs = _sahi.topSahi().XHRs;
    xs[xs.length] = this;
    this._async = false;
};
SahiXHRWrapper.prototype.open = function(method, url, async, username, password){
    url = ""+url;
    this._async = async;
    var opened = this.xhr.open(method, url, async, username, password);
    if (url.indexOf("/_s_/") == -1){
        try{
            var xs = _sahi.topSahi().XHRs;
            xs[xs.length] = this;
        }catch(e){}
        // Add sahi-isxhr if same domain
        // Repurcussions: https://developer.mozilla.org/en/http_access_control
        // Does not affect IE really, but not taking chances.
    	if (url.indexOf("://") == -1 || url.indexOf(location.host) != -1) {
    		this.xhr.setRequestHeader("sahi-isxhr", "true");
    	}
    }
    var fn = this.stateChange;
    var obj = this;
    this.xhr.onreadystatechange = function(){fn.apply(obj, arguments);}
    return opened;
};
SahiXHRWrapper.prototype.getAllResponseHeaders = function(){
    return this.xhr.getAllResponseHeaders();
};
SahiXHRWrapper.prototype.getResponseHeader = function(s){
    return this.xhr.getResponseHeader(s);
};
SahiXHRWrapper.prototype.setRequestHeader = function(k, v){
    return this.xhr.setRequestHeader(k, v);
};
SahiXHRWrapper.prototype.send = function(s){
    var sent = this.xhr.send(s);
    if (!this._async) this.populateProps();
    return sent;
};
SahiXHRWrapper.prototype.stateChange = function(){
    this.readyState = this.xhr.readyState;
    if (this.readyState==4){
        this.populateProps();
    }
    if (this.onreadystatechange) this.onreadystatechange();
};
SahiXHRWrapper.prototype.abort = function(){
	return this.xhr.abort();
}
SahiXHRWrapper.prototype.populateProps = function(){
    this.responseText = this.xhr.responseText;
    this.responseXML = this.xhr.responseXML;
    this.responseXml = this.xhr.responseXML;
    this.status = this.xhr.status;
    this.statusText = this.xhr.statusText;
};
if (_sahi._isIE() && typeof XMLHttpRequest != "undefined"){
    window.real_XMLHttpRequest = XMLHttpRequest;
    XMLHttpRequest = SahiXHRWrapper;
}
SahiHashMap = function(){
	this.keys = new Array();
	this.values = new Array();
	this.put = function(k, v){
		var i = this.getIndex(this.keys, k);
		if (i == -1) i = this.keys.length;
		this.keys[i] = k;
		this.values[i] = v;
	}
	this.get = function(k){
		var i = this.getIndex(this.keys, k);
		return this.values[i];
	}
	this.getIndex = function(ar, k){
		for (var i=0; i<ar.length; i++){
			if (k === ar[i]) return i;
		}		
		return -1;
	}
}
Sahi.prototype.toJSON = function(el, map){
	try {
		if (!map) map = new SahiHashMap();
		var j = map.get(el);
		if (j && j == "___in_progress___") {
			return '"recursive_access"'; 
		}
		map.put(el, '___in_progress___');
		var v = this.toJSON2(el, map);
		map.put(el, v);
		return v;
	} catch (e) {
		return "error during toJSON conversion";
	}
}
Sahi.prototype.toJSON2 = function(el, map){
    if (el == null || el == undefined) return 'null';
	if (el instanceof RegExp) return el.toString();
    if (el instanceof Date){
        return String(el);
    }else if (typeof el == 'string'){
        if (/["\\\x00-\x1f]/.test(el)) {
            return '"' + el.replace(/([\x00-\x1f\\"])/g, function (a, b) {
                var c = _sahi.escapeMap[b];
                if (c) {
                    return c;
                }
                c = b.charCodeAt();
                return '\\u00' +
                    Math.floor(c / 16).toString(16) +
                    (c % 16).toString(16);
            }) + '"';
        }
        return '"' + el + '"';
    }else if (el instanceof Array){
        var ar = [];
        for (var i=0; i<el.length; i++){
            ar[i] = this.toJSON(el[i], map);
        }
        return '[' + ar.join(',') + ']';
    }else if (typeof el == 'number'){
        return new String(el);
    }else if (typeof el == 'boolean'){
        return String(el);
    }else if (el instanceof Object){
    	if (el.tagName) {
    		var elInfo = this.identify(el, true);
    	    if (elInfo == null || elInfo.apis == null) return null;
    	    return (elInfo.apis.length > 0) ? "_sahi." + this.escapeDollar(this.getAccessor1(elInfo.apis[0])) : null;
    	} else {
            var ar = [];
            for (var k in el){
                var v = el[k];
                if (typeof v != 'function'){
                    ar[ar.length] = this.toJSON(k, map) + ':' + this.toJSON(v, map);
                }
            }
            return '{' + ar.join(',') + '}';
    	}
    }
};
Sahi.prototype.isComet = function(u){
	return /\/comet[\/.]/.test(u);
}
Sahi.prototype.isIgnorableId = function(id){
	return this.ignorableIdsPattern.test(id);
};
Sahi.prototype.iframeFromStr = function(iframe){
    if (typeof iframe == "string") return this._byId(iframe);
    return iframe;
};
Sahi.prototype._rteWrite = function(iframe, s){
    this.iframeFromStr(iframe).contentWindow.document.body.innerHTML = s;
};
Sahi.prototype._rteHTML = function(iframe){
    return this.iframeFromStr(iframe).contentWindow.document.body.innerHTML;
};
Sahi.prototype._rteText = function(iframe){
    return this._getText(this.iframeFromStr(iframe).contentWindow.document.body);
};
Sahi.prototype._re = function(s){
    return eval("/"+s.replace(/\s+/g, '\\s+')+"/");
};
//Sahi.prototype._scriptName = function(){
//	this._evalOnRhino("_scriptName()");
////    return this.__scriptName;
//};
//Sahi.prototype._scriptPath = function(){
//	return this.__scriptPath;
//};
Sahi.prototype._parentNode = function (el, tagName, occurrence){
	if (tagName == null && occurrence == null){
		tagName = "ANY";
	} else if (typeof(tagName) == "number") {
		occurrence = tagName;
		tagName = "ANY";
	}
	return this.getParentNode(el, tagName, occurrence);
};
Sahi.prototype._parentCell = function(el, occurrence){
    return this._parentNode(el, "TD", occurrence);
};
Sahi.prototype._parentRow = function(el, occurrence){
    return this._parentNode(el, "TR", occurrence);
};
Sahi.prototype._parentTable = function(el, occurrence){
    return this._parentNode(el, "TABLE", occurrence);
};
Sahi.prototype.getDoc = function(constraints){
	if (constraints.relations.length == 0) return constraints.window.document;
	return this.getDoc2(constraints.relations);
}
Sahi.prototype.getDoc2 = function(relations){
	if (this.isArray(relations)){
		var nodes = [];
		for (var i=0; i<relations.length; i++){
			var relation = relations[i];
			if (!relation.type && this.isWindow(relation)) {
				nodes.push(relation.document); // window
			}
			if (relation.relation && relation.relation == "_near"){
				nodes = nodes.concat(this.getDoc2(relation).nodes);
			}
		}
		var inEl = null;
		for (var i=0; i<relations.length; i++){
			var relation = relations[i];
			if (relation.relation && relation.relation == "_in"){
				var relEl = relation.element;
				if (inEl != null && !this._contains(inEl, relEl) && !this._contains(relEl, inEl)){
					// mutually exclusive nodes
					return new SahiDocProxy([]);
				}
				if(inEl == null || this._contains(inEl, relEl)){
					// inner element
					inEl = relEl;
				}
			}			
		}
		if (inEl == null) return new SahiDocProxy(nodes); 
		if (nodes.length == 0) return inEl;
		var narrowed = [];
		for (var i=0; i<nodes.length; i++){
			if (this._contains(inEl, nodes[i])){
				narrowed.push(nodes[i]);
			}
			if (this._contains(nodes[i], inEl)){
				narrowed.push(inEl);
			}			
		}		
		return new SahiDocProxy(narrowed); 
	}
    if (relations.relation){
    	if (relations.relation == "_in") return relations.element;
	    if (relations.relation == "_near"){
	    	var parents = [];
	    	for (var i=1; i<7; i++){
	    		parents[parents.length] = this.getParentNode(relations.element, "ANY", i);
	    	}
	    	return new SahiDocProxy(parents);
	    }
    }
    return relations.document;
};
SahiDocProxy = function(nodes){
	this.nodes = nodes;
};
SahiDocProxy.prototype.getElementsByTagName = function(tag){
	var tags = [];
	for (var i=0; i<this.nodes.length; i++){
		if (this.nodes[i] == null) continue;
		var childNodes = _sahi.getElementsByTagName(tag, this.nodes[i]);
		for (var j=0; j<childNodes.length; j++){
			var childNode = childNodes[j];
			var alreadyAdded = false;
			for (var k=0; k<tags.length; k++){
				if (tags[k] === childNode){
					alreadyAdded = true;
					break;
				}
			}
			if (!alreadyAdded){
				tags[tags.length] = childNode;
			}
		}		
	}
	return tags;
};
Sahi.prototype._in = function(el){
	this.checkNull(el, "_in");
	return {"element":el, "relation":"_in", "type": "dom"};
};
Sahi.prototype._near = function(el){
	this.checkNull(el, "_near");
	return {"element":el, "relation":"_near", "type": "dom"};
};

Sahi.prototype.getLeftTop = function(el){
	var pos = el.getBoundingClientRect();
	return [pos.left,pos.top];
};

Sahi.prototype._under = function(el, offset, limitUnder){
	this.checkNull(el, "_under");

	var xy = this.getLeftTop(el);
	var alignX = xy[0];
	var alignXOuter = alignX + el.offsetWidth;
	var h = el.offsetHeight;
	var limitY = xy[1] + (isNaN(h) ? 0 : h/2);
	var underOffset = offset == null ? 0 : offset;
	if(this.is_defined(limitUnder)) limitUnder+=limitY;	
	return {"element":el, "relation":"_under", "type": "position", 
		alignX: (alignX - underOffset),
		alignXOuter: (alignXOuter + underOffset),
		limitY: limitY,
		limitUnder: limitUnder};
};

Sahi.prototype._above = function(el, offset, limitTop){
	this.checkNull(el, "_above");

	var xy = this.getLeftTop(el);
	var alignX = xy[0];
	var alignXOuter = alignX + el.offsetWidth;
	var h = el.offsetHeight;
	var limitY = xy[1] - (isNaN(h) ? 0 : h/2);
	var aboveOffset = offset == null ? 0 : offset;
	if(this.is_defined(limitTop)) limitTop = limitY - limitTop;
	return {"element":el, "relation":"_above", "type": "position", 
		alignX: (alignX - aboveOffset),
		alignXOuter: (alignXOuter + aboveOffset),
		limitY: limitY,
		limitTop : limitTop};
	  
};

Sahi.prototype._leftOf = function(el, offset){
	this.checkNull(el, "_leftOf");

	var xy = this.getLeftTop(el);
	var alignY = xy[1];
	var alignYOuter = alignY + el.offsetHeight;
	var w = el.offsetWidth;
	var limitX = xy[0];
	var leftOffset = offset == null ? 0 : offset;

	return {"element":el, "relation":"_leftOf", "type": "position", 
		alignY: (alignY - leftOffset),
		alignYOuter: (alignYOuter + leftOffset),
		limitX: limitX};
};

Sahi.prototype._rightOf = function(el, offset){
	this.checkNull(el, "_rightOf");

	var xy = this.getLeftTop(el);
	var alignY = xy[1];
	var alignYOuter = alignY + el.offsetHeight;
	var w = el.offsetWidth;
	var limitX = xy[0] + (isNaN(w) ? 0 : w/2);
	var rightOffset = offset == null ? 0 : offset;

	return {"element":el, "relation":"_rightOf", "type": "position", 
		alignY: (alignY - rightOffset),
		alignYOuter: (alignYOuter + rightOffset),
		limitX: limitX};
};

Sahi.prototype._leftOrRightOf = function(el, offset){
	this.checkNull(el, "_leftOrRightOf");

	var alignY = this.getLeftTop(el)[1];
	var alignYOuter = alignY + el.offsetHeight;
	var rightOffset = offset == null ? 0 : offset;

	return {"element":el, "relation":"_rowOf", "type": "position", 
		alignY: (alignY - rightOffset),
		alignYOuter: (alignYOuter + rightOffset)};
};
Sahi.prototype._aboveOrUnder = function(el, offset){
	this.checkNull(el, "_aboveOrUnder");

	var alignX = this.getLeftTop(el)[0];
	var alignXOuter = alignX + el.offsetWidth;
	var aboveOffset = offset == null ? 0 : offset;

	return {"element":el, "relation":"_colOf", "type": "position", 
		alignX: (alignX - aboveOffset),
		alignXOuter: (alignXOuter + aboveOffset)};
};

Sahi.prototype._xy = function(el, x, y){
	this.checkNull(el, "_xy");
	this.xyoffsets.put(el, [x,y]);
	return el;
}

Sahi.prototype.addSahi = function(s) {
    return this.decode(this.sendToServer("/_s_/dyn/ControllerUI_getSahiScript?code=" + this.encode(s)));
};
_sahi.prevOnError = window.onerror;
window.onerror = _sahi.wrap(_sahi.onError);
Sahi.prototype.setServerVarPlain = function (name, value, isGlobal) {
//	if (name == "___lastValue___") this._debug(name+"="+value);
    this.sendToServer("/_s_/dyn/SessionState_setVar?name=" + this.encode(name) + "&value=" + this.encode(value) + "&isglobal="+(isGlobal?1:0));
};
/* execCommand start */
if (_sahi._isIE()){
	_sahi.real_execCommand = document.execCommand;
	document.execCommand = function(){return _sahi_dummyExecCommand.apply(document, arguments);};
}
Sahi.prototype.encode = function(str) {  
	  return encodeURIComponent(str).replace(/%20/g, '+').replace(/!/g, '%21').replace(/'/g, '%27').replace(/\(/g, '%28').  
	                                 replace(/\)/g, '%29').replace(/\*/g, '%2A');
} 
Sahi.prototype.decode = function(msg){
	if (!msg) return msg;
	return decodeURIComponent(msg.replace(/[+]/g, ' '));	
}
function _sahi_dummyExecCommand(){
	if (arguments[0] == 'ClearAuthenticationCache'){
		_sahi.sendToServer("/_s_/dyn/SessionState_removeAllCredentials");
		return true;
	}else{
		return _sahi.real_execCommand.apply(window.document, arguments);
	}
}
/* execCommand end */

/** id start **/
Sahi.prototype.getOptionId = function (sel, val) {
	if (sel.selectedIndex != -1)
		return sel.options[sel.selectedIndex].id;
};
Sahi.prototype.addADAr = function(a){
	this.ADs[this.ADs.length] = a;
};
Sahi.prototype.getAD = function(el){
	var defs = [];
	for (var i=0; i<this.ADs.length; i++){
		var d = this.ADs[i];  
		if (this.areTagNamesEqual(d.tag, el.tagName)){
			if (!el.type) defs[defs.length] = d;
			else if (!d.type || this.getElementType(el) == d.type) defs[defs.length] = d; 
		}
	}
	return defs;
};
Sahi.prototype.getDomRelAr = function(args){
	var rels = [];
	var poss = [];
	var w = null;
	for (var i=1; i<args.length; i++){
		var a = args[i];
		if (a) {
			if (this.isWindow(a)) w = a;
			if (a.type == "dom") rels.push(a);
			if (a.type == "position") poss.push(a);
		}
	}
	if (!w) w = this.top();
	return {relations:rels, positionals:poss, window:w};
}
Sahi.prototype.isWindow = function(o){
	return (o && o.location && o.document) != null;
}
Sahi.prototype.addAD = function(a){
	this.addADAr(a);
	var old = Sahi.prototype[a.name];
	var newFn = function(identifier){
		var inEl = this.getDomRelAr(arguments);
		if (old) {
			var el = old.apply(this, arguments);
			if (el) return el;
		}
		var attributes = a.attributes;
		if (typeof identifier == "object" && !(identifier instanceof RegExp)){
			attributes = ["associative_array"];
		}
		for (var i=0; i<a.attributes.length; i++){
			var res = this.getBlankResult();
			if (a.type){
				var el = this.findElementHelper(identifier, inEl, a.type, res, attributes[i], a.tag).element;
			} else {
				var el = this.findTagHelper(identifier, inEl, a.tag, res, attributes[i]).element;
			}
			if (el != null) return el;
		}
	};
	if (!a.idOnly) Sahi.prototype[a.name] = newFn;
};
Sahi.prototype.identify = function(el, ignoreEncaps){
//	this.alignY = this.alignX = null;
	if (el == null) return null;
	var apis = [];
	var assertions = [];
	var tagLC = el.tagName.toLowerCase();
	var accs = this.getAD(el);
	var relation = null;
	var relationStr = null;
	var anchor = this.topSahi().anchor;
	if (anchor){
		if (anchor == el){
			// do nothing
		} else if (this._contains(anchor, el)) {
			relation = this._in(anchor);
			relationStr = "_in(" + this.topSahi().anchorStr + ")";
		} else if (this._contains(this.getParentNode(anchor, "ANY", 7, true), el)){
			relation = this._near(anchor);
			relationStr = "_near(" + this.topSahi().anchorStr + ")";
		}
	} 
	for (var k=0; k<accs.length; k++){
		var acc = accs[k];
		if (acc && acc.attributes){
			var r = acc.attributes;
			for (var i=0; i<r.length; i++){
				try{
					var attr = r[i];
					if (attr == "index"){
						var ix = this.getIdentifyIx(null, el, null, relation);
						if (ix != -1 && this[acc.name](ix) == el){
							apis[apis.length] = this.buildAccessorInfo(el, acc, ix, relationStr);
						}				
					} else if (typeof attr == "string" && !ignoreEncaps && attr.indexOf("encaps") == 0) {
						var parentTag = attr.substring(attr.indexOf("_") + 1);
						var p = this._parentNode(el, parentTag);
						var pAccs = this.identify(p);
						// TODO: check this assertions get added to both child and parent. may come out incorrect.
						if (pAccs){
							apis = apis.concat(pAccs.apis);
							assertions = assertions.concat(pAccs.assertions);
						}
					} else {
						var val = this.getAttribute(el, attr);
						if (val && !this.isIgnorableId(val) && !(attr == "sahiText" && val.length > 200)){
							if (this[acc.name](val, relation) == el){
								apis[apis.length] = this.buildAccessorInfo(el, acc, val, relationStr);
							} else {
								var ix = this.getIdentifyIx(val, el, attr, relation);
								val = val + "[" + ix + "]";
								if (ix != -1 && this[acc.name](val) == el){
									apis[apis.length] = this.buildAccessorInfo(el, acc, val, relationStr);
								}
							}
						}
					}
				}catch(e){
					//alert(e +" " + attr + " " + el.tagName);
				}
			}
		}
	}
	
	if (apis.length > 0) {
		assertions = assertions.concat(this.getAssertions(accs, apis[0]));
	}
	
	return {apis: apis, assertions: assertions};
};
Sahi.prototype.buildAccessorInfo = function(el, acc, identifier, relationStr){
	return new AccessorInfo("", identifier, acc.name, acc.action, (acc.value ? this.getAttribute(el, acc.value):null), acc.value, relationStr);
};
Sahi.prototype.getIdentifyIx = function(val, el, attr, inEl){
	var inEl = this.getDomRelAr(arguments);
	var tagLC = el.tagName.toLowerCase();
	var res = this.getBlankResult();
	if (this.isFormElement(el)){
		return this.findElementIxHelper(val, el.type, el, inEl, res, attr, tagLC).cnt;
	} else {
		return this.findTagIxHelper(val, el, inEl, tagLC, res, attr).cnt;
	}	
};
Sahi.prototype.isFormElement = function(el){
	var n = el.tagName.toLowerCase();
	return n == "input" || n == "button" || n == "textarea" || n == "select" || n == "option";
}
Sahi.prototype.getAttribute = function (el, attr){
	if (typeof attr == "function"){
		return attr(el);
	}
	if (attr.indexOf("|") != -1){
	    var attrs = attr.split("|");
	    for (var i=0; i<attrs.length; i++){
	    	var v = this.getAttribute(el, attrs[i]);
	        if (v != null && v != "") return v;
	    }
	}else{
        if (attr == "sahiText") {
            return this._getText(el);
        }
        return el[attr];
	}
};
Sahi.prototype.makeLibFunctionsAvailable = function(){
	var fns = ["_scriptName", "_scriptPath", "_suiteInfo", "_userDataDir", 
	           "_sessionInfo", "_userDataPath", "_readFile", "_readCSVFile", 
	           "_readURL", "_scriptStatus", "_stackTrace", "_selectWindow", 
	           "_resolvePath"];
	for (var i=0; i<fns.length; i++){		
		this.addRhinoFn(fns[i]);
	}
}
Sahi.prototype.addRhinoFn = function(fnName){
	this[fnName] = function(){
		var s = "";
		for (var i=0; i<arguments.length; i++){
			if (i != 0) s += ", ";
			s += this.toJSON(arguments[i]); 
		}
		return this._evalOnRhino(fnName + "(" + s + ")");
	}	
}
Sahi.prototype._evalOnRhino = function (s){
	try{
		var v = this.sendToServer("/_s_/dyn/RhinoRuntime_eval?toEval=" + this.encode(s));
		return eval("(" + this.decode(v) + ")");
	}catch(e){return null;}
}
Sahi.prototype.getFileFromURL = function(el){
	var src = el.src; 
	src = src.replace(/[;?].*$/, '');
	return src.substring(src.lastIndexOf("/")+1);
}
//Sahi.prototype.getXPathCrumb = function(el){
//	var locators = {
//			A:["sahiText", "link="],
//			ANY: ["id", ]
//			}
//	if (el.id) return el.tagName "[@id=" + 
//}
Sahi.prototype.getXPath = function(el){
	var n = el;
	var s = "";
	while (true){
		var p = n.parentNode;
		if (p == n || p == null) break;
		var ix = this.findInArray(this.getElementsByTagName(n.tagName, p), n);
		var sfx = ix > 0 ? "["+(ix+1)+"]" : "";
		s = n.tagName.toLowerCase() + sfx + (s == "" ? "" : ("/" + s));
		n = p;
	}
	return "/" + s;
}
Sahi.prototype.prepareADs = function(){
//	this.addAD({tag: "SPAN", type: null, event:"click", name: "_spanWithImage", 
//		attributes: [function(el){ if (el.parentNode.tagName == "TD"){return _sahi._getText(el);}}], action: "_click", value: "sahiText"});

	this.addAD({tag: "A", type: null, event:"click", name: "_link", attributes: ["sahiText", "title|alt", "id", "index", "href", "className"], action: "_click", value: "sahiText"});
	this.addAD({tag: "IMG", type: null, event:"click", name: "_image", attributes: ["title|alt", "id", 
	                  this.getFileFromURL, "index", "className"], action: "_click"});
	this.addAD({tag: "LABEL", type: null, event:"click", name: "_label", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "LI", type: null, event:"click", name: "_listItem", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "UL", type: null, event:"click", name: "_list", attributes: ["id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "OL", type: null, event:"click", name: "_list", attributes: ["id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "DIV", type: null, event:"click", name: "_div", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "SPAN", type: null, event:"click", name: "_span", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "TABLE", type: null, event:"click", name: "_table", attributes: ["id", "className", "index"], action: null, value: "sahiText"});
	this.addAD({tag: "TR", type: null, event:"click", name: "_row", attributes: ["id", "className", "sahiText", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "TD", type: null, event:"click", name: "_cell", attributes: ["sahiText", "id", "className", "index", "encaps_TR", "encaps_TABLE"], action: "_click", idOnly: true, value: "sahiText"});
	this.addAD({tag: "TH", type: null, event:"click", name: "_tableHeader", attributes: ["sahiText", "id", "className", "encaps_TABLE"], action: "_click", value: "sahiText"});

	this.addAD({tag: "INPUT", type: "button", event:"click", name: "_button", attributes: ["value", "name", "id", "index", "className"], action: "_click", value: "value"});
	this.addAD({tag: "BUTTON", type: "button", event:"click", name: "_button", attributes: ["sahiText", "name", "id", "className", "index"], action: "_click", value: "sahiText"});
	
	this.addAD({tag: "INPUT", type: "checkbox", event:"click", name: "_checkbox", attributes: ["name", "id", "value", "className", "index"], action: "_click", value: "checked",
			assertions: function(value){return [("true" == ("" + value)) ? _sahi.language.ASSERT_CHECKED : _sahi.language.ASSERT_NOT_CHECKED];}});
	this.addAD({tag: "INPUT", type: "password", event:"change", name: "_password", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	this.addAD({tag: "INPUT", type: "radio", event:"click", name: "_radio", attributes: ["id", "name", "value", "className", "index"], action: "_click", value: "checked", 
			assertions: function(value){return [("true" == ("" + value)) ? _sahi.language.ASSERT_CHECKED : _sahi.language.ASSERT_NOT_CHECKED];}});	
	
	this.addAD({tag: "INPUT", type: "submit", event:"click", name: "_submit", attributes: ["value", "name", "id", "className", "index"], action: "_click", value: "value"});	
	this.addAD({tag: "BUTTON", type: "submit", event:"click", name: "_submit", attributes: ["sahiText", "name", "id", "className", "index"], action: "_click", value: "sahiText"});	

	this.addAD({tag: "INPUT", type: "text", event:"change", name: "_textbox", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	
	this.addAD({tag: "INPUT", type: "reset", event:"click", name: "_reset", attributes: ["value", "name", "id", "className", "index"], action: "_click", value: "value"});	
	this.addAD({tag: "BUTTON", type: "reset", event:"click", name: "_reset", attributes: ["sahiText", "name", "id", "className", "index"], action: "_click", value: "sahiText"});	

	this.addAD({tag: "INPUT", type: "hidden", event:"", name: "_hidden", attributes: ["name", "id", "className", "index"], action: "_setValue", value: "value"});	
	
	this.addAD({tag: "INPUT", type: "file", event:"click", name: "_file", attributes: ["name", "id", "index", "className"], action: "_setFile", value: "value"});	
	this.addAD({tag: "INPUT", type: "image", event:"click", name: "_imageSubmitButton", attributes: ["title|alt", "name", "id", 
	                  this.getFileFromURL, "index", "className"], action: "_click"});	
	this.addAD({tag: "INPUT", type: "date", event:"change", name: "_datebox", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	this.addAD({tag: "INPUT", type: "datetime", event:"change", name: "_datetimebox", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	this.addAD({tag: "INPUT", type: "datetime-local", event:"change", name: "_datetimelocalbox", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	this.addAD({tag: "INPUT", type: "email", event:"change", name: "_emailbox", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	this.addAD({tag: "INPUT", type: "month", event:"change", name: "_monthbox", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	this.addAD({tag: "INPUT", type: "number", event:"change", name: "_numberbox", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	this.addAD({tag: "INPUT", type: "range", event:"change", name: "_rangebox", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	this.addAD({tag: "INPUT", type: "search", event:"change", name: "_searchbox", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	this.addAD({tag: "INPUT", type: "tel", event:"change", name: "_telephonebox", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	this.addAD({tag: "INPUT", type: "time", event:"change", name: "_timebox", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	this.addAD({tag: "INPUT", type: "url", event:"change", name: "_urlbox", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	this.addAD({tag: "INPUT", type: "week", event:"change", name: "_weekbox", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});

	
	
	this.addAD({tag: "SELECT", type: null, event:"change", name: "_select", attributes: ["name", "id", "index", "className"], action: "_setSelected", value: function(el){return _sahi._getSelectedText(el) || _sahi.getOptionId(el, el.value) || el.value;},
		assertions: function(value){return [_sahi.language.ASSERT_SELECTION];}});	
	this.addAD({tag: "OPTION", type: null, event:"none", name: "_option", attributes: ["encaps_SELECT", "sahiText", "value", "id", "index"], action: "", value: "sahiText"});	
	this.addAD({tag: "TEXTAREA", type: null, event:"change", name: "_textarea", attributes: ["name", "id", "index", "className"], action: "_setValue", value: "value"});
	this.addAD({tag: "H1", type: null, event:"click", name: "_heading1", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "H2", type: null, event:"click", name: "_heading2", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "H3", type: null, event:"click", name: "_heading3", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "H4", type: null, event:"click", name: "_heading4", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "H5", type: null, event:"click", name: "_heading5", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "H6", type: null, event:"click", name: "_heading6", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	
	this.addAD({tag: "AREA", type: null, event:"click", name: "_area", attributes: ["id", "title|alt", "href", "shape", "className", "index"], action: "_click"});
	this.addAD({tag: "MAP", type: null, event:"click", name: "_map", attributes: ["name", "id", "title", "className", "index"], action: "_click"});

	this.addAD({tag: "P", type: null, event:"click", name: "_paragraph", attributes: ["encaps_A", "id", "className", "sahiText", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "I", type: null, event:"click", name: "_italic", attributes: ["encaps_A", "sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "EM", type: null, event:"click", name: "_emphasis", attributes: ["encaps_A", "sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "B", type: null, event:"click", name: "_bold", attributes: ["encaps_A", "sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "STRONG", type: null, event:"click", name: "_strong", attributes: ["encaps_A", "sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "PRE", type: null, event:"click", name: "_preformatted", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "CODE", type: null, event:"click", name: "_code", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "BLOCKQUOTE", type: null, event:"click", name: "_blockquote", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "CANVAS", type: null, event:"click", name: "_canvas", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "ABBR", type: null, event:"click", name: "_abbr", attributes: ["encaps_A", "sahiText", "title", "id", "className", "index"], action: "_click", value: "sahiText"});

	var o_fn1 = function(o){try{return o._sahi_getFlexId();}catch(e){}};
	var o_fn2 = function(o){try{return o._sahi_getUID();}catch(e){}};
	this.addAD({tag: "OBJECT", type: null, event:"click", name: "_object", attributes: ["id", "name", "data", o_fn1, o_fn2], action: "_click", value: ""});
	this.addAD({tag: "EMBED", type: null, event:"click", name: "_embed", attributes: ["id", "name", o_fn1, o_fn2], action: "_click", value: ""});

	this.addAD({tag: "DL", type: null, event:"click", name: "_dList", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "DT", type: null, event:"click", name: "_dTerm", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});
	this.addAD({tag: "DD", type: null, event:"click", name: "_dDesc", attributes: ["sahiText", "id", "className", "index"], action: "_click", value: "sahiText"});

};
Sahi.prototype.getAssertions = function(accs, info){
	var a = [this.language.ASSERT_EXISTS, this.language.ASSERT_VISIBLE];
	for (var k=0; k<accs.length; k++){
		var acc = accs[k];
		if (acc.assertions)
			a = a.concat(acc.assertions(info.value));
	}
	if (info.valueType == "sahiText"){
		a[a.length] = this.language.ASSERT_EQUAL_TEXT; 
		a[a.length] = this.language.ASSERT_CONTAINS_TEXT; 
	} else if (info.valueType == "value"){
		a[a.length] = this.language.ASSERT_EQUAL_VALUE; 
	}
	return a;
};
var sahiLanguage = {
		ASSERT_EXISTS: "<popup>_assertExists(<accessor>);",
		ASSERT_VISIBLE: "<popup>_assert(_isVisible(<accessor>));",
		ASSERT_EQUAL_TEXT: "<popup>_assertEqual(<value>, _getText(<accessor>));",
		ASSERT_CONTAINS_TEXT: "<popup>_assertContainsText(<value>, <accessor>);",
		ASSERT_EQUAL_VALUE: "<popup>_assertEqual(<value>, _getValue(<accessor>));",
		ASSERT_SELECTION: "<popup>_assertEqual(<value>, _getSelectedText(<accessor>));",
		ASSERT_CHECKED: "<popup>_assert(<accessor>.checked);",
		ASSERT_NOT_CHECKED: "<popup>_assertNotTrue(<accessor>.checked);",
		POPUP: "_popup(<window_name>).",
		DOMAIN: "_domain(<domain>)."
};
_sahi.language = sahiLanguage;
/** id end **/
_sahi.init();
/** Selenium start **/
Sahi.prototype._bySeleniumLocator = function(locator, popupTarget, frameTarget){
	if (locator.indexOf("//") == 0) return this._byXPath(locator);
	else if(locator.indexOf("xpath=") == 0) return this._byXPath(locator.replace("xpath=", ""));
	else{
		var selenium = new BrowserBot(this.top());
		if(popupTarget != ("" || "undefined" || "null" || null)) selenium.selectWindow(popupTarget);
		else if(frameTarget != ("" || "undefined" || "null" || null)) selenium.selectFrame(frameTarget);
		return selenium.findElementOrNull(locator);
	}
//	else if (locator.indexOf("document") == 0) return this._accessor(locator);
//	else if (locator.indexOf("link=") == 0) return this._link(locator.replace("link=", ""));
//	else return this._byId(locator) || this.byName(locator, "*");
}

Sahi.prototype._doSeleniumSelect = function(selectLocator, optionLocator, append){
	var optionItem = new OptionLocatorFactory();
	var selectItem = this._bySeleniumLocator(selectLocator);
	this._setSelected(selectItem, optionItem.fromLocatorString(optionLocator).findOption(selectItem).index, append);
}

/** Selenium end **/
Sahi.prototype.loadXPathScript = function(){
	if (this._isFF() || this._isHTMLUnit()) return false;
	if (!(document.implementation && document.implementation.hasFeature && document.implementation.hasFeature("XPath", null))){
		this.loadScript('/_s_/spr/ext/javascript-xpath/javascript-xpath.js', "_sahi_concat");
	}
}
Sahi.prototype.loadScript = function(src, id){
	var newcontent = document.createElement('script');
	newcontent.src = src;
	var bef = document.getElementById(id);
	bef.parentNode.insertBefore(newcontent, bef);	
}
//document.body.onclick = function(){alert(window.event.clientX + " " + window.event.clientY)}
Sahi.prototype.setLastHTMLSnapShotFile = function(filePath){
	try{
		var $htmlUnitViewer = new Packages.com.pushtotest.sahi.SahiHTMLUnit();
		if($htmlUnitViewer){
			if(filePath){
				var $file = new Packages.java.io.File(filePath);
				filePath = $htmlUnitViewer.takeSnapShot($file);
			}
		}
	} catch(e){}
}

__sahiDebug__("concat.js: end");

/** Pro start **/
Sahi.prototype.setAnchor = function(s){
	this.anchorStr = s;
	try{
		this.anchor = eval(this.addSahi(s));
	}catch(e){}
};
Sahi.prototype.removeAnchor = function(){
	this.anchorStr = null;
	this.anchor = null;
}
/** Flex start **/
Sahi.prototype.isFlexObj = function(el){
	try {return el.isSFL;}
	catch(e){return false;}
}
function isSahiAvailable() {
	return true;
}
function SflWrapper(o){
	this.isSFL = true;
	this.object = o;
	this.id = o._sahi_getFlexId();
	this.accessorAPINames = new Array();
	this.addAccessorFns();
	
}
__fl_debugStr = "";
SflWrapper.prototype.debug = function(s){
	_sahi._debug(s);
	return;
	__fl_debugStr += "\n" + s;
	_sahi.showStepsInController(__fl_debugStr);
}
SflWrapper.prototype.display = function(s){
	var val = "";
	if (typeof _sahi == "object") {
		var ar = eval("("+s+")");
		var alts = [];
		for (var i=0; i<ar.length; i++) {
			var item = ar[i];
			alts.push(SflWrapper.convertToFn(item));
		}
		_sahi.sendIdentifierInfo(alts, 
				alts[0], 
				ar[0].value, 
				"", 
				[]);
	}
}
SflWrapper.prototype.record = function(elJSON, action, value){
	if (action == "click") value = null;
	if (typeof _sahi == "object") {
		var s = SflWrapper.convertToFn(elJSON);
		if (_sahi.isRecording()){
			_sahi.recordStep(this.getStep(action, s, value));
		}
	}
}
SflWrapper.prototype.getStep = function(action, accessorS, value){
	return "_" + action + "(" + accessorS + (value ? (", " + value) : "") + ");";
}
SflWrapper.prototype.exists = function(){
	return this.fetch("constructor") != "Error:null_object";
}
SflWrapper.prototype.isVisible = function(){
	if (!this.exists()) return false;
	try {
		return this.get("visible") == "true";
	}catch(e){return false;}
}
SflWrapper.prototype.xgetFlexApp = function(id){
	if (navigator.appName.indexOf ("Microsoft") !=-1) return window[id];
	return document[id];
}
SflWrapper.prototype.addAccessorFns = function(){
	var actionMethodNames = ["click", "mouseDown", "mouseOver", "mouseUp", "dragDrop", "dragDropXY", "setAsDroppable",
	                         "setValue", "choose", "listProperties", 
	                         "introspect", "fetch", "executeFn", 
	                         "set", "get", "highlight", "getValue", 
	                         "getText", "getRowNo", "getColumnNo", "getGridData", 
	                         "getTextOrToolTip", "getDataProviderData", 
	                         "rightClick", "doubleClick"];	
	for (var i=0; i<actionMethodNames.length; i++){
		var methodName = actionMethodNames[i];
		this[methodName] = this.getFlexFn(methodName, true);
	}
}
SflWrapper.prototype.getFlexFn = function(methodName, isAction){
	return function(){
		var ar = new Array();
		if (this.command) ar[0] = this.command;
		for (var i=0; i<arguments.length; i++){
			ar[ar.length] = arguments[i];
		}
		var command = {api:methodName, args:ar};
		//_sahi._alert(_sahi.toJSON(command));
		if (isAction){
			var s = this.object._sahi_eval(command);
			if (s && (typeof s == "string") && s.indexOf("SAHI_FLEX_ERROR") == 0) throw new Error(s);
			return s;
		}else {
			this.command = command;
			return this;
		}
	}
}
/*
SflWrapper.prototype.getAPIObj = function(apiName, args){
	if (_sahi.isArray(args)) args = [args];
	return {api:apiName, args:args};
}
*/
SflWrapper.convertToFn = function(s){
	var o = (typeof s == "string") ? eval("(" + s + ")") : s;
	return '_flex("' + o.objectId + '").' + o.api + '(' + _sahi.toJSON(o.args[0]) + ')';
}
SflWrapper.prototype.addMetaData = function(metadata){
	if (!metadata.apiName) metadata.apiName = this.getAccessorAPIName(metadata.qn);
	SflWrapper.prototype[metadata.apiName] = this.getFlexFn(metadata.apiName);
	this.object._sahi_addMetaData(metadata);
}
SflWrapper.prototype.find = function(type, identifier){
	return this[type](identifier);
}
SflWrapper.prototype.getAccessorAPIName = function(qn){
	var ix = qn.indexOf("::");
	if (ix != -1) {
		var a = qn.substring(ix+2).toLowerCase();
		return (qn.indexOf("spark") == 0) ? ("s_"+a) : a; 
	}
	return qn;
}

SflWrapper.prototype.addAllRecorderListeners = function(){
	this.object._sahi_addAllRecorderListeners();
}
SflWrapper.prototype.near = function(nearObj){
	this.command.args[0] = {id:this.command.args[0], near:nearObj.command};
	return this;
}
SflWrapper.prototype.inside = function(inObj){
	this.command.args[0] = {id:this.command.args[0], inside:inObj.command};
	return this;
}
SflWrapper.prototype.getData = function(){
	return eval("(" + this.getDataProviderData().replace(/[\r\n]/g, "") + ")");
}
SflWrapper.prototype.currentCursorID = function(){
	return this.object._sahi_currentCursorID();
}
SflWrapper.prototype.addAllMetaData = function(){
//	this.addMetaData({qn: "mx.core::UIComponent", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.core::UITextField", attributes: ["encaps_mx.controls::DateField", "encaps_mx.controls::DateChooser", "encaps_mx.controls::ComboBox", 
	                                                           "encaps_mx.controls::TextInput", "encaps_mx.controls::TextArea",
	                                                           "label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "", value: "text"});
	
	this.addMetaData({qn: "mx.controls::AdvancedDataGrid", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::AdvancedDataGridBaseEx", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::AdvancedDataGridGroupItemRenderer", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::Alert", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::Button", attributes: ["encaps_mx.controls::ColorPicker", "encaps_mx.controls::DateField", "encaps_mx.controls::ComboBox", "label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::ButtonBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::ButtonLabelPlacement", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::CheckBox", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "selected"});
	this.addMetaData({qn: "mx.controls::ColorPicker", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "selectedColor"});
	this.addMetaData({qn: "mx.controls::ComboBase", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::ComboBox", attributes: ["label", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setSelected", value: "selectedItemLabel"});
	this.addMetaData({qn: "mx.controls::DataGrid", attributes: ["encaps_spark.components::PopUpAnchor","label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls.dataGridClasses::DataGridHeader", attributes: ["text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "text"});
	this.addMetaData({qn: "mx.controls.dataGridClasses::DataGridColumn", attributes: ["headerText", "dataField", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "text"});
	this.addMetaData({qn: "mx.controls.dataGridClasses::DataGridItemRenderer", attributes: ["text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "text"});
	this.addMetaData({qn: "mx.controls::DateChooser", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "selectedDate"});
	this.addMetaData({qn: "mx.controls::DateField", attributes: ["label", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "selectedDate"});
	this.addMetaData({qn: "mx.controls::FileSystemComboBox", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::FileSystemDataGrid", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::FileSystemEnumerationMode", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::FileSystemHistoryButton", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::FileSystemList", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::FileSystemSizeDisplayMode", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::FileSystemTree", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::FlexNativeMenu", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::FormItemLabel", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::HorizontalList", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::HRule", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::HScrollBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::HSlider", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "sliderValue"});
	this.addMetaData({qn: "mx.controls::HTML", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::Image", attributes: ["text", "name", "content", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "toolTip"});
	this.addMetaData({qn: "mx.controls::Label", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "", value: "text"});
	this.addMetaData({qn: "mx.controls::LinkBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::LinkButton", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::List", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::Menu", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::MenuBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls.menuClasses::MenuBarItem", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::MXFTETextInput", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::NavBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::NumericStepper", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "value"});
	this.addMetaData({qn: "mx.controls::OLAPDataGrid", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::PopUpButton", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::PopUpMenuButton", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::ProgressBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::ProgressBarDirection", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::ProgressBarLabelPlacement", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::ProgressBarMode", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::RadioButton", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::RadioButtonGroup", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::RichTextEditor", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::Spacer", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::SWFLoader", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::TabBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::Text", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "text"});
	this.addMetaData({qn: "mx.controls::TextArea", attributes: ["label", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "text"});
	this.addMetaData({qn: "mx.controls::TextInput", attributes: ["encaps_mx.controls::DateField", "label", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "text"});
	this.addMetaData({qn: "mx.controls::TileList", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::ToggleButtonBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::ToolTip", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::Tree", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::VideoDisplay", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::VRule", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::VScrollBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls::VSlider", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "sliderValue"});
	this.addMetaData({qn: "mx.controls.scrollClasses::ScrollThumb", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});

	this.addMetaData({qn: "mx.charts::PieChart", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.charts.series::PieSeries", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.charts.renderers::WedgeItemRenderer", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.charts.series::ColumnSet", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.charts.series::ColumnSeries", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	
	// move this to user_ext.js
	this.addMetaData({qn: "siteAnalysis.view.components::SiteColumnChart", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "categoryAnalysis.view.components::CategoryColumnChart", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	
	
	this.addMetaData({qn: "mx.controls.listClasses::ListItemRenderer", attributes: ["data", "label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "data"});
	this.addMetaData({qn: "mx.controls.listClasses::ListBaseContentHolder", attributes: ["text", "data", "label", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "data"});
	this.addMetaData({qn: "mx.controls.listClasses::AdvancedListBaseContentHolder", attributes: ["text", "data", "label", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "data"});
	this.addMetaData({qn: "mx.controls.tabBarClasses::Tab", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls.buttonBarClasses::ButtonBarButton", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls.sliderClasses::SliderLabel", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.controls.sliderClasses::SliderThumb", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	
	this.addMetaData({qn: "mx.containers::Accordion", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::ApplicationControlBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::Box", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::BoxDirection", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::Canvas", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::ControlBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::DividedBox", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::Form", attributes: ["encaps_spark.components::PopUpAnchor","label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::FormHeading", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::FormItem", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::FormItemDirection", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::Grid", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::GridItem", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::GridRow", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::HBox", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "", value: "label"});
	this.addMetaData({qn: "mx.containers::HDividedBox", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "", value: "label"});
	this.addMetaData({qn: "mx.containers::Panel", attributes: ["title", "label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "", value: "label"});
	this.addMetaData({qn: "mx.containers::TabNavigator", attributes: ["spark.components::NavigatorContent","label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "", value: "label"});
	this.addMetaData({qn: "mx.containers::Tile", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::TileDirection", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::TitleWindow", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::VBox", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::VDividedBox", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.containers::ViewStack", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "mx.charts::ColumnChart", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	
	//Spark Components Start
	this.addMetaData({qn: "spark.components::Panel", attributes: ["title", "label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "", value: "label"});
	this.addMetaData({qn: "spark.components::Label", attributes: ["encaps_spark.components::FormItem","encaps_spark.components::DropDownList","encaps_spark.components::ComboBox","encaps_spark.components::FormHeading", "label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "", value: "text"});
	this.addMetaData({qn: "spark.components::TextInput", attributes: ["encaps_spark.components::FormItem","encaps_spark.components::NumericStepper", "label", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "text"});
	this.addMetaData({qn: "spark.components::Button", attributes: ["encaps_spark.components::FormItem","encaps_spark.components::Spinner","encaps_spark.components::VScrollBar", "encaps_spark.components::HScrollBar","encaps_spark.components::VSlider", "encaps_spark.components::HSlider",
	                                                               "encaps_spark.components::DropDownList", "encaps_spark.components::NumericStepper","encaps_spark.components::ComboBox",
	                                                               "label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::HSlider", attributes: ["encaps_spark.components::VGroup","label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "value"});
	this.addMetaData({qn: "spark.components::VSlider", attributes: ["encaps_spark.components::VGroup","label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "value"});
	this.addMetaData({qn: "spark.components::DropDownList", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setSelected", value: "selectedItemLabel"});
	this.addMetaData({qn: "spark.components::NumericStepper", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "value"});
	this.addMetaData({qn: "spark.components::CheckBox", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "selected"});
//	this.addMetaData({qn: "mx.controls::Text", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "text"});
//	this.addMetaData({qn: "mx.controls::TextArea", attributes: ["label", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "text"});
	
	//Theeran Start
	this.addMetaData({qn: "spark.components::TextArea", attributes: ["label","text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "name"});
	this.addMetaData({qn: "spark.components::ButtonBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value:"label"});
	this.addMetaData({qn: "spark.components::ButtonBarButton", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value:"label"});
	this.addMetaData({qn: "spark.components::CheckBox", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value:"selected"});
	this.addMetaData({qn: "spark.components::ComboBox", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setSelected", value: "selectedItemLabel"});
	this.addMetaData({qn: "spark.components::DataGroup", attributes: ["encaps_spark.components::Label","encaps_spark.components::List","encaps_spark.components::ButtonBar","encaps_spark.components::TabBar","label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "", value: "label"});
	this.addMetaData({qn: "spark.components::DropDownList", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setSelected", value: "selectedItemLabel"});
//	this.addMetaData({qn: "spark.components::Group", attributes: ["encaps_spark.components::Form","encaps_spark.components::DataGrid","encaps_spark.components::VideoPlayer","encaps_spark.components::TitleWindow","encaps_spark.components::RichText","encaps_spark.components::VScrollBar","encaps_spark.components::TextArea","encaps_spark.components::NavigatorContent","encaps_spark.components::PopUpAnchor","label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
//	this.addMetaData({qn: "spark.components::HGroup", attributes: ["encaps_spark.components::VScrollBar","label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "name"});
//	this.addMetaData({qn: "spark.components::VGroup", attributes: ["encaps_spark.components::DataGrid","encaps_spark.components::HSlider","encaps_spark.components::HSrollBar","encaps_spark.components::NumericStepper","label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "name"});
	this.addMetaData({qn: "spark.components::HScrollBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "change", value: "value"});
	this.addMetaData({qn: "spark.components::HSlider", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "change", value: "value"});
	this.addMetaData({qn: "spark.components::List", attributes: ["encaps_spark.components::Label", "label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setSelected", value: "selectedItem"});
	this.addMetaData({qn: "spark.components::NavigatorContent", attributes: ["encaps_spark.components::Label","label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::NumericStepper", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "value"});
	this.addMetaData({qn: "spark.components::PopUpAnchor", attributes: ["title", "label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "", value: ""});
	this.addMetaData({qn: "spark.components::RadioButton", attributes: ["encaps_spark.components::RadioButtonGroup","label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::TextArea", attributes: ["label", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "text"});
	this.addMetaData({qn: "spark.components::VScrollBar", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "change", value: "label"});
	this.addMetaData({qn: "spark.components::RadioButtonGroup", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::RichEditableText", attributes: ["encaps_spark.components::TextArea","encaps_spark.components::TextInput", "label", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "text"});
	//this.addMetaData({qn: "spark.components::Scroller", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "value"});
	this.addMetaData({qn: "spark.components::Spinner", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "value"});
	this.addMetaData({qn: "spark.components::TabBar", attributes: ["label", "text", "name", "automationName", "tool-Tip", "id", "autoGeneratedName", "index"], action: "change", value: "selectedItem"});
	this.addMetaData({qn: "spark.components::TileGroup", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::TitleWindow", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::ToggleButton", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::VideoDisplay", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::VideoPlayer", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::PopUpAnchor", attributes: ["title", "label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "", value: ""});
	this.addMetaData({qn: "spark.components::RadioButtonGroup", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::RichText", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "setValue", value: "text"});
	this.addMetaData({qn: "spark.components::TileGroup", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::DataGrid", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::DataItem", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::Form", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::FormHeading", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::FormItem", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components.gridClasses::GridColumn", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "dataField"});
	this.addMetaData({qn: "spark.components::Grid", attributes: ["encaps_spark.components::DataItem","label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::GridColumnHeaderGroup", attributes: ["label", "text", "name", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "label"});
	this.addMetaData({qn: "spark.components::Image", attributes: ["text", "name", "content", "automationName", "toolTip", "id", "autoGeneratedName", "index"], action: "click", value: "toolTip"});

	//Theeran End
	//Spark Components End
	SflWrapper.prototype.cell = this.getFlexFn("cell");
	
	this.addCustomMetaData();
}
Sahi.prototype.getFlexWrapper = function(o){
	var win = this.getWindow(o);
	var t = win.SflWrapper;
	return new t(o);
}
Sahi.prototype._flex = function(id){
	var o = this._embed(id) || this._object(id);
	return this.getFlexWrapper(o);
}
Sahi.prototype._findFlexElement = function(fl, api, id, rel){
	var el = fl[api](id);
	if (rel && rel.type == "dom") {
		if (rel.relation == "_in") return el.inside(rel.element);
		if (rel.relation == "_near") return el.near(rel.element);
	}
	return el;
}
Sahi.prototype._sfl_executeFn = function(el, fnName){
	return el.executeFn.apply(el, this.getArgsAr(arguments, 1));
}
Sahi.prototype._sfl_set = function(el, key, value){
	return el.set(key, value);
}
Sahi.prototype._sfl_get = function(el, key){
	return el.get(key);
}
Sahi.prototype._sfl_listProperties = function(el, fn){
	return el.listProperties();
}
Sahi.prototype._sfl_introspect = function(el){
	return el.introspect();
}
Sahi.prototype._sfl_getGridData = function(el){
	return el.getGridData();
}
Sahi.prototype._sfl_getData = function(el){
	return el.getData();
}
Sahi.prototype.setMyFlexId = function(uid){
	try {
		var o = this._embed(uid) || this._object(uid);
		//alert(uid + " " + o);
		if (!o) return null;
		var flexId = (!this.isBlankOrNull(o.id)) ? o.id : o.name;
		o._sahi_setFlexId(flexId);
	}catch(e){alert(e);}
}
Sahi.prototype.isApplet = function(){
	return false;
}
/** Flex end **/



/** Pro end **/
/*
// Event listener listening code 
var __old_addEvLis = HTMLInputElement.prototype.addEventListener;
HTMLInputElement.prototype.addEventListener = function(type, listener, useCapture){
	_sahi._debug(type + " " + this.id + " " + listener);
    __old_addEvLis.apply(this, arguments);
}
*/
