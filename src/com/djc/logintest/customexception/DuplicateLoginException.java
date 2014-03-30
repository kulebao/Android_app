package com.djc.logintest.customexception;

public class DuplicateLoginException extends CustomException {

	private static final long serialVersionUID = 1L;

	public DuplicateLoginException() {
		super();
	}

	public DuplicateLoginException(String detailMessage) {
		super(detailMessage);
	}

}
