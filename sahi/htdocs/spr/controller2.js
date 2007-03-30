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
function checkOpener() {
    try {
        var x = top.opener.document;
    }
    catch (e) {
    }
}
function sahi(){
    return sahiOpener()._sahi;        
}
function sahiOpener() {
    return top.opener._sahi.top();
}
window.onerror = checkOpener;
function trim(s) {
    s = s.replace(/^[ \t]/, "", "g");
    return s.replace(/[ \t]$/, "", "g");
}

function checkURL(url) {
    if (url == null || trim(url) == "") return "";
    if (url.indexOf("://") == -1) return "http://" + url;
    return url;
}
function play() {
    try {
        sahi().playManual(parseInt(document.playform.step.value))
    } catch (e) {
        displayLogs("Please open the Controller again. \n(Press ALT-DblClick on the main window.)");
    }
    return true;
}
function stepWisePlay() {
    var i = parseInt(document.playform.step.value);
    if (i==0) i = i+1
    sahi().setCurrentIndex(i);
    sahi().stepWisePlay();
    sahiOpener().eval("_sahi.ex(true)");
}
function a() {
    sahiSetServerVar("sahi_play", 1);
}
function pause() {
    sahi().pause();
}
function stopPlay() {
    sahi().stopPlaying();
}
function resetStep() {
    document.playform.step.value = 0;
    sahiSetServerVar("sahiIx", 0);
    sahiSetServerVar("sahiLocalIx", 0);
}
function clearLogs() {
    document.logForm.logs.value = "";
}
function stopRec() {
    try {
        sahi().stopRecording();
    } catch(ex) {
        alert(ex);
    }
}
top.isWinOpen = true;
function doOnTabsUnLoad(s) {
    sahiSendToServer('/_s_/dyn/ControllerUI_closed');
    try {
        top.isWinOpen = false;
    } catch(ex) {
        sahiHandleException(ex);
    }
}
function doOnRecUnLoad(s) {
    sendRecorderSnapshot();
}
function doOnPlaybackUnLoad(s) {
    sendPlaybackSnapshot();
}
function sendPlaybackSnapshot() {
    var s = "";
    s += addVar("controller_url", document.scripturlform.url.value);
    s += addVar("controller_logs", document.logForm.logs.value);
    s += addVar("controller_step", document.playform.step.value);
    s += addVar("controller_url_starturl", document.scripturlform.starturl.value);
    s += addVar("controller_pb_dir", document.scriptfileform.dir.value);
    s += addVar("controller_file_starturl", document.scriptfileform.starturl.value);
    var showUrl = "" + (document.getElementById("seturl").style.display == "block");
    s += addVar("controller_show_url", showUrl);
    sahiSetServerVar("playback_state", s);
}
function sendRecorderSnapshot() {
    var s = "";
    s += addVar("controller_recorder_file", document.recordstartform.file.value);
    s += addVar("controller_el_value", document.currentForm.elValue.value);
    s += addVar("controller_comment", document.currentForm.comment.value);
    s += addVar("controller_accessor", document.currentForm.accessor.value);
    s += addVar("controller_alternative", document.currentForm.alternative.value);
    s += addVar("controller_debug", document.currentForm.debug.value);
    s += addVar("controller_history", document.currentForm.history.value);
    s += addVar("controller_waitTime", document.currentForm.waitTime.value);
    s += addVar("controller_result", document.currentForm.result.value);
    s += addVar("controller_rec_dir", document.recordstartform.dir.value);
    sahiSetServerVar("recorder_state", s);
}

function addVar(n, v) {
    return n + "=" + v + "_$sahi$_";
}
_recVars = null;
function getRecVar(name) {
    if (_recVars == null) {
        _recVars = loadVars("recorder_state");
    }
    return blankIfNull(_recVars[name]);
}

function loadVars(serverVarName) {
    var s = sahiGetServerVar(serverVarName);
    var a = new Array();
    if (s) {
        var nv = s.split("_$sahi$_");
        for (var i = 0; i < nv.length; i++) {
            var ix = nv[i].indexOf("=");
            var n = nv[i].substring(0, ix);
            var v = nv[i].substring(ix + 1);
            a[n] = blankIfNull(v);
        }
    }
    return a;
}
_pbVars = null;
function getPbVar(name) {
    if (_pbVars == null) {
        _pbVars = loadVars("playback_state");
    }
    return blankIfNull(_pbVars[name]);
}
var _selectedScriptDir = null;
function doOnRecLoad() {
    var f = document.recordstartform;
    populateOptions(f.dir, _scriptDirList, _selectedScriptDir);
    initRecorderTab();
}

// Returns the number of caracters of the longest element in a list
function getLongestListElementSize(p_list) {
    var longestSize = 0;
    for (var i = 0; i < p_list.length; ++i) {
        if (p_list[i].length > longestSize) {
            longestSize = p_list[i].length;
        }
    }
    return longestSize;
}

// Changes the width of an element. If more than 1 element has the same name, we resize
//  the first one.
function resizeElementWidth(p_elementName, p_size) {
    var el = document.getElementById(p_elementName);
    if (!el) {
        el = document.getElementsByName(p_elementName)[0];
    }
    if (parseInt(el.style.width) < p_size) el.style.width = p_size;
}

// Resize a dropdown list so we can see its enrite content.
function resizeDropdown(p_dropdownContent, p_dropdownName, p_prefix) {
    var longest = getLongestListElementSize(p_dropdownContent);
    // A caracter is about 7 pixel long
    var newDropdownSize = (longest - p_prefix) * 7 + 20;
    resizeElementWidth(p_dropdownName, newDropdownSize);
}

// Resize the "Script dir" dowpdown list
function resizeScriptDirDropdown() {
    resizeDropdown(_scriptDirList, "dir", 0);
}

// Resize the "File" dropdown list
function resizeScriptFileDropdown() {
    resizeDropdown(_scriptList, "file", document.scriptfileform.dir.value.length);
}

function populateScripts() {
    populateOptions(document.scriptfileform.file, _scriptList, _selectedScript, "-- Choose Script --", document.scriptfileform.dir.value);
    resizeScriptFileDropdown();
}

function populateOptions(el, opts, selectedOpt, defaultOpt, prefix) {
    el.options.length = 0;
    if (defaultOpt) {
        el.options[0] = new Option(defaultOpt, "");
    }
    for (var i = 0; i < opts.length; i++) {
        var ix = el.options.length;
        if (prefix) {
            if (opts[i].indexOf(prefix) == 0) {
                el.options[ix] = new Option(opts[i].substring(prefix.length), opts[i]);
                if (opts[i] == selectedOpt) el.options[ix].selected = true;
            }
        } else {
            el.options[ix] = new Option(opts[i], opts[i]);
            if (opts[i] == selectedOpt) el.options[ix].selected = true;
        }
    }
//    alert(el.options.length)
}

function doOnPlaybackLoad() {
    populateOptions(document.scriptfileform.dir, _scriptDirList, _selectedScriptDir);
    initPlaybackTab();
    populateOptions(document.scriptfileform.file, _scriptList, _selectedScript, "-- Choose Script --", document.scriptfileform.dir.value);

    resizeScriptFileDropdown();
    resizeScriptDirDropdown();

    var ix = sahiGetCurrentIndex();
    if (ix != null) {
        displayStepNum(ix);
    }
}
function doOnTabsLoad() {
    try {
        var hilightedTab = sahiGetServerVar("controller_tab")
        if (hilightedTab == null || hilightedTab == "") hilightedTab = "recorder";
        showTab(hilightedTab);
        top.isWinOpen = true;
    } catch(ex) {
        sahiHandleException(ex);
    }
}
function displayStepNum(ix) {
    try {
        if (document.playform)
            document.playform.step.value = "" + ix;
    } catch(e) {
        sahiHandleException(e);
    }
}
function sahiGetCurrentIndex() {
    try {
        var i = parseInt(sahiGetServerVar("sahiIx"));
        return ("" + i != "NaN") ? i : 0;
    } catch(e) {
        sahiHandleException(e);
    }
}
function displayQuery(s) {
    //    document.currentForm.query.value = forceWrap(s);
}
function displayLogs(s) {
    document.logForm.logs.value += s + "\n";
    document.logForm.logs.scrollTop = document.logForm.logs.scrollHeight;
}

function forceWrap(s1) {
    var ix = s1.indexOf("\n");
    var s = s1;
    var rest = "";
    if (ix != -1) {
        s = s1.substring(0, ix);
        rest = s1.substring(ix);
    }
    var start = 0;
    var BR_LEN = 51;
    var len = s.length;
    var broken = "";
    while (true) {
        if (start + BR_LEN >= len) {
            broken += s.substring(start);
            break;
        }
        else {
            broken += s.substring(start, start + BR_LEN) + "\n";
            start += BR_LEN;
        }
    }
    return broken + rest;
}
function addToScriptList(fn) {
    _scriptList[_scriptList.length] = fn;
}
function setSelectedScript(s) {
    _selectedScript = s;
}
function addToScriptDirList(fn) {
    _scriptDirList[_scriptDirList.length] = fn;
}
function setSelectedScriptDir(s) {
    _selectedScriptDir = s;
}
var isRecordAll = true;
function recordAll() {
    isRecordAll = !isRecordAll;
}

function onRecordStartFormSubmit(f) {
    if (document.recordstartform.file.value == "") {
        alert("Please enter a name for the script");
        document.recordstartform.file.focus();
        return false;
    }
    if (sahiOpener()) {
        sahi().startRecording(recordAll);
        //    	window.setTimeout("top.location.reload();", 1000);
    }
    return true;
}
var _scriptList = new Array();
var _scriptDirList = new Array();

function hilightTab(n) {
    document.getElementById("playbackTab").className = "dimTab";
    document.getElementById("recorderTab").className = "dimTab";
    //    document.getElementById("settingsTab").className = "dimTab";
    document.getElementById(n + "Tab").className = "hiTab";
    sahiSetServerVar("controller_tab", n);
}
function initRecorderTab() {
    document.recordstartform.file.value = getRecVar("controller_recorder_file");
    document.currentForm.elValue.value = getRecVar("controller_el_value");
    document.currentForm.accessor.value = getRecVar("controller_accessor");
    document.currentForm.alternative.value = getRecVar("controller_alternative");
    document.currentForm.comment.value = getRecVar("controller_comment");
    document.currentForm.history.value = getRecVar("controller_history");
    document.currentForm.debug.value = getRecVar("controller_debug");
    document.currentForm.waitTime.value = getRecVar("controller_waitTime");
    document.currentForm.result.value = getRecVar("controller_result");
    var dir = getRecVar("controller_rec_dir");
    if (dir && dir != null) document.recordstartform.dir.value = getRecVar("controller_rec_dir");
}
function showTab(s) {
    if (top.main.location.href.indexOf(s + '.htm') != -1) return;
    hilightTab(s);
    top.main.location.href = s + '.htm'
}
function listProperties(){
    document.currentForm.debug.value = sahi()._eval("sahiList("+addSahi(document.currentForm.accessor.value)+")");
}
function initPlaybackTab() {
    var dir = getPbVar("controller_pb_dir");
    if (dir != null && dir != "") document.scriptfileform.dir.value = dir;
    document.scripturlform.url.value = getPbVar("controller_url");
    document.logForm.logs.value = getPbVar("controller_logs");
    document.scripturlform.starturl.value = getPbVar("controller_url_starturl");
    document.scriptfileform.starturl.value = getPbVar("controller_file_starturl");
    document.playform.step.value = getPbVar("controller_step");
    byFile(getPbVar("controller_show_url") != "true");
}
function displayInfo(info, escapedAccessor, escapedValue) {
    var f = document.currentForm;
    if (f) {
        f.elValue.value = escapedValue ? escapedValue : "";
        f.accessor.value = escapedAccessor;
        f.alternative.value = info.accessor;
    }
}

function resetValue(){
    var f = document.currentForm;
    try{
        f.elValue.value = getEvaluateExpressionResult(f.accessor.value);
    }catch(e){}
}

function setAPI(){
    var el = document.getElementById("apiTextbox");
//    try{
        el.value = document.getElementById("apiSelect").value;
//    }catch(e){}
}

function handleEnterKey(e, el){
    if (!e) e = window.event;
    if (e.keyCode && e.keyCode == 26){
        resetValue();
        return false;
    }
}

function addWait() {
    try {
        sahi().addWait(document.currentForm.waitTime.value);
    } catch(ex) {
        alert("Please enter the number of milliseconds to wait (should be >= 200)");
        document.currentForm.waitTime.value = 3000;
    }
}

function mark() {
    sahi().mark(document.currentForm.comment.value);
    //   sahiSendToServer('/_s_/dyn/Recorder_record?event=mark&value='+escape(document.currentForm.comment.value));
}

function getEvaluateExpressionResult(str){
    sahiSetServerVar("sahiEvaluateExpr", "true");
    var res = "";
    try {
        res = sahi()._eval(addSahi(str));
    } catch(e) {
        if (e.exceptionType && e.exceptionType == "SahiAssertionException") {
            res = "[Assertion Failed]" + (e.messageText?e.messageText:"");
        }
        else {
            res = "[Exception] " + e;
        }
        sahiHandleException(e);
    }
    sahiSetServerVar("sahiEvaluateExpr", "false");
    return res;
}

function evaluateExpr(showErr) {
    if (!showErr) showErr = false;
    document.currentForm.history.value += "\n" + document.currentForm.debug.value;
    var res = getEvaluateExpressionResult(document.currentForm.debug.value);
    if (showErr) {
        document.currentForm.result.value = "" + res;
    }
}
function demoClick() {
    setDebugValue("_click(" + document.currentForm.accessor.value + ");");
    evaluateExpr();
}
function demoHighlight() {
    setDebugValue("_highlight(" + document.currentForm.accessor.value + ");");
    evaluateExpr();
}

function demoSetValue() {
    var acc = document.currentForm.accessor.value;
    if (acc.indexOf("_select") == 0 || acc.indexOf('e("select")') != -1) {
        setDebugValue("_setSelected(" + acc + ", \"" + document.currentForm.elValue.value + "\");");
    } else
        setDebugValue("_setValue(" + acc + ", \"" + document.currentForm.elValue.value + "\");");
    evaluateExpr();
}
function setDebugValue(s) {
    document.currentForm.history.value += "\n" + document.currentForm.debug.value;
    document.currentForm.debug.value = s;
}
function append() {
    sahiSendToServer('/_s_/dyn/Recorder_record?cmd=' + escape(document.currentForm.debug.value));
}

function addSahi(s) {
    return sahiSendToServer("/_s_/dyn/ControllerUI_getSahiScript?code=" + sahi().escape(s));
}

function blankIfNull(s) {
    return (s == null || s == "null") ? "" : s;
}
function byFile(showFile) {
    document.getElementById("seturl").style.display = showFile?"none":"block";
    document.getElementById("setfile").style.display = showFile?"block":"none";
}
function checkScript(f) {
    if (f.file && f.file.value == "") {
        alert("Please choose a script file");
        return false;
    }
    if (f.url && f.url.value == "") {
        alert("Please specify the url to script file");
        return false;
    }
    return true;

}
function onScriptFormSubmit(f) {
    if (!checkScript(f)) return false;
    if (f.starturl.value == "") f.starturl.value = sahiOpener().location.href;
    var url = checkURL(f.starturl.value);
    resetStep();
    clearLogs();
    window.setTimeout("reloadPage('" + url + "')", 100);
}
function reloadPage(u) {
    if (u == "") {
        sahiOpener().location.reload(true);
    } else {
        sahiOpener().location.href = u;
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
function showHistory() {
    var histWin = window.open("history.htm", "sahi_history", "height=500px,width=450px");
}
function resizeTA2(el, minusRight, minusTop) {
    if (parseInt(navigator.appVersion) > 3) {
        if (navigator.appName == "Netscape") {
            winW = window.innerWidth;
            winH = window.innerHeight;
        }
        if (navigator.appName.indexOf("Microsoft") != -1) {
            winW = document.body.offsetWidth;
            winH = document.body.offsetHeight;
        }
    }
    el.style.width = winW - minusRight;
    el.style.height = winH - minusTop;
}
function showStack() {
    var curIx = document.playform.step.value;
    var win = window.open("blank.htm");
    var cmds = sahi().cmds;
    var s = "";
    for (var i = 0; i < cmds.length; i++) {
        var sel = (i == curIx - 1);
        s += "queue[" + i + "] = " + (sel?"<b>":"") + cmds[i] + (sel?"</b>":"") + "<br>";
    }
    s += "<br>Size: " + cmds.length;
    win.document.write(s);
    win.document.close();
}

function suggest(){
    var selectBox = document.getElementById("suggestDD");
    var accessor = document.currentForm.accessor.value;
    if (accessor.indexOf('.') != -1){
        var dot = accessor.lastIndexOf('.');
        var elStr = accessor.substring(0, dot);
        var prop = accessor.substring(dot + 1);
        var el = sahi()._eval(addSahi(elStr));
        selectBox.options.length = 0;
        for (var i in el){
            if (i.indexOf(prop) == 0)
                selectBox.options[selectBox.options.length] = new Option(i, i);            
        }
    }
}

function appendToAccessor(){
    var accessor = document.currentForm.accessor.value;
    if (accessor.indexOf('.') != -1){
        var dot = accessor.lastIndexOf('.');
        var elStr = accessor.substring(0, dot);
        var prop = accessor.substring(dot + 1);
        document.currentForm.accessor.value = elStr + "." + document.getElementById("suggestDD").value;
    }
}


// Suggest List start
var stripSahi = function (s){
	return s.replace(/sahi_/g, "_");
}
function getAccessorProps(str){
    var elStr = "window";
    var options = [];
    var dot = -1;
    if (str.indexOf('.') != -1){
        dot = str.lastIndexOf('.');
        elStr = str.substring(0, dot);
    }
	var prop = str.substring(dot + 1);
	var el = null;
	try{
        el = sahi()._eval(addSahi(elStr));
	}catch(e){}
	for (var i in el){
		i = stripSahi(i);
		if (i.indexOf(prop) == 0 && i != prop)
			options[options.length] = new Option(i, i);
	}
    return options;
}

function getAPIs(str){
    var options = [];                                       
    var el = null;
    try{
        el = sahi();
    }catch(e){}
    if (str == null || str == "") str = "_";
//    str = "sahi"+str;

    var d = "";

    for (var i in el){
        d += i + "<br>";
        if (i.indexOf(str) == 0 && i != str && el[i]){
            var val = i
            var fnStr = el[i].toString();
            var args = trim(fnStr.substring(fnStr.indexOf(" "), fnStr.indexOf("{")));
            if (args == "") continue; 
            val = i + args;
            val = stripSahi(val);
            options[options.length] = new Option(val, val);
        }
    }
//    alert(d);
    return options;
}
// Suggest List end
function hideAllSuggests(e){
    if (!e) e = window.event;
    if (e.keyCode == Suggest.KEY_ESCAPE){
        Suggest.hideAll();
    }
}
