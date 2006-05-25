function checkOpener(){
	try{
		var x = top.opener.document;
	}
	catch (e){
	}
}
window.onerror=checkOpener;
function trim(s){
	return s.replace(/[ \t]/, "", "g");
}

function checkURL(url){
	if (url == null || trim(url) == "") return "";
	if (url.indexOf("://") == -1) return "http://" + url;
	return url;
}
function play(){	
	try{
		top.opener.top.sahiSetCurrentIndex(parseInt(document.playform.step.value));
		top.opener.top.unpause();
		top.opener.top.sahiEx();
	}catch (e){
//		alert("Please open the Controller again. \n(Press ALT-DblClick on the main window.)");
//		top.close();
	}
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
    sahiSendToServer('/_s_/dyn/ControllerUI_closed');
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
	s+=addVar("controller_history", document.currentForm.history.value);
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
		var hilightedTab = sahiGetServerVar("controller_tab")
		if (hilightedTab == null || hilightedTab == "") hilightedTab = "record";
        eval("show"+hilightedTab+"()");
        top.opener.top._isSahiWinOpen = true;
    }catch(ex){
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
    	top.opener.sahiStartRecording(recordAll);
//    	window.setTimeout("top.location.reload();", 1000);
    }
    return true;
}
var _scriptList = new Array();


var KEY_SHIFT = 16;
var KEY_CONTROL = 17;
var KEY_ALT = 18;
var KEY_Q = 81;
var KEY_K = 75;

try{
	top.opener.top._isControlKeyPressed = false;
	top.opener.top._isQKeyPressed = false;
	top.opener.top._isAltKeyPressed = false;
}catch(ex){}
function sahiKeyUp(e){
	try{
    if (!e) e = window.event;
    if (e.keyCode == KEY_CONTROL) top.opener.top._isControlKeyPressed = false;
    if (e.keyCode == KEY_ALT) top.opener.top._isAltKeyPressed = false;
    }catch(ex){
    }
}
function sahiKeyDown(e){
	try{
	    if (!e) e = window.event;
	    if (e.keyCode == KEY_CONTROL) top.opener.top._isControlKeyPressed = true;
	    else if (e.keyCode == KEY_ALT) top.opener.top._isAltKeyPressed = true;
    }catch(e){
    }
}
try{
    document.onkeydown=sahiKeyDown;
    document.onkeypress=sahiKeyUp;
    document.onkeyup=sahiKeyUp;
}catch(ex){}


function showrecord(){
	if (top.main.location.href.indexOf('recorder.htm')!=-1) return;
	hilightTab("record")
	top.main.location.href='recorder.htm'
}

function hilightTab(n){
	document.getElementById("playbackTab").className="dimTab";
	document.getElementById("recordTab").className="dimTab";	
	document.getElementById(n+"Tab").className="hiTab";
	sahiSetServerVar("controller_tab", n);
}
function initRecorderTab(){
	document.recordstartform.file.value = getRecVar("controller_recorder_file");
	document.currentForm.elValue.value = getRecVar("controller_el_value");
	document.currentForm.accessor.value = getRecVar("controller_accessor");	
	document.currentForm.alternative.value = getRecVar("controller_alternative");		
	document.currentForm.comment.value = getRecVar("controller_comment");	
	document.currentForm.history.value = getRecVar("controller_history");
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
function displayInfo(info, escapedAccessor, escapedValue){
	var f = document.currentForm;
	if (f){	
		f.elValue.value = escapedValue ? escapedValue : "";
		f.accessor.value = escapedAccessor;
		f.alternative.value = info.accessor;
	}
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
//   sahiSendToServer('/_s_/dyn/Recorder_record?event=mark&value='+escape(document.currentForm.comment.value));
}

function evaluateExpr(showErr){
	sahiSetServerVar("sahiEvaluateExpr", "true");
	try{
		var res = top.opener.sahi_eval(addSahi(document.currentForm.debug.value));
	}catch(e){
		if (e.exceptionType &&  e.exceptionType == "SahiAssertionException"){
			res = "[Assertion Failed]"+(e.messageText?e.messageText:"");
		}
		else {
			res = "[Exception] "+e;
		}
		sahiHandleException(e);
	}
	if (showErr) document.currentForm.result.value = res;
	sahiSetServerVar("sahiEvaluateExpr", "false");
}
function demoClick(){
	setDebugValue("_click("+document.currentForm.accessor.value+");");
	evaluateExpr();
}
function demoHighlight(){
	setDebugValue("_highlight("+document.currentForm.accessor.value+");");
	evaluateExpr();
}

function demoSetValue(){
	setDebugValue("_setValue("+document.currentForm.accessor.value+", \""+document.currentForm.elValue.value+"\");");
	evaluateExpr();
}
function setDebugValue(s){
	document.currentForm.history.value += "\n"+document.currentForm.debug.value;
	document.currentForm.debug.value = s;
}
function append(){
   sahiSendToServer('/_s_/dyn/Recorder_record?cmd='+escape(document.currentForm.debug.value));
}

function addSahi(s){
	return sahiSendToServer("/_s_/dyn/ControllerUI_getSahiScript?code="+escape(s));
}

function blankIfNull(s){
	return (s==null || s=="null") ? "" : s;
}
function byFile(showFile){
	document.getElementById("seturl").style.display=showFile?"none":"block";
	document.getElementById("setfile").style.display=showFile?"block":"none";
}
function checkScript(f){
	if (f.file && f.file.value == ""){
		alert("Please choose a script file");
		return false;
	}
	if (f.url && f.url.value == ""){
		alert("Please specify the url to script file");
		return false;
	}
	return true;
	
}
function onScriptFormSubmit(f){
	if (!checkScript(f)) return false;
	var url = checkURL(f.starturl.value);
	resetStep();
	clearLogs();
	window.setTimeout("reloadPage('"+url+"')", 100);
}
function reloadPage(u){
	if (u == ""){
    	top.opener.top.location.reload(true);
	}else{
		top.opener.top.location.href = u;
	}	
//	top.location.reload();
}
function getSel()
{
	var txt = '';
	if (window.getSelection)
	{
		txt = window.getSelection();
	}
	else if (document.getSelection)
	{
		txt = document.getSelection();
	}
	else if (document.selection)
	{
		txt = document.selection.createRange().text;
	}
	return txt;
}
function showHistory(){
	var histWin = window.open("history.htm", "sahi_history", "height=500px,width=450px");
}