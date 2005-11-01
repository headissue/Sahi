function sahiSetRetries(i){
    sahiSetServerVar("sahiRetries", i);
}
function sahiGetRetries(){
    var i = parseInt(sahiGetServerVar("sahiRetries"));
    return (""+i != "NaN") ? i : 0;
}
function sahiOnError(msg, url, lno){
    var debugInfo = "Javascript error on page";
    sahiLogPlayBack("msg: "+msg+"\nurl: "+url+"\nLine no: "+lno, "error", debugInfo);
}
window.onerror=sahiOnError;
var _sahiControl;
function sahiOpenWin(e){
try{
    if (!e) e = window.event;
    top._sahiControl = window.open("", "_sahiControl", getWinParams(e));
    if (!top._sahiControl.play){
        top._sahiControl = window.open("/_s_/spr/controller.htm", "_sahiControl", getWinParams(e));
    }
    if (top._sahiControl) top._sahiControl.opener = this;
    if (e) top._sahiControl.focus();
}catch(e){}
}
function getWinParams(e){
    var x = e ? e.screenX-40 : 500;
    var y = e ? e.screenY-60 : 100;
    var positionParams = "";
    if (e){
        if (sahiIsIE()) positionParams = ",screenX="+x+",screenY="+y;
        else positionParams = ",screenX="+x+",screenY="+y;
    }
    return "height=480px,width=460px,resizable=yes, toolbars=no"+positionParams;
}
function getSahiWinHandle(){
    if (top._sahiControl && !top._sahiControl.isClosed) return top._sahiControl;
}
function sahiOpenWin1(e){
    if (!top._isAltKeyPressed) return;
    top.sahiOpenWin = sahiOpenWin;
    top.sahiOpenWin(e);
    top._isAltKeyPressed = false;
}
var _lastAccessedInfo;
function sahiMouseOver(e){
    if (!top._isControlKeyPressed) return;
    try{
      var controlWin = getSahiWinHandle();
      if (controlWin){
        controlWin.displayStepNum();
        var acc = sahiGetAccessorInfo(sahiGetKnownTags(getTarget(e)));
        controlWin.displayInfo(acc);
        top._lastAccessedInfo = acc ? acc : top._lastAccessedInfo;
      }
    }catch(ex){}
}


var _key;
var KEY_SHIFT = 16;
var KEY_CONTROL = 17;
var KEY_ALT = 18;
var KEY_Q = 81;
var KEY_K = 75;

top._isControlKeyPressed = false;
top._isQKeyPressed = false;
top._isAltKeyPressed = false;
function sahiKeyUp(e){
    if (!e) e = window.event;
    if (prevOnkeyup){
//    	prevOnkeyup(e);
    }
    if (e.keyCode == KEY_CONTROL) top._isControlKeyPressed = false;
    if (e.keyCode == KEY_ALT) top._isAltKeyPressed = false;
}
function sahiKeyDown(e){
    if (!e) e = window.event;
    if (prevOnkeydown){
//    	prevOnkeydown(e);
    }
    if (e.keyCode == KEY_CONTROL) top._isControlKeyPressed = true;
    else if (e.keyCode == KEY_ALT) top._isAltKeyPressed = true;
}


var IDLE_INTERVAL=1000;
var INTERVAL=300;
var RETRY_INTERVAL=1000;
var MAX_RETRIES=10;

var _sahiCmds = new Array();
var _sahiCmdDebugInfo = new Array();
var _sahi_wait = -1;

function sahiAdd(cmd, debugInfo){
    var i = _sahiCmds.length;
  _sahiCmds[i] = cmd;
  _sahiCmdDebugInfo[i] = debugInfo;
}

function sahiPlay(){
   window.setTimeout("try{sahiEx();}catch(ex){}", INTERVAL);
}
function areWindowsLoaded(win){
	var fs = win.frames;
	if (!fs || fs.length == 0){
		return win.sahiLoaded;
	}else{
		for (var i=0; i<fs.length; i++){
			if (!areWindowsLoaded(fs[i])) return false;
		}
	}	
	return true;
}

var interval = INTERVAL;
function sahiEx(){
    try{
        try{
            var i=sahiGetCurrentIndex();
            if (isSahiPlaying() && _sahiCmds.length == i){
                sahiStopPlaying();
                return;
            }
            if (isSahiPlaying() && _sahiCmds[i]!=null){
				if (!areWindowsLoaded(top)){
					window.setTimeout("try{sahiEx();}catch(ex){}", interval);
					return;
				}
                try{
	                if (canEvalInBase(_sahiCmds[i])) {
		                //set before so that this step is not lost when a page unloads due to eval
	                	sahiSetCurrentIndex(i+1); 
	                }
                    if (canEval(_sahiCmds[i])){
		                updateControlWinDisplay(_sahiCmds[i]);
                    	eval(_sahiCmds[i]);
		                var debugInfo = ""+_sahiCmdDebugInfo[i]+" step ["+i+"]";
		                sahiLogPlayBack(_sahiCmds[i], "success", debugInfo);
		                sahiSetRetries(0); // _sahi_attempts = 0;
                    }
                }catch (ex1){
                    if (ex1 instanceof SahiAssertionException) {
                    	var retries = sahiGetRetries();
			            if (retries < MAX_RETRIES/2){
			                sahiSetRetries(retries+1);
			                interval = IDLE_INTERVAL;
	                    	sahiSetCurrentIndex(sahiGetCurrentIndex()-1);
							window.setTimeout("try{sahiEx();}catch(ex){}", interval);
							return;			                
			            }else{
	                        debugInfo = ""+_sahiCmdDebugInfo[i]+" step ["+i+"]";
	                        sahiLogPlayBack(_sahiCmds[i] + ex1.messageText, "failure", debugInfo);
	                        sahiSetRetries(0);
	                    }
                    }else if (ex1 instanceof SahiNotMyWindowException){
                    	throw ex1;
                    }else {
                    	sahiSetCurrentIndex(sahiGetCurrentIndex()-1);
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
                var debugInfo = ""+_sahiCmdDebugInfo[i]+" step ["+i+"]";
                sahiLogPlayBack(_sahiCmds[i], "error", debugInfo);
                sahiStopPlaying();
            }
        }
        window.setTimeout("try{sahiEx();}catch(ex){}", interval);
    }catch(ex2){
        if (isSahiPlaying()){
            window.setTimeout("try{sahiEx();}catch(ex){alert(ex)}", 1000);
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
function updateControlWinDisplay(s){
	try{
		if (window.status) window.status = s;
	}catch(ex){}
	
    try{
      var controlWin = getSahiWinHandle();
      if (controlWin && !controlWin.closed){
        controlWin.displayStepNum();
        controlWin.displayLogs(s);
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
    return sahiGetServerVar("sahi_play")=="1";
}
function sahiStartPlaying(){
	sahiSendToServer("/_s_/dyn/startplay");
	sahiSetServerVar("sahi_play", 1);
}
function sahiStopPlaying(){
	sahiSendToServer("/_s_/dyn/stopplay");
	sahiSetServerVar("sahi_play", 0);
	updateControlWinDisplay("--Stopped Playback--");
}
function sahiStartRecording(){
   	sahiAddHandlersToAllFrames(top);
	sahiSetServerVar("sahi_record", 1);
}
function sahiStopRecording(){
	sahiSendToServer("/_s_/dyn/recordstop");
	sahiSetServerVar("sahi_record", 0);
}
function sahiContinuePlay(){
    sahiPlay();
}

function sahiLogPlayBack(msg, type, debugInfo){
	sahiSendToServer("/_s_/dyn/log?msg=" + escape(msg) + "&type=" + type + "&debugInfo=" + escape(debugInfo));
}
