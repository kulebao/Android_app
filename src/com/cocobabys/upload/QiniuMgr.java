package com.cocobabys.upload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.cocobabys.log.LogWriter;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import android.graphics.Bitmap;

public class QiniuMgr implements UploadMgr {
	private static final int COUNT_LIMIT = 1;
	private static final int MAX_UPLOAD_WAIT = 600;
	private CountDownLatch countDownLatch = new CountDownLatch(COUNT_LIMIT);
	private UpCompletionHandler listener;

	private volatile boolean sendSuccess = false;

	public synchronized boolean isSendSuccess() {
		return sendSuccess;
	}

	public synchronized void setSendSuccess(boolean sendSuccess) {
		this.sendSuccess = sendSuccess;
	}

	public QiniuMgr() {
		listener = new UpCompletionHandler() {
			@Override
			public void complete(String key, ResponseInfo info, JSONObject res) {
				boolean ok = info.isOK();
				if (ok) {
					setSendSuccess(true);
				} else {
					setSendSuccess(false);
					LogWriter.getInstance().print(QiniuMgr.class,
							"upload failed! key=" + key + " " + "res =" + info.toString());
				}
				countDownLatch.countDown();
			}
		};
	}

	public void uploadPhoto(Bitmap bitmap, String url, String uptoken) throws Exception {
		if (countDownLatch.getCount() != COUNT_LIMIT) {
			throw new Exception("Ilegal state count = " + countDownLatch.getCount());
		}
		byte[] bitmap2ByteArray = bitmap2ByteArray(bitmap);
		UploadManager uploadManager = new UploadManager();
		uploadManager.put(bitmap2ByteArray, url, uptoken, listener, null);

		checkResult(url);
	}

	private void checkResult(String url) throws Exception {
		try {
			countDownLatch.await(MAX_UPLOAD_WAIT, TimeUnit.SECONDS);
		} catch (Exception e) {
			LogWriter.getInstance().print(QiniuMgr.class, "upload failed! timeout! url=" + url);
			throw e;
		}

		if (!sendSuccess) {
			throw new Exception("upload faile! url=" + url);
		}
	}

	public void uploadFile(String filePath, String url, String uptoken, UpProgressHandler progressHandler)
			throws Exception {
		if (countDownLatch.getCount() != COUNT_LIMIT) {
			throw new Exception("Ilegal state count = " + countDownLatch.getCount());
		}
		UploadManager uploadManager = new UploadManager();
		uploadManager.put(filePath, url, uptoken, listener,
				new UploadOptions(null, null, false, progressHandler, null));

		checkResult(url);
	}

	public void uploadFile(String filePath, String url, String uptoken) throws Exception {
		if (countDownLatch.getCount() != COUNT_LIMIT) {
			throw new Exception("Ilegal state count = " + countDownLatch.getCount());
		}
		UploadManager uploadManager = new UploadManager();
		uploadManager.put(filePath, url, uptoken, listener, null);

		checkResult(url);
	}

	private InputStream bitmap2InputStream(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 90, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}

	private byte[] bitmap2ByteArray(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 90, baos);
		return baos.toByteArray();
	}

	@Override
	public void uploadPhoto(Bitmap bitmap, String url) throws Exception {
		throw new Exception("do not support this method!");
	}

	@Override
	public void uploadPhoto(InputStream is, String url, String uptoken) throws Exception {
		throw new Exception("do not support this method!");
	}

}
