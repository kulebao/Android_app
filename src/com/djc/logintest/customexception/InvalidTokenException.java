package com.djc.logintest.customexception;

public class InvalidTokenException extends CustomException {

	public InvalidTokenException() {
		super();
	}

	public InvalidTokenException(String detailMessage) {
		super(detailMessage);
	}

}
