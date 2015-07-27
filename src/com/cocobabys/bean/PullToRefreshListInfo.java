package com.cocobabys.bean;

import com.cocobabys.constant.ConstantValue;

public class PullToRefreshListInfo {
	private int to = 0;
	private int from = 0;
	private int most = 0;
	private int type = ConstantValue.TYPE_GET_HEAD;

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getMost() {
		return most;
	}

	public void setMost(int most) {
		this.most = most;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
