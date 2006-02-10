function o(f){
	window.open("", "channel", "height=100px,width=100px");
	f.rand.value=new Date();
	f.sahisid.value = sahiReadCookie("sahisid");
}
function play(){
	
//    top.opener.sahiStartPlaying();
//    top.opener.sahiSetCurrentIndex(parseInt(document.playform.step.value));
//
//    if (parseInt(document.playform.step.value) == 0){
//    	top.opener.top.location.reload(true);
//	}
//	else{
//		top.opener.top.sahiEx();
//	}
	top.opener.top.sahiSetCurrentIndex(parseInt(document.playform.step.value));
	top.opener.top.unpause();
	top.opener.top.sahiEx();
	return true;
}
function stepWisePlay(){
	top.opener.top.unpause();
//    top.opener.sahiStartPlaying();
    if (parseInt(document.playform.step.value) == 0){
	    top.opener.sahiSetCurrentIndex(parseInt(document.playform.step.value));
    	top.opener.top.location.reload(true);
//    	top.opener.top.sahiEx(isStep);
	}
	else{
		top.opener.top.sahiEx(true);
	}
}
function pause(){
	top.opener.top.pause();
}
function stopPlay(){
    top.opener.sahiStopPlaying();
}
function resetStep(){
    document.playform.step.value = 0;
}
function clearLogs(){
    document.logForm.logs.value = "";
}
function stopRec(){
    try{
		top.opener.sahiStopRecording();
    }catch(ex){alert(ex);}
}
function doOnTabsUnLoad(s){
    sahiSendToServer('/_s_/dyn/winclosed');
    try{
        top.opener.top._isSahiWinOpen = false;
    }catch(ex){
    	sahiHandleException(ex);
    }
}
function doOnRecUnLoad(s){
	sendRecorderSnapshot();
}
function doOnPlaybackUnLoad(s){
	sendPlaybackSnapshot();
}
function sendPlaybackSnapshot(){
	var s="";
	s+=addVar("controller_url", document.scripturlform.url.value);
	s+=addVar("controller_logs", document.logForm.logs.value);
	s+=addVar("controller_step", document.playform.step.value);
	s+=addVar("controller_step", document.playform.step.value);
	s+=addVar("controller_url_starturl", document.scripturlform.starturl.value);
	s+=addVar("controller_file_starturl", document.scriptfileform.starturl.value);
	var showUrl = ""+(document.getElementById("seturl").style.display=="block");
	s+=addVar("controller_show_url", showUrl);
	sahiSetServerVar("playback_state", s);
}
function sendRecorderSnapshot(){
	var s="";
	s+=addVar("controller_recorder_file", document.recordstartform.file.value);
	s+=addVar("controller_el_value", document.currentForm.elValue.value);
	s+=addVar("controller_comment", document.currentForm.comment.value);
	s+=addVar("controller_accessor", document.currentForm.accessor.value);	
	s+=addVar("controller_alternative", document.currentForm.alternative.value);	
	s+=addVar("controller_debug", document.currentForm.debug.value);
	s+=addVar("controller_waitTime", document.currentForm.waitTime.value);
	s+=addVar("controller_result", document.currentForm.result.value);
	sahiSetServerVar("recorder_state", s);
}

function addVar(n, v){
	return n+"="+v+"_$sahi$_";
}
_recVars = null;
function getRecVar(name){
	if (_recVars == null){
		_recVars = loadVars("recorder_state");
	}
	return blankIfNull(_recVars[name]);
}

function loadVars(serverVarName){
	var s = sahiGetServerVar(serverVarName);
	var a = new Array();
	if (s){
		var nv = s.split("_$sahi$_");
		for (var i=0; i<nv.length; i++){
			var ix = nv[i].indexOf("=");
			var n = nv[i].substring(0, ix);
			var v = nv[i].substring(ix+1);
			a[n]=blankIfNull(v);
		}	
	}
	return a;
}
_pbVars = null;
function getPbVar(name){
	if (_pbVars == null){
		_pbVars = loadVars("playback_state");
	}
	return blankIfNull(_pbVars[name]);
}
function doOnRecLoad(){
	initRecorderTab();
}
function doOnPlaybackLoad(){
    document.scriptfileform.file.options.length = 0;
    document.scriptfileform.file.options[0] = new Option("-- Choose Script --", "");
    for (var i=1; i<=_scriptList.length; i++){
        document.scriptfileform.file.options[i] = new Option(_scriptList[i-1], _scriptList[i-1]);
        if (_scriptList[i-1] == _selectedScript)
            document.scriptfileform.file.options[i].selected = true;
    }
    if (sahiGetCurrentIndex() != null){
        displayStepNum();
    }
    initPlaybackTab();
}
function doOnTabsLoad(){
    try{
        top.opener.top._isSahiWinOpen = true;
		var hilightedTab = sahiGetServerVar("controller_tab")
		if (hilightedTab == null || hilightedTab == "") hilightedTab = "playback";
        eval("show"+hilightedTab+"()");
    }catch(ex){
    	throw ex;
		sahiHandleException(ex);
    }
}
function displayStepNum(){
	try{
		if (document.playform)
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

function onRecordStartFormSubmit(f){
    if (document.recordstartform.file.value==""){
        alert("Please enter a name for the script");
        document.recordstartform.file.focus();
        return false;
    }
    if (top.opener) {
    	o(f);
    	top.opener.sahiStartRecording(recordAll);
    	window.setTimeout("top.location.reload();", 1000);
    }
    return true;
}
var _scriptList = new Array();


var KEY_SHIFT = 16;
var KEY_CONTROL = 17;
var KEY_ALT = 18;
var KEY_Q = 81;
var KEY_K = 75;

top.opener.top._isControlKeyPressed = false;
top.opener.top._isQKeyPressed = false;
top.opener.top._isAltKeyPressed = false;
function sahiKeyUp(e){
    if (!e) e = window.event;
    if (e.keyCode == KEY_CONTROL) top.opener.top._isControlKeyPressed = false;
    if (e.keyCode == KEY_ALT) top.opener.top._isAltKeyPressed = false;
}
function sahiKeyDown(e){
	try{
	    if (!e) e = window.event;
	    if (e.keyCode == KEY_CONTROL) top.opener.top._isControlKeyPressed = true;
	    else if (e.keyCode == KEY_ALT) top.opener.top._isAltKeyPressed = true;
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


function showrecord(){
	if (top.main.location.href.indexOf('recorder.htm')!=-1) return;
	hilightTab("record")
	top.main.location.href='recorder.htm'
}
//function showlogs(){
//	hilightTab("logs");
//	top.main.location.href='/_s_/spr/logs/';
//}
//function showscript(){
//	hilightTab("script");
//	top.main.location.href='/_s_/dyn/currentscript/';
//}
//function showparsed(){
//	hilightTab("parsed");
//	top.main.location.href='/_s_/dyn/currentparsedscript/'
//}
function hilightTab(n){
//	document.getElementById("logsTab").className="dimTab";
	document.getElementById("playbackTab").className="dimTab";
	document.getElementById("recordTab").className="dimTab";	
//	document.getElementById("scriptTab").className="dimTab";	
//	document.getElementById("parsedTab").className="dimTab";	
	document.getElementById(n+"Tab").className="hiTab";
	sahiSetServerVar("controller_tab", n);
}
function initRecorderTab(){
	document.recordstartform.file.value = getRecVar("controller_recorder_file");
	document.currentForm.elValue.value = getRecVar("controller_el_value");
	document.currentForm.accessor.value = getRecVar("controller_accessor");	
	document.currentForm.alternative.value = getRecVar("controller_alternative");		
	document.currentForm.comment.value = getRecVar("controller_comment");	
	document.currentForm.debug.value = getRecVar("controller_debug");
	document.currentForm.waitTime.value = getRecVar("controller_waitTime");
	document.currentForm.result.value = getRecVar("controller_result");
}
function showplayback(){
	if (top.main.location.href.indexOf('playback.htm')!=-1) return;
	hilightTab("playback");
	top.main.location.href='playback.htm'
}

function initPlaybackTab(){
	document.scripturlform.url.value = getPbVar("controller_url");
	document.logForm.logs.value = getPbVar("controller_logs");	
	document.scripturlform.starturl.value = getPbVar("controller_url_starturl");	
	document.scriptfileform.starturl.value = getPbVar("controller_file_starturl");	
	document.playform.step.value = getPbVar("controller_step");
	byFile(getPbVar("controller_show_url")!="true");
}
function displayInfo(info){
	var f = document.currentForm;
	if (f){	
		f.elValue.value = info.value ? info.value : "";
		f.accessor.value = getAccessor1(info);
		f.alternative.value = info.accessor;
	}
}
function getAccessor1(info){
    if ("" == info.shortHand) {
        return info.accessor;
    } else {
        if ("image" == info.type) {
            return "_imageSubmitButton(" + sahiQuoteIfString(info.shortHand) + ")";
        } else if ("img" == info.type) {
            return "_image(" + sahiQuoteIfString(info.shortHand) + ")";
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
    try{
    	top.opener.addWait(document.currentForm.waitTime.value);
    }catch(ex){
    	alert("Please enter the number of milliseconds to wait (should be >= 200)");
    	document.currentForm.waitTime.value = 3000;
	}
}

function mark(){
	top.opener.mark(document.currentForm.comment.value);
//   sahiSendToServer('/_s_/dyn/record?event=mark&value='+escape(document.currentForm.comment.value));
}

function evaluateExpr(){
	sahiSetServerVar("sahiEvaluateExpr", "true");
	try{
		document.currentForm.result.value = top.opener.sahi_eval(addSahi(document.currentForm.debug.value));
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
function demoClick(){
	document.currentForm.debug.value = "_click("+document.currentForm.accessor.value+");";
	evaluateExpr();
}
function demoHighlight(){
	document.currentForm.debug.value = "_highlight("+document.currentForm.accessor.value+");";
	evaluateExpr();
}

function demoSetValue(){
	document.currentForm.debug.value = "_setValue("+document.currentForm.accessor.value+", \""+document.currentForm.elValue.value+"\");";
	evaluateExpr();
}
function append(){
   sahiSendToServer('/_s_/dyn/record?event=append&value='+escape(document.currentForm.debug.value));
}

function addSahi(s){
	return sahiSendToServer("/_s_/dyn/getSahiScript?code="+escape(s));
}

function blankIfNull(s){
	return (s==null || s=="null") ? "" : s;
}
function byFile(showFile){
	document.getElementById("seturl").style.display=showFile?"none":"block";
	document.getElementById("setfile").style.display=showFile?"block":"none";
}
function onScriptFormSubmit(f){
	o(f);
	resetStep();
	clearLogs();
	window.setTimeout("reloadPage('"+f.starturl.value+"')", 1000);
}
function reloadPage(u){
	if (u == ""){
    	top.opener.top.location.reload(true);
	}else{
		top.opener.top.location.href = u;
	}	
	top.location.reload();
}