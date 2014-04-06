package com.cocobabys.customexception;

public class BindFailException extends CustomException {

	private static final long serialVersionUID = 1L;

	public BindFailException() {
		super();
	}

	public BindFailException(String detailMessage) {
		super(detailMessage);
	}

}
