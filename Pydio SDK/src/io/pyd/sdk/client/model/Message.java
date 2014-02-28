package io.pyd.sdk.client.model;

public class Message extends Exception{

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;

	
	private String message;
	private int code = -1;
	private int type = -1;
	
	
	public int getType() {
		return type;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public void setCode(int code){
		this.code = code;
	}
	
	
	public static Message create(int code, int type, String message){
		Message m = new Message();
		m.code = code;
		m.type = type;
		m.message = message;
		return m;
	}
	
}