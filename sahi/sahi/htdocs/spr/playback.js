removeEvent(window, "load", sahiInit);
removeEvent(window, "beforeunload", sahiOnBeforeUnLoad);
addEvent(window, "load", sahiInit);
addEvent(window, "beforeunload", sahiOnBeforeUnLoad);
try{
if (!tried){
	if (top._isSahiWinOpen){
		top.sahiOpenWin();
	}
	tried = true;
}
}catch(e){}