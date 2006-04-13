function sahiAdd(cmd, debugInfo){
	var d = debugInfo.split(":");
	document.write("<a href='_s_/scripts/"+d[0]+"#"+d[1]+"'>"+cmd+"</a>");
}
