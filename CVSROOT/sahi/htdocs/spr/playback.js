removeEvent(window, "load", sahiInit);
removeEvent(window, "beforeunload", sahiOnBeforeUnLoad);
addEvent(window, "load", sahiInit);
addEvent(window, "beforeunload", sahiOnBeforeUnLoad);
