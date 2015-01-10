package com.cocobabys.upload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.graphics.Bitmap;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.upload.qiniu.io.IO;
import com.cocobabys.upload.qiniu.io.PutExtra;
import com.cocobabys.upload.qiniu.utils.InputStreamAt;

public class QiniuMgr implements UploadMgr {

	QiniuMgr() {
	}

	@Override
	public void uploadPhoto(Bitmap bitmap, String url, String uptoken) {
		InputStream is = bitmap2InputStream(bitmap);
		uploadPhoto(is, url, uptoken);
	}

	@Override
	public void uploadPhoto(InputStream is, String url, String uptoken) {
		InputStreamAt inputStreamAt = InputStreamAt.fromInputStream(MyApplication.getInstance(), is);
		PutExtra extra = new PutExtra();
		IO.putFileEx(uptoken, url, inputStreamAt, extra);
	}

	@Override
	public void uploadFile(String filePath, String url, String uptoken) {
		try {
			InputStream inputStream = new FileInputStream(new File(filePath));
			uploadPhoto(inputStream, url, uptoken);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private InputStream bitmap2InputStream(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 90, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}

	@Override
	public void uploadPhoto(Bitmap bitmap, String url) {
		throw new RuntimeException("NOT SUPPORT UploadPhoto(Bitmap bitmap, String url) method!!");
	}

}
