package com.cocobabys.upload;

import java.io.InputStream;

import android.graphics.Bitmap;

public interface UploadMgr {
	public void uploadPhoto(Bitmap bitmap, String url) throws Exception;

	public void uploadPhoto(Bitmap bitmap, String url, String uptoken) throws Exception;

	public void uploadPhoto(InputStream is, String url, String uptoken) throws Exception;

	public void uploadFile(String filePath, String url, String uptoken) throws Exception;

}
