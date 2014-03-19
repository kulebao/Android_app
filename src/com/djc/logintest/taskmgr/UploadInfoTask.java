package com.djc.logintest.taskmgr;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.handler.TaskResultHandler;
import com.djc.logintest.net.UploadChildInfoMethod;
import com.djc.logintest.net.UploadTokenMethod;
import com.djc.logintest.proxy.MyProxy;
import com.djc.logintest.proxy.MyProxyImpl;
import com.djc.logintest.upload.UploadFactory;
import com.djc.logintest.utils.Utils;

public class UploadInfoTask extends AsyncTask<Void, Void, Integer> {
	private TaskResultHandler hander;
	private Bitmap bitmap = null;
	private String content;

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public UploadInfoTask(TaskResultHandler handler, String content) {
		this.hander = handler;
		this.content = content;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		int bret = EventType.UPLOAD_FAILED;
		if (bitmap != null) {
			try {
				// url 是保存在云存储的相对路径
				String url = Utils.getUploadChildUrl();
				String uploadToken = UploadTokenMethod.getMethod()
						.getUploadToken(url);
				if (TextUtils.isEmpty(uploadToken)) {
					return bret;
				}
				// OSSMgr.UploadPhoto(bitmap,url);
				UploadFactory.createUploadMgr().UploadPhoto(bitmap, url,
						uploadToken);

			} catch (Exception e) {
				// 如果上传文件失败，直接返回错误
				e.printStackTrace();
				return bret;
			}
		}

		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public Object handle() throws Exception {
				return UploadChildInfoMethod.getMethod().uploadChildInfo(
						content);
			}
		});

		bret = EventType.NET_WORK_INVALID;
		try {
			bret = (Integer) bind.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bret;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		hander.handleResult(result, bitmap);
	}

}
