
function play(){
    window.opener.sahiStartPlaying();
    window.opener.sahiSetCurrentIndex(parseInt(document.playform.step.value));
    if (parseInt(document.playform.step.value) == 0){
//    	window.opener.top.location.reload(true);
	}
	else{
//		window.opener.top.sahiEx();
	}
		window.opener.top.sahiEx();
}
function stepWisePlay(){
    window.opener.sahiStartPlaying();
    if (parseInt(document.playform.step.value) == 0){
	    window.opener.sahiSetCurrentIndex(parseInt(document.playform.step.value));
    	window.opener.location.top.reload(true);
    	window.opener.top.sahiEx();
	}
	else{
		window.opener.top.sahiEx();
	}
	stopPlay();
}
function stopPlay(){
    window.opener.sahiStopPlaying();
}
function resetStep(){
    document.playform.step.value = 0;
}
function clearLogs(){
    document.logForm.logs.value = "";
}
function stopRec(){
    try{
        frames["channel"].location.href="/_s_/dyn/recordstop";
		top.opener.sahiStopRecording();
    }catch(ex){alert(ex);}
}

function doOnUnLoad(s){
    sahiSendToServer('/_s_/dyn/winclosed');
    try{
        window.opener.top._isSahiWinOpen = false;
    }catch(ex){sahiHandleException(ex);}
}

function doOnLoad(){
    try{
        window.opener.top._isSahiWinOpen = true;
        document.scriptfileform.file.options.length = 0;
        document.scriptfileform.file.options[0] = new Option("-- Choose Script --", "");
        for (var i=1; i<=_scriptList.length; i++){
            document.scriptfileform.file.options[i] = new Option(_scriptList[i-1], _scriptList[i-1]);
            if (_scriptList[i-1] == _selectedScript)
                document.scriptfileform.file.options[i].selected = true;
        }
        window.focus();
        if (sahiGetCurrentIndex() != null){
            displayStepNum();
        }
        if (sahiIsRecording()){
        	showRec();
        }else{
        	showPlayback();
        }
    }catch(ex){
		sahiHandleException(ex);
    }
}
function displayStepNum(){
	try{
	    document.playform.step.value = ""+sahiGetCurrentIndex();
    }catch(e){
    	sahiHandleException(e);
    }
}
function sahiGetCurrentIndex(){
	try{
    	var i = parseInt(sahiGetServerVar("sahiIx"));
   		return (""+i != "NaN") ? i : 0;
    }catch(e){
    	sahiHandleException(e);
    }
}
function displayQuery(s){
//    document.currentForm.query.value = forceWrap(s);
}
function displayLogs(s){
    document.logForm.logs.value += forceWrap(s)+"\n";
	document.logForm.logs.scrollTop = document.logForm.logs.scrollHeight;
}

function forceWrap(s1){
    var ix = s1.indexOf("\n");
    var s = s1;
    var rest = "";
    if (ix != -1){
        s = s1.substring(0, ix);
        rest = s1.substring(ix);
    }
    var start = 0;
    var BR_LEN = 51;
    var len = s.length;
    var broken="";
    while (true){
        if (start + BR_LEN >= len){
            broken += s.substring(start);
            break;
        }
        else{
            broken += s.substring(start, start+BR_LEN)+"\n";
            start += BR_LEN;
        }
    }
    return broken+rest;
}
function addToScriptList(fn){
    _scriptList[_scriptList.length] = fn;
}
function setSelectedScript(s){
    _selectedScript = s;
}
var isRecordAll = true;
function recordAll(){
    isRecordAll = !isRecordAll;
}

function checkFile(){
    if (document.recordform.file.value==""){
        alert("Please enter a name for the script");
        document.recordform.file.focus();
        return false;
    }
    if (window.opener) {
    	window.opener.sahiStartRecording(recordAll);
    }
    return true;
}
var _scriptList = new Array();


var KEY_SHIFT = 16;
var KEY_CONTROL = 17;
var KEY_ALT = 18;
var KEY_Q = 81;
var KEY_K = 75;

//document.domain = "yahoo.com";
opener.top._isControlKeyPressed = false;
opener.top._isQKeyPressed = false;
opener.top._isAltKeyPressed = false;
function sahiKeyUp(e){
    if (!e) e = window.event;
    if (e.keyCode == KEY_CONTROL) opener.top._isControlKeyPressed = false;
    if (e.keyCode == KEY_ALT) opener.top._isAltKeyPressed = false;
}
function sahiKeyDown(e){
	try{
	    if (!e) e = window.event;
	    if (e.keyCode == KEY_CONTROL) opener.top._isControlKeyPressed = true;
	    else if (e.keyCode == KEY_ALT) opener.top._isAltKeyPressed = true;
    }catch(e){
    	sahiHandleException(e);
    }
}
try{
    document.onkeydown=sahiKeyDown;
    document.onkeypress=sahiKeyUp;
    document.onkeyup=sahiKeyUp;
}catch(ex){
	sahiHandleException(ex);
}


function showRec(){
	document.getElementById("rec").style.display="block";
	document.getElementById("playback").style.display="none";
	document.getElementById("playbackTab").className="dimTab";
	document.getElementById("recTab").className="hiTab";
	document.recordform.file.focus();
}
function showPlayback(){
	document.getElementById("rec").style.display="none";
	document.getElementById("playback").style.display="block";
	document.getElementById("playbackTab").className="hiTab";
	document.getElementById("recTab").className="dimTab";
	document.scriptfileform.file.focus();
}

function displayInfo(info){
	var f = document.currentForm;
	f.elValue.value = info.value ? info.value : "";
	f.accessor.value = getAccessor1(info);
}
function getAccessor1(info){
    if ("" == info.shortHand) {
        return "_accessor(\"" + info.accessor + "\")";
    } else {
        if ("image" == info.type) {
            return "_imageSubmit(" + sahiQuoteIfString(info.shortHand) + ")";
        } else if ("link" == info.type) {
            return "_link(" + sahiQuoteIfString(info.shortHand) + ")";
        } else if ("select-one" == info.type || "select-multiple" == info.type) {
            return "_select(" + sahiQuoteIfString(info.shortHand) + ")";
        } else if ("text" == info.type) {
            return "_textbox(" + sahiQuoteIfString(info.shortHand) + ")";
        } else if ("cell" == info.type) {
            return "_cell(" + info.shortHand + ")";
        }
        return "_" + info.type + "(" + sahiQuoteIfString(info.shortHand) + ")";
    }
}

function sahiQuoteIfString(s){
	if (typeof s == "string") {
		return "\"" + s + "\"";
	}
	return s;
}

function addWait(){
    if (!sahiIsRecording()) return;
    try{
        var val = parseInt(document.currentForm.waitTime.value);
        if ((""+val) == "NaN" || val < 200) throw new Error();
        sahiSendToServer('/_s_/dyn/record?event=wait&value='+val);
    }catch(ex){
    	alert("Please enter the number of milliseconds to wait (should be >= 200)");
    	document.currentForm.waitTime.value = 3000;
	}
}

function mark(){
   sahiSendToServer('/_s_/dyn/record?event=mark&value='+escape(document.currentForm.comment.value));
}

function evaluateExpr(){
	sahiSetServerVar("sahiEvaluateExpr", "true");
	try{
		document.currentForm.result.value = opener.sahi_eval(addSahi(document.currentForm.debug.value));
	}catch(e){
		if (e.exceptionType &&  e.exceptionType == "SahiAssertionException"){
			document.currentForm.result.value = "[Assertion Failed]"+(e.messageText?e.messageText:"");
		}
		else {
			document.currentForm.result.value = "[Exception] "+e;
		}
		sahiHandleException(e);
	}
	sahiSetServerVar("sahiEvaluateExpr", "false");
}

function append(){
   sahiSendToServer('/_s_/dyn/record?event=append&value='+escape(document.currentForm.debug.value));
}

function addSahi(s){
	return sahiSendToServer("/_s_/dyn/getSahiScript?code="+escape(s));
}



