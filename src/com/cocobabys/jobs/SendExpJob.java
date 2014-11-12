package com.cocobabys.jobs;

import java.io.File;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.net.ExpMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.UploadTokenMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.upload.UploadFactory;
import com.cocobabys.upload.UploadMgr;
import com.cocobabys.utils.MethodUtils;
import com.cocobabys.utils.Utils;

public class SendExpJob extends MyJob {
	private static final int STANDARD_PIC = 1080 * 1080;
	private Handler handler;
	private String text;
	private List<String> mediums;

	public SendExpJob(Handler handler, String text, List<String> mediums) {
		this.handler = handler;
		this.text = text;
		this.mediums = mediums;
	}

	@Override
	public void run() {
		MethodResult bret = new MethodResult(EventType.POST_EXP_FAIL);
		try {
			uploadBmpToServer();

			final String content = getContent();
			MyProxy proxy = new MyProxy();
			MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
				@Override
				public MethodResult handle() throws Exception {
					MethodResult result = ExpMethod.getMethod().sendExp(content);
					return result;
				}
			});

			bret = MethodUtils.getBindResult(bind);
			saveBmpToSDCard();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Message msg = Message.obtain();
			msg.what = bret.getResultType();
			handler.sendMessage(msg);
		}

	}

	private String getContent() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(JSONConstant.TOPIC, DataMgr.getInstance().getSelectedChild().getServer_id());
		jsonObject.put(ExpInfo.CONTENT, text);

		JSONObject senderObj = new JSONObject();

		String server_id = DataMgr.getInstance().getSelfInfoByPhone().getParent_id();
		senderObj.put(JSONConstant.ID, server_id);
		senderObj.put(JSONConstant.TYPE, ExpInfo.PARENT_TYPE);

		jsonObject.put(JSONConstant.SENDER, senderObj);

		if (!mediums.isEmpty()) {
			JSONArray array = new JSONArray();
			for (String url : mediums) {
				JSONObject object = new JSONObject();
				object.put(JSONConstant.URL, UploadFactory.CLOUD_STORAGE_HOST + Utils.getExpRelativePath(url));
				object.put(JSONConstant.TYPE, JSONConstant.IMAGE_TYPE);
				array.put(object);
			}

			jsonObject.put(ExpInfo.MEDIUM, array);
		}

		return jsonObject.toString();
	}

	private void uploadBmpToServer() throws Exception {
		String uploadToken = UploadTokenMethod.getMethod().getUploadToken("");
		if (TextUtils.isEmpty(uploadToken)) {
			throw new RuntimeException("getUploadToken failed ");
		}

		UploadMgr uploadMgr = UploadFactory.createUploadMgr();

		for (int i = 0; i < mediums.size(); i++) {
			String sdCardPath = mediums.get(i);
			String name = Utils.getExpRelativePath(sdCardPath);
			uploadImpl(uploadToken, uploadMgr, sdCardPath, name);
			sendProgressEvent(i + 2);
		}

	}

	private void sendProgressEvent(int progress) {
		Message obtain = Message.obtain();
		obtain.what = EventType.UPLOAD_ICON_SUCCESS;
		obtain.arg1 = progress;
		obtain.arg2 = ConstantValue.DO_NOT_CANCEL_DIALOG;
		handler.sendMessage(obtain);
	}

	private void uploadImpl(String uploadToken, UploadMgr uploadMgr, String url, String name) throws Exception {
		Bitmap bitmap = Utils.getLoacalBitmap(url, STANDARD_PIC);

		try {
			uploadMgr.uploadPhoto(bitmap, name, uploadToken);
		} catch (Exception e) {
			e.printStackTrace();
			Thread.sleep(500);
			// 重试一次
			uploadMgr.uploadPhoto(bitmap, name, uploadToken);
		}
	}

	// 将图库中的照片保存到应用目录，注意要与ExpInfo中serverUrlToLocalUrl方法的自己发送目录相对应，这样就不用再到服务器上去下载
	private void saveBmpToSDCard() throws Exception {
		for (int i = 0; i < mediums.size(); i++) {
			String sdCardPath = mediums.get(i);
			Bitmap loacalBitmap = Utils.getLoacalBitmap(mediums.get(i), STANDARD_PIC);
			String path = Utils.getSDCardPicRootPath() + File.separator + Utils.getExpRelativePath(sdCardPath);
			Utils.makeDirs(Utils.getDir(path));
			Utils.saveBitmapToSDCard(loacalBitmap, path);
			loacalBitmap.recycle();
		}
	}

}
