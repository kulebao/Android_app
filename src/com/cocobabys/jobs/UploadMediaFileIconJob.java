package com.cocobabys.jobs;

import java.io.File;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.NewChatMethod;
import com.cocobabys.net.UploadTokenMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.upload.UploadFactory;
import com.cocobabys.utils.MethodUtils;
import com.cocobabys.utils.Utils;

public class UploadMediaFileIconJob extends MyJob {
	private Handler handler;
	private long lastid;
	private String childid;
	private String mediaPath = "";
	private String mediaType = "";

	public UploadMediaFileIconJob(Handler handler, String mediaPath,
			String mediaType, long lastid, String childid) {
		this.handler = handler;
		this.lastid = lastid;
		this.mediaPath = mediaPath;
		this.mediaType = mediaType;
		this.childid = childid;
	}

	@Override
	public void run() {
		MethodResult bret = new MethodResult(EventType.SEND_CHAT_FAIL);
		try {
			String url = uploadFileToServer();
			// 上传到云服务器后，生成的外部链接
			String media = UploadFactory.getUploadHost() + url;
			final String content = InfoHelper.formatChatContent("", media,
					childid, mediaType);

			MyProxy proxy = new MyProxy();
			MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
				@Override
				public MethodResult handle() throws Exception {
					MethodResult result = NewChatMethod.getMethod().sendChat(
							content, lastid, childid);
					return result;
				}
			});

			bret = MethodUtils.getBindResult(bind);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Message msg = Message.obtain();
			msg.what = bret.getResultType();
			msg.obj = bret.getResultObj();
			handler.sendMessage(msg);
		}

	}

	private String uploadFileToServer() throws Exception {
		String url = mediaPath.replace(Utils.getSDCardMediaRootPath(mediaType)
				+ File.separator, "");
		String uploadToken = UploadTokenMethod.getMethod().getUploadToken("");
		if (TextUtils.isEmpty(uploadToken)) {
			throw new RuntimeException("getUploadToken failed ");
		}
		Log.d("DDD", "uploadFileToServer voice url=" + url);
		UploadFactory.createUploadMgr().uploadFile(mediaPath, url, uploadToken);
		return url;
	}
}
