function sahiOnEv(e){
	if (sahiGetServerVar("sahiEvaluateExpr") == "true") return;
	var targ = getTarget(e);
    if (document.all){
        if (targ.prevOnChange) targ.prevOnChange();
        if (targ.prevOnBlur) targ.prevOnBlur();
    }else{
        if (targ.prevOnBlur) targ.prevOnBlur();
        if (targ.prevOnChange) targ.prevOnChange();
    }
    sahiSendToServer('/_s_/dyn/record?'+getSahiPopUpQS()+sahiGetAccessorInfoQS(sahiGetAccessorInfo(targ)));
    if (targ.prevOnClick) targ.prevOnClick();
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
        sahiSendToServer('/_s_/dyn/record?' + sahiGetAccessorInfoQS(accessorsOnPage[i]));
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
        sahiSendToServer('/_s_/dyn/record?'+getSahiPopUpQS()+sahiGetAccessorInfoQS(top._lastAccessedInfo, true));
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
        return new AccessorInfo(accessor, shortHand, "image", "click");
    }else if(type == "text" || type == "textarea" || type == "password"){
        return new AccessorInfo(accessor, shortHand, type, "setvalue", el.value);
    }else if(type == "select-one" || type == "select-multiple"){
        return new AccessorInfo(accessor, shortHand, type, "setselected", sahiGetOptionText(el, el.value));
    }else if (el.tagName.toLowerCase() == "a") {
        return new AccessorInfo(accessor, shortHand, "link", "click");
    }else if (type == "button" || type == "submit"){
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
            shortHand = (el.innerText) ? el.innerText : el.text;
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
            if (!shortHand || shortHand=="") shortHand = el.name;
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
	return "_accessor(" + quoted(sahiGetAccessor(el)) + ")";
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

function sahiAddHandlers(win){
	if (!win) win = self;
    var fs = win.document.forms;
    for (var i=0; i<fs.length; i++){
        var f = fs[i];
        var els = f.elements;
        for (var j=0; j<els.length; j++){
	        sahiAttachFormElementEvents(els[j]);
        }
    }
    var ls = win.document.links;
    for (var i=0; i<ls.length; i++){
        var l = ls[i];
        sahiAttachLinkEvents(l)
    }
    var imgs = win.document.images;
    for (var i=0; i<imgs.length; i++){
    	sahiAttachImageEvents(imgs[i]);
    }
}

function sahiAttachEvents(el){
	var tagName = el.tagName.toLowerCase();
	if (tagName == "a"){
		sahiAttachLinkEvents(el)
	}else if (el.form && el.type){
		sahiAttachFormElementEvents(el);
	}else if (tagName == "img"){
		sahiAttachImageEvents(el);
	}
}
function sahiAttachFormElementEvents(el){
    var type = el.type;
    if (type == "text" || type == "textarea" || type == "password"){
        el.prevOnBlur = el.onblur;
        el.prevOnChange = el.onchange;
        el.onchange = sahiOnEv;
    }else if (type == "select-one" || type == "select-multiple"){
        el.prevOnBlur = el.onblur;
        el.prevOnChange = el.onchange;
        el.onchange = sahiOnEv;
    }else if (type == "button" || type == "submit" || type == "checkbox" || type == "radio"){
        el.prevOnClick = el.onclick;
        el.onclick = sahiOnEv;
    }
}
function sahiAttachLinkEvents(el){
    el.onclick = sahiOnEv;
}
function sahiAttachImageEvents(el){
    if (el.onclick){
        el.prevOnClick = el.onclick;
        el.onclick = sahiOnEv;
    }
}
