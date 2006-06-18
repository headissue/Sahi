_sahisid = '$sessionId';
try{
top._sahisid = '$sessionId';
top._isSahiWinOpen = $isWindowOpen;
top.sahiCreateCookie('sahisid', '$sessionId');
top._isSahiWinOpen = $isWindowOpen;
top._isSahiPaused = $isSahiPaused;
top._isSahiPlaying = $isSahiPlaying;
top._isSahiRecording = $isSahiRecording;
sahiHotKey = '$hotkey';
}catch(e){}
//alert('$sessionId');
