package com.cocobabys.bean;

import java.io.File;

import com.cocobabys.utils.Utils;

public class AdInfo {
	private static String AD_PATH = "ad_path";

	private int position_id = 0;
	private String link = "";
	private String image = "";
	private String name = "";

	public int getPosition_id() {
		return position_id;
	}

	public void setPosition_id(int position_id) {
		this.position_id = position_id;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocalFileName() {
		String dir = Utils.getSDCardPicRootPath() + File.separator + AD_PATH
				+ File.separator;
		Utils.mkDirs(dir);

		return dir + position_id;
	}

}
