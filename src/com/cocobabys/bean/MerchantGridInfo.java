package com.cocobabys.bean;

import com.cocobabys.constant.ConstantValue;

public class MerchantGridInfo {

	// 未选中情况下的背景图片id
	private int imageID;

	// 选中情况下的背景图片id
	private int selectedImageID;
	private int category = ConstantValue.MERCHANT_CATEGORY_CAMERA;

	public MerchantGridInfo(int imageID, int selectedImageID, int category) {
		this.imageID = imageID;
		this.selectedImageID = selectedImageID;
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

	public int getSelectedImageID() {
		return selectedImageID;
	}

	public void setSelectedImageID(int selectedImageID) {
		this.selectedImageID = selectedImageID;
	}

}
