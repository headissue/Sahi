_sahisid = '$sessionId';
_isSahiWinOpen = $isWindowOpen;
try{
top._sahisid = '$sessionId';
top._isSahiWinOpen = $isWindowOpen;
top.sahiCreateCookie('sahisid', '$sessionId');
sahiHotKey = '$hotkey';
}catch(e){}
//alert('$sessionId');
