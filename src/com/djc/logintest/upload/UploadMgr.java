package com.djc.logintest.upload;

import com.djc.logintest.upload.qiniu.auth.JSONObjectRet;

import android.graphics.Bitmap;

public interface UploadMgr {
	public void UploadPhoto(Bitmap bitmap, String url);

	public void UploadPhoto(Bitmap bitmap, String url, String uptoken);
}
