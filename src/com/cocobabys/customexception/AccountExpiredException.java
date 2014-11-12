package com.cocobabys.customexception;

public class AccountExpiredException extends CustomException {

	private static final long serialVersionUID = 1L;

	public AccountExpiredException() {
		super();
	}

	public AccountExpiredException(String detailMessage) {
		super(detailMessage);
	}

}
