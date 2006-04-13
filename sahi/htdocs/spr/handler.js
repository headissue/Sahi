var lastQs = "";
var lastTime = 0;
function sahiOnEv(e){
	if (e.handled == true) return; //FF
	if (sahiGetServerVar("sahiEvaluateExpr") == "true") return;
	var targ = getTarget(e);
	var qs = getSahiPopUpQS()+sahiGetAccessorInfoQS(sahiGetAccessorInfo(targ));
	if (sahiHasEventBeenRecorded(qs)) return; //IE
    sahiSendToServer('/_s_/dyn/Recorder_record?'+qs);
    e.handled = true; //FF
}
function sahiHasEventBeenRecorded(qs){
	var now = (new Date()).getTime();
	if (qs == lastQs && (now - lastTime)<500 ) return true;
	lastQs = qs;
	lastTime = now;
	return false;
}
function getSahiPopUpQS(){
	if (window.top.opener != null && window.top.opener != window.top) {
		return "popup="+top.name+"&";
	}
	return "";
}
/*
function sahiOnPageLoad(){
    var accessorsOnPage = findAllAccessors();
    for(i = 0; i < accessorsOnPage.length; i++){
        sahiSendToServer('/_s_/dyn/Recorder_record?' + sahiGetAccessorInfoQS(accessorsOnPage[i]));
    }
}

function findAllAccessors(){
    var forms = document.forms;
    var accessorsOnPage =  new Array();
    var k = 0;
    for(i = 0; i < forms.length; i++){
        var form = forms[i];
        for(j =0; j< form.elements.length; j++){
            var element = form.elements[j];
            var type = element.type;
            var accessorInfo = sahiGetAccessorInfo(element);
            if(accessorInfo != null){
                if(accessorInfo.value == "click"){
                    accessorsOnPage[k] = accessorInfo;
                    k++;
                }
            }
        }
    }
    return accessorsOnPage;
}
*/

function doAssert(e){
    if (!sahiIsRecording()) return;
    try{
        if (!top._lastAccessedInfo) top._lastAccessedInfo = sahiGetAccessorInfo(e);
        sahiSendToServer('/_s_/dyn/Recorder_record?'+getSahiPopUpQS()+sahiGetAccessorInfoQS(top._lastAccessedInfo, true));
    }catch(ex){sahiHandleException(ex);}
}

function getTarget(e){
	var targ;
	if (!e) e = window.event;
	var evType = e.type;
	if (e.target) targ = e.target;
	else if (e.srcElement) targ = e.srcElement;
	if (targ.nodeType == 3) // defeat Safari bug
		targ = targ.parentNode;
	return targ;
}

function sahiGetAccessorInfo(el){
    var type = el.type;

    var accessor = sahiGetAccessor(el);
    var shortHand = getShortHand(el, accessor);
    if (el.tagName.toLowerCase() == "img"){
        return new AccessorInfo(accessor, shortHand, "img", "click");
    }else if(type == "text" || type == "textarea" || type == "password"){
        return new AccessorInfo(accessor, shortHand, type, "setvalue", el.value);
    }else if(type == "select-one" || type == "select-multiple"){
        return new AccessorInfo(accessor, shortHand, type, "setselected", sahiGetOptionText(el, el.value));
    }else if (el.tagName.toLowerCase() == "a") {
        return new AccessorInfo(accessor, shortHand, "link", "click");
    }else if (type == "button" || type == "submit" || type == "image"){
        return new AccessorInfo(accessor, shortHand, type, "click");
    }else if (type == "checkbox" || type == "radio"){
        return new AccessorInfo(accessor, shortHand, type, "click", el.value);
    }else if (el.tagName.toLowerCase() == "td"){
        return new AccessorInfo(accessor, shortHand, "cell", "", sahiIsIE()? el.innerText : el.textContent);
    }
}

function getShortHand(el, accessor){
    var shortHand = "";
    try{
        if (el.tagName.toLowerCase() == "img"){
            shortHand = el.alt;
            if (!shortHand || shortHand=="") shortHand = el.id;
            if (shortHand && shortHand!=""){
	            if (sahiFindImage(shortHand) != el){
	            	var ix = sahiFindImageIx(shortHand, el);
	            	if (ix == -1) return "";
					return shortHand + "["+ ix +"]";
	            }
	        }else{
            	var ix = sahiFindImageIx(null, el);
            	if (ix != -1) shortHand = ix;
            }
            return shortHand;
        }else if (el.tagName.toLowerCase() == "a"){
            shortHand = sahiGetText(el);//(el.innerText) ? el.innerText : el.text;
            shortHand = sahiTrim(shortHand);
            if (!shortHand || shortHand=="") shortHand = el.id;
            if (shortHand && shortHand!=""){
	            if (sahiFindLink(shortHand) != el){
	            	var ix = sahiFindLinkIx(shortHand, el);
	            	if (ix == -1) return "";
					return shortHand + "["+ ix +"]";
	            }            
            }
            return shortHand;
        }else if (el.tagName.toLowerCase() == "input" || el.tagName.toLowerCase() == "textarea" || el.tagName.toLowerCase().indexOf("select") != -1){
        	if (el.type == "button" || el.type == "submit") shortHand = el.value;
        	if (el.type == "image")	shortHand = el.alt;
            else if (!shortHand || shortHand=="") shortHand = el.name;
            if (!shortHand || shortHand=="") shortHand = el.id;
            if (shortHand && shortHand!=""){
	            if (sahiFindElement(shortHand, el.type) != el){
	            	var ix = sahiFindElementIx(shortHand, el, el.type);
	            	if (ix == -1) return "";
					return shortHand + "["+ ix +"]";
	            }            
            }else{
            	var ix = sahiFindElementIx(null, el, el.type);
            	if (ix != -1) shortHand = ix;
            }
            return shortHand;
        }else if (el.tagName.toLowerCase() == "td"){
        	shortHand = el.id;
            if (shortHand && shortHand!=""){
	            if (sahiFindCell(shortHand) != el){
	            	var ix = sahiFindCellIx(shortHand, el);
	            	if (ix != -1) return quoted(shortHand + "["+ ix +"]");
	            }  
	            return quoted(shortHand);          
            }
    		shortHand = getTableShortHand(sahiGetTableEl(el));//"_table(\""+tabId+"\")";
    		shortHand += ", "+sahiGetRow(el).rowIndex;
    		shortHand += ", "+el.cellIndex;
        }
    }catch(ex){sahiHandleException(ex);}
    return shortHand;
}
function getTableShortHand(el){
	var shortHand = el.id;
    if (shortHand && shortHand!=""){
        if (sahiFindTable(shortHand) != el){
        	var ix = sahiFindTableIx(shortHand, el);
        	if (ix != -1) return "_table(" + quoted(shortHand + "["+ ix +"]") +")";
        } 
        return "_table(" + quoted(shortHand) +")";           
    }
    return "_table(" + sahiFindTableIx(null, el) + ")";
}

function AccessorInfo(accessor, shortHand, type, event, value){
    this.accessor = accessor;
    this.shortHand = shortHand;
    this.type = type;
    this.event = event;
    this.value = value;
}

function sahiGetAccessorInfoQS(ai, isAssert){
    var s = "event="+ (isAssert? "assert" : ai.event);
    s += "&accessor="+escape(ai.accessor);
    s += "&shorthand="+escape(ai.shortHand);
    s += "&type="+ai.type;
    if (ai.value){
        s += "&value="+escape(ai.value);
    }
    return s;
}

function sahiGetAccessorInfoDisplay(el){
    var ai = sahiGetAccessorInfo(el);
    var s = "accessor="+ai.accessor;
    s += "\nshorthand="+ai.shortHand;
    s += "\ntype="+ai.type;
    if (ai.value){
        s += "\nvalue="+ai.value;
    }
    return s;
}

function sahiGetOptionText(sel, val){
    var l = sel.options.length;
    for (var i=0; i<l; i++){
        if (sel.options[i].value == val) return sel.options[i].text;
    }
    return null;
}

function sahiAddHandlersToAllFrames(win){
	var fs = win.frames;
	if (!fs || fs.length == 0){
		sahiAddHandlers(self);
	}else{
		for (var i=0; i<fs.length; i++){
			sahiAddHandlersToAllFrames(fs[i]);
		}
	}	
}
function sahiDocEventHandler(e){
	if (!e) e = window.event;
	var t = getTarget(e);
	if (t && !t.hasAttached && t.tagName){
		var tag = t.tagName.toLowerCase();
		if (tag == "a" || t.form || tag == "img" || tag == "div" || tag == "span" || tag == "td" || tag == "table"){
			sahiAttachEvents(t);
		}
		/*
		if (t.onmouseover){
			// addEventListenersForCapturing
			debug("onmouseover"+tag);
		}
		*/
		t.hasAttached = true;
	}

}
function sahiAddHandlers(win){
	if (!win) win = self;
	var doc = win.document;
	addEvent(doc, "keyup", sahiDocEventHandler);
	addEvent(doc, "mousemove", sahiDocEventHandler);
}

function sahiAttachEvents(el){
	if (el.onclick == sahiOnEv || el.onchange == sahiOnEv) return;
	var tagName = el.tagName.toLowerCase();
	if (tagName == "a"){
		sahiAttachLinkEvents(el)
	}else if (el.form && el.type){
		sahiAttachFormElementEvents(el);
	}else if (tagName == "img" || tagName == "div" || tagName == "span" || tagName == "td" || tagName == "table"){
		sahiAttachImageEvents(el);
	}
}
function sahiAttachFormElementEvents(el){
    var type = el.type;
    if (el.onchange == sahiOnEv || el.onblur == sahiOnEv || el.onclick == sahiOnEv) return;
    if (type == "text" || type == "textarea" || type == "password"){
    	addEvent(el, "change", sahiOnEv);
    }else if (type == "select-one" || type == "select-multiple"){
    	addEvent(el, "change", sahiOnEv);
    }else if (type == "button" || type == "submit" || type == "checkbox" || type == "radio" || type == "image"){
    	addEvent(el, "click", sahiOnEv);
    }
}
function sahiAttachLinkEvents(el){
    addEvent(el, "click", sahiOnEv);
}
function sahiAttachImageEvents(el){
 	addEvent(el, "click", sahiOnEv);
}
function addEvent(el, ev, fn){
	if (!el) return;
	if (el.attachEvent){
		el.attachEvent("on"+ev, fn);
	}else if (el.addEventListener){
		el.addEventListener(ev, fn, false);
	}	
}