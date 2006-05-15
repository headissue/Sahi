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
