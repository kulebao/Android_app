package com.cocobabys.jobs;

import java.io.File;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.dbmgr.info.NativeMediumInfo;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.NewChatMethod;
import com.cocobabys.net.UploadTokenMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.upload.UploadFactory;
import com.cocobabys.utils.MethodUtils;
import com.cocobabys.utils.Utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class UploadChatIconJob extends MyJob {
	private Handler handler;
	private Bitmap bitmap = null;
	private long lastid;
	private String childid;
	// 文件名关键字
	private long currentTimeMillis;
	private String bitmapPath;

	public UploadChatIconJob(Handler handler, Bitmap bitmap, long lastid,
			String childid, String bitmapPath) {
		this.handler = handler;
		this.lastid = lastid;
		this.bitmap = bitmap;
		this.childid = childid;
		this.bitmapPath = bitmapPath;
	}

	@Override
	public void run() {
		currentTimeMillis = System.currentTimeMillis();
		MethodResult bret = new MethodResult(EventType.SEND_CHAT_FAIL);
		try {
			String url = uploadBmpToServer();
			// 上传到云服务器后，生成的外部链接
			String image = UploadFactory.getUploadHost() + url;
			final String content = InfoHelper.formatChatContent("", image,
					childid, JSONConstant.IMAGE_TYPE);

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
			
			NativeMediumInfo info = new NativeMediumInfo();
			info.setKey(currentTimeMillis+"");
			info.setValue(bitmapPath);
			DataMgr.getInstance().addNativeMediumInfo(info);
			
			// saveBmpToSDCard(url);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Message msg = Message.obtain();
			msg.what = bret.getResultType();
			msg.obj = bret.getResultObj();
			handler.sendMessage(msg);
		}

	}

	private void saveBmpToSDCard(String url) throws Exception {
		String path = Utils.getSDCardPicRootPath() + File.separator + url;
		String dir = Utils.getDir(path);
		Utils.makeDirs(dir);
		Log.d("DDD", "saveBmpToSDCard url=" + url);
		Log.d("DDD", "saveBmpToSDCard dir=" + dir);
		Log.d("DDD", "saveBmpToSDCard path=" + path);
		Utils.saveBitmapToSDCard(bitmap, path);
	}

	private String uploadBmpToServer() throws Exception {
		String url = Utils.getChatIconUrl(currentTimeMillis);
		String uploadToken = UploadTokenMethod.getMethod().getUploadToken("");
		if (TextUtils.isEmpty(uploadToken)) {
			throw new RuntimeException("getUploadToken failed ");
		}
		UploadFactory.createUploadMgr().uploadPhoto(bitmap, url, uploadToken);
		return url;
	}

}
