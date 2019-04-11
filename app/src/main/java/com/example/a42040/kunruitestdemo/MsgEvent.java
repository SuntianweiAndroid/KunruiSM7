package com.example.a42040.kunruitestdemo;


public class MsgEvent {
	private String type;
	private Object data;

	public MsgEvent(String type, Object data) {
		super();
		this.data = data;
		this.type = type;
	}

	public Object getMsg() {
		return data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
