package net.sf.sahi.response;

public class NoContentResponse extends HttpResponse {
	public NoContentResponse(){
        setFirstLine("HTTP/1.0 204 No Content");
        setHeader("Cache-Control", "no-cache");
        setHeader("Pragma", "no-cache");
        setHeader("Expires", "0");
        //setHeader("Content-Length", "0");
        setRawHeaders(getRebuiltHeaderBytes());
	}
}
