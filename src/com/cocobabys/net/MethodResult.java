package com.cocobabys.net;

import com.cocobabys.constant.EventType;

public class MethodResult {
	private int resultType = EventType.SERVER_INNER_ERROR;
	private Object resultObj = new Object();

	public MethodResult() {
	}

	public MethodResult(int resultType, Object resultObj) {
		this(resultType);
		this.resultObj = resultObj;
	}

	public MethodResult(int resultType) {
		this.resultType = resultType;
	}

	public int getResultType() {
		return resultType;
	}

	public void setResultType(int resultType) {
		this.resultType = resultType;
	}

	public Object getResultObj() {
		return resultObj;
	}

	public void setResultObj(Object resultObj) {
		this.resultObj = resultObj;
	}

}
