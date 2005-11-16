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
        var str = (el.innerText) ? el.innerText : el.text;
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

function sahiFindElement(id, type){
	var res = getBlankResult();
	var retVal = null;
	if (type == "button" || type == "submit"){
		retVal = sahiFindElementHelper(id, top, type, res, "value").element;
		if (retVal != null) return retVal;
	}
	
	res = getBlankResult();
	retVal = sahiFindElementHelper(id, top, type, res, "name").element;
	if (retVal != null) return retVal;
	
	res = getBlankResult();
	return sahiFindElementHelper(id, top, type, res, "id").element;
}

function sahiFindFormElementByIndex(ix, win, type, res){
    var fs = win.document.forms;
    if (fs){
	    for (var i=0; i<fs.length; i++){
	    	var els = fs[i].elements;
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
	    }
    }
    var frs = win.frames;
    if (frs){
        for (var j=0; j<frs.length; j++){
            res = sahiFindFormElementByIndex(ix, frs[j], type, res);
            if (res && res.found) return res;
        }
    }    
    return res;
}

function sahiFindElementHelper(id, win, type, res, param){
	if ((typeof id) == "number"){
		res = sahiFindFormElementByIndex(id, win, type, res);
		if (res.found) return res;
	}else{
		var o = getArrayNameAndIndex(id);
	    var ix = o.index;
	    var fetch = o.name;
	    var fs = win.document.forms;
	    if (fs){
		    for (var i=0; i<fs.length; i++){
		    	var els = fs[i].elements;
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
	    }    
    }
    var frs = win.frames;
    if (frs){
        for (var j=0; j<frs.length; j++){
            res = sahiFindElementHelper(id, frs[j], type, res, param);
			if (res && res.found) return res;
        }
    }
    return res;
}

function sahiFindElementIx(id, toMatch, type){
	var res = getBlankResult();
	var retVal = -1;

	if (id == null || id == ""){
		retVal = sahiFindElementIxHelper(id, type, toMatch, top, res, null).cnt;
		if (retVal != -1) return retVal;
	}

	if (type == "button" || type == "submit"){
		retVal = sahiFindElementIxHelper(id, type, toMatch, top, res, "value").cnt;
		if (retVal != -1) return retVal;
	}

	res = getBlankResult();
	retVal = sahiFindElementIxHelper(id, type, toMatch, top, res, "name").cnt;
	if (retVal != -1) return retVal;

	res = getBlankResult();
	retVal = sahiFindElementIxHelper(id, type, toMatch, top, res, "id").cnt;
	return retVal;

}
function sahiFindElementIxHelper(id, type, toMatch, win, res, param){
	if (res && res.found) return res;

    var fs = win.document.forms;
    if (fs){
	    for (var i=0; i<fs.length; i++){
	    	var els = fs[i].elements;
	    	for (var j=0; j<els.length; j++){
		        if (els[j].type == type &&  (param == null || sahiAreEqual(els[j], param, id))){
		        	res.cnt++;
		        	if (els[j] == toMatch){
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
            res = sahiFindElementIxHelper(id, type, toMatch, frs[j], res, param);
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

function sahiFindResByIndexInList(ix, list, res){
	if (list == null) return res;
	var el = list[ix];	
    if (el == null) return res;
	res.element = el;
	res.found = true;
	return res;
}

function sahiFindTagHelper(id, win, type, res, param){
	if ((typeof id) == "number"){
		res = sahiFindResByIndexInList(id, win.document.getElementsByTagName(type), res);
		if (res.found) return res;
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
