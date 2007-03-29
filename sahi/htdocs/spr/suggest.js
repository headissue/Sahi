Suggest.suggests = [];
function Suggest(textEl, selectEl, menuId){
	this.textEl = textEl;
	this.selectEl = selectEl;
	this.menuId = menuId;
	Suggest.suggests[menuId] = this;
}

Suggest.prototype.reposition = function (x, y){
    el = this.selectEl;
    el.style.position = "absolute";
	el.style.left = x;
	el.style.top = y;
    el.style.display = "block";
}

Suggest.prototype.downArrow = function(e){
	try {
		this.selectEl.focus();
		this.selectEl.options[0].selected = true;
	} catch (e) {}
}

Suggest.prototype.handleUpArrow = function(e){
    if (!e) e = window.event;
    if (e.keyCode && e.keyCode == Suggest.KEY_ARROW_UP){
        if (this.selectEl.selectedIndex == 0){
            this.textEl.focus();
        }
    }
}

Suggest.prototype.escape = function(e){
	this.hide();
}

Suggest.hideAll = function (e){
    for (i in Suggest.suggests){
        Suggest.suggests[i].hide();
    }
}

Suggest.prototype.suggest = function (e){
    var str = this.textEl.value;

    this.selectEl.options.length = 0;
    
    var options = this.getOptions(str);
    for (var i=0; i<options.length; i++){
        this.selectEl.options[i] = options[i];
    }

    this.selectEl.size = (this.selectEl.options.length > 10) ? 10 : this.selectEl.options.length;

	if (this.selectEl.options.length > 0) {
		this.selectEl.options[0].selected = true;
		this.reposition(this.findPosX(this.textEl) + (this.textEl.value.length * 4), this.findPosY(this.textEl)+20);    
	} else {
		this.hide();
	}
}

Suggest.prototype.textboxEvent = function (e){
	if (!e) e = window.event;
	if (e.keyCode) {
		if (e.keyCode == Suggest.KEY_ARROW_DOWN) {
			this.downArrow();
			return;
		} else if (e.keyCode == Suggest.KEY_ESCAPE) {
			this.escape();
			return;
		} else if (e.keyCode == Suggest.KEY_ENTER){
            if (this.onchange) this.onchange();
        }
    }
	this.suggest(e);
}

Suggest.KEY_ENTER = 13;
Suggest.KEY_TAB = 9;
Suggest.KEY_ESCAPE = 27;
Suggest.KEY_ARROW_DOWN = 40;
Suggest.KEY_ARROW_UP = 38;

Suggest.prototype.selectEvent = function (e){
	if (!e) e = window.event;
	if (e.keyCode){
		if (e.keyCode == Suggest.KEY_TAB || e.keyCode == Suggest.KEY_ENTER){
			this.choose();
		} else if (e.keyCode == Suggest.KEY_ESCAPE) {
			this.hide();
		}
	}
}

Suggest.prototype.choose = function () {
	var accessor = this.textEl.value;
	if (accessor.indexOf('.') != -1){
		var dot = accessor.lastIndexOf('.');
		var elStr = accessor.substring(0, dot);
		var prop = accessor.substring(dot + 1);
		this.textEl.value = elStr + "." + this.selectEl.value;
    }else{
		this.textEl.value = this.selectEl.value;
	}
	this.textEl.focus();
	this.hide();
    if (this.onchange) this.onchange();
}

Suggest.prototype.hide = function (){
	this.selectEl.style.display = 'none';
}

Suggest.prototype.findPosY = function (obj)
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

Suggest.prototype.findPosX = function (obj)
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
