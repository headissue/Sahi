var d = document.domain;
var ix = d.lastIndexOf(".");
ix = d.lastIndexOf(".", ix-1);
if (ix!=-1){
    //document.domain = d.substring(ix+1)
}