/**
 * Copyright V Narayan Raman
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
