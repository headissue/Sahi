function sahiOnBeforeUnLoad(){
	if (prevOnBeforeUnLoad) {
		prevOnBeforeUnLoad();
	}
	window.sahiLoaded=false;
}
function sahiInit(){
	if (prevOnLoad) {
		prevOnLoad();
	}
	try{
		window.sahiLoaded = true;	
		prevOndblclick = document.ondblclick;
		prevOnkeydown = document.onkeydown;
		prevOnkeyup = document.onkeyup;
		
	    document.ondblclick=sahiOpenWin1;
	    document.onkeydown=sahiKeyDown;
	    document.onkeyup=sahiKeyUp;
	    document.onmousemove=sahiMouseOver;
	}catch(ex){
	    //alert(ex);
	}

	try{
	    if (self == top){ // would not execute if in a frameset page
	        sahiContinuePlay();
	    }
	    if (sahiIsFirstExecutableFrame()){ // set properties on frameset page
	        top.sahiPlay = sahiPlay;
	        top.sahiEx = sahiEx;
	        top.sahiSetCurrentIndex = sahiSetCurrentIndex;
	        top.isSahiPlaying = isSahiPlaying;
	        top.sahiStartPlaying = sahiStartPlaying;
	        top.sahiStopPlaying = sahiStopPlaying;
	        top.sahiGetCurrentIndex = sahiGetCurrentIndex;
	        top.sahiCreateCookie = sahiCreateCookie;
	        top.sahiReadCookie = sahiReadCookie;
	        top.sahiEraseCookie = sahiEraseCookie;
	        top.getSahiWinHandle = getSahiWinHandle; 
	        top.sahiLogPlayBack = sahiLogPlayBack; 
	    }
        top.sahiContinuePlay();
	    if (_isSahiWinOpen) sahiOpenWin();
	    if (sahiIsRecording()) sahiAddHandlers();
	}catch(ex){
	    throw ex;
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
var prevOnLoad = window.onload;
window.onload = sahiInit;


var prevOnBeforeUnLoad = window.onbeforeunload;
window.onbeforeunload = sahiOnBeforeUnLoad;
//sahiInit();
