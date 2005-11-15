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
            || tag == "input" || tag == "textarea" || tag == "textarea" || tag == "td" || tag == "table" || ((tag == "div" || tag == "span") && (src.id && src.id !=""))) return el;
        el = el.parentNode;
    }
}

function sahiById(src){
    var s = src.id;
    if (isBlankOrNull(s)) return "";
    return 'getElementById("'+s+'")';
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
    else if (tag=="input" || tag=="textarea" || tag=="select"){
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
    if (!isBlankOrNull(src.name)){
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

function sahiGetFormElement(src){
    var n = "";
    if (!isBlankOrNull(src.name)){
        n = 'elements["'+src.name+'"]';
    }else {
        var f = src.form;
        for (var j=0; j<f.length; j++){
            if (f[j] == src){
                n = "elements["+j+"]";
            }
        }
    }
    var f = sahiGetForm(src.form);
    return (n == "") ? f : f+"."+n;
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
    if (tag=="input" || tag=="textarea" || tag=="select"){
        var el2 = eval(s);
        //alert(s+" "+el2+" "+(el2 == src));
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