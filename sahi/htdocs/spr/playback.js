/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
_sahi.removeEvent(window, "load", Sahi.init);
_sahi.removeEvent(window, "beforeunload", Sahi.onBeforeUnLoad);
_sahi.addEvent(window, "load", Sahi.init);
_sahi.addEvent(window, "beforeunload", Sahi.onBeforeUnLoad);
try{
if (!tried){
    if (_sahi.isWinOpen){
        _sahi.openWin();
    }
    tried = true;
}
}catch(e){}