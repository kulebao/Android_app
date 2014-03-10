package com.djc.logintest.upload;

public class UploadFactory {
	public static String CLOUD_STORAGE_HOST = "http://cocobabys.qiniudn.com/";
	public static String BUCKET_NAME = "cocobabys";

	private UploadFactory() {
	}

	public static UploadMgr createUploadMgr() {
		return new QiniuMgr();
	}
}