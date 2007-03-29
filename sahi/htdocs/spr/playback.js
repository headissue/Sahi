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
_sahi.removeEvent(window, "load", function(e){_sahi.init(e);});
_sahi.removeEvent(window, "beforeunload", function(){_sahi.onBeforeUnLoad();});
_sahi.addEvent(window, "load", function(e){_sahi.init(e);});
//Sahi.prototype.real_onbeforeunload = window.onbeforeunload;
//window.onbeforeunload =  function(){_sahi.onBeforeUnLoad();}
_sahi.addEvent(window, "beforeunload", function(){_sahi.onBeforeUnLoad();});
try{
if (!tried){
    if (_sahi.isWinOpen){
		_sahi.openWin();
	}
	tried = true;
}
}catch(e){}