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
_sahi.removeEvent(window, "load", _sahiInit);
_sahi.removeEvent(window, "beforeunload", _sahiOnBeforeUnLoad);
_sahi.addEvent(window, "load", _sahiInit);
_sahi.addEvent(window, "beforeunload", _sahiOnBeforeUnLoad);
try{
if (!tried){
    if (_sahi.top()._sahi.isWinOpen){
		_sahi.top()._sahi.openWin();
	}
	tried = true;
}
}catch(e){}