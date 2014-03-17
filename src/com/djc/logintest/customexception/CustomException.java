package com.djc.logintest.customexception;

public class CustomException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CustomException() {
		super();
	}

	public CustomException(String detailMessage) {
		super(detailMessage);
	}

}
