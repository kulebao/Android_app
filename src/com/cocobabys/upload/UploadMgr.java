package com.cocobabys.upload;

import java.io.InputStream;

import android.graphics.Bitmap;

public interface UploadMgr {
	public void uploadPhoto(Bitmap bitmap, String url);

	public void uploadPhoto(Bitmap bitmap, String url, String uptoken);

	public void uploadPhoto(InputStream is, String url, String uptoken);

	public void uploadFile(String filePath, String url, String uptoken);

}
