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

INTERVAL = $interval;
ONERROR_INTERVAL = $onErrorInterval;
MAX_RETRIES = $maxRetries;
SAHI_MAX_WAIT_FOR_LOAD = $maxWaitForLoad;

sahiWaitForLoad = SAHI_MAX_WAIT_FOR_LOAD;
interval = INTERVAL;

}catch(e){}
//alert('$sessionId');
