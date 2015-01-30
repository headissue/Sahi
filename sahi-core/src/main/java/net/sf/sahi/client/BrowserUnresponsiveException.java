package net.sf.sahi.client;

public class BrowserUnresponsiveException extends RuntimeException{
	private static final long serialVersionUID = 5906800128428514815L;
	private final String message;
	public BrowserUnresponsiveException(String message) {
		super();
		this.message = message;
	}
	public String toString(){
		return this.getClass().getName()+" : "+message;
	}

}
