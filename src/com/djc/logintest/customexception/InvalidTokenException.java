package com.djc.logintest.customexception;

public class InvalidTokenException extends CustomException {

	private static final long serialVersionUID = 1L;

	public InvalidTokenException() {
		super();
	}

	public InvalidTokenException(String detailMessage) {
		super(detailMessage);
	}

}
