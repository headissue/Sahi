sahiRemoveEvent(window, "load", sahiInit);
sahiRemoveEvent(window, "beforeunload", sahiOnBeforeUnLoad);
sahiAddEvent(window, "load", sahiInit);
sahiAddEvent(window, "beforeunload", sahiOnBeforeUnLoad);
try{
if (!tried){
	if (top._isSahiWinOpen){
		top.sahiOpenWin();
	}
	tried = true;
}
}catch(e){}