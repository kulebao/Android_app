package com.djc.logintest.upload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.graphics.Bitmap;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.upload.qiniu.io.IO;
import com.djc.logintest.upload.qiniu.io.PutExtra;
import com.djc.logintest.upload.qiniu.utils.InputStreamAt;

public class QiniuMgr implements UploadMgr {

	QiniuMgr() {
	}

	@Override
	public void UploadPhoto(Bitmap bitmap, String url, String uptoken) {
		InputStream is = Bitmap2InputStream(bitmap);
		InputStreamAt inputStreamAt = InputStreamAt.fromInputStream(
				MyApplication.getInstance(), is);
		PutExtra extra = new PutExtra();
		IO.putFileEx(uptoken, url, inputStreamAt, extra);
	}

	private InputStream Bitmap2InputStream(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}

	@Override
	public void UploadPhoto(Bitmap bitmap, String url) {
		throw new RuntimeException(
				"NOT SUPPORT UploadPhoto(Bitmap bitmap, String url) method!!");
	}
}
