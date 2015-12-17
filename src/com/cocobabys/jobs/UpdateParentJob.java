package com.cocobabys.jobs;

import com.cocobabys.bean.FullParentInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.ParentMethod;
import com.cocobabys.net.UploadTokenMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.upload.UploadFactory;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.MethodUtils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class UpdateParentJob extends MyJob {
	private Handler handler;
	private Bitmap bitmap = null;
	private String portrait;
	// url 是保存在云存储的相对路径
	private String relativePath;

	public UpdateParentJob(Handler handler, String portrait, Bitmap bitmap, String relativePath) {
		this.handler = handler;
		this.portrait = portrait;
		this.relativePath = relativePath;
		this.bitmap = bitmap;
	}

	@Override
	public void run() {
		MethodResult bret = new MethodResult(EventType.UPDATE_PARENT_FAIL);
		try {
			if (bitmap != null) {
				String uploadToken = UploadTokenMethod.getMethod().getUploadToken(relativePath);
				if (TextUtils.isEmpty(uploadToken)) {
					return;
				}
				UploadFactory.createUploadMgr().uploadPhoto(bitmap, relativePath, uploadToken);

				final FullParentInfo parent = ParentMethod.getMethod().getParent(DataUtils.getAccount());

				if (parent == null) {
					return;
				}

				parent.setPortrait(portrait);

				MyProxy proxy = new MyProxy();
				MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
					@Override
					public Object handle() throws Exception {
						return ParentMethod.getMethod().updateParent(parent);
					}
				});

				bret = MethodUtils.getBindResult(bind);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Message msg = Message.obtain();
			msg.what = bret.getResultType();
			msg.obj = bitmap;
			handler.sendMessage(msg);
		}
	}
}
