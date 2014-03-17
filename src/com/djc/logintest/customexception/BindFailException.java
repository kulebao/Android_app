package com.djc.logintest.customexception;

public class BindFailException extends CustomException {

	public BindFailException() {
		super();
	}

	public BindFailException(String detailMessage) {
		super(detailMessage);
	}

}
