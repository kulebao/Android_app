package com.cocobabys.bean;

import com.cocobabys.constant.ConstantValue;

public class MerchantGridInfo {
	private int imageID;
	private int category = ConstantValue.MERCHANT_CATEGORY_CAMERA;

	public MerchantGridInfo(int imageID, int category) {
		this.imageID = imageID;
		this.category = category;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getImageID() {
		return imageID;
	}

	public void setImageID(int imageID) {
		this.imageID = imageID;
	}

}
