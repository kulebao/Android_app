package com.cocobabys.upload;

public class UploadFactory {
	// public static String CLOUD_STORAGE_HOST =
	// "http://cocobabys.qiniudn.com/";
	public static String CLOUD_STORAGE_HOST = "https://dn-cocobabys.qbox.me/";
	public static String BUCKET_NAME = "cocobabys";

	private UploadFactory() {
	}

	public static UploadMgr createUploadMgr() {
		return new QiniuMgr();
	}
}
