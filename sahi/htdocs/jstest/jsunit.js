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
_testFns = new Array();
function assertTrue(msg, bool){
    if (!bool) alert(msg);
}
function assertEquals(msg, expected, actual){
    if (expected != actual) alert(msg+"\nExpected=["+expected+"]\nActual=["+actual+"]");
}
/*
function testOnLoad(){
    for (var j in  window){
        var el = window[j]
        try{
            if (el && el.name && el.name.indexOf("test") != -1 && el.name != "testOnLoad"){
                alert("Executing "+el.name);
                el();
            }
        } catch(ex){alert(ex);}
    }
}
*/
function testOnLoad(){
    for (var j=0; j<_testFns.length; j++){
        try{
            _testFns[j]();
        } catch(ex){alert(ex);}
    }
}
function addTest(testFn){
    _testFns[_testFns.length] = testFn;
}
function list(el){
    var s="";
    var j=0;
    for (var i in el){
        s+=i+",";
        j++;
        if (j%4==0) s+="\n";
    }
    alert(s);
}
