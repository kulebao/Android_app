package com.djc.logintest.customexception;

public class DecodeBitmapException extends CustomException {

	private static final long serialVersionUID = 1L;

	public DecodeBitmapException() {
		super();
	}

	public DecodeBitmapException(String detailMessage) {
		super(detailMessage);
	}

}
