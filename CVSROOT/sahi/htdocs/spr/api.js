function sahi_click(el){
    if (el && el.href && el.tagName=="A") sahi_clickLinkByAccessor (el);
    else if (el && el.tagName=="IMG") sahi_clickImage(el);
    else if (el.prevOnClick) el.prevOnClick();
    else if (el.click){
		if (el.type == "submit"){
						
		}
    	el.click();
    }
    else if (el.onclick) el.onclick();
}

function sahi_clickLinkByAccessor(ln){
	var win = sahiGetWin(ln);
    //point(ln);
    if (ln.target==null || ln.target=="") ln.target = "_self";
	if (ln.onclick) ln.onclick();
    if (ln.href.indexOf("javascript:")==0){
        var s = ln.href.substring(11);
        eval(s);
    }else{
        win.open(ln.href, ln.target);
    }
}

function appendSahiSid(url){
   	return url + (url.indexOf("?")==-1 ? "?" : "&") + "sahisid="+sahiReadCookie("sahisid");
}

function sahi_clickImage(el){
    if (el.prevOnClick) el.prevOnClick();
	if (el.click) el.click();
    else {
	    if (el.onclick) el.onclick();
	    else {
			var ln = sahiGetEncapsulatingLink(el);
			if (ln != null && ln != el){
				sahi_clickLinkByAccessor(ln);
			}
		}
	}	
}
// api for link click end

// api for set value start
function sahi_setValue(el, val){
    el.value = val;
    if (el.onchange) el.onchange();
}
function sahi_setSelected(el, val){
    var l = el.options.length;
    for (var i=0; i<l; i++){
        if (el.options[i].text.indexOf(sahiTrim(val)) != -1){
            el.options[i].selected = true;
            if (el.onchange) el.onchange();
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
     return sahiFindElement(n, "button");
}
function sahi_submit(n){
     return sahiFindElement(n, "submit");
}

function sahi_wait(i){
    _sahi_wait = i;
}

function sahi_textbox(n){
    return sahiFindElement(n, "text");

}
function sahi_password(n){
    return sahiFindElement(n, "password");
}
function sahi_checkbox(n){
    return sahiFindElement(n, "checkbox");
}
function sahi_textarea(n){
    return sahiFindElement(n, "textarea");
}
function sahi_accessor(n){
    return eval(n);
}
function sahi_select(n){
    var el = sahiFindElement(n, "select-one");
    if (!el) el = sahiFindElement(n, "select-multiple");
    return el;
}
function sahi_radio(n){
    return sahiFindElement(n, "radio");
}
function sahi_image(n){
    return sahiFindImage(n, top, "img");
}
function sahi_imageSubmitButton(n){
    return sahiFindElement(n, "image");
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
function sahi_alert(s){
	return alert(s);
//	window.open("/_s_/dyn/alert.htm?msg="+s, "", "height=60px,width=460px,resizable=yes,toolbars=no,statusbar=no");
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
function sahi_confirm(s){
	return true;//confirm(s);
//	window.open("/_s_/dyn/confirm.htm?msg="+s, "", "height=60px,width=460px,resizable=yes,toolbars=no,statusbar=no");
}
function sahi_prompt(n){
	return prompt(n);
}
function sahi_cell(id, row, col){
	if (row==null && col==null){
		return sahiFindCell(id);
	}
	return id.rows[row].cells[col];
}
function sahi_table(n){
	return sahiFindTable(n);
}
function sahi_containsHTML(el, htm){
	return el && el.innerHTML && el.innerHTML.indexOf(htm) != -1;
}
function sahi_containsText(el, txt){
	return el && el.innerText && el.innerText.indexOf(txt) != -1;
}
function sahi_popup(n){
	if (top.opener != null && top.name == n){
		sahiSetCurrentIndex(sahiGetCurrentIndex()+1);
		return top;
	}
	return SahiNotMyWindowException();
}

top.sahi_clickLinkByAccessor = sahi_clickLinkByAccessor;
top.sahi_setValue = sahi_setValue;
top.sahi_wait = sahi_wait;
top.sahi_textbox = sahi_textbox;
top.sahi_password = sahi_password;
top.sahi_checkbox = sahi_checkbox;
top.sahi_textarea = sahi_textarea;
top.sahi_select = sahi_select;
top.sahi_radio = sahi_radio;
top.sahi_image = sahi_image;
top.sahi_link = sahi_link;

