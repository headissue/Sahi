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

String.isBlankOrNull = function (s) {
    return (s == "" || s == null);
}

var Sahi = function(){
    this.cmds = new Array();
    this.cmdDebugInfo = new Array();

    this.cmdsLocal = new Array();
    this.cmdDebugInfoLocal = new Array();

    this.waitInterval = -1;

    this.lastAlertText = "";

    this.confirmReturnValue = new Array();
    this.lastConfirmText = null;

    this.promptReturnValue = new Array();
    this.lastPromptText = null;
    this.waitCondition = null;

    this.locals = [];
}
var _sahi = new Sahi();
var tried = false;
var _sahi_top = window.top;
Sahi.prototype.top = function () {
    return _sahi_top;
    //Hack for frames named "top"
}
//getMostAccessibleAncestor = function () {
//    var t = window;
//    while (t != _sahi_top) {
//        p = t.parent;
//        if (p == t) return t;
//        try {
//            var test = p.document.domain;
//        } catch(e) {
//            // diff domain
//            return t;
//        }
//        t = p;
//    }
//    return t;
//}

Sahi.prototype.getAccessor = function (src) {
    var fr = this.getFrame(this.top(), "top");
    var a = this.getPartialAccessor(src);
    if (a == "" || a == null) return a;
    var elStr = fr + ".document." + a;
    var v = this.getArrayElement(elStr, src);
    return v;
}

Sahi.prototype.getKnownTags = function (src) {
    var el = src;
    while (true) {
        if (!el) return src;
        if (!el.tagName || el.tagName.toLowerCase() == "html" || el.tagName.toLowerCase() == "body") return null;
        var tag = el.tagName.toLowerCase();
        if (tag == "a" || tag == "select" || tag == "img" || tag == "form"
                || tag == "input" || tag == "button" || tag == "textarea"
                || tag == "textarea" || tag == "td" || tag == "table"
                || ((tag == "div" || tag == "span") && (el.id && el.id != ""))) return el;
        el = el.parentNode;
    }
}

Sahi.prototype.byId = function (src) {
    var s = src.id;
    if (String.isBlankOrNull(s)) return "";
    return "getElementById('" + s + "')";
}
Sahi.prototype.getPartialAccessor = function (src) {
    if (src == null || src.tagName == null) return null;
    var tag = src.tagName.toLowerCase();
    var a = this.byId(src);
    if (a != "" && eval("document." + a) == src) {
        return a;
    }

    if (tag == "img") {
        return this.getImg(src);
    }
    else if (tag == "a") {
        return this.getLink(src);
    }
    else if (tag == "form") {
        return this.getForm(src);
    }
    else if (tag == "button" || tag == "input" || tag == "textarea" || tag == "select") {
        return this.getFormElement(src);
    }
    else if (tag == "td") {
        return this.getTableCell(src);
    }
    else if (tag == "table") {
        return this.getTable(src);
    }
    return null;
}
Sahi.prototype.getLink = function (src) {
    var lnx = document.links;
    for (var j = 0; j < lnx.length; j++) {
        if (lnx[j] == src) {
            return "links[" + j + "]";
        }
    }
    return  null;
}
Sahi.prototype.getImg = function (src) {
    var lnx = document.images;
    for (var j = 0; j < lnx.length; j++) {
        if (lnx[j] == src) {
            return "images[" + j + "]";
        }
    }
    return  null;
}

Sahi.prototype.getForm = function (src) {
    if (!String.isBlankOrNull(src.name) && this.nameNotAnInputElement(src)) {
        return "forms['" + src.name + "']";
    }
    var fs = document.forms;
    for (var j = 0; j < fs.length; j++) {
        if (fs[j] == src) {
            return "forms[" + j + "]";
        }
    }
    return null;
}
Sahi.prototype.nameNotAnInputElement = function (src) {
    return (typeof src.name != "object");
}
Sahi.prototype.getFormElement = function (src) {
    return this.getByTagName(src);
    /*
    if (!String.isBlankOrNull(src.name)){
        n = 'elements["'+src.name+'"]';
    }else {
        var els = src.form.elements;
        for (var j=0; j<els.length; j++){
            if (els[j] == src){
                n = "elements["+j+"]";
            }
        }
    }
    var f = this.getForm(src.form);
    return (n == "") ? f : f+"."+n;
    */
}

Sahi.prototype.getByTagName = function (src) {
    var tagName = src.tagName.toLowerCase();
    var els = document.getElementsByTagName(tagName);
    return "getElementsByTagName('" + tagName + "')[" + this.findInArray(els, src) + "]";
}

Sahi.prototype.getTable = function (src) {
    var tables = document.getElementsByTagName("table");
    if (src.id && src.id != null && src == document.getElementById(src.id)) {
        return "getElementById('" + src.id + "')";
    }
    return "getElementsByTagName('table')[" + this.findInArray(tables, src) + "]";
}

Sahi.prototype.getTableCell = function (src) {
    var tables = document.getElementsByTagName("table");
    var row = this.getRow(src);
    if (row.id && row.id != null && row == document.getElementById(row.id)) {
        return "getElementById('" + row.id + "').cells[" + src.cellIndex + "]";
    }
    var table = this.getTableEl(src);
    if (table.id && table.id != null && table == document.getElementById(table.id)) {
        return "getElementById('" + table.id + "').rows[" + this.getRow(src).rowIndex + "].cells[" + src.cellIndex + "]";
    }
    return "getElementsByTagName('table')[" + this.findInArray(tables, this.getTableEl(src)) + "].rows[" + this.getRow(src).rowIndex + "].cells[" + src.cellIndex + "]";
}

Sahi.prototype.getRow = function (src) {
    return this.getParentNode(src, "tr");
}

Sahi.prototype.getTableEl = function (src) {
    return this.getParentNode(src, "table");
}

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
}

Sahi.prototype.getEncapsulatingLink = function (src) {
    var el = src;
    while (el && el.tagName && el.tagName.toLowerCase() != "a") {
        el = el.parentNode;
    }
    return el;
}

Sahi.prototype.getFrame = function (win, s) {
    if (win == self) return s;
    var frs = win.frames;
    for (var j = 0; j < frs.length; j++) {
        var n = frs[j].name;
        if (String.isBlankOrNull(n)) n = "frames[" + j + "]";
        var sub = this.getFrame(frs[j], n);
        if (sub != null) {
            return s + "." + sub;
        }
    }
    return null;
}

var linkClick = function (e) {
    var performDefault = true;
    if (this.prevClick) {
        performDefault = this.prevClick.apply(this, arguments);
    }
    if (performDefault != false) {
        _sahi.navigateLink(this);
    }
}

Sahi.prototype._dragDrop = function (draggable, droppable) {
    this.checkNull(draggable);
    this.checkNull(droppable);
    this.simulateMouseEvent(draggable, "mousedown");
    draggable.style.left = this.findPosX(droppable) - this.findPosX(draggable) + 2;
    draggable.style.top = this.findPosY(droppable) - this.findPosY(draggable) + 2;
    draggable.style.zIndex = 1;
    this.simulateMouseEvent(droppable, "mousedown");

    this.simulateMouseEvent(draggable, "mousemove");
    this.simulateMouseEvent(droppable, "mousemove");

    this.simulateMouseEvent(draggable, "mouseup");
    this.simulateMouseEvent(droppable, "mouseup");
}
Sahi.prototype.checkNull = function (el) {
    if (el == null) {
        el.forceNullPointerException();
    }
}
Sahi.prototype._click = function (el) {
    this.checkNull(el);
    this.simulateClick(el, false, false);
}

Sahi.prototype._doubleClick = function (el) {
    this.checkNull(el);
    this.simulateClick(el, false, true);
}

Sahi.prototype._rightClick = function (el) {
    this.checkNull(el);
    this.simulateClick(el, true, false);
}

Sahi.prototype._mouseOver = function (el) {
    this.checkNull(el);
    this.simulateMouseEvent(el, "mousemove");
    this.simulateMouseEvent(el, "mouseover");
}

Sahi.prototype._keyPress = function (el, c, combo) {
    var prev = el.value ? el.value : null;
    this.simulateMouseEvent(el, "focus");
    this.simulateKeyEvent(c, el, "keydown", combo);
    this.simulateKeyEvent(c, el, "keypress", combo);
    if (prev && (prev + c != el.value)) {
        //                    if (!el.maxLength || el.value.length < el.maxLength)
        el.value += c;
    }
    this.simulateKeyEvent(c, el, "keyup", combo);
}

Sahi.prototype._focus = function (el) {
    this.simulateMouseEvent(el, "focus");
}

Sahi.prototype._keyDown = function (el, c, combo) {
    this.simulateKeyEvent(c, el, "keydown", combo);
}

Sahi.prototype._keyUp = function (el, c, combo) {
    this.simulateKeyEvent(c, el, "keyup", combo);
}


Sahi.prototype._readFile = function (fileName) {
    var qs = "fileName=" + fileName;
    return this._callServer("net.sf.this..plugin.FileReader_contents", qs)
}
Sahi.prototype._getDB = function (driver, jdbcurl, username, password) {
    return new this.dB(driver, jdbcurl, username, password);
}
Sahi.prototype.dB = function (driver, jdbcurl, username, password) {
    this.driver = driver;
    this.jdbcurl = jdbcurl;
    this.username = username;
    this.password = password;
    this.select = function (sql) {
        var qs = "driver=" + this.driver + "&jdbcurl=" + this.jdbcurl + "&username=" + this.username + "&password=" + this.password + "&sql=" + sql;
        return eval(this._callServer("net.sf.this..plugin.DBClient_select", qs));
    }
    this.update = function (sql) {
        var qs = "driver=" + this.driver + "&jdbcurl=" + this.jdbcurl + "&username=" + this.username + "&password=" + this.password + "&sql=" + sql;
        return eval(this._callServer("net.sf.this..plugin.DBClient_execute", qs));
    }
}

Sahi.prototype.simulateClick = function (el, isRight, isDouble) {
    var n = el;

    if (this.isIE() && !isRight) {
        if (el && ((el.type && (el.type == "submit" || el.type == "button" || el.type == "reset" || el.type == "image" || el.type == "checkbox" || el.type == "radio")))) {
            return el.click();
        }
    }

    while (n != null) {
        if (n.tagName && n.tagName == "A") {
            n.prevClick = n.onclick;
            n.onclick = linkClick;
        }
        n = n.parentNode;
    }

    this.simulateMouseEvent(el, "mousemove");
    this.simulateMouseEvent(el, "focus");
    this.simulateMouseEvent(el, "mouseover");
    this.simulateMouseEvent(el, "mousedown", isRight);
    this.simulateMouseEvent(el, "mouseup", isRight);
    if (isRight) {
        this.simulateMouseEvent(el, "contextmenu", isRight, isDouble);
    } else {
        try {
            this.simulateMouseEvent(el, "click", isRight, isDouble);
            if (this.isSafariLike()) {
                try {
                    if (el.onclick) el.onclick();
                    if (el.parentNode.tagName == "A") {
                        el.parentNode.onclick();
                    }
                } catch(ex) {
                    this._debug(ex.message);
                }
                if (el.form) {
                    if (typeof el.checked == "boolean") {
                        el.checked = (el.type == "radio") ? true : !el.checked;
                    } else if (el.type == "submit") {
                        var goOn = el.form.onsubmit();
                        if (goOn != false) {
                            el.form.submit();
                            this.onBeforeUnLoad();
                        }
                    }
                }
            }
        } catch(e) {
        }
    }
    this.simulateMouseEvent(el, "blur");
    n = el;
    while (n != null) {
        if (n.tagName && n.tagName == "A") {
            n.onclick = n.prevClick;
        }
        n = n.parentNode;
    }
}
Sahi.prototype.isSafariLike = function () {
    return /Konqueror|Safari|KHTML/.test(navigator.userAgent);
}
Sahi.prototype.simulateMouseEvent = function (el, type, isRight, isDouble) {
    var x = this.findPosX(el);
    var y = this.findPosY(el);
    if (document.createEvent) {
        if (this.isSafariLike()) {
            var evt = el.ownerDocument.createEvent('HTMLEvents')
            evt.initEvent(type, true, true);
            el.dispatchEvent(evt);
        }
        else {
            // FF
            var evt = el.ownerDocument.createEvent("MouseEvents");
            evt.initMouseEvent(
                    (isDouble ? "dbl" : "") + type,
                    true, //can bubble
                    true,
                    el.ownerDocument.defaultView,
                    (isDouble ? 2 : 1),
                    x, //screen x
                    y, //screen y
                    x, //client x
                    y, //client y
                    false,
                    false,
                    false,
                    false,
                    isRight ? 2 : 0,
                    null);
            el.dispatchEvent(evt);
        }
    } else {
        // IE
        var evt = el.ownerDocument.createEventObject();
        evt.clientX = x;
        evt.clientY = y;
        el.fireEvent("on" + (isDouble ? "dbl" : "") + type, evt);
        evt.cancelBubble = true;
    }
}
Sahi.pointTimer = 0;
Sahi.prototype._highlight = function (el) {
    var d = this.findElementById(this.top(), "sahi_pointer_div");
    d.innerHTML = "<span style='color:red;font-family:verdana;font-size:20px;'>&raquo;</span>";
    d.style.position = "absolute";
    var x = this.findPosX(el) - 10;
    var y = this.findPosY(el) - 8;
    d.style.left = x + "px";
    d.style.top = y + "px";
    d.style.zIndex = 10;
    d.style.display = "block";
    Sahi.pointTimer = window.setTimeout("this.fade()", 2000);
    window.scrollTo(x, y);
}
Sahi.prototype.fade = function () {
    window.clearTimeout(Sahi.pointTimer);
    var d = this.findElementById(this.top(), "sahi_pointer_div");
    d.style.position = "absolute";
    d.style.left = "0px";
    d.style.top = "0px";
    d.style.zIndex = 0;
    d.style.display = "none";
}
Sahi.prototype.findPosX = function (obj)
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

Sahi.prototype.findPosY = function (obj)
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

Sahi.prototype.navigateLink = function (ln) {
    if (!ln) return;
    var win;
    if (this.isSafariLike()) {
        win = this.getWin(ln);
    } else {
        win = ln.ownerDocument.defaultView;
        //FF
        if (!win) win = ln.ownerDocument.parentWindow; //IE
    }
    if (ln.href.indexOf("javascript:") == 0) {
        var s = ln.href.substring(11);
        win.eval(unescape(s));
    } else {
        var target = ln.target;
        if (ln.target == null || ln.target == "") target = "_self";
        if (this.isSafariLike()) {
            var targetWin = win.open("", target);
            try {
                targetWin._sahi.onBeforeUnLoad();
            } catch(e) {
                this._debug(e.message);
            }
            targetWin.location.href = ln.href;
        }
        else win.open(ln.href, target);
    }
}

Sahi.prototype.getClickEv = function (el) {
    var e = new Object();
    if (this.isIE()) el.srcElement = e;
    else e.target = el;
    e.stopPropagation = this.noop;
    return e;
}

Sahi.prototype.noop = function () {
}

// api for link click end

// api for set value start
Sahi.prototype._setValue = function (el, val) {
    val = "" + val;
    var prevVal = el.value;
    if (!document.createEvent) el.value = val;
    if (el.type && el.type.indexOf("select") != -1) {
    } else {
        var append = false;
        el.value = "";
        if (typeof val == "string") {
            for (var i = 0; i < val.length; i++) {
                var c = val.charAt(i);
                this.simulateKeyEvent(c, el, "keydown");
                this.simulateKeyEvent(c, el, "keypress");
                if (i == 0 && el.value != c) {
                    append = true;
                }
                if (append) {
                    //                    if (!el.maxLength || el.value.length < el.maxLength)
                    el.value += c;
                }
                this.simulateKeyEvent(c, el, "keyup");
            }
        }
    }
    if (prevVal != val && el.onchange) {
        this.simulateEvent(el, "change");
    }
}

Sahi.prototype._setFile = function (el, v, url) {
    //    this._debug(el.ownerDocument.defaultView.location.href)
    if (!url) url = (String.isBlankOrNull(el.form.action) || (typeof el.form.action != "string")) ? el.ownerDocument.defaultView.location.href : el.form.action;
    if (url && (q = url.indexOf("?")) != -1) url = url.substring(0, q)
    this._callServer("FileUpload_setFile", "n=" + el.name + "&v=" + v + "&action=" + escape(url));
}

Sahi.prototype.simulateEvent = function (target, evType) {
    if (document.createEvent) {
        var evt = new Object();
        evt.type = evType;
        evt.bubbles = true;
        evt.cancelable = true;
        if (!target) return;
        var event = target.ownerDocument.createEvent("HTMLEvents");
        event.initEvent(evt.type, evt.bubbles, evt.cancelable);
        target.dispatchEvent(event);
    } else {
        var evt = target.ownerDocument.createEventObject();
        evt.type = evType;
        evt.bubbles = true;
        evt.cancelable = true;
        evt.cancelBubble = true;
        target.fireEvent("on" + evType, evt);
    }
}

Sahi.prototype.simulateKeyEvent = function (c, target, evType, combo) {
    var x = this.findPosX(target);
    var y = this.findPosY(target);

    if (document.createEvent) {
        if (this.isSafariLike()) {
            var event = target.ownerDocument.createEvent('HTMLEvents')
            event.initEvent(evType, false, false);
            target.dispatchEvent(event);
        } else {
            var evt = new Object();
            evt.type = evType;
            evt.bubbles = true;
            evt.cancelable = true;
            evt.ctrlKey = combo == "CTRL";
            evt.altKey = combo == "ALT";
            evt.metaKey = combo == "META";
            evt.charCode = c.charCodeAt(0);
            evt.shiftKey = combo == "SHIFT" || c.toUpperCase().charCodeAt(0) == evt.charCode;

            if (!target) return;
            var event = target.ownerDocument.createEvent("KeyEvents");
            event.initKeyEvent(evt.type, evt.bubbles, evt.cancelable, target.ownerDocument.defaultView,
                    evt.ctrlKey, evt.altKey, evt.shiftKey, evt.metaKey, evt.keyCode, evt.charCode);
            target.dispatchEvent(event);
        }
    } else {
        var evt = target.ownerDocument.createEventObject();
        evt.type = evType;
        evt.bubbles = true;
        evt.cancelable = true;
        evt.clientX = x;
        evt.clientY = y;
        evt.ctrlKey = combo == "CTRL";
        evt.altKey = combo == "ALT";
        evt.metaKey = combo == "META";
        evt.keyCode = c.charCodeAt(0);
        evt.charCode = c.charCodeAt(0);
        evt.shiftKey = c.toUpperCase().charCodeAt(0) == evt.charCode;
        evt.shiftLeft = combo == "SHIFT" || c.toUpperCase().charCodeAt(0) == evt.charCode;
        evt.cancelBubble = true;
        target.fireEvent("on" + evType, evt);
    }
}

Sahi.prototype._setSelected = function (el, val, isMultiple) {
    var l = el.options.length;
    var done = false;
    for (var i = 0; i < l; i++) {
        if (!isMultiple) el.options[i].selected = false;
        if (this.areEqual(el.options[i], "text", val)) {
            el.options[i].selected = true;
            done = true;
            this.simulateEvent(el, "change");
        }
    }
    if (!done) throw new Error();
}
// api for set value end
Sahi.prototype._check = function (el, val) {
    el.checked = val;
    if (el.onclick) el.onclick();
}

Sahi.prototype._button = function (n) {
    var el = this.findElement(n, "button", "input");
    if (el == null) el = this.findElement(n, "button", "button");
    return el;
}
Sahi.prototype._reset = function (n) {
    var el = this.findElement(n, "reset", "input");
    if (el == null) el = this.findElement(n, "reset", "button");
    return el;
}
Sahi.prototype._submit = function (n) {
    var el = this.findElement(n, "submit", "input");
    if (el == null) el = this.findElement(n, "submit", "button");
    return el;
}
Sahi.prototype._wait = function (i, condn) {
    this.setServerVar("waitConditionTime", new Date().valueOf()+i);
    if (condn) {
        _sahi.waitCondition = condn;
        this.setServerVar("waitCondition", condn)
        window.setTimeout("_sahi.cancelWaitCondition()", i);
    }
    else {
        window.setTimeout("_sahi.cancelWaitCondition()", i);
        this.waitInterval = i;
    }
}

Sahi.prototype.cancelWaitCondition = function (){
    this.waitCondition=null;
    this.waitInterval=INTERVAL;
    this.setServerVar("waitCondition", "");
    this.setServerVar("waitConditionTime", "-1");
}

Sahi.prototype._file = function (n) {
    return this.findElement(n, "file", "input");
}
Sahi.prototype._textbox = function (n) {
    return this.findElement(n, "text", "input");
}
Sahi.prototype._password = function (n) {
    return this.findElement(n, "password", "input");
}
Sahi.prototype._checkbox = function (n) {
    return this.findElement(n, "checkbox", "input");
}
Sahi.prototype._textarea = function (n) {
    return this.findElement(n, "textarea", "textarea");
}
Sahi.prototype._accessor = function (n) {
    return eval(n);
}
Sahi.prototype._byId = function (id) {
    return this.findElementById(this.top(), id);
}
Sahi.prototype._select = function (n) {
    var el = this.findElement(n, "select", "select");
    //    if (!el) el = this.findElement(n, "select-multiple", "select");
    return el;
}
Sahi.prototype._radio = function (n) {
    return this.findElement(n, "radio", "input");
}
Sahi.prototype._div = function (id) {
    return this.divSpanByText(this.top(), id, "div");
}
Sahi.prototype._span = function (id) {
    return this.divSpanByText(this.top(), id, "span");
}
Sahi.prototype._spandiv = function (id) {
    var el = this.divSpanByText(this.top(), id, "span");
    if (el == null) el = this.divSpanByText(this.top(), id, "div");
    return el;
}
Sahi.prototype.divSpanByText = function (win, id, tagName) {
    var res = null;
    var els = win.document.getElementsByTagName(tagName);
    for (var i = 0; i < els.length; i++) {
        if (this._getText(els[i]) == id) {
            return els[i];
        }
    }
    var frs = win.frames;
    if (frs) {
        for (var j = 0; j < frs.length; j++) {
            res = this.divSpanByText(frs[j], id, tagName);
            if (res) return res;
        }
    }
    return res;
}
Sahi.prototype._image = function (n) {
    return this.findImage(n, this.top(), "img");
}
Sahi.prototype._imageSubmitButton = function (n) {
    return this.findElement(n, "image", "input");
}
Sahi.prototype._link = function (n) {
    return this.findLink(n, this.top());
}
Sahi.prototype._simulateEvent = function (el, ev) {
    if (this.isIE()) {
        var newFn = (eval("el.on" + ev.type)).toString();
        newFn = newFn.replace("anonymous()", "s_anon(s_ev)", "g").replace("event", "s_ev", "g");
        eval(newFn);
        s_anon(ev);
    } else {
        eval("el.on" + ev.type + "(ev);");
    }
}
Sahi.prototype._setGlobal = function (name, value) {
    //    this._debug("*** name="+name+" value="+value);
    this.setServerVar(name, value);
}
Sahi.prototype._getGlobal = function (name) {
    var value = this.getServerVar(name);
    //    this._debug("GET name="+name+" value="+value);
    return value;
}
Sahi.prototype._set = function (name, value) {
    this.locals[name] = value;
}
Sahi.prototype._get = function (name) {
    var value = this.locals[name];
    return value;
}
Sahi.prototype._assertNotNull = function (n, s) {
    if (n == null) throw new SahiAssertionException(1, s);
    return true;
}
Sahi.prototype._assertNull = function (n, s) {
    if (n != null) throw new SahiAssertionException(2, s);
    return true;
}
Sahi.prototype._assertTrue = function (n, s) {
    if (n != true) throw new SahiAssertionException(5, s);
    return true;
}
Sahi.prototype._assertNotTrue = function (n, s) {
    if (n) throw new SahiAssertionException(6, s);
    return true;
}
Sahi.prototype._assertFalse = Sahi.prototype._assertNotTrue;
Sahi.prototype._assertEqual = function (expected, actual, s) {
    if (this.trim(expected) != this.trim(actual)) throw new SahiAssertionException(3, (s ? s : "") + "\nExpected:[" + expected + "]\nActual:[" + actual + "]");
    return true;
}
Sahi.prototype._assertNotEqual = function (expected, actual, s) {
    if (this.trim(expected) == this.trim(actual)) throw new SahiAssertionException(4, s);
    return true;
}
Sahi.prototype._assertContainsText = function (expected, el, s) {
    var text = this._getText(el);
    var present = false;
    if (expected instanceof RegExp)
        present = expected != null && text.match(expected) != null
    else present = text.indexOf(expected) != -1
    if (!present) throw new SahiAssertionException(3, (s ? s : "") + "\nExpected:[" + expected + "] to be part of [" + text + "]");
    return true;
}
Sahi.prototype._getSelectedText = function (el) {
    var opts = el.options;
    for (var i = 0; i < opts.length; i++) {
        if (el.value == opts[i].value) return opts[i].text;
    }
    return null;
}
Sahi.prototype._option = function (el, text) {
    var opts = el.options;
    for (var i = 0; i < opts.length; i++) {
        if (text == opts[i].text) return opts[i];
    }
    return null;
}
Sahi.prototype._getText = function (el) {
    this.checkNull(el);
    return this.trim(this.isIE() || this.isSafariLike() ? el.innerText : el.textContent);
}
Sahi.prototype._getCellText = Sahi.prototype._getText;
Sahi.prototype.getRowIndexWith = function (txt, tableEl) {
    var r = this.getRowWith(txt, tableEl);
    return (r == null) ? -1 : r.rowIndex;
}
Sahi.prototype.getRowWith = function (txt, tableEl) {
    for (var i = 0; i < tableEl.rows.length; i++) {
        var r = tableEl.rows[i];
        for (var j = 0; j < r.cells.length; j++) {
            if (this._getText(r.cells[j]).indexOf(txt) != -1) {
                return r;
            }
        }
    }
    return null;
}
Sahi.prototype.getColIndexWith = function (txt, tableEl) {
    for (var i = 0; i < tableEl.rows.length; i++) {
        var r = tableEl.rows[i];
        for (var j = 0; j < r.cells.length; j++) {
            if (this._getText(r.cells[j]).indexOf(txt) != -1) {
                return j;
            }
        }
    }
    return -1;
}
Sahi.prototype._alert = function (s) {
    return sahi_real_alert(s);
}
Sahi.prototype.alertMock = function (s) {
    if (_sahi.isPlaying()) {
        _sahi.top()._sahi.lastAlertText = s;
    } else {
        return sahi_real_alert(s);
    }
}
Sahi.prototype._lastAlert = function () {
    var v = _sahi.top()._sahi.lastAlertText;
    return v;
}
Sahi.prototype._eval = function (s) {
    return eval(s);
}
Sahi.prototype._call = function (s) {
    return s;
}
Sahi.prototype._random = function (n) {
    return Math.floor(Math.random() * (n + 1));
}
Sahi.prototype._savedRandom = function (id, min, max) {
    if (min == null) min = 0;
    if (max == null) max = 10000;
    var r = this._getGlobal("srandom" + id);
    if (r == null || r == "") {
        r = min + this._random(max - min);
        this._setGlobal("srandom" + id, r);
    }
    return r;
}
Sahi.prototype._resetSavedRandom = function (id) {
    this._setGlobal("srandom" + id, "");
}


Sahi.prototype._expectConfirm = function (text, value) {
    _sahi.confirmReturnValue[text] = value;
}
Sahi.prototype.confirmMock = function (s) {
    if (_sahi.isPlaying()) {
        var retVal = _sahi.confirmReturnValue[s];
        if (retVal == null) retVal = true;
        _sahi.lastConfirmText = s;
        _sahi.confirmReturnValue[s] = null;
        return retVal;
    } else {
        var retVal = sahi_real_confirm(s);
        _sahi.sendToServer('/_s_/dyn/Recorder_record?cmd=' + _sahi.escape("_expectConfirm(\"" + s + "\", " + retVal + ")"));
        return retVal;
    }
}
Sahi.prototype._lastConfirm = function () {
    var v = _sahi.lastConfirmText;
    return v;
}

Sahi.prototype.promptMock = function (s) {
    if (_sahi.isPlaying()) {
        var retVal = _sahi.promptReturnValue[s];
        if (retVal == null) retVal = "";
        _sahi.lastPromptText = s;
        _sahi.promptReturnValue[s] = null;
        return retVal;
    } else {
        var retVal = sahi_real_prompt(s);
        _sahi.sendToServer('/_s_/dyn/Recorder_record?cmd=' + _sahi.escape("_expectPrompt(\"" + s + "\", \"" + retVal + "\")"));
        return retVal;
    }
}
Sahi.prototype._lastPrompt = function () {
    var v = this.lastPromptText;
    return v;
}

Sahi.prototype._expectPrompt = function (text, value) {
    _sahi.promptReturnValue[text] = value;
}
Sahi.prototype._prompt = function (s) {
    return sahi_real_prompt(s);
}

Sahi.prototype._cell = function (id, row, col) {
    if (id == null) return null;
    if (row == null && col == null) {
        return this.findCell(id);
    }
    var rowIx = row;
    var colIx = col;
    if (typeof row == "string") {
        rowIx = this.getRowIndexWith(row, id);
        if (rowIx == -1) return null;
    }
    if (typeof col == "string") {
        colIx = this.getColIndexWith(col, id);
        if (colIx == -1) return null;
    }
    if (id.rows[rowIx] == null) return null;
    return id.rows[rowIx].cells[colIx];
}
Sahi.prototype._table = function (n) {
    return this.findTable(n);
}
Sahi.prototype._row = function (tableEl, rowIx) {
    if (typeof rowIx == "string") {
        return this.getRowWith(rowIx, tableEl);
    }
    if (typeof rowIx == "number") {
        return tableEl.rows[rowIx];
    }
    return null;
}
Sahi.prototype._containsHTML = function (el, htm) {
    return el && el.innerHTML && el.innerHTML.indexOf(htm) != -1;
}
Sahi.prototype._containsText = function (el, txt) {
    return el && this.getText(el).indexOf(txt) != -1;
}
Sahi.prototype._popup = function (n) {
    if (this.top().name == n || this.top().document.title == n) {
        return this.top();
    }
    throw new SahiNotMyWindowException();
}
Sahi.prototype._log = function (s, type) {
    this.logPlayBack(s, type);
}
Sahi.prototype._navigateTo = function (url, force) {
    if (force || this.top().location.href != url)
        this.top().setTimeout("location.href = '"+url+"'", 1);
}
Sahi.prototype._callServer = function (cmd, qs) {
    return this.sendToServer("/_s_/dyn/" + cmd + (qs == null ? "" : ("?" + qs)));
}
Sahi.prototype._removeMock = function (pattern) {
    return this._callServer("MockResponder_remove", "pattern=" + pattern);
}
Sahi.prototype._addMock = function (pattern, clazz) {
    if (clazz == null) clazz = "MockResponder_simple";
    return this._callServer("MockResponder_add", "pattern=" + pattern + "&class=" + clazz);
}
Sahi.prototype._mockImage = function (pattern, clazz) {
    if (clazz == null) clazz = "MockResponder_mockImage";
    return this._callServer("MockResponder_add", "pattern=" + pattern + "&class=" + clazz);
}
Sahi.prototype._debug = function (s) {
    return this._callServer("Debug_toOut", "msg=" + this.escape(s));
}
Sahi.prototype._debugToErr = function (s) {
    return this._callServer("Debug_toErr", "msg=" + this.escape(s));
}
Sahi.prototype._debugToFile = function (s, file) {
    if (file == null) return;
    return this._callServer("Debug_toFile", "msg=" + this.escape(s) + "&file=" + this.escape(file));
}
Sahi.prototype._enableKeepAlive = function () {
    this.sendToServer('/_s_/dyn/Configuration_enableKeepAlive');
}
Sahi.prototype._disableKeepAlive = function () {
    this.sendToServer('/_s_/dyn/Configuration_disableKeepAlive');
}

// finds document of any element
Sahi.prototype.getWin = function (el) {
    if (el == null) return self;
    if (el.nodeName.indexOf("document") != -1) return this.getFrame1(this.top(), el);
    return this.getWin(el.parentNode);
}
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
}

Sahi.prototype.simulateChange = function (el) {
    if (document.all) {
        if (el.onchange) el.onchange();
        if (el.onblur) el.onblur();
    } else {
        if (el.onblur) el.onblur();
        if (el.onchange) el.onchange();
    }
}
Sahi.prototype.areEqual = function (el, param, value) {
    if (param == "linkText") {
        var str = this.getText(el);
        if (value instanceof RegExp)
            return str != null && str.match(value) != null
        return (this.trim(str) == this.trim(value));
    }
    else {
        if (value instanceof RegExp)
            return el[param] != null && el[param].match(value) != null
        return (el[param] == value);
    }
}
Sahi.prototype.findLink = function (id) {
    var res = this.getBlankResult();
    var retVal = this.findImageHelper(id, this.top(), res, "linkText", false).element;
    if (retVal != null) return retVal;

    res = this.getBlankResult();
    return this.findImageHelper(id, this.top(), res, "id", false).element;
}
Sahi.prototype.findImage = function (id) {
    var res = this.getBlankResult();
    var retVal = this.findImageHelper(id, this.top(), res, "title", true).element;
    if (retVal != null) return retVal;
    retVal = this.findImageHelper(id, this.top(), res, "alt", true).element;
    if (retVal != null) return retVal;

    res = this.getBlankResult();
    return this.findImageHelper(id, this.top(), res, "id", true).element;
}
Sahi.prototype.findImageHelper = function (id, win, res, param, isImg) {
    var imgs = isImg ? win.document.images : win.document.links;

    if ((typeof id) == "number") {
        res.cnt = 0;
        res = this.findImageByIx(id, this.top(), res, isImg);
        return res;
    } else {
        var o = this.getArrayNameAndIndex(id);
        var imgIx = o.index;
        var fetch = o.name;
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

    var frs = win.frames;
    if (frs) {
        for (var j = 0; j < frs.length; j++) {
            res = this.findImageHelper(id, frs[j], res, param, isImg);
            if (res && res.found) return res;
        }
    }
    return res;
}

Sahi.prototype.findImageByIx = function (ix, win, res, isImg) {
    var imgs = isImg ? win.document.images : win.document.links;
    if (imgs[ix - res.cnt]) {
        res.element = imgs[ix - res.cnt];
        res.found = true;
        return res;
    }
    res.cnt += imgs.length;
    var frs = win.frames;
    if (frs) {
        for (var j = 0; j < frs.length; j++) {
            res = this.findImageByIx(ix, frs[j], res, isImg);
            if (res && res.found) return res;
        }
    }
    return res;
}

Sahi.prototype.findLinkIx = function (id, toMatch) {
    var res = this.getBlankResult();
    if (id == null || id == "") {
        var retVal = this.findImageIxHelper(id, toMatch, this.top(), res, null, false).cnt;
        if (retVal != -1) return retVal;
    }

    res = this.getBlankResult();
    var retVal = this.findImageIxHelper(id, toMatch, this.top(), res, "linkText", false).cnt;
    if (retVal != -1) return retVal;

    res = this.getBlankResult();
    return this.findImageIxHelper(id, toMatch, this.top(), res, "id", false).cnt;
}
Sahi.prototype.findImageIx = function (id, toMatch) {
    var res = this.getBlankResult();
    if (id == null || id == "") {
        var retVal = this.findImageIxHelper(id, toMatch, this.top(), res, null, true).cnt;
        if (retVal != -1) return retVal;
    }

    res = this.getBlankResult();
    var retVal = this.findImageIxHelper(id, toMatch, this.top(), res, "alt", true).cnt;
    if (retVal != -1) return retVal;

    res = this.getBlankResult();
    return this.findImageIxHelper(id, toMatch, this.top(), res, "id", true).cnt;
}
Sahi.prototype.findImageIxHelper = function (id, toMatch, win, res, param, isImg) {
    if (res && res.found) return res;

    var imgs = isImg ? win.document.images : win.document.links;
    for (var i = 0; i < imgs.length; i++) {
        if (param == null || this.areEqual(imgs[i], param, id)) {
            res.cnt++;
            if (imgs[i] == toMatch) {
                res.found = true;
                return res;
            }
        }
    }
    var frs = win.frames;
    if (frs) {
        for (var j = 0; j < frs.length; j++) {
            res = this.findImageIxHelper(id, toMatch, frs[j], res, param, isImg);
            if (res && res.found) return res;
        }
    }
    return res;
}
Sahi.prototype.findElementById = function (win, id) {
    var res = null;
    if (win.document.getElementById(id) != null) {
        return win.document.getElementById(id);
    }
    var frs = win.frames;
    if (frs) {
        for (var j = 0; j < frs.length; j++) {
            res = this.findElementById(frs[j], id);
            if (res) return res;
        }
    }
    return res;
}
Sahi.prototype.findElement = function (id, type, tagName) {
    var res = this.getBlankResult();
    var retVal = null;
    if (type == "button" || type == "reset" || type == "submit") {
        retVal = this.findElementHelper(id, this.top(), type, res, "value", tagName).element;
        if (retVal != null) return retVal;
    }
    else if (type == "image") {
        retVal = this.findElementHelper(id, this.top(), type, res, "title", tagName).element;
        if (retVal != null) return retVal;
        retVal = this.findElementHelper(id, this.top(), type, res, "alt", tagName).element;
        if (retVal != null) return retVal;
    }

    res = this.getBlankResult();
    retVal = this.findElementHelper(id, this.top(), type, res, "name", tagName).element;
    if (retVal != null) return retVal;

    res = this.getBlankResult();
    return this.findElementHelper(id, this.top(), type, res, "id", tagName).element;
}

Sahi.prototype.findFormElementByIndex = function (ix, win, type, res, tagName) {
    var els = win.document.getElementsByTagName(tagName);
    for (var j = 0; j < els.length; j++) {
        var el = els[j];
        if (el != null && this.areEqualTypes(el.type, type)) {
            res.cnt++;
            if (res.cnt == ix) {
                res.element = el;
                res.found = true;
                return res;
            }
        }
    }
    var frs = win.frames;
    if (frs) {
        for (var j = 0; j < frs.length; j++) {
            res = this.findFormElementByIndex(ix, frs[j], type, res, tagName);
            if (res && res.found) return res;
        }
    }
    return res;
}

Sahi.prototype.findElementHelper = function (id, win, type, res, param, tagName) {
    if ((typeof id) == "number") {
        res = this.findFormElementByIndex(id, win, type, res, tagName);
        if (res.found) return res;
    } else {
        var els = win.document.getElementsByTagName(tagName);
        for (var j = 0; j < els.length; j++) {
            if (this.areEqualTypes(els[j].type, type) && this.areEqual(els[j], param, id)) {
                res.element = els[j];
                res.found = true;
                return res;
            }
        }

        var o = this.getArrayNameAndIndex(id);
        var ix = o.index;
        var fetch = o.name;
        els = win.document.getElementsByTagName(tagName);
        for (var j = 0; j < els.length; j++) {
            if (this.areEqualTypes(els[j].type, type) && this.areEqual(els[j], param, fetch)) {
                res.cnt++;
                if (res.cnt == ix || ix == -1) {
                    res.element = els[j];
                    res.found = true;
                    return res;
                }
            }
        }


    }
    var frs = win.frames;
    if (frs) {
        for (var j = 0; j < frs.length; j++) {
            res = this.findElementHelper(id, frs[j], type, res, param, tagName);
            if (res && res.found) return res;
        }
    }
    return res;
}

Sahi.prototype.findElementIx = function (id, toMatch, type, tagName) {
    var res = this.getBlankResult();
    var retVal = -1;

    if (id == null || id == "") {
        retVal = this.findElementIxHelper(id, type, toMatch, this.top(), res, null, tagName).cnt;
        if (retVal != -1) return retVal;
    }

    if (type == "button" || type == "reset" || type == "submit") {
        retVal = this.findElementIxHelper(id, type, toMatch, this.top(), res, "value", tagName).cnt;
        if (retVal != -1) return retVal;
    }
    else if (type == "image") {
        retVal = this.findElementIxHelper(id, type, toMatch, this.top(), res, "title", tagName).cnt;
        if (retVal != -1) return retVal;
        retVal = this.findElementIxHelper(id, type, toMatch, this.top(), res, "alt", tagName).cnt;
        if (retVal != -1) return retVal;
    }
    res = this.getBlankResult();
    retVal = this.findElementIxHelper(id, type, toMatch, this.top(), res, "name", tagName).cnt;
    if (retVal != -1) return retVal;

    res = this.getBlankResult();
    retVal = this.findElementIxHelper(id, type, toMatch, this.top(), res, "id", tagName).cnt;
    return retVal;

}
Sahi.prototype.findElementIxHelper = function (id, type, toMatch, win, res, param, tagName) {
    if (res && res.found) return res;
    var els = win.document.getElementsByTagName(tagName);
    for (var j = 0; j < els.length; j++) {
        if (this.areEqualTypes(els[j].type, type) && this.areEqual(els[j], param, id)) {
            res.cnt++;
            if (els[j] == toMatch) {
                res.found = true;
                return res;
            }
        }
    }
    var frs = win.frames;
    if (frs) {
        for (var j = 0; j < frs.length; j++) {
            res = this.findElementIxHelper(id, type, toMatch, frs[j], res, param, tagName);
            if (res && res.found) return res;
        }
    }
    return res;
}
Sahi.prototype.areEqualTypes = function (type1, type2) {
    if (type1 == type2) return true;
    return (type1.indexOf("select") != -1 && type2.indexOf("select") != -1);
}
Sahi.prototype.findCell = function (id) {
    var res = this.getBlankResult();
    return this.findTagHelper(id, this.top(), "td", res, "id").element;
}

Sahi.prototype.findCellIx = function (id, toMatch) {
    var res = this.getBlankResult();
    var retVal = this.findTagIxHelper(id, toMatch, this.top(), "td", res, "id").cnt;
    if (retVal != -1) return retVal;
}
Sahi.prototype.getBlankResult = function () {
    var res = new Object();
    res.cnt = -1;
    res.found = false;
    res.element = null;
    return res;
}

Sahi.prototype.getArrayNameAndIndex = function (id) {
    var o = new Object();
    if (!(id instanceof RegExp) && id.match(/(.*)\[([0-9]*)\]$/)) {
        o.name = RegExp.$1;
        o.index = parseInt(RegExp.$2);
    } else {
        o.name = id;
        o.index = -1;
    }
    return o;
}
Sahi.prototype.findInForms = function (id, win, type) {
    var fms = win.document.forms;
    if (fms == null) return null;
    for (var j = 0; j < fms.length; j++) {
        var el = this.findInForm(id, fms[j], type);
        if (el != null) return el;
    }
    return null;
}
Sahi.prototype.findInForm = function (name, fm, type) {
    var els = fm.elements;
    var matchedEls = new Array();
    for (var i = 0; i < els.length; i++) {
        var el = els[i];
        if (el.name == name && el.type && this.areEqualTypes(el.type, type)) {
            matchedEls[matchedEls.length] = el;
        }
        else if ((el.type == "button" || el.type == "submit") && el.value == name && el.type == type) {
            matchedEls[matchedEls.length] = el;
        }
    }
    return (matchedEls.length > 0) ? (matchedEls.length == 1 ? matchedEls[0] : matchedEls ) : null;
}

Sahi.prototype.findTableIx = function (id, toMatch) {
    var res = this.getBlankResult();
    var retVal = this.findTagIxHelper(id, toMatch, this.top(), "table", res, (id ? "id" : null)).cnt;
    if (retVal != -1) return retVal;
}

Sahi.prototype.findTable = function (id) {
    var res = this.getBlankResult();
    return this.findTagHelper(id, this.top(), "table", res, "id").element;
}

Sahi.prototype.findResByIndexInList = function (ix, win, type, res) {
    var tags = win.document.getElementsByTagName(type);
    if (tags[ix - res.cnt]) {
        res.element = tags[ix - res.cnt];
        res.found = true;
        return res;
    }
    res.cnt += tags.length;
    var frs = win.frames;
    if (frs) {
        for (var j = 0; j < frs.length; j++) {
            res = this.findResByIndexInList(ix, frs[j], type, res);
            if (res && res.found) return res;
        }
    }
    return res;
}


Sahi.prototype.findTagHelper = function (id, win, type, res, param) {
    if ((typeof id) == "number") {
        res.cnt = 0;
        res = this.findResByIndexInList(id, win, type, res);
        return res;
    } else {
        var o = this.getArrayNameAndIndex(id);
        var ix = o.index;
        var fetch = o.name;
        var tags = win.document.getElementsByTagName(type);
        if (tags) {
            for (var i = 0; i < tags.length; i++) {
                if (this.areEqual(tags[i], param, fetch)) {
                    res.cnt++;
                    if (res.cnt == ix || ix == -1) {
                        res.element = tags[i];
                        res.found = true;
                        return res;
                    }
                }
            }
        }
    }

    var frs = win.frames;
    if (frs) {
        for (var j = 0; j < frs.length; j++) {
            res = this.findTagHelper(id, frs[j], type, res, param);
            if (res && res.found) return res;
        }
    }
    return res;
}
Sahi.prototype.findTagIxHelper = function (id, toMatch, win, type, res, param) {
    if (res && res.found) return res;

    var tags = win.document.getElementsByTagName(type);
    if (tags) {
        for (var i = 0; i < tags.length; i++) {
            if (param == null || this.areEqual(tags[i], param, id)) {
                res.cnt++;
                if (tags[i] == toMatch) {
                    res.found = true;
                    return res;
                }
            }
        }
    }
    var frs = win.frames;
    if (frs) {
        for (var j = 0; j < frs.length; j++) {
            res = this.findTagIxHelper(id, toMatch, frs[j], type, res, param);
            if (res && res.found) return res;
        }
    }
    return res;
}
Sahi.prototype.canSimulateClick = function (el) {
    return (el.click || el.dispatchEvent);
}

Sahi.prototype.isRecording = function () {
    if (this.top().Sahi._isRecording == null)
        this.top().Sahi._isRecording = this.getServerVar("sahi_record") == "1";
    return this.top().Sahi._isRecording;
}
Sahi.prototype.createCookie = function (name, value, days)
{
    var expires = "";
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toGMTString();
    }
    document.cookie = name + "=" + value + expires + "; path=/";
}

Sahi.prototype.readCookie = function (name)
{
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++)
    {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

Sahi.prototype.eraseCookie = function (name)
{
    this.createCookie(name, "", -1);
}
Sahi.prototype._event = function (type, keyCode) {
    this.type = type;
    this.keyCode = keyCode;
}
SahiAssertionException = function (msgNum, msgText) {
    this.messageNumber = msgNum;
    this.messageText = msgText;
    this.exceptionType = "SahiAssertionException";
}
SahiNotMyWindowException = function () {
    this.name = "SahiNotMyWindowException";
    this.message = "SahiNotMyWindowException";
}
var lastQs = "";
var lastTime = 0;
Sahi.prototype.onEv = function (e) {
    if (e.handled == true) return true; //FF
    if (_sahi.getServerVar("this.evaluateExpr") == "true") return true;
    var targ = _sahi.getTarget(e);
    if (e.type == "click") {
        if (targ.form && targ.type) {
            var type = targ.type;
            if (type == "text" || type == "textarea" || type == "password"
                    || type == "select-one" || type == "select-multiple") return true;
        }
    }
    var info = _sahi.getAccessorInfo(targ);
    var cmd = _sahi.getScript(info);
    if (cmd == null) return true;
    if (_sahi.hasEventBeenRecorded(cmd)) return true; //IE
    _sahi.sendToServer('/_s_/dyn/Recorder_record?cmd=' + _sahi.escape(cmd));
    e.handled = true;
    //FF
    _sahi.showInController(info);
    return true;
}
Sahi.prototype.showInController = function (info) {
    try {
        var c = this.getWinHandle();
        if (c) {
            var d = c.top.main.document.currentForm.debug;
            c.top.main.document.currentForm.history.value += "\n" + d.value;
            d.value = this.getScript(info);
            //			d.value+="\n"+getScript(info);
            //			d.scrollTop = d.scrollHeight;
        }
    } catch(ex2) {
        //		throw ex2;
    }
}
Sahi.prototype.hasEventBeenRecorded = function (qs) {
    var now = (new Date()).getTime();
    if (qs == lastQs && (now - lastTime) < 500) return true;
    lastQs = qs;
    lastTime = now;
    return false;
}
Sahi.prototype.getPopupName = function () {
    var n = null;
    if (this.isPopup()) {
        n = this.top().name;
        if (!n || n == "") {
            n = this.top().document.title;
        }
    }
    return n ? n : "";
}
Sahi.prototype.isPopup = function () {
    return _sahi.top().opener != null && _sahi.top().opener != window._sahi.top()
}
Sahi.prototype.addWait = function (time) {
    var val = parseInt(time);
    if (("" + val) == "NaN" || val < 200) throw new Error();
    showInController(new AccessorInfo("", "", "", "wait", time));
    //    this.sendToServer('/_s_/dyn/Recorder_record?event=wait&value='+val);
}
Sahi.prototype.mark = function (s) {
    showInController(new AccessorInfo("", "", "", "mark", s));
}
Sahi.prototype.doAssert = function (e) {
    try {
        if (!this.top()._lastAccessedInfo) return;
        this.top()._lastAccessedInfo.event = "assert";
        this.showInController(this.top()._lastAccessedInfo);
        //      this.sendToServer('/_s_/dyn/Recorder_record?'+getSahiPopUpQS()+this.getAccessorInfoQS(this.top()._lastAccessedInfo, true));
    } catch(ex) {
        this.handleException(ex);
    }
}

Sahi.prototype.getTarget = function (e) {
    var targ;
    if (!e) e = window.event;
    var evType = e.type;
    if (e.target) targ = e.target;
    else if (e.srcElement) targ = e.srcElement;
    if (targ.nodeType == 3) // defeat Safari bug
        targ = targ.parentNode;
    return targ;
}

Sahi.prototype.getAccessorInfo = function (el) {
    if (el == null) return null;
    var type = el.type;
    var accessor = this.getAccessor(el);
    var shortHand = this.getShortHand(el, accessor);
    //    alert(type+" -- "+accessor+" --- "+shortHand);
    if (el.tagName.toLowerCase() == "img") {
        return new AccessorInfo(accessor, shortHand, "img", "click");
    } else if (type == "text" || type == "textarea" || type == "password") {
        return new AccessorInfo(accessor, shortHand, type, "setvalue", el.value);
    } else if (type == "select-one" || type == "select-multiple") {
        return new AccessorInfo(accessor, shortHand, type, "setselected", this.getOptionText(el, el.value));
    } else if (el.tagName.toLowerCase() == "a") {
        return new AccessorInfo(accessor, shortHand, "link", "click");
    } else if (type == "button" || type == "reset" || type == "submit" || type == "image") {
        return new AccessorInfo(accessor, shortHand, type, "click");
    } else if (type == "checkbox" || type == "radio") {
        return new AccessorInfo(accessor, shortHand, type, "click", el.checked);
    } else if (type == "file") {
        return new AccessorInfo(accessor, shortHand, type, "setFile", el.value);
    } else if (el.tagName.toLowerCase() == "td") {
        return new AccessorInfo(accessor, shortHand, "cell", "click", this.isIE() ? el.innerText : el.textContent);
    } else if (el.tagName.toLowerCase() == "div" || el.tagName.toLowerCase() == "span") {
        return new AccessorInfo(accessor, shortHand, "byId", "click", this.isIE() ? el.innerText : el.textContent);
    }
}

Sahi.prototype.getShortHand = function (el, accessor) {
    var shortHand = "";
    try {
        var tagLC = el.tagName.toLowerCase();
        if (tagLC == "img") {
            shortHand = el.alt;
            if (!shortHand || shortHand == "") shortHand = el.id;
            if (shortHand && shortHand != "") {
                if (this.findImage(shortHand) != el) {
                    var ix = this.findImageIx(shortHand, el);
                    if (ix == -1) return "";
                    return shortHand + "[" + ix + "]";
                }
            } else {
                var ix = this.findImageIx(null, el);
                if (ix != -1) shortHand = ix;
            }
            return shortHand;
        } else if (tagLC == "a") {
            shortHand = this.getText(el);
            //(el.innerText) ? el.innerText : el.text;
            shortHand = this.trim(shortHand);
            if (!shortHand || shortHand == "") shortHand = el.id;
            if (shortHand && shortHand != "") {
                if (this.findLink(shortHand) != el) {
                    var ix = this.findLinkIx(shortHand, el);
                    if (ix == -1) return "";
                    return shortHand + "[" + ix + "]";
                }
            }
            return shortHand;
        } else if (tagLC == "button" || tagLC == "input" || tagLC == "textarea" || tagLC.indexOf("select") != -1) {
            if (el.type == "button" || el.type == "reset" || el.type == "submit") shortHand = el.value;
            if (el.type == "image") {
                shortHand = el.title;
                if (!shortHand) shortHand = el.alt;
            }
            else if (!shortHand || shortHand == "") shortHand = el.name;
            if (!shortHand || shortHand == "") shortHand = el.id;
            if (shortHand && shortHand != "") {
                if (this.findElement(shortHand, el.type, tagLC) != el) {
                    var ix = this.findElementIx(shortHand, el, el.type, tagLC);
                    if (ix == -1) return "";
                    return shortHand + "[" + ix + "]";
                }
            } else {
                var ix = this.findElementIx(null, el, el.type, tagLC);
                if (ix != -1) shortHand = ix;
            }
            return shortHand;
        } else if (el.tagName.toLowerCase() == "td") {
            shortHand = el.id;
            if (shortHand && shortHand != "") {
                if (this.findCell(shortHand) != el) {
                    var ix = this.findCellIx(shortHand, el);
                    if (ix != -1) return this.quoted(shortHand + "[" + ix + "]");
                }
                return this.quoted(shortHand);
            }
            shortHand = this.getTableShortHand(this.getTableEl(el));
            //"_table(\""+tabId+"\")";
            shortHand += ", " + this.getRow(el).rowIndex;
            shortHand += ", " + el.cellIndex;
        } else if (el.tagName.toLowerCase() == "span" || el.tagName.toLowerCase() == "div") {
            shortHand = el.id;
        }
    } catch(ex) {
        this.handleException(ex);
    }
    return shortHand;
}
Sahi.prototype.getTableShortHand = function (el) {
    var shortHand = el.id;
    if (shortHand && shortHand != "") {
        if (this.findTable(shortHand) != el) {
            var ix = this.findTableIx(shortHand, el);
            if (ix != -1) return "_table(" + this.quoted(shortHand + "[" + ix + "]") + ")";
        }
        return "_table(" + this.quoted(shortHand) + ")";
    }
    return "_table(" + this.findTableIx(null, el) + ")";
}

var AccessorInfo = function (accessor, shortHand, type, event, value) {
    this.accessor = accessor;
    this.shortHand = shortHand;
    this.type = type;
    this.event = event;
    this.value = value;
}

Sahi.prototype.getAccessorInfoQS = function (ai, isAssert) {
    if (ai == null || ai.event == null) return;
    var s = "event=" + (isAssert ? "assert" : ai.event);
    s += "&accessor=" + this.escape(this.convertUnicode(ai.accessor));
    s += "&shorthand=" + this.escape(this.convertUnicode(ai.shortHand));
    s += "&type=" + ai.type;
    if (ai.value) {
        s += "&value=" + this.escape(this.convertUnicode(ai.value));
    }
    return s;
}

Sahi.prototype.getOptionText = function (sel, val) {
    var l = sel.options.length;
    for (var i = 0; i < l; i++) {
        if (sel.options[i].value == val) return sel.options[i].text;
    }
    return null;
}

Sahi.prototype.addHandlersToAllFrames = function (win) {
    var fs = win.frames;
    if (!fs || fs.length == 0) {
        this.addHandlers(self);
    } else {
        for (var i = 0; i < fs.length; i++) {
            this.addHandlersToAllFrames(fs[i]);
        }
    }
}
Sahi.prototype.docEventHandler = function (e) {
    if (!e) e = window.event;
    var t = _sahi.getTarget(e);
    if (t && !t.hasAttached && t.tagName) {
        var tag = t.tagName.toLowerCase();
        if (tag == "a" || t.form || tag == "img" || tag == "div" || tag == "span" || tag == "td" || tag == "table") {
            _sahi.attachEvents(t);
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
Sahi.prototype.addHandlers = function (win) {
    if (!win) win = self;
    var doc = win.document;
    this.addEvent(doc, "keyup", this.docEventHandler);
    this.addEvent(doc, "mousemove", this.docEventHandler);
}

Sahi.prototype.attachEvents = function (el) {
    var tagName = el.tagName.toLowerCase();
    if (tagName == "a") {
        this.attachLinkEvents(el)
    } else if (el.form && el.type) {
        this.attachFormElementEvents(el);
    } else if (tagName == "img" || tagName == "div" || tagName == "span" || tagName == "td" || tagName == "table") {
        this.attachImageEvents(el);
    }
}
Sahi.prototype.attachFormElementEvents = function (el) {
    var type = el.type;
    if (el.onchange == this.onEv || el.onblur == this.onEv || el.onclick == this.onEv) return;
    if (type == "text" || type == "file" || type == "textarea" || type == "password") {
        this.addEvent(el, "change", this.onEv);
    } else if (type == "select-one" || type == "select-multiple") {
        this.addEvent(el, "change", this.onEv);
    } else if (type == "button" || type == "submit" || type == "reset" || type == "checkbox" || type == "radio" || type == "image") {
        this.addEvent(el, "click", this.onEv);
    }
}
Sahi.prototype.attachLinkEvents = function (el) {
    this.addEvent(el, "click", this.onEv);
}
Sahi.prototype.attachImageEvents = function (el) {
    this.addEvent(el, "click", this.onEv);
}
Sahi.prototype.addEvent = function (el, ev, fn) {
    if (!el) return;
    if (el.attachEvent) {
        el.attachEvent("on" + ev, fn);
    } else if (el.addEventListener) {
        el.addEventListener(ev, fn, false);
    }
}
Sahi.prototype.removeEvent = function (el, ev, fn) {
    if (!el) return;
    if (el.attachEvent) {
        el.detachEvent("on" + ev, fn);
    } else if (el.removeEventListener) {
        el.removeEventListener(ev, fn, false);
    }
}
Sahi.prototype.setRetries = function (i) {
    this.setServerVar("sahi_retries", i);
}
Sahi.prototype.getRetries = function () {
    var i = parseInt(this.getServerVar("sahi_retries"));
    return ("" + i != "NaN") ? i : 0;
}
Sahi.prototype.getExceptionString = function (e)
{
    var stack = e.stack ? e.stack : "No trace available";
    return e.name + ": " + e.message + "<br>" + stack.replace(/\n/g, "<br>");
}

Sahi.prototype.onError = function (msg, url, lno) {
    try {
        var debugInfo = "Javascript error on page";
        if (!url) url = "";
        if (!lno) lno = "";
        if (msg && msg.indexOf("Access to XPConnect service denied") != -1) { //FF hack
            this.logPlayBack("msg: " + msg + "\nurl: " + url + "\nLine no: " + lno, "info", debugInfo);
        }
        else this.logPlayBack("msg: " + msg + "\nurl: " + url + "\nLine no: " + lno, "info", debugInfo);
    } catch(swallow) {
    }
}
window.onerror = _sahi.onError;
Sahi._control = null;
Sahi.prototype.openWin = function (e) {
    try {
        if (!e) e = window.event;
        this.top().Sahi._control = window.open("", "_sahiControl", this.getWinParams(e));
        var diffDom = false;
        try {
            var checkDiffDomain = this.top().Sahi._control.document.domain;
        } catch(domainInaccessible) {
            diffDom = true;
        }
        if (diffDom || !this.top()._sahi.isWinOpen) {
            this.top().Sahi._control = window.open("/_s_/spr/controller2.htm", "_sahiControl", this.getWinParams(e));
        }
        if (this.top().Sahi._control) this.top().Sahi._control.opener = window;
        if (e) this.top().Sahi._control.focus();
    } catch(ex) {
        this.handleException(ex);
    }
}
Sahi.prototype.getWinParams = function (e) {
    var x = e ? e.screenX - 40 : 500;
    var y = e ? e.screenY - 60 : 100;
    var positionParams = "";
    if (e) {
        if (this.isIE()) positionParams = ",screenX=" + x + ",screenY=" + y;
        else positionParams = ",screenX=" + x + ",screenY=" + y;
    }
    return "height=550px,width=460px,resizable=yes,toolbar=no,status=no" + positionParams;
}
Sahi.prototype.getWinHandle = function () {
    if (this.top().Sahi._control && !this.top().Sahi._control.closed) return this.top().Sahi._control;
}
Sahi.openControllerWindow = function (e) {
    if (!e) e = window.event;
    if (!_sahi.isHotKeyPressed(e)) return true;
    _sahi.openWin(e);
    return true;
}
Sahi.prototype.isHotKeyPressed = function (e) {
    return ((this.hotKey == "SHIFT" && e.shiftKey)
            || (this.hotKey == "CTRL" && e.ctrlKey)
            || (this.hotKey == "ALT" && e.altKey)
            || (this.hotKey == "META" && e.metaKey));
}
var _lastAccessedInfo;
Sahi.prototype.mouseOver = function (e) {
    try {
        if (_sahi.getTarget(e) == null) return;
        if (!e.ctrlKey) return;
        var controlWin = _sahi.getWinHandle();
        if (controlWin) {
            var acc = _sahi.getAccessorInfo(_sahi.getKnownTags(_sahi.getTarget(e)));
            try {
                if (acc) controlWin.main.displayInfo(acc, _sahi.escapeDollar(_sahi.getAccessor1(acc)), _sahi.escapeValue(acc.value));
            } catch(ex2) {
                throw ex2;
            }
            _sahi.top()._lastAccessedInfo = acc ? acc : _sahi.top()._lastAccessedInfo;
        }
    } catch(ex) {
        throw ex;
    }
}
Sahi.prototype.escapeDollar = function (s) {
    if (s == null) return null;
    return s.replace(/[$]/g, "\\$");
}
Sahi.prototype.getAccessor1 = function (info) {
    if (info == null) return null;
    if ("" == info.shortHand || info.shortHand == null) {
        return info.accessor;
    } else {
        if ("image" == info.type) {
            return "_imageSubmitButton(" + this.escapeForScript(info.shortHand) + ")";
        } else if ("img" == info.type) {
            return "_image(" + this.escapeForScript(info.shortHand) + ")";
        } else if ("link" == info.type) {
            return "_link(" + this.escapeForScript(info.shortHand) + ")";
        } else if ("select-one" == info.type || "select-multiple" == info.type) {
            return "_select(" + this.escapeForScript(info.shortHand) + ")";
        } else if ("text" == info.type) {
            return "_textbox(" + this.escapeForScript(info.shortHand) + ")";
        } else if ("file" == info.type) {
            return "_file(" + this.escapeForScript(info.shortHand) + ")";
        } else if ("cell" == info.type) {
            return "_cell(" + info.shortHand + ")";
        }
        return "_" + info.type + "(" + this.escapeForScript(info.shortHand) + ")";
    }
}
Sahi.prototype.escapeForScript = function (s) {
    return this.quoteIfString(s);
}
var _key;
var KEY_SHIFT = 16;
var KEY_CONTROL = 17;
var KEY_ALT = 18;
var KEY_Q = 81;
var KEY_K = 75;

var INTERVAL = 100;
var ONERROR_INTERVAL = 1000;
var MAX_RETRIES = 5;
var SAHI_MAX_WAIT_FOR_LOAD = 30;
_sahi.waitForLoad = SAHI_MAX_WAIT_FOR_LOAD;
var interval = INTERVAL;

var _localIx = 0;


Sahi.prototype.schedule = function (cmd, debugInfo) {
    if (!this.cmds) return;
    var i = this.cmds.length;
    this.cmds[i] = cmd;
    this.cmdDebugInfo[i] = debugInfo;
}
Sahi.prototype.instant = function (cmd, debugInfo) {
    if (!this.cmds) return;
    var i = this.cmdsLocal.length;
    this.cmdsLocal[i] = cmd;
    this.cmdDebugInfoLocal[i] = debugInfo;
}
Sahi.prototype.play = function () {
    window.setTimeout("try{_sahi.ex();}catch(ex){}", this.waitInterval > 0 && !this.waitCondition ? this.waitInterval : INTERVAL);
}
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
                return win.document.readyState == "complete" || this.loaded;
            } catch(e) {
                return true;
                //diff domain; don't bother
            }
        } else {
            for (var i = 0; i < fs.length; i++) {
                if (!this.areWindowsLoaded(fs[i])) return false;
            }
            if (win.document && win.document.getElementsByTagName("frameset").length == 0)
                return this.loaded;
            else return true;
        }
    }
    catch(ex) {
        this.logErr("2 to " + typeof ex);
        this.logErr("3 pr " + ex.prototype);
        return true;
        //for diff domains.
    }
}
var _isLocal = false;
Sahi._timer = null

Sahi.prototype.execNextStep = function (isStep, interval) {
    if (isStep) return;
    if (Sahi._timer) window.clearTimeout(Sahi._timer);
    Sahi._timer = window.setTimeout("try{_sahi.ex();}catch(ex){}", interval);
}
Sahi.prototype.gotErrors = function (b) {
    this.setServerVar("sahi_has_errors", b ? "1" : "0");
}
Sahi.prototype.hadErrors = function () {
    return this.getServerVar("sahi_has_errors") == "1";
}
Sahi.prototype.ex = function (isStep) {
    var cmds = this.cmds;
    var debugs = this.cmdDebugInfo;
    try {
        try {
            if (this.isPaused() && !isStep) return;
            var i = this.getCurrentIndex();
            if (_isLocal) {
                cmds = this.cmdsLocal;
                debugs = this.cmdDebugInfoLocal;
            }
            if (this.isPlaying() && cmds.length == i) {
                this.stopPlaying();
                return;
            }
            if ((isStep || this.isPlaying()) && cmds[i] != null) {
                if (_sahi.waitCondition) {
                    var again = true;
                    try {
                        if (eval(_sahi.waitCondition)) {
                            again = false;
                            _sahi.cancelWaitCondition();
                        }
                    } catch(e1) {
                    }
                    if (again) {
                        this.execNextStep(isStep, interval);
                        return;
                    }
                }
                if (!this.areWindowsLoaded(this.top()) && this.waitForLoad > 0) {
                    this.waitForLoad--;
                    this.execNextStep(isStep, interval);
                    return;
                }
                try {
                    this.waitForLoad = SAHI_MAX_WAIT_FOR_LOAD;
                    var debugInfo = "" + debugs[i];
                    try {
                        if (cmds[i].indexOf("_sahi._popup") != -1) {
                            // needed popup so see if I am the needed popup
                            eval(cmds[i].substring(0, cmds[i].indexOf(")") + 1));
                        } else {
                            // don't need popup, so if I am popup, throw error.
                            var popup = this.getPopupName();
                            if (popup != null && popup != "") {
                                throw new SahiNotMyWindowException();
                            }
                        }
                        this.updateControlWinDisplay(cmds[i], i);
                        this.setCurrentIndex(i + 1);
                        if (cmds[i].indexOf("_sahi._call") != -1 && cmds[i].indexOf("_sahi._callServer") == -1) {
                            var bkup = this.schedule;
                            var exc = null;
                            this.schedule = this.instant;
                            try {
                                eval(cmds[i]);
                            } catch(e) {
                                exc = e;
                            }
                            this.schedule = bkup;
                            if (exc) {
                                _isLocal = false;
                                this.cmdsLocal = new Array();
                                this.setRetries(MAX_RETRIES);
                                throw exc;
                            }
                            _isLocal = (this.cmdsLocal.length > 0);
                            //sahi_alert("Calling");
                            this.ex(isStep);
                        } else {
                            eval(cmds[i]);
                            this.reportSuccess(cmds[i], debugInfo);
                        }
                    } catch(e) {
                        this.setCurrentIndex(i);
                        throw e;
                    }
                } catch (ex1) {
                    if (ex1 instanceof SahiAssertionException) {
                        var retries = this.getRetries();
                        if (retries < MAX_RETRIES / 2) {
                            this.setRetries(retries + 1);
                            interval = ONERROR_INTERVAL;
                            this.execNextStep(isStep, interval);
                            return;
                        } else {
                            var debugInfo = "" + debugs[i];
                            var failureMsg = "Assertion Failed. " + (ex1.messageText ? ex1.messageText : "");
                            this.logPlayBack(cmds[i], "failure", debugInfo, failureMsg);
                            this.setRetries(0);
                            this.setCurrentIndex(i + 1);
                            this.gotErrors(true);
                        }
                    } else if (ex1 instanceof SahiNotMyWindowException) {
                        throw ex1;
                    } else {
                        throw ex1;
                    }
                }
                interval = this.waitInterval > 0 ? this.waitInterval : INTERVAL;
                this.waitInterval = -1;
            }
            else {
                return;
            }
        } catch(ex) {
            var retries = this.getRetries();
            if (retries < MAX_RETRIES) {
                this.setRetries(retries + 1);
                interval = ONERROR_INTERVAL;
            }                                                         
            else {
                var debugInfo = "" + debugs[i];
                if (this.getServerVar("sahi_play") == "1") {
                    this.logPlayBack(cmds[i], "error", debugInfo, this.getExceptionString(ex));
                }
                this.gotErrors(true);
                this.stopPlaying();
            }
        }
        this.execNextStep(isStep, interval);
    } catch(ex2) {
        if (this.isPlaying()) {
            this.execNextStep(isStep, interval);
        }
    }
}
Sahi.prototype.canEvalInBase = function (cmd) {
    return  (this.top().opener == null && !this.isForPopup(cmd)) || (this.top().opener && this.top().opener._sahi.top() == this.top());
}
Sahi.prototype.isForPopup = function (cmd) {
    return cmd.indexOf("_sahi._popup") == 0;
}
Sahi.prototype.canEval = function (cmd) {
    return (this.top().opener == null && !this.isForPopup(cmd)) // for base window
            || (this.top().opener && this.top().opener._sahi.top() == this.top()) // for links in firefox
            || (this.top().opener != null && this.isForPopup(cmd));
    // for popups
}
Sahi.prototype.pause = function () {
    this._isPaused = true;
    this.setServerVar("sahi_paused", 1);
}
Sahi.prototype.unpause = function () {
    this._isPaused = false;
    this.setServerVar("sahi_paused", 0);
    this.setServerVar("sahi_play", 1);
    this.top()._sahi._isPlaying = true;
}
Sahi.prototype.isPaused = function () {
    if (this.top()._sahi._isPaused == null)
        this.top()._sahi._isPaused = this.getServerVar("sahi_paused") == "1";
    return this.top()._sahi._isPaused;
}
Sahi.prototype.updateControlWinDisplay = function (s, i) {
    try {
        var controlWin = this.getWinHandle();
        if (controlWin && !controlWin.closed) {
            if (i) controlWin.main.displayStepNum(i + 1);
            controlWin.main.displayLogs(s);
        }
    } catch(ex) {
    }
}
Sahi.prototype.setCurrentIndex = function (i) {
    if (_isLocal) {
        this.setServerVar("this.localIx", i);
    }
    else this.setServerVar("this.ix", i);
}
Sahi.prototype.getCurrentIndex = function () {
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
}
Sahi.prototype.isPlaying = function () {
    if (this.top()._sahi._isPlaying == null)
        this.top()._sahi._isPlaying = this.getServerVar("sahi_play") == "1";
    return this.top()._sahi._isPlaying;
}
Sahi.prototype.playManual = function (ix) {
    this.gotErrors(false);
    this.setCurrentIndex(ix);
    this.unpause();
    this.ex();
}
Sahi.prototype.startPlaying = function () {
    this.sendToServer("/_s_/dyn/Player_start");
    this.setServerVar("sahi_play", 1);
    //	this.top()._isPlaying = true;
}
Sahi.prototype.stepWisePlay = function () {
    this.sendToServer("/_s_/dyn/Player_stepWisePlay");
}
Sahi.prototype.stopPlaying = function () {
    this.sendToServer("/_s_/dyn/Player_stop");
    this.setServerVar("sahi_play", 0);
    this.updateControlWinDisplay("--Stopped Playback: " + (this.hadErrors() ? "FAILURE" : "SUCCESS") + "--");
    this.gotErrors(false);
    this.top()._sahi._isPlaying = false;
}
Sahi.prototype.startRecording = function () {
    this.top().Sahi._isRecording = true;
    this.addHandlersToAllFrames(this.top());
}
Sahi.prototype.stopRecording = function () {
    this.top().Sahi._isRecording = false;
    this.sendToServer("/_s_/dyn/Recorder_stop");
    this.setServerVar("sahi_record", 0);
}
Sahi.prototype.reportSuccess = function (msg, debugInfo) {
    var type = (msg.indexOf("sahi_assert") == 0) ? "success" : "info";
    //this.sendToServer("/_s_/dyn/Player_success?msg=" + this.escape(msg) + "&type=" + type + "&debugInfo=" + (debugInfo?this.escape(debugInfo):""));
    this.logPlayBack(msg, type, debugInfo);
}
Sahi.prototype.logPlayBack = function (msg, type, debugInfo, failureMsg) {
    //this.sendToServer("/_s_/dyn/Log?msg=" + this.escape(msg) + "&type=" + type + "&debugInfo=" + (debugInfo?this.escape(debugInfo):""));
    this.sendToServer("/_s_/dyn/TestReporter_logTestResult?msg=" + this.escape(msg) + "&type=" + type
            + "&debugInfo=" + (debugInfo ? this.escape(debugInfo) : "") + (failureMsg ? "&failureMsg=" + this.escape(failureMsg) : ""));
}
Sahi.prototype.trim = function (s) {
    if (s == null) return s;
    if ((typeof s) != "string") return s;
    s = s.replace(/&nbsp;/g, ' ');
    s = s.replace(/\xA0/g, ' ');
    s = s.replace(/^[ \t\n\r]*/g, '');
    s = s.replace(/[ \t\n\r]*$/g, '');
    s = s.replace(/[\t\n\r]{1,}/g, ' ');
    return s;
}
Sahi.prototype.list = function (el) {
    var s = "";
    var f = "";
    var j = 0;
    if (typeof el == "object" || typeof el == "array") {
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
}

Sahi.prototype.findInArray = function (ar, el) {
    for (var i = 0; i < ar.length; i++) {
        if (ar[i] == el) return i;
    }
    return -1;
}
Sahi.prototype.isIE = function () {
    var browser = navigator.appName;
    return browser == "Microsoft Internet Explorer";
}
Sahi.prototype.createRequestObject = function () {
    var obj;
    if (this.isIE()) {
        obj = new ActiveXObject("Microsoft.XMLHTTP");
    } else {
        obj = new XMLHttpRequest();
    }
    return obj;
}
Sahi.prototype.getServerVar = function (name) {
    var v = this.sendToServer("/_s_/dyn/SessionState_getVar?name=" + this.escape(name));
    if (v == "null") return null;
    return v;
}
Sahi.prototype.setServerVar = function (name, value) {
    this.sendToServer("/_s_/dyn/SessionState_setVar?name=" + this.escape(name) + "&value=" + this.escape(value));
}
Sahi.prototype.logErr = function (msg) {
    //    return;
    this.sendToServer("/_s_/dyn/Log?msg=" + this.escape(msg) + "&type=err");
}

Sahi.prototype.getParentNode = function (el, tagName) {
    var parent = el.parentNode;
    while (parent && parent.tagName.toLowerCase() != "body" && parent.tagName.toLowerCase() != "html") {
        if (parent.tagName.toLowerCase() == tagName.toLowerCase()) return parent;
        parent = parent.parentNode;
    }
    return null;
}
Sahi.prototype.sendToServer = function (url) {
    try {
        var rand = (new Date()).getTime() + Math.floor(Math.random() * (10000));
        var http = this.createRequestObject();
        url = url + (url.indexOf("?") == -1 ? "?" : "&") + "t=" + rand;
        var post = url.substring(url.indexOf("?") + 1);
        url = url.substring(0, url.indexOf("?"));
        http.open("POST", url, false);
        http.send(post);
        return http.responseText;
    } catch(ex) {
        this.handleException(ex)
    }
}
s_v = function (v) {
    var type = typeof v;
    if (type == "number") return v;
    else if (type == "string") return "\"" + v + "\"";
    else return v;
}
Sahi.prototype.quoted = function (s) {
    return '"' + s.replace(/"/g, '\\"') + '"';
}
Sahi.prototype.handleException = function (e) {
    //	alert(e);
    //	throw e;
}
Sahi.prototype.getText = function (el) {
    if (el.innerHTML)
        return this.getTextFromHTML(el.innerHTML);
    return null;
}
Sahi.prototype.getTextFromHTML = function (s) {
    s = s.replace(/<[^>]*>/g, "");
    s = s.replace(/&amp;/g, "&");
    s = s.replace(/&lt;/g, "<");
    s = s.replace(/&gt;/g, ">");
    s = s.replace(/&nbsp;/g, " ");
    return s;
}
Sahi.prototype.convertUnicode = function (source) {
    if (source == null) return null;
    var result = '';
    for (var i = 0; i < source.length; i++) {
        if (source.charCodeAt(i) > 127)
            result += this.addSlashU(source.charCodeAt(i).toString(16));
        else result += source.charAt(i);
    }
    return result;
}
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
}

var _sahiOnBeforeUnLoad = function () {
    _sahi.onBeforeUnLoad();
}
Sahi.prototype.onBeforeUnLoad = function () {
    this.loaded = false;
}
/*
trap1 = function (e){
	if (!e) e = window.event;
	if (this.top().Sahi._control) debug(this.list(e));
	if (prevDown) prevDown(e);
}
var prevDown = null;
*/
var _sahiInit = function (e){
    _sahi.init(e);
}
Sahi.prototype.init = function (e) {
    try {
        this.loaded = true;
        this.activateHotKey();
    } catch(ex) {
        this.handleException(ex);
    }
    this.redefineDocWrite();
    if (this.waitInterval > 0){
        if (this.waitCondition){
            this._wait(this.waitInterval, this.waitCondition);
        }else {
            this._wait(this.waitInterval);            
        }
    }

    try {
        if (self == this.top()) {
            this.play();
        }
        if (this.isRecording()) this.addHandlers();
    } catch(ex) {
        //		throw ex;
        this.handleException(ex);
    }
}
Sahi.prototype.activateHotKey = function () {
    try {
        this.addEvent(document, "dblclick", Sahi.openControllerWindow);
        this.addEvent(document, "mousemove", this.mouseOver);
        if (this.isSafariLike()) {
            var prev = document.ondblclick;
            document.ondblclick = function(e) {
                if (prev != null) prev(e);
                this.openControllerWindow(e)
            };
        }
    } catch(ex) {
        this.handleException(ex);
    }
}
Sahi.prototype.isFirstExecutableFrame = function () {
    var fs = this.top().frames;
    for (var i = 0; i < fs.length; i++) {
        if (self == this.top().frames[i]) return true;
        if ("" + (typeof this.top().frames[i].location) != "undefined") { // = undefined when previous frames are not accessible due to some reason (may be from diff domain)
            return false;
        }
    }
    return false;
}
Sahi.prototype.getScript = function (info) {
    var accessor = this.escapeDollar(this.getAccessor1(info));
    if (accessor == null) return null;
    var ev = info.event;
    var value = info.value;
    var type = info.type
    var popup = this.getPopupName();

    var cmd = null;
    if (value == null)
        value = "";
    if (ev == "load") {
        cmd = "_wait(2000);";
    } else if (ev == "click") {
        cmd = "_click(" + accessor + ");";
    } else if (ev == "setvalue") {
        cmd = "_setValue(" + accessor + ", " + this.quotedEscapeValue(value) + ");";
    } else if (ev == "setselected") {
        cmd = "_setSelected(" + accessor + ", " + this.quotedEscapeValue(value) + ");";
    } else if (ev == "assert") {
        cmd = "_assertNotNull(" + accessor + ");\r\n";
        if (type == "cell") {
            cmd += "_assertEqual(" + this.quotedEscapeValue(value) + ", _getText(" + accessor + "));\n";
            cmd += "_assertContainsText(" + this.quotedEscapeValue(value) + ", " + accessor + ");";
        } else if (type == "select-one" || type == "select-multiple") {
            cmd += "_assertEqual(" + this.quotedEscapeValue(value) + ", _getSelectedText(" + accessor + "));";
        } else if (type == "text" || type == "textarea" || type == "password") {
            cmd += "_assertEqual(" + this.quotedEscapeValue(value) + ", " + accessor + ".value);";
        } else if (type == "checkbox" || type == "radio") {
            cmd += "_assert" + ("true" == "" + value ? "" : "Not" ) + "True(" + accessor + ".checked);";
        } else if (type != "link" && type != "img") {
            cmd += "_assertContainsText(" + this.quotedEscapeValue(value) + ", " + accessor + ");";
        }
    }
    else
        if (ev == "wait") {
            cmd = "_wait(" + value + ");";
        } else if (ev == "mark") {
            cmd = "//MARK: " + value;
        } else if (ev == "setFile") {
            cmd = "_setFile(" + accessor + ", " + this.quotedEscapeValue(value) + ");";
        }
    if (cmd != null && popup != null && popup != "") {
        cmd = "_popup(\"" + popup + "\")." + cmd;
    }
    return cmd;
}

Sahi.prototype.quotedEscapeValue = function (s) {
    return this.quoted(this.escapeValue(s));
}

Sahi.prototype.escapeValue = function (s) {
    if (s == null || typeof s != "string") return s;
    return this.convertUnicode(s.replace(/\r/g, "").replace(/\\/g, "\\\\").replace(/\n/g, "\\n"));
}

Sahi.prototype.escape = function (s) {
    if (s == null) return s;
    return escape(s).replace(/[+]/g, "%2B");
}

Sahi.prototype.saveCondition = function (a) {
    this._setGlobal("condn" + this.getCurrentIndex(), a ? "true" : "false");
    this.cmds = new Array();
    this.cmdDebugInfo = new Array();
    eval(_sahi.execSteps);
}

Sahi.prototype.quoteIfString = function (shortHand) {
    if (("" + shortHand).match(/^[0-9]+$/)) return shortHand;
    return this.quotedEscapeValue(shortHand);
}


Sahi.prototype._execute = function (command, sync) {
    var is_sync = sync ? "true" : "false";
    var status = this._callServer("CommandInvoker_execute", "command=" + escape(command) + "&sync=" + is_sync);
    if ("success" != status) {
        throw new Error("Execute Command Failed!");
    }
}

Sahi.prototype.activateHotKey();

Sahi.prototype._style = function (el, style) {  //http://dhtmlkitchen.com/
    if (!document.getElementById) return;

    var value = el.style[this.toCamelCase(style)];

    if (!value)
        if (document.defaultView)
            value = document.defaultView.
                    getComputedStyle(el, "").getPropertyValue(style);

        else if (el.currentStyle)
            value = el.currentStyle[toCamelCase(style)];

    return value;
}

Sahi.prototype.toCamelCase = function (s) {
    var exp = /-([a-z])/
    for (;exp.test(s); s = s.replace(exp, RegExp.$1.toUpperCase()));
    return s;
}
// document.write start
xxs = "<script src='/_s_/spr/concat.js'></scr" + "ipt>" +
          "<script src='http://www.this.domain.com/_s_/dyn/SessionState/state.js'></scr" + "ipt>" +
          "<script src='http://www.this.domain.com/_s_/dyn/Player_script/script.js'></scr" + "ipt>" +
          "<script src='/_s_/spr/playback.js'></scr" + "ipt>";

Sahi.prototype.docClose = function () {
    if (this.isIE()) {
        this.oldDocWrite(this.buffer);
        document.write(xxs);
        document.close();
        this.loaded = true;
        this.play();
    } else {
//        document.this.oldDocWrite(document.this.prefix + this.buffer + "<script>document.this.oldDocClose()</scr"+"ipt>");
////        document.this.oldDocWrite(this.buffer);
////        document.close();
//        sahi_real_alert(xxs);
//        try{
////            document.this.oldDocWrite(document.this.prefix);
//        }catch(e){}
//        try{
//            document.this.oldDocClose();
//        }catch(e){}
//        window.this.loaded = true;
//        this.play();
    }
}
Sahi.buffer = "";
Sahi.prototype.docWrite = function (s) {
    Sahi.buffer += s;
}
Sahi.prototype.redefineDocWrite = function (){
    if (this.isIE()){
//        this.oldDocWrite = document.write;
//        this.oldDocClose = document.close;
//        document.write = this.docWrite;
//        document.close = this.docClose;
    }
    else{
//        Document.prototype.this.prefix = xxs;
//        Document.prototype.this.oldDocWrite = document.write;
//        Document.prototype.this.oldDocClose = document.close;
//        document.write = this.docWrite;
//        document.close = this.docClose;
    }

}
// document.write end


window.sahi_real_alert = window.alert;
window.sahi_real_confirm = window.confirm;
window.sahi_real_prompt = window.prompt;

window._sahi.alert = window.alert;
window._sahi.confirm = window.confirm;
window._sahi.prompt = window.prompt;

window.alert = _sahi.alertMock;
window.confirm = _sahi.confirmMock;
window.prompt = _sahi.promptMock;
