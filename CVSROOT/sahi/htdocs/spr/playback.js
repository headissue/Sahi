function sahiOnBeforeUnLoad(){
	if (sahiPrevOnBeforeUnLoad) {
		sahiPrevOnBeforeUnLoad();
	}
	window.sahiLoaded=false;
}
function sahiInit(){
	if (sahiPrevOnLoad) {
		sahiPrevOnLoad();
	}
	try{
		window.sahiLoaded = true;	
		sahiPrevOndblclick = document.ondblclick;
		sahiPrevOnkeydown = document.onkeydown;
		sahiPrevOnkeyup = document.onkeyup;
		
	    document.ondblclick=sahiOpenWin1;
	    document.onkeydown=sahiKeyDown;
	    document.onkeyup=sahiKeyUp;
	    document.onmousemove=sahiMouseOver;
	}catch(ex){
	    sahiHandleException(ex);
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
var sahiPrevOnLoad = window.onload;
window.onload = sahiInit;


var sahiPrevOnBeforeUnLoad = window.onbeforeunload;
window.onbeforeunload = sahiOnBeforeUnLoad;
//sahiInit();
