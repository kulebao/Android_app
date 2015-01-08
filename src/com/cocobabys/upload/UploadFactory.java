package com.cocobabys.upload;

import com.cocobabys.activities.MyApplication;

public class UploadFactory {
	// public static String CLOUD_STORAGE_HOST =
	// "http://cocobabys.qiniudn.com/";
	private static String CLOUD_STORAGE_HOST_TEST = "https://dn-cocobabys-test.qbox.me/";
	private static String BUCKET_NAME_TEST = "cocobabys-test";

	private static String CLOUD_STORAGE_HOST = "https://dn-cocobabys.qbox.me/";
	private static String BUCKET_NAME = "cocobabys";

	private UploadFactory() {
	}

	public static String getUploadHost() {
		if (MyApplication.getInstance().isForTest()) {
			return CLOUD_STORAGE_HOST_TEST;
		} else {
			return CLOUD_STORAGE_HOST;
		}
	}

	public static String getUploadBucket() {
		if (MyApplication.getInstance().isForTest()) {
			return BUCKET_NAME_TEST;
		} else {
			return BUCKET_NAME;
		}
	}

	public static UploadMgr createUploadMgr() {
		return new QiniuMgr();
	}
}
