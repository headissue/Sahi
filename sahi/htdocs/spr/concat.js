var tried = false;
function sahiGetAccessor(src){
    var fr = sahiGetFrame(top, "top");
    var a = sahiGetPartialAccessor(src);
    if (a=="" || a==null) return a;
    var elStr = fr+".document."+a;
    var v = sahiGetArrayElement(elStr, src);
    return v;
}

function sahiGetKnownTags(src){
    var el = src;
    while (true) {
        if (!el) return src;
        if (!el.tagName || el.tagName.toLowerCase() == "html" || el.tagName.toLowerCase() == "body") return null;
        var tag = el.tagName.toLowerCase();
        if (tag == "a" || tag == "select" || tag == "img" || tag == "form"
            || tag == "input" || tag == "button" || tag == "textarea" 
            || tag == "textarea" || tag == "td" || tag == "table" 
            || ((tag == "div" || tag == "span") && (el.id && el.id !=""))) return el;
        el = el.parentNode;
    }
}

function sahiById(src){
    var s = src.id;
    if (isBlankOrNull(s)) return "";
    return "getElementById('"+s+"')";
}
function sahiGetPartialAccessor(src){
    if (src == null || src.tagName==null) return null;
    var tag = src.tagName.toLowerCase();
    var a = sahiById(src);
    if (a != "" && eval("document."+a) == src) {
        return a;
    }

    if (tag=="img"){
        return sahiGetImg(src);
    }
    else if (tag=="a"){
        return sahiGetLink(src);
    }
    else if (tag=="form"){
        return sahiGetForm(src);
    }
    else if (tag=="button" || tag=="input" || tag=="textarea" || tag=="select"){
        return sahiGetFormElement(src);
    }
    else if (tag == "td"){
        return sahiGetTableCell(src);
    }
    else if (tag == "table"){
        return sahiGetTable(src);
    }
}
function sahiGetLink(src){
    var lnx = document.links;
    for (var j=0; j<lnx.length; j++){
        if (lnx[j] == src){
            return "links["+j+"]";
        }
    }
    return  null;
}
function sahiGetImg(src){
    var lnx = document.images;
    for (var j=0; j<lnx.length; j++){
        if (lnx[j] == src){
            return "images["+j+"]";
        }
    }
    return  null;
}

function sahiGetForm(src){
    if (!isBlankOrNull(src.name) && nameNotAnInputElement(src)){
        return "forms['"+src.name+"']";
    }
    var fs = document.forms;
    for (var j=0; j<fs.length; j++){
        if (fs[j] == src){
            return "forms["+j+"]";
        }
    }
    return null;
}
function nameNotAnInputElement(src){
	return (typeof src.name != "object");
}
function sahiGetFormElement(src){
	return sahiGetByTagName(src);
    /*
    if (!isBlankOrNull(src.name)){
        n = 'elements["'+src.name+'"]';
    }else {
        var els = src.form.elements;
        for (var j=0; j<els.length; j++){
            if (els[j] == src){
                n = "elements["+j+"]";
            }
        }
    }
    var f = sahiGetForm(src.form);
    return (n == "") ? f : f+"."+n;
    */
}

function sahiGetByTagName(src, tagName){
	var tagName = src.tagName.toLowerCase();
	var els = document.getElementsByTagName(tagName);
	return "getElementsByTagName('"+tagName+"')[" + sahiFindInArray(els, src) + "]";
}

function sahiGetTable(src){
	var tables = document.getElementsByTagName("table");
	if (src.id && src.id !=null && src==document.getElementById(table.id)){
	    return "getElementById('"+table.id+"')";
	}
	return "getElementsByTagName('table')[" + sahiFindInArray(tables, src) + "]";

}

function sahiGetTableCell(src){
	var tables = document.getElementsByTagName("table");
	var row = sahiGetRow(src);
	if (row.id && row.id !=null && row==document.getElementById(row.id)){
		return "getElementById('"+row.id+"').cells["+src.cellIndex+"]";
	}
	var table = sahiGetTableEl(src);
	if (table.id && table.id !=null && table==document.getElementById(table.id)){
	    return "getElementById('"+table.id+"').rows["+sahiGetRow(src).rowIndex+"].cells["+src.cellIndex+"]";
	}
	return "getElementsByTagName('table')["+sahiFindInArray(tables, sahiGetTableEl(src)) + "].rows["+sahiGetRow(src).rowIndex+"].cells["+src.cellIndex+"]";
}

function sahiGetRow(src){
    return sahiGetParentNode(src, "tr");
}

function sahiGetTableEl(src){
    return sahiGetParentNode(src, "table");
}

function sahiGetArrayElement(s, src){
    var tag = src.tagName.toLowerCase();
    if (tag=="input" || tag=="textarea" || tag.indexOf("select") != -1){
        var el2 = eval(s);
        if (el2 == src) return s;
        var ix = -1;
        if (el2 && el2.length){
            ix = sahiFindInArray(el2, src);
            return s+"["+ix+"]";
        }
    }
    return s;
}

function sahiGetEncapsulatingLink(src){
    var el = src;
    while (el && el.tagName && el.tagName.toLowerCase() != "a") {
         el = el.parentNode;
    }
    return el;
}

function sahiGetFrame(win, s){
    if (win == self) return s;
    var frs = win.frames;
    for (var j=0; j<frs.length; j++){
        var n = frs[j].name;
        if (isBlankOrNull(n)) n = "frames["+j+"]";
        var sub = sahiGetFrame(frs[j], n);
        if (sub != null){
            return s+"."+sub;
        }
    }
    return null;
}

function isBlankOrNull(s){
    return (s=="" || s==null);
}
function linkClick(e){
	var performDefault = true;
	if (this.prevClick){
		performDefault = this.prevClick.apply(this, arguments);
	}
	if (performDefault != false){
		sahiNavigateLink(this);
	}
}

function sahi_dragDrop(draggable, droppable){
	sahiSimulateMouseEvent(draggable, "mousedown");
	draggable.style.left=findPosX(droppable) - findPosX(draggable) + 2;
	draggable.style.top=findPosY(droppable) - findPosY(draggable) + 2;
	draggable.style.zIndex=1;
	sahiSimulateMouseEvent(droppable, "mousedown");
	
	sahiSimulateMouseEvent(draggable, "mousemove");
	sahiSimulateMouseEvent(droppable, "mousemove");
	
	sahiSimulateMouseEvent(draggable, "mouseup");
	sahiSimulateMouseEvent(droppable, "mouseup");
}

function sahi_click(el){
	if (el == null) return;
	var n = el;
	while (n != null){
		if (n.tagName && n.tagName == "A"){
			n.prevClick = n.onclick;
			n.onclick = linkClick;
		}
		n = n.parentNode;
	}
	
	if (sahiIsIE() && el && (el.type=="submit" || el.type=="image" || el.type=="checkbox" || el.type=="radio")){
		return el.click();
	}
	
	sahiSimulateMouseEvent(el, "mousemove");
	sahiSimulateMouseEvent(el, "focus");
	sahiSimulateMouseEvent(el, "mouseover");
	sahiSimulateMouseEvent(el, "mousedown");
	sahiSimulateMouseEvent(el, "mouseup");
	try{
	sahiSimulateMouseEvent(el, "click");
	}catch(e){
	} 
	sahiSimulateMouseEvent(el, "blur");
	var n = el;
	while (n != null){
		if (n.tagName && n.tagName == "A"){
			n.onclick = n.prevClick;
		}
		n = n.parentNode;
	} 
}
function sahiSimulateMouseEvent(el, type){
	var x = findPosX(el);
	var y = findPosY(el);
	if(document.createEvent){
		var evt = el.ownerDocument.createEvent("MouseEvents");
		evt.initMouseEvent(type,
		true, //can bubble
		true,
		el.ownerDocument.defaultView,
		1,
		x, //screen x
		y, //screen y
		x, //client x
		y, //client y
		false,
		false,
		false,
		false,
		0,
		null);
		el.dispatchEvent(evt);
	}else{
		var evt = el.ownerDocument.createEventObject();
		// Set an expando property on the event object. This will be used by the
		// event handler to determine what element was clicked on.
		evt.clientX = x;
		evt.clientY = y;
		evt.button = 1;
		el.fireEvent("on"+type,evt);
		evt.cancelBubble = true;
	}
}
var sahiPointTimer;
function sahi_highlight(el){
	var d =	sahiFindElementById(top, "sahi_pointer_div");
	d.innerHTML = "<span style='color:red;font-family:verdana;font-size:20px;'>&raquo;</span>";
	d.style.position = "absolute";
	var x = findPosX(el)-10;
	var y = findPosY(el)-8;
	d.style.left = x+"px";
	d.style.top = y+"px";
	d.style.zIndex = 10;
	d.style.display = "block";	
	sahiPointTimer = window.setTimeout("sahiFade()", 2000);
	window.scrollTo(x, y);	
}
function sahiFade(){
	window.clearTimeout(sahiPointTimer);
	var d =	sahiFindElementById(top, "sahi_pointer_div");
	d.style.position = "absolute";
	d.style.left = "0px";
	d.style.top = "0px";
	d.style.zIndex = 0;
	d.style.display = "none";	
}
function findPosX(obj)
{
	var curleft = 0;
	if (obj.offsetParent)
	{
		while (obj.offsetParent)
		{
			curleft += obj.offsetLeft
			obj = obj.offsetParent;
		}
	}
	else if (obj.x)
		curleft += obj.x;
	return curleft;
}

function findPosY(obj)
{
	var curtop = 0;
	if (obj.offsetParent)
	{
		while (obj.offsetParent)
		{
			curtop += obj.offsetTop
			obj = obj.offsetParent;
		}
	}
	else if (obj.y)
		curtop += obj.y;
	return curtop;
}

function sahiNavigateLink(ln){
	if (!ln) return;
	var win = ln.ownerDocument.defaultView; //FF
	if (!win) win = ln.ownerDocument.parentWindow; //IE
    if (ln.href.indexOf("javascript:")==0){
        var s = ln.href.substring(11);
        win.eval(s);
    }else{
    	var target = ln.target;
 		if (ln.target==null || ln.target=="") target = "_self";
        win.open(ln.href, target);
    } 
}
function appendSahiSid(url){
   	return url + (url.indexOf("?")==-1 ? "?" : "&") + "sahisid="+sahiReadCookie("sahisid");
}

function sahiGetClickEv(el){
	var e = new Object();
	if (sahiIsIE()) el.srcElement = e;
	else e.target = el;
	e.stopPropagation = noop;
	return e;
}

function noop(){}

// api for link click end

// api for set value start
function sahi_setValue(el, val){
	val = ""+val;
	var prevVal = el.value;
    if(!document.createEvent) el.value = val;
    if (el.type && el.type.indexOf("select") != -1){
    }else{
    	var append = false;
    	el.value = "";
	    if (typeof val == "string"){
    		for (var i=0; i<val.length; i++){
    			var c = val.charAt(i);
    			sahiSimulateKeyEvent(c, el, "keydown");
    			sahiSimulateKeyEvent(c, el, "keypress");
    			if (i==0 && el.value != c){
    				append = true;
    			}
    			if (append) el.value += c;
    			sahiSimulateKeyEvent(c, el, "keyup");
    		}
    	}
    }
    if (prevVal != val && el.onchange){
    	sahiSimulateEvent(el, "change");
    }
}

function sahiSimulateEvent(target, evType){
	if(document.createEvent){
		var evt = new Object();
		evt.type = evType;
		evt.bubbles = true;
		evt.cancelable = true;		
		if (!target) return; 
		var event = target.ownerDocument.createEvent("HTMLEvents"); 
		event.initEvent(evt.type, evt.bubbles, evt.cancelable); 
		target.dispatchEvent(event); 
	}else{
		var evt = target.ownerDocument.createEventObject();
		evt.type = evType;
		evt.bubbles = true;
		evt.cancelable = true;
		evt.cancelBubble = true;
		target.fireEvent("on"+evType, evt);
	}
}

function sahiSimulateKeyEvent(c, target, evType){
	var x = findPosX(target);
	var y = findPosY(target);

	if(document.createEvent){
		var evt = new Object();
		evt.type = evType;
		evt.bubbles = true;
		evt.cancelable = true;
		evt.ctrlKey = false; 
		evt.altKey = false;
		evt.metaKey = false;
		if ((c!='.')) evt.keyCode = c.toUpperCase().charCodeAt(0);
		evt.charCode = c.charCodeAt(0);
		evt.shiftKey = evt.keyCode != evt.charCode;
		
		if (!target) return; 
		var event = target.ownerDocument.createEvent("KeyEvents"); 
		event.initKeyEvent(evt.type, evt.bubbles, evt.cancelable, target.ownerDocument.defaultView, 
		evt.ctrlKey, evt.altKey, evt.shiftKey, evt.metaKey, evt.keyCode, evt.charCode); 
		target.dispatchEvent(event); 
	}else{
		var evt = target.ownerDocument.createEventObject();
		evt.type = evType;
		evt.bubbles = true;
		evt.cancelable = true;
		evt.clientX = x;
		evt.clientY = y;
		evt.ctrlKey = false; 
		evt.altKey = false;
		evt.metaKey = false;
		evt.keyCode = c.toUpperCase().charCodeAt(0);
		evt.charCode = c.charCodeAt(0);
		evt.shiftKey = evt.keyCode != evt.charCode;
		evt.cancelBubble = true;
//		alert(evt.keyCode+" "+evt.charCode);
		target.fireEvent("on"+evType, evt);
		//alert(target.value+" "+c);
		//if (target.value) target.value+=c;
	}
}

function sahi_setSelected(el, val){
    var l = el.options.length;
    for (var i=0; i<l; i++){
        if (el.options[i].text.indexOf(sahiTrim(val)) != -1){
            el.options[i].selected = true;
//            if (el.onchange) el.onchange();
            return;
        }
    }
    throw new Error();
}
// api for set value end
function sahi_check(el, val){
    el.checked = val;
    if (el.onclick) el.onclick();
}

function sahi_button(n){
     return sahiFindElement(n, "button", "input");
}
function sahi_submit(n){
     return sahiFindElement(n, "submit", "input");
}

function sahi_wait(i){
    _sahi_wait = i;
}

function sahi_textbox(n){
    return sahiFindElement(n, "text", "input");

}
function sahi_password(n){
    return sahiFindElement(n, "password", "input");
}
function sahi_checkbox(n){
    return sahiFindElement(n, "checkbox", "input");
}
function sahi_textarea(n){
    return sahiFindElement(n, "textarea", "textarea");
}
function sahi_accessor(n){
    return eval(n);
}
function sahi_byId(id){
	return sahiFindElementById(top, id);
}
function sahi_select(n){
    var el = sahiFindElement(n, "select-one", "select");
    if (!el) el = sahiFindElement(n, "select-multiple", "select");
    return el;
}
function sahi_radio(n){
    return sahiFindElement(n, "radio", "input");
}
function sahi_div(id){
	return sahiDivSpanByText(top, id, "div");
}
function sahi_span(id){
	return sahiDivSpanByText(top, id, "span");
}
function sahi_spandiv(id){
	var el = sahiDivSpanByText(top, id, "span");
	if (el == null) el = sahiDivSpanByText(top, id, "div");
	return el;
}
function sahiDivSpanByText(win, id, tagName){
	var res = null;
	var els = win.document.getElementsByTagName(tagName);
	for (var i=0; i<els.length; i++){
		if (sahi_getText(els[i]) == id){
			return els[i];
		}
	}
    var frs = win.frames;
    if (frs){
        for (var j=0; j<frs.length; j++){
            res = sahiDivSpanByText(frs[j], id, tagName);
            if (res) return res;
        }
    }  
    return res;  
}
function sahi_image(n){
    return sahiFindImage(n, top, "img");
}
function sahi_imageSubmitButton(n){
    return sahiFindElement(n, "image", "input");
}
function sahi_link(n){
    return sahiFindLink(n, top);
}
function sahi_simulateEvent(el, ev){
    if (sahiIsIE()){
        var newFn = (eval("el.on"+ev.type)).toString();
        newFn = newFn.replace("anonymous()", "s_anon(s_ev)", "g").replace("event", "s_ev", "g");
        eval (newFn);
        s_anon(ev);
    }else{
        eval ("el.on"+ev.type+"(ev);");
    }
}
function sahi_setGlobal(name, value){
	sahiSetServerVar(name, value);
}
function sahi_getGlobal(name){
	return sahiGetServerVar(name);
}

function sahi_assertNotNull(n, s){
    if (n==null) throw new SahiAssertionException(1, s);
    return true;
}
function sahi_assertNull(n, s){
    if (n!=null) throw new SahiAssertionException(2, s);
    return true;
}
function sahi_assertTrue(n, s){
    if (n!=true) throw new SahiAssertionException(5, s);
    return true;
}
function sahi_assertNotTrue(n, s){
    if (n) throw new SahiAssertionException(6, s);
    return true;
}
function sahi_assertEqual(expected, actual, s){
    if (sahiTrim(expected) != sahiTrim(actual)) throw new SahiAssertionException(3, (s?s:"")+"\nExpected:["+expected+"]\nActual:["+actual+"]");
    return true;
}
function sahi_assertNotEqual(expected, actual, s){
    if (sahiTrim(expected) == sahiTrim(actual)) throw new SahiAssertionException(4, s);
    return true;
}
function sahi_getSelectedText(el){
    var opts = el.options;
    for (var i=0; i<opts.length; i++){
        if (el.value == opts[i].value) return opts[i].text;
    }
    return null;
}
function sahi_getCellText(el){
    return sahiTrim(sahiIsIE() ? el.innerText : el.textContent);
}
function sahi_getText(el){
    return sahiTrim(sahiIsIE() ? el.innerText : el.textContent);
}
function sahiGetRowIndexWith(txt, tableEl){
	var r = sahiGetRowWith(txt, tableEl);
	return (r == null) ? -1 : r.rowIndex;
}
function sahiGetRowWith(txt, tableEl){
	for (var i=0; i<tableEl.rows.length; i++){
		var r = tableEl.rows[i];
		for (var j=0; j<r.cells.length; j++){
			if (sahi_getText(r.cells[j]).indexOf(txt) != -1){
				return r;
			}
		}
	}
	return null;
}
function sahiGetColIndexWith(txt, tableEl){
	for (var i=0; i<tableEl.rows.length; i++){
		var r = tableEl.rows[i];
		for (var j=0; j<r.cells.length; j++){
			if (sahi_getText(r.cells[j]).indexOf(txt) != -1){
				return j;
			}
		}
	}
	return -1;	
}

var _sahiLastAlert = "";
function sahi_alert(s){
	if (isSahiPlaying()){
		_sahiLastAlert = s;
	}else{
		return sahi_real_alert(s);
	}
}
function sahi_eval(s){
    return eval(s);
}
function sahi_call(s){
    return s;
}
function sahi_random(n){
	return Math.floor(Math.random()*(n+1));
}
function sahi_savedRandom(id, min, max){
	if (min == null) min = 0;
	if (max == null) max = 10000;
	var r = sahi_getGlobal("srandom"+id);
	if (r == null || r == "") {
		r = min + sahi_random(max - min);
		sahi_setGlobal("srandom"+id, r);
	}
	return r;
}
function sahi_resetSavedRandom(id){
	sahi_setGlobal("srandom"+id, "");
}
function sahi_confirm(s){
	return true;//confirm(s);
//	window.open("/_s_/dyn/confirm.htm?msg="+s, "", "height=60px,width=460px,resizable=yes,toolbars=no,statusbar=no");
}
function sahi_prompt(n){
	return sahi_real_prompt(n);
}
function sahi_cell(id, row, col){
	if (row==null && col==null){
		return sahiFindCell(id);
	}
	var rowIx = row;
	var colIx = col;
	if (typeof row == "string"){
		rowIx = sahiGetRowIndexWith(row, id);
		if (rowIx == -1) return null;
	}
	if (typeof col == "string"){
		colIx = sahiGetColIndexWith(col, id);
		if (colIx == -1) return null;
	}
	return id.rows[rowIx].cells[colIx];
}
function sahi_table(n){
	return sahiFindTable(n);
}
function sahi_row(tableEl, rowIx){
	if (typeof rowIx == "string"){
		return sahiGetRowWith(rowIx, tableEl);
	}
	if (typeof rowIx == "number"){
		return tableEl.rows[rowIx];
	}
	return null;
}
function sahi_containsHTML(el, htm){
	return el && el.innerHTML && el.innerHTML.indexOf(htm) != -1;
}
function sahi_containsText(el, txt){
	return el && sahiGetText(el).indexOf(txt) != -1;
}
function sahi_popup(n){
	if (top.name == n){
		return top;
	}
	throw new SahiNotMyWindowException();
}
function sahi_log(s, type){
	sahiLogPlayBack(s, type);
}
function sahi_navigateTo(url){
	top.location.href = url;
}
function sahi_callServer(cmd, qs){
	return sahiSendToServer("/_s_/dyn/"+cmd+(qs == null ? "" :  ("?"+qs)));
}

// finds document of any element
function sahiGetWin(el){
	if (el == null) return self;
	if (el.nodeName.indexOf("document")!=-1) return sahiGetFrame1(top, el);
	return sahiGetWin(el.parentNode);
}
// finds window to which a document belongs
function sahiGetFrame1(win, doc){
    if (win.document == doc) return win;
    var frs = win.frames;
    for (var j=0; j<frs.length; j++){
        var sub = sahiGetFrame1(frs[j], doc);
        if (sub != null){
            return sub;
        }
    }
    return null;
}

function sahiSimulateChange(el){
    if (document.all){
        if (el.onchange) el.onchange();
        if (el.onblur) el.onblur();
    }else{
        if (el.onblur) el.onblur();
        if (el.onchange) el.onchange();
    }
}
function point(el){
}
function sahiAreEqual(el, param, value){
	if (param == "linkText"){
        var str = sahiGetText(el);
//        sahi_real_alert(str+ " " +value);
        return (sahiTrim(str) == sahiTrim(value));
	}
	else{
		return (el[param] == value);
	}
}
function sahiFindLink(id){
	var res = getBlankResult();
	var retVal = sahiFindImageHelper(id, top, res, "linkText", false).element;
	if (retVal != null) return retVal;
	
	res = getBlankResult();
	return sahiFindImageHelper(id, top, res, "id", false).element;
}
function sahiFindImage(id){
	var res = getBlankResult();
	var retVal = sahiFindImageHelper(id, top, res, "alt", true).element;
	if (retVal != null) return retVal;
	
	res = getBlankResult();
	return sahiFindImageHelper(id, top, res, "id", true).element;
}
function sahiFindImageHelper(id, win, res, param, isImg){
    var imgs = isImg ? win.document.images : win.document.links;
	
	if ((typeof id) == "number"){
		res.cnt = 0;
		res = sahiFindImageByIx(id, top, res, isImg);
		return res;
	}else{
		var o = getArrayNameAndIndex(id);
	    var imgIx = o.index;
	    var fetch = o.name;
	    for (var i=0; i<imgs.length; i++){
	        if (sahiAreEqual(imgs[i], param, fetch)){
	        	res.cnt++;
	        	if (res.cnt == imgIx || imgIx == -1){
	        		res.element = imgs[i];
	        		res.found = true;
	        		return res;
	        	}
	        }
	    }
    }

    var frs = win.frames;
    if (frs){
        for (var j=0; j<frs.length; j++){
            res = sahiFindImageHelper(id, frs[j], res, param, isImg);
            if (res && res.found) return res;
        }
    }
    return res;
}

function sahiFindImageByIx(ix, win, res, isImg){
    var imgs = isImg ? win.document.images : win.document.links;
    if (imgs[ix - res.cnt]) {
    	res.element = imgs[ix - res.cnt];
    	res.found = true;
    	return res;
    }
	res.cnt += imgs.length;
    var frs = win.frames;
    if (frs){
        for (var j=0; j<frs.length; j++){
            res = sahiFindImageByIx(ix, frs[j], res, isImg);
            if (res && res.found) return res;
        }
    }  
    return res;  
}

function sahiFindLinkIx(id, toMatch){
	var res = getBlankResult();
	if (id == null || id == ""){
		retVal = sahiFindImageIxHelper(id, toMatch, top, res, null, false).cnt;
		if (retVal != -1) return retVal;
	}

	var res = getBlankResult();
	var retVal = sahiFindImageIxHelper(id, toMatch, top, res, "linkText", false).cnt;
	if (retVal != -1) return retVal;
	
	res = getBlankResult();
	return sahiFindImageIxHelper(id, toMatch, top, res, "id", false).cnt;
}
function sahiFindImageIx(id, toMatch){
	var res = getBlankResult();
	if (id == null || id == ""){
		retVal = sahiFindImageIxHelper(id, toMatch, top, res, null, true).cnt;
		if (retVal != -1) return retVal;
	}
	
	var res = getBlankResult();
	var retVal = sahiFindImageIxHelper(id, toMatch, top, res, "alt", true).cnt;
	if (retVal != -1) return retVal;
	
	res = getBlankResult();
	return sahiFindImageIxHelper(id, toMatch, top, res, "id", true).cnt;
}
function sahiFindImageIxHelper(id, toMatch, win, res, param, isImg){
	if (res && res.found) return res;
	
    var imgs = isImg ? win.document.images : win.document.links;
    for (var i=0; i<imgs.length; i++){
        if (param == null || sahiAreEqual(imgs[i], param, id)){
        	res.cnt++;
        	if (imgs[i] == toMatch){
        		res.found = true;
        		return res;
        	}
        }
    }
    var frs = win.frames;
    if (frs){
        for (var j=0; j<frs.length; j++){
            res = sahiFindImageIxHelper(id, toMatch, frs[j], res, param, isImg);
			if (res && res.found) return res;
        }
    }
    return res;
}
function sahiFindElementById(win, id){
	var res = null;
	if (win.document.getElementById(id) != null){
		return win.document.getElementById(id);
	}
    var frs = win.frames;
    if (frs){
        for (var j=0; j<frs.length; j++){
            res = sahiFindElementById(frs[j], id);
            if (res) return res;
        }
    }  
    return res;  
}
function sahiFindElement(id, type, tagName){
	var res = getBlankResult();
	var retVal = null;
	if (type == "button" || type == "submit"){
		retVal = sahiFindElementHelper(id, top, type, res, "value", tagName).element;
		if (retVal != null) return retVal;
	}
	else if (type == "image"){
		retVal = sahiFindElementHelper(id, top, type, res, "alt", tagName).element;
		if (retVal != null) return retVal;
	}
	
	res = getBlankResult();
	retVal = sahiFindElementHelper(id, top, type, res, "name", tagName).element;
	if (retVal != null) return retVal;
	
	res = getBlankResult();
	return sahiFindElementHelper(id, top, type, res, "id", tagName).element;
}

function sahiFindFormElementByIndex(ix, win, type, res, tagName){
	var els = win.document.getElementsByTagName(tagName);
	for (var j=0; j<els.length; j++){
	   	var el = els[j];
    	if (el != null && el.type == type){
    		res.cnt++;
    		if (res.cnt == ix){
	    		res.element = el;
	    		res.found = true;
	    		return res;
    		}
    	}
	}
    var frs = win.frames;
    if (frs){
        for (var j=0; j<frs.length; j++){
            res = sahiFindFormElementByIndex(ix, frs[j], type, res, tagName);
            if (res && res.found) return res;
        }
    }    
    return res;
}

function sahiFindElementHelper(id, win, type, res, param, tagName){
	if ((typeof id) == "number"){
		res = sahiFindFormElementByIndex(id, win, type, res, tagName);
		if (res.found) return res;
	}else{
		var o = getArrayNameAndIndex(id);
	    var ix = o.index;
	    var fetch = o.name;
		var els = win.document.getElementsByTagName(tagName);
		for (var j=0; j<els.length; j++){
			if (els[j].type == type && sahiAreEqual(els[j], param, fetch)){
				res.cnt++;
				if (res.cnt == ix || ix == -1){
					res.element = els[j];
					res.found = true;
					return res;
				}
			}
		}
    
    }
    var frs = win.frames;
    if (frs){
        for (var j=0; j<frs.length; j++){
            res = sahiFindElementHelper(id, frs[j], type, res, param, tagName);
			if (res && res.found) return res;
        }
    }
    return res;
}

function sahiFindElementIx(id, toMatch, type, tagName){
	var res = getBlankResult();
	var retVal = -1;

	if (id == null || id == ""){
		retVal = sahiFindElementIxHelper(id, type, toMatch, top, res, null, tagName).cnt;
		if (retVal != -1) return retVal;
	}

	if (type == "button" || type == "submit"){
		retVal = sahiFindElementIxHelper(id, type, toMatch, top, res, "value", tagName).cnt;
		if (retVal != -1) return retVal;
	}	
	else if (type == "image"){
		retVal = sahiFindElementIxHelper(id, type, toMatch, top, res, "alt", tagName).cnt;
		if (retVal != -1) return retVal;
	}
	res = getBlankResult();
	retVal = sahiFindElementIxHelper(id, type, toMatch, top, res, "name", tagName).cnt;
	if (retVal != -1) return retVal;

	res = getBlankResult();
	retVal = sahiFindElementIxHelper(id, type, toMatch, top, res, "id", tagName).cnt;
	return retVal;

}
function sahiFindElementIxHelper(id, type, toMatch, win, res, param, tagName){
	if (res && res.found) return res;
	var els = win.document.getElementsByTagName(tagName);
	for (var j=0; j<els.length; j++){
        if (els[j].type == type && sahiAreEqual(els[j], param, id)){
        	res.cnt++;
        	if (els[j] == toMatch){
        		res.found = true;
        		return res;
        	}
        }
    }
    var frs = win.frames;
    if (frs){
        for (var j=0; j<frs.length; j++){
            res = sahiFindElementIxHelper(id, type, toMatch, frs[j], res, param, tagName);
			if (res && res.found) return res;
        }
    }
    return res;
}
function sahiFindCell(id){
	var res = getBlankResult();
	return sahiFindTagHelper(id, top, "td", res, "id").element;
}

function sahiFindCellIx(id, toMatch){
	var res = getBlankResult();
	var retVal = sahiFindTagIxHelper(id, toMatch, top, "td", res, "id").cnt;
	if (retVal != -1) return retVal;
}
function getBlankResult(){
	var res = new Object();
	res.cnt = -1;
	res.found = false;    
	res.element = null;
	return res;
}

function getArrayNameAndIndex(id){
	var o = new Object();
    if (id.match(/(.*)\[([0-9]*)\]$/)){
        o.name = RegExp.$1;
        o.index = parseInt(RegExp.$2);
    }else{
    	o.name = id;
    	o.index = -1;
    }
    return o;
}
function findInForms(id, win, type){
    var fms = win.document.forms;
    if (fms == null) return null;
    for (var j=0; j<fms.length; j++){
        var el = findInForm(id, fms[j], type);
        if (el != null) return el;
    }
    return null;
}
function findInForm(name, fm, type){
    var els = fm.elements;
    var matchedEls = new Array();
    for (var i=0; i<els.length; i++){
        var el = els[i];
        if (el.name == name && el.type && el.type==type){
            matchedEls[matchedEls.length] = el;
        }
        else if ((el.type == "button" || el.type == "submit") && el.value == name && el.type==type){
            matchedEls[matchedEls.length] = el;
        }
    }
    return (matchedEls.length > 0) ? (matchedEls.length == 1 ? matchedEls[0] : matchedEls ) : null;
}

function sahiFindTableIx(id, toMatch){
	var res = getBlankResult();
	var retVal = sahiFindTagIxHelper(id, toMatch, top, "table", res, (id?"id":null)).cnt;
	if (retVal != -1) return retVal;
}

function sahiFindTable(id){
	var res = getBlankResult();
	return sahiFindTagHelper(id, top, "table", res, "id").element;
}

function sahiFindResByIndexInList(ix, win, type, res){
    var tags = win.document.getElementsByTagName(type);
    if (tags[ix - res.cnt]) {
    	res.element = tags[ix - res.cnt];
    	res.found = true;
    	return res;
    }
	res.cnt += tags.length;
    var frs = win.frames;
    if (frs){
        for (var j=0; j<frs.length; j++){
            res = sahiFindResByIndexInList(ix, frs[j], type, res);
            if (res && res.found) return res;
        }
    }  
    return res;  
}


function sahiFindTagHelper(id, win, type, res, param){
	if ((typeof id) == "number"){
		res.cnt = 0;
		res = sahiFindResByIndexInList(id, win, type, res);
		return res;
	}else{
		var o = getArrayNameAndIndex(id);
	    var ix = o.index;
	    var fetch = o.name;
	    var tags = win.document.getElementsByTagName(type);
	    if (tags){
		    for (var i=0; i<tags.length; i++){
		        if (sahiAreEqual(tags[i], param, fetch)){
		        	res.cnt++;
		        	if (res.cnt == ix || ix == -1){
			        	res.element = tags[i];
		        		res.found = true;
		        		return res;
		        	}
		        }
		    }
	    }
	}    
    
    var frs = win.frames;
    if (frs){
        for (var j=0; j<frs.length; j++){
            res = sahiFindTagHelper(id, frs[j], type, res, param);
			if (res && res.found) return res;
        }
    }
    return res;
}
function sahiFindTagIxHelper(id, toMatch, win, type, res, param){
	if (res && res.found) return res;
	
    var tags = win.document.getElementsByTagName(type);
    if (tags){
	    for (var i=0; i<tags.length; i++){
	        if (param == null || sahiAreEqual(tags[i], param, id)){
	        	res.cnt++;
	        	if (tags[i] == toMatch){
	        		res.found = true;
	        		return res;
	        	}
	        }
	    }
    }
    var frs = win.frames;
    if (frs){
        for (var j=0; j<frs.length; j++){
            res = sahiFindTagIxHelper(id, toMatch, frs[j], type, res, param);
			if (res && res.found) return res;
        }
    }
    return res;
}
function sahiCanSimulateClick(el){
	return (el.click || el.dispatchEvent);
}

function sahiIsRecording(){
	if (top._isSahiRecording == null)
		top._isSahiRecording = sahiGetServerVar("sahi_record") == "1";
	return top._isSahiRecording;
}
function sahiCreateCookie(name,value,days)
{
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

function sahiReadCookie(name)
{
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++)
	{
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

function sahiEraseCookie(name)
{
	sahiCreateCookie(name,"",-1);
}
function sahi_event(type, keyCode){
    this.type = type;
    this.keyCode = keyCode;
}function SahiAssertionException(msgNum, msgText) {
	this.messageNumber = msgNum;
	this.messageText = msgText;
	this.exceptionType = "SahiAssertionException";
}
function SahiNotMyWindowException(){
}
var lastQs = "";
var lastTime = 0;
function sahiOnEv(e){
	if (e.handled == true) return true; //FF
	if (sahiGetServerVar("sahiEvaluateExpr") == "true") return true;
	var targ = getTarget(e);
	if (e.type == "click" ){
		if (targ.form && targ.type){
			var type = targ.type;
			if (type == "text" || type == "textarea" || type == "password" 
			|| type == "select-one" || type == "select-multiple") return true;
		}
	}
	var info = sahiGetAccessorInfo(targ);
	var cmd = getScript(info);
	if (cmd == null) return true;
	if (sahiHasEventBeenRecorded(cmd)) return true; //IE
    sahiSendToServer('/_s_/dyn/Recorder_record?cmd='+escape(cmd));
    e.handled = true; //FF
	showInController(info);
    return true;
}
function showInController(info){
	try{
		var c = getSahiWinHandle();
		if (c) {
			var d = c.top.main.document.currentForm.debug;
			c.top.main.document.currentForm.history.value += "\n"+d.value;
			d.value=getScript(info);
//			d.value+="\n"+getScript(info);
//			d.scrollTop = d.scrollHeight;			
		}
	}catch(ex2){
//		throw ex2;
	}
}
function sahiHasEventBeenRecorded(qs){
	var now = (new Date()).getTime();
	if (qs == lastQs && (now - lastTime)<500 ) return true;
	lastQs = qs;
	lastTime = now;
	return false;
}
function getPopupName(){
	if (window.top.opener != null && window.top.opener != window.top) {
		return top.name;
	}
	return "";
}
function addWait(time){
    var val = parseInt(time);
    if ((""+val) == "NaN" || val < 200) throw new Error();
    showInController(new AccessorInfo("", "", "", "wait", time));
//    sahiSendToServer('/_s_/dyn/Recorder_record?event=wait&value='+val);
}
function mark(s){
    showInController(new AccessorInfo("", "", "", "mark", s));
}
function doAssert(e){
    try{
        if (!top._lastAccessedInfo) return;
        top._lastAccessedInfo.event = "assert";
        showInController(top._lastAccessedInfo);
//      sahiSendToServer('/_s_/dyn/Recorder_record?'+getSahiPopUpQS()+sahiGetAccessorInfoQS(top._lastAccessedInfo, true));
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
	if (el == null) return null;
    var type = el.type;
    var accessor = sahiGetAccessor(el);
    var shortHand = getShortHand(el, accessor);
//    alert(type+" -- "+accessor+" --- "+shortHand);
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
        return new AccessorInfo(accessor, shortHand, type, "click", el.checked);
    }else if (el.tagName.toLowerCase() == "td"){
        return new AccessorInfo(accessor, shortHand, "cell", "click", sahiIsIE()? el.innerText : el.textContent);
    }else if (el.tagName.toLowerCase() == "div" || el.tagName.toLowerCase() == "span"){
    	return new AccessorInfo(accessor, shortHand, "byId", "click", sahiIsIE()? el.innerText : el.textContent);
    }
}

function getShortHand(el, accessor){
    var shortHand = "";
    try{
    	var tagLC = el.tagName.toLowerCase();
        if (tagLC == "img"){
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
        }else if (tagLC == "a"){
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
        }else if (tagLC == "button" || tagLC == "input" || tagLC == "textarea" || tagLC.indexOf("select") != -1){
        	if (el.type == "button" || el.type == "submit") shortHand = el.value;
        	if (el.type == "image")	shortHand = el.alt;
            else if (!shortHand || shortHand=="") shortHand = el.name;
            if (!shortHand || shortHand=="") shortHand = el.id;
            if (shortHand && shortHand!=""){
	            if (sahiFindElement(shortHand, el.type, tagLC) != el){
	            	var ix = sahiFindElementIx(shortHand, el, el.type, tagLC);
	            	if (ix == -1) return "";
					return shortHand + "["+ ix +"]";
	            }            
            }else{
	            var ix = sahiFindElementIx(null, el, el.type, tagLC);
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
        }else if (el.tagName.toLowerCase() == "span" || el.tagName.toLowerCase() == "div"){
        	shortHand = el.id;
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
	if (ai == null || ai.event==null) return;
    var s = "event="+ (isAssert? "assert" : ai.event);
    s += "&accessor="+escape(sahiConvertUnicode(ai.accessor));
    s += "&shorthand="+escape(sahiConvertUnicode(ai.shortHand));
    s += "&type="+ai.type;
    if (ai.value){
        s += "&value="+escape(sahiConvertUnicode(ai.value));
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
function removeEvent(el, ev, fn){
	if (!el) return;
	if (el.attachEvent){
		el.detachEvent("on"+ev, fn);
	}else if (el.removeEventListener){
		el.removeEventListener(ev, fn, false);
	}	
}
function sahiSetRetries(i){
    sahiSetServerVar("sahi_retries", i);
}
function sahiGetRetries(){
    var i = parseInt(sahiGetServerVar("sahi_retries"));
    return (""+i != "NaN") ? i : 0;
}
function sahiOnError(msg, url, lno){
    var debugInfo = "Javascript error on page";
    if (msg && msg.indexOf("Access to XPConnect service denied") != -1){ //FF hack    
    	sahiLogPlayBack("msg: "+msg+"\nurl: "+url+"\nLine no: "+lno, "info", debugInfo);
    }
	else sahiLogPlayBack("msg: "+msg+"\nurl: "+url+"\nLine no: "+lno, "error", debugInfo);
}
window.onerror=sahiOnError;
var _sahiControl;
function sahiOpenWin(e){
try{
    if (!e) e = window.event;
    top._sahiControl = window.open("", "_sahiControl", getWinParams(e));
    var diffDom = false;
    try{
    	var checkDiffDomain = top._sahiControl.document.domain;
    }catch(domainInaccessible){
	    diffDom = true;
    }
    if (diffDom || !top._sahiControl.isOpen){
	    top._sahiControl = window.open("/_s_/spr/controller2.htm", "_sahiControl", getWinParams(e));
	}
    if (top._sahiControl) top._sahiControl.opener = this;
    if (e) top._sahiControl.focus();
}catch(ex){sahiHandleException(ex);}
}
function getWinParams(e){
    var x = e ? e.screenX-40 : 500;
    var y = e ? e.screenY-60 : 100;
    var positionParams = "";
    if (e){
        if (sahiIsIE()) positionParams = ",screenX="+x+",screenY="+y;
        else positionParams = ",screenX="+x+",screenY="+y;
    }
    return "height=520px,width=460px,resizable=yes, toolbars=no"+positionParams;
}
function getSahiWinHandle(){
    if (top._sahiControl && !top._sahiControl.isClosed) return top._sahiControl;
}
function sahiOpenControllerWindow(e){
	if (!e) e = window.event;
    if (!sahiIsHotKeyPressed(e)) return true;
    sahiOpenWin(e);
    return true;
}
function sahiIsHotKeyPressed(e){
	return ((sahiHotKey == "SHIFT" && e.shiftKey)
		||(sahiHotKey == "CTRL" && e.ctrlKey)
		||(sahiHotKey == "ALT" && e.altKey)
		||(sahiHotKey == "META" && e.metaKey));
}
var _lastAccessedInfo;
function sahiMouseOver(e){
    try{
	if (getTarget(e) == null) return;
    if (!e.ctrlKey) return;
      var controlWin = getSahiWinHandle();
      if (controlWin){
        controlWin.main.displayStepNum();
        var acc = sahiGetAccessorInfo(sahiGetKnownTags(getTarget(e)));
        try{
        	if (acc) controlWin.main.displayInfo(acc, getAccessor1(acc), sahiConvertUnicode(acc.value));
        }catch(ex2){}
        top._lastAccessedInfo = acc ? acc : top._lastAccessedInfo;
      }
    }catch(ex){throw ex}
}

function getAccessor1(info){
    if ("" == info.shortHand || info.shortHand == null) {
        return info.accessor;
    } else {
        if ("image" == info.type) {
            return "_imageSubmitButton(" + escapeForScript(info.shortHand) + ")";
        } else if ("img" == info.type) {
            return "_image(" + escapeForScript(info.shortHand) + ")";
        } else if ("link" == info.type) {
            return "_link(" + escapeForScript(info.shortHand) + ")";
        } else if ("select-one" == info.type || "select-multiple" == info.type) {
            return "_select(" + escapeForScript(info.shortHand) + ")";
        } else if ("text" == info.type) {
            return "_textbox(" + escapeForScript(info.shortHand) + ")";
        } else if ("cell" == info.type) {
            return "_cell(" + info.shortHand + ")";
        }
        return "_" + info.type + "(" + escapeForScript(info.shortHand) + ")";
    }
}
function escapeForScript(s){
	return sahiQuoteIfString(s);
}
var _key;
var KEY_SHIFT = 16;
var KEY_CONTROL = 17;
var KEY_ALT = 18;
var KEY_Q = 81;
var KEY_K = 75;

var IDLE_INTERVAL=1000;
var INTERVAL=50;
var RETRY_INTERVAL=1000;
var MAX_RETRIES=3;

var _sahiCmds = new Array();
var _sahiCmdDebugInfo = new Array();
var _sahi_wait = -1;

function sahiSchedule(cmd, debugInfo){
	if (!_sahiCmds) return;
    var i = _sahiCmds.length;
	_sahiCmds[i] = cmd;
	_sahiCmdDebugInfo[i] = debugInfo;
}

function sahiPlay(){
   window.setTimeout("try{sahiEx();}catch(ex){}", INTERVAL);
}
function areWindowsLoaded(win){
	try{
		if (win.location.href == "about:blank") return true;
	}catch(e){
		return true; // diff domain
	}
	try{
		var fs = win.frames;
		if (!fs || fs.length == 0){
			try{
				return win.sahiLoaded;
			}catch(e){
				return true; //diff domain; don't bother
			}
		}else{
			for (var i=0; i<fs.length; i++){
				if (!areWindowsLoaded(fs[i])) return false;
			}
			if (win.document && win.document.getElementsByTagName("frameset").length == 0) 
				return win.sahiLoaded;
			else return true;
		}	
	}
	catch(ex){
		sahiLogErr("2 to "+typeof ex);
		sahiLogErr("3 pr "+ex.prototype);	
		return true;//for diff domains.
	}
}
var SAHI_MAX_WAIT_FOR_LOAD = 5;
var sahiWaitForLoad = SAHI_MAX_WAIT_FOR_LOAD;
var interval = INTERVAL;
function sahiEx(isStep){
    try{
        try{
        	if (isPaused() && !isStep) return;
            var i=sahiGetCurrentIndex();
            if (isSahiPlaying() && _sahiCmds.length == i){
                sahiStopPlaying();
                return;
            }
            if ((isStep || isSahiPlaying()) && _sahiCmds[i]!=null){
				if (!areWindowsLoaded(top) && sahiWaitForLoad>0){
					sahiWaitForLoad-- ;
					if (!isStep) window.setTimeout("try{sahiEx();}catch(ex){}", interval);
					return;
				}
                try{
	                sahiWaitForLoad = SAHI_MAX_WAIT_FOR_LOAD;
	                updateControlWinDisplay(_sahiCmds[i]);
	                var debugInfo = ""+_sahiCmdDebugInfo[i];
	                var level = (_sahiCmds[i].indexOf("sahi_assert") == 0)?"success":"info";
	                try{
	                	sahiSetCurrentIndex(i+1); 
                		eval(_sahiCmds[i]);
                	}catch(e){
                		sahiSetCurrentIndex(i); 
                		throw e;
                	}
	                sahiReportSuccess(_sahiCmds[i], level, debugInfo);
//	                sahiSetRetries(0); // _sahi_attempts = 0;
                }catch (ex1){
                    if (ex1 instanceof SahiAssertionException) {
                    	var retries = sahiGetRetries();
			            if (retries < MAX_RETRIES/2){
			                sahiSetRetries(retries+1);
			                interval = IDLE_INTERVAL;
							if (!isStep) window.setTimeout("try{sahiEx();}catch(ex){}", interval);
							return;			                
			            }else{
	                        debugInfo = ""+_sahiCmdDebugInfo[i];
	                        var msg = " Assertion Failed. " + (ex1.messageText ? ex1.messageText : "");
	                        sahiLogPlayBack(_sahiCmds[i] + msg, "failure", debugInfo);
	                        sahiSetRetries(0);
	                        sahiSetCurrentIndex(i+1); 
	                    }
                    }else if (ex1 instanceof SahiNotMyWindowException){
                    	throw ex1;
                    }else {
	                    throw ex1;
                    }
                }
                interval = _sahi_wait > 0 ? _sahi_wait : INTERVAL;
                _sahi_wait = -1;
            }
            else{
                return;
            }
        }catch(ex){
        	var retries = sahiGetRetries();
            if (retries < MAX_RETRIES){
                sahiSetRetries(retries+1);
                interval = IDLE_INTERVAL;
            }
            else {
                var debugInfo = ""+_sahiCmdDebugInfo[i];
                sahiLogPlayBack(_sahiCmds[i], "error", debugInfo);
                sahiStopPlaying();
            }
        }
        if (!isStep) window.setTimeout("try{sahiEx();}catch(ex){}", interval);
    }catch(ex2){
        if (isSahiPlaying()){
            if (!isStep) window.setTimeout("try{sahiEx();}catch(ex){sahi_real_alert(ex)}", 1000);
        }
    }
}
function canEvalInBase(cmd){
	return  (top.opener == null && !isForPopup(cmd)) || (top.opener && top.opener.top == top);
}
function isForPopup(cmd){
	return cmd.indexOf("sahi_popup") == 0;
}
function canEval(cmd){
	return (top.opener == null && !isForPopup(cmd)) // for base window
            || (top.opener && top.opener.top == top) // for links in firefox
            || (top.opener != null && isForPopup(cmd)); // for popups
}
function pause(){
	top._isSahiPaused = true;
	sahiSetServerVar("sahi_paused", 1);
}
function unpause(){
	top._isSahiPaused = false;
	sahiSetServerVar("sahi_paused", 0);
	sahiSetServerVar("sahi_play", 1);
	top._isSahiPlaying = true;
}
function isPaused(){
	if (top._isSahiPaused == null)
		top._isSahiPaused = sahiGetServerVar("sahi_paused") == "1";
	return top._isSahiPaused;
}
function updateControlWinDisplay(s){
	try{
		if (window.status) window.status = s;
	}catch(ex){}
	
    try{
      var controlWin = getSahiWinHandle();
      if (controlWin && !controlWin.closed){
        controlWin.main.displayStepNum();
        controlWin.main.displayLogs(s);
      }
    }catch(ex){}
}
function sahiSetCurrentIndex(i){
    sahiSetServerVar("sahiIx", i);
}
function sahiGetCurrentIndex(){
    var i = parseInt(sahiGetServerVar("sahiIx"));
    return (""+i != "NaN") ? i : 0;
}
function isSahiPlaying(){
	if (top._isSahiPlaying == null)
		top._isSahiPlaying = sahiGetServerVar("sahi_play")=="1";
    return top._isSahiPlaying;
}
function sahiStartPlaying(){
	sahiSendToServer("/_s_/dyn/Player_start");
	sahiSetServerVar("sahi_play", 1);
//	top._isSahiPlaying = true;
}
function sahiStopPlaying(){
	sahiSendToServer("/_s_/dyn/Player_stop");
	sahiSetServerVar("sahi_play", 0);
	updateControlWinDisplay("--Stopped Playback--");
	top._isSahiPlaying = false;
}
function sahiStartRecording(){
	top._isSahiRecording = true;
   	sahiAddHandlersToAllFrames(top);
}
function sahiStopRecording(){
	top._isSahiRecording = false;
	sahiSendToServer("/_s_/dyn/Recorder_stop");
	sahiSetServerVar("sahi_record", 0);
}
function sahiReportSuccess(msg, type, debugInfo){
	sahiSendToServer("/_s_/dyn/Player_success?msg=" + escape(msg) + "&type=" + type + "&debugInfo=" + (debugInfo?escape(debugInfo):""));
}
function sahiLogPlayBack(msg, type, debugInfo){
	sahiSendToServer("/_s_/dyn/Log?msg=" + escape(msg) + "&type=" + type + "&debugInfo=" + (debugInfo?escape(debugInfo):""));
}
function sahiTrim(s){
    if (s==null) return s;
    if ((typeof s) != "string") return s;
    s = s.replace(/&nbsp;/g, ' ');
    s = s.replace(/\xA0/g, ' ');
    s = s.replace(/^[ \t\n\r]*/g, '');
    s = s.replace(/[ \t\n\r]*$/g, '');
    s = s.replace(/[\t\n\r]{1,}/g, ' ');
    return s;
}
function sahiList(el, p){
    var s="";
    var j=0;
    for (var i in el){
        if (!p || (""+i).indexOf(p)!=-1){
            s+=i+"="+el[i]+";<br>";
            j++;
//            if (j%4==0) s+="\n";
        }
    }
    return s;
}
function debug(s){
	var win = window.open("", "_blank");
	win.document.write(s);
}
function arrayCopy(ar1, ar2){
    var ar = new Array();
    for (var i=0; i<ar1.length; i++){
        ar[ar.length] = ar1[i];
    }
    for (var i=0; i<ar2.length; i++){
        ar[ar.length] = ar2[i];
    }
    return ar;
}
function getElementOrArray(ar){
    if (ar && ar.length==1) return ar[0];
    return ar;
}
function sahiFindInArray(ar, el){
    for (var i=0; i<ar.length; i++){
        if (ar[i] == el) return i;
    }
    return -1;
}
function sahiIsIE(){
	var browser = navigator.appName;
	return browser == "Microsoft Internet Explorer";
}
function sahiCreateRequestObject(){
	var obj;
	if(sahiIsIE()){
		obj = new ActiveXObject("Microsoft.XMLHTTP");
	}else{
		obj = new XMLHttpRequest();
	}
	return obj;
}
function sahiGetServerVar(name){
	var v = sahiSendToServer("/_s_/dyn/SessionState_getVar?name="+escape(name));
	if (v == "null") return null;
	return v;
}
function sahiSetServerVar(name, value){
	sahiSendToServer("/_s_/dyn/SessionState_setVar?name="+escape(name)+"&value="+escape(value));
}
function sahiLogErr(msg){
//    return;
	sahiSendToServer("/_s_/dyn/Log?msg=" + escape(msg) + "&type=err" );
}

function sahiGetParentNode(el, tagName){
    var parent = el.parentNode;
    while (parent &&  parent.tagName.toLowerCase() != "body" && parent.tagName.toLowerCase() != "html"){
        if (parent.tagName.toLowerCase() == tagName.toLowerCase()) return parent;
        parent = parent.parentNode;
    }
    return null;
}
function sahiSendToServer(url){
	try{
	    var rand = (new Date()).getTime() + Math.floor(Math.random()*(10000));
	    var http = sahiCreateRequestObject();
	    url = url + (url.indexOf("?")==-1 ? "?" : "&") + "t=" + rand;
		http.open("GET", url, false);
	    http.send(null);
	    return http.responseText;
    }catch(ex){sahiHandleException(ex)}
}
function s_v(v){
    var type = typeof v;
    if (type == "number") return v;
    else if (type == "string") return "\""+v+"\"";
    else return v;
}
function quoted(s){
	return '"' + s.replace(/"/g, '\\"') + '"';
}
function sahiHandleException(e){
//	alert(e);
//	throw e;
}
function sahiGetText(el){
	if (el.innerHTML)
		return sahiGetTextFromHTML(el.innerHTML);
	return null;
}
function sahiGetTextFromHTML(s){
	s = s.replace(/<[^>]*>/g, "");
	s = s.replace(/&amp;/g, "&");
	s = s.replace(/&lt;/g, "<");
	s = s.replace(/&gt;/g, ">");
	s = s.replace(/&nbsp;/g, " ");
	return s;	
}
function sahiConvertUnicode(source){
	if (source == null) return null;
	var result = '';
	for (i=0; i<source.length; i++){
		if (source.charCodeAt(i) > 127)
			result += addSlashU(source.charCodeAt(i).toString(16));
		else result += source.charAt(i);
	}
	return result;
}
function addSlashU(num){
	var buildU
	switch(num.length){
		case 1:
		buildU="\\u000"+num
		break
		case 2:
		buildU="\\u00"+num
		break
		case 3:
		buildU="\\u0"+num
		break
		case 4:
		buildU="\\u"+num
		break
	}
	return buildU;
}

function sahiOnBeforeUnLoad(){
	window.sahiLoaded=false;
}
/*
function trap1(e){
	if (!e) e = window.event;
	if (top._sahiControl) debug(sahiList(e));
	if (prevDown) prevDown(e);
}
var prevDown = null;
*/
function sahiInit(e){
	try{
		window.sahiLoaded = true;	
		sahiActivateHotKey();
	}catch(ex){
	    sahiHandleException(ex);
	}

	try{
	    if (self == top){ 
	        sahiPlay();
	    }
	    if (sahiIsRecording()) sahiAddHandlers();
	}catch(ex){
//		throw ex;
	    sahiHandleException(ex);
	}
}
function sahiActivateHotKey(){
	try{
		addEvent(document, "dblclick", sahiOpenControllerWindow);
		addEvent(document, "mousemove", sahiMouseOver);
	}catch(ex){
	    sahiHandleException(ex);
	}	
}
function sahiIsFirstExecutableFrame(){
	var fs = top.frames;
	for (var i=0; i<fs.length; i++){
		if (self == top.frames[i]) return true;
		if (""+(typeof top.frames[i].location) != "undefined"){ // = undefined when previous frames are not accessible due to some reason (may be from diff domain)
			return false;
		}
	}
	return false;
}
function getScript(info) {
	var accessor = getAccessor1(info);
	if (accessor == null) return null;
	var ev = info.event;
	var value = info.value;
	var type = info.type
	var popup = getPopupName();

	cmd = null;
	if (value == null)
		value = "";
	if (ev == "load") {
		cmd = "_wait(2000);";
	} else if (ev == "click") {
		cmd = "_click(" + accessor + ");";
	} else if (ev == "setvalue") {
		cmd = "_setValue(" + accessor + ", " + sahiConvertUnicode(quoted(value)) +  ");";
	} else if (ev == "setselected") {
		cmd = "_setSelected(" + accessor + ", " + sahiConvertUnicode(quoted(value)) +  ");";
	} else if (ev == "assert") {
		cmd = "_assertNotNull(" + accessor + ");\r\n";
		if (type == "cell") {
			cmd += "_assertEqual(" + quoted(sahiConvertUnicode(value)) +  ", _getCellText(" + accessor + "));";
		} else if (type == "select-one" || type == "select-multiple") {
			cmd += "_assertEqual(" + quoted(sahiConvertUnicode(value)) +  ", _getSelectedText(" + accessor + "));";
		} else if (type == "text" || type == "textarea" || type == "password") {
			cmd += "_assertEqual(" + quoted(sahiConvertUnicode(value)) +  ", " + accessor + ".value);";
		} else if (type == "checkbox" || type == "radio") {
			cmd += "_assert" + ("true" == ""+value ? "":"Not" ) +"True("+accessor + ".checked);";
		}
	} else if (ev == "wait") {
		cmd = "_wait(" + value + ");";
	} else if (ev == "mark") {
		cmd = "//MARK: " + value;
	} 
	if (cmd != null && popup != null && popup != "") {
		cmd = "_popup(\"" + popup + "\")." + cmd;
	}
	return cmd;
}
function sahiQuoteIfString(shortHand) {
	if ((""+shortHand).match(/^[0-9]+$/)) return shortHand;
	return quoted(sahiConvertUnicode(shortHand));
}
sahiActivateHotKey();

window.sahi_real_alert = window.alert;
window.sahi_real_confirm = window.confirm;
window.sahi_real_prompt = window.prompt;

window.alert = sahi_alert;
window.confirm = sahi_confirm;
window.prompt = sahi_prompt;


