package com.djc.logintest.taskmgr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.customexception.DuplicateLoginException;
import com.djc.logintest.customexception.InvalidTokenException;
import com.djc.logintest.dbmgr.info.ChatInfo;
import com.djc.logintest.net.ChatMethod;
import com.djc.logintest.net.UploadTokenMethod;
import com.djc.logintest.upload.UploadFactory;
import com.djc.logintest.utils.Utils;

public class UploadChatIconTask extends AsyncTask<Void, Void, Integer> {
	private Handler handler;
	private Bitmap bitmap = null;
	private int lastid;
	List<ChatInfo> list = new ArrayList<ChatInfo>();

	public UploadChatIconTask(Handler handler, Bitmap bitmap, int lastid) {
		this.handler = handler;
		this.lastid = lastid;
		this.bitmap = bitmap;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		int result = EventType.NET_WORK_INVALID;
		boolean networkConnected = Utils.isNetworkConnected(MyApplication
				.getInstance());
		if (networkConnected) {
			try {
				String url = uploadBmpToServer();
				saveBmpToSDCard(url);

				// 上传到云服务器后，生成的外部链接
				String image = UploadFactory.CLOUD_STORAGE_HOST + url;
				list = ChatMethod.getMethod().sendChat(
						formatChatContent(image), lastid);
				result = EventType.SUCCESS;
			} catch (InvalidTokenException e) {
				result = EventType.TOKEN_INVALID;
			} catch (DuplicateLoginException e) {
				result = EventType.PHONE_NUM_IS_ALREADY_LOGIN;
			} catch (Exception e) {
				// 如果上传文件失败，直接返回错误
				e.printStackTrace();
				result = EventType.FAIL;
			}
		}
		return result;
	}

	private String formatChatContent(String image) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(ChatInfo.CONTENT, "");
			jsonObject.put("phone", Utils.getProp(JSONConstant.ACCOUNT_NAME));
			jsonObject.put(JSONConstant.TIME_STAMP, System.currentTimeMillis());
			jsonObject.put(ChatInfo.SENDER, "");
			jsonObject.put("image", image);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
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
		String url = Utils.getChatIconUrl(System.currentTimeMillis());
		// OSSMgr.UploadPhoto(bitmap, url);
		// url 是保存在云存储的相对路径
		String uploadToken = UploadTokenMethod.getMethod().getUploadToken("");
		if (TextUtils.isEmpty(uploadToken)) {
			throw new RuntimeException("getUploadToken failed ");
		}
		UploadFactory.createUploadMgr().UploadPhoto(bitmap, url, uploadToken);
		return url;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		Message msg = Message.obtain();
		msg.what = result;
		msg.obj = list;
		handler.sendMessage(msg);
	}

}
