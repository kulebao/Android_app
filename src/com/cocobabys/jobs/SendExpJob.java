package com.cocobabys.jobs;

import java.io.File;
import java.util.ArrayList;
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
import com.cocobabys.dbmgr.info.NativeMediumInfo;
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
	private static final int STANDARD_PIC = 600 * 800;
	private Handler handler;
	private String text;
	private List<String> mediums;
	// 与mediums 一一对应
	private List<String> nativePath = new ArrayList<String>();
	private String mediumType;
	// 当做文件名关键字
	private long currentTimeMillis;

	public SendExpJob(Handler handler, String text, List<String> mediums,
			String mediumType) {
		this.handler = handler;
		this.text = text;
		this.mediums = mediums;
		this.mediumType = mediumType;
	}

	@Override
	public void run() {
		currentTimeMillis = System.currentTimeMillis();
		MethodResult bret = new MethodResult(EventType.POST_EXP_FAIL);
		try {
			uploadFileToServer();

			final String content = getContent();
			MyProxy proxy = new MyProxy();
			MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
				@Override
				public MethodResult handle() throws Exception {
					MethodResult result = ExpMethod.getMethod()
							.sendExp(content);
					return result;
				}
			});

			bret = MethodUtils.getBindResult(bind);

			saveData(bret);

			// saveBmpToSDCard();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Message msg = Message.obtain();
			msg.what = bret.getResultType();
			handler.sendMessage(msg);
		}

	}

	private void saveData(MethodResult bret) {
		if (!mediums.isEmpty()) {
			// 该信息已经保存到数据库，这里如果存在medium，将medium路径保存到数据库，以免从本地选择的资源又再次到服务器下载

			List<NativeMediumInfo> list = new ArrayList<NativeMediumInfo>();

			for (int i = 0; i < mediums.size(); i++) {
				NativeMediumInfo info = new NativeMediumInfo();
				info.setKey(nativePath.get(i));
				info.setValue(mediums.get(i));
				list.add(info);
			}
			DataMgr.getInstance().addNativeMediumInfoList(list);

			// 如果发送的是视频文件，此时保存对应的nail
			ExpInfo expInfo = (ExpInfo) bret.getResultObj();
			if (JSONConstant.VIDEO_TYPE.equals(mediumType)) {
				saveAndCompressNail(expInfo);
			}
		}
	}

	private String getContent() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(JSONConstant.TOPIC, DataMgr.getInstance()
				.getSelectedChild().getServer_id());
		jsonObject.put(ExpInfo.CONTENT, text);

		JSONObject senderObj = new JSONObject();

		String server_id = DataMgr.getInstance().getSelfInfoByPhone()
				.getParent_id();
		senderObj.put(JSONConstant.ID, server_id);
		senderObj.put(JSONConstant.TYPE, ExpInfo.PARENT_TYPE);

		jsonObject.put(JSONConstant.SENDER, senderObj);

		if (!mediums.isEmpty()) {
			JSONArray array = new JSONArray();
			for (int i = 0; i < mediums.size(); i++) {
				JSONObject object = new JSONObject();
				object.put(JSONConstant.URL, UploadFactory.getUploadHost()
						+ Utils.getExpRelativePathExt(nativePath.get(i)));
				object.put(JSONConstant.TYPE, mediumType);
				array.put(object);
			}

			jsonObject.put(ExpInfo.MEDIUM, array);
		}

		return jsonObject.toString();
	}

	private void uploadFileToServer() throws Exception {
		String uploadToken = UploadTokenMethod.getMethod().getUploadToken("");
		if (TextUtils.isEmpty(uploadToken)) {
			throw new RuntimeException("getUploadToken failed ");
		}

		UploadMgr uploadMgr = UploadFactory.createUploadMgr();

		for (int i = 0; i < mediums.size(); i++) {
			String sdCardPath = mediums.get(i);
			// String name = Utils.getExpRelativePath(sdCardPath);
			String realName = currentTimeMillis + i + getMediumEnds();

			String name = Utils.getExpRelativePathExt(realName);

			uploadImpl(uploadToken, uploadMgr, sdCardPath, name);

			nativePath.add(realName);

			sendProgressEvent(i + 2);
		}

	}

	private String getMediumEnds() {
		return JSONConstant.IMAGE_TYPE.equals(mediumType) ? ".jpg"
				: Utils.DEFAULT_VIDEO_ENDS;
	}

	private void sendProgressEvent(int progress) {
		Message obtain = Message.obtain();
		obtain.what = EventType.UPLOAD_ICON_SUCCESS;
		obtain.arg1 = progress;
		obtain.arg2 = ConstantValue.DO_NOT_CANCEL_DIALOG;
		handler.sendMessage(obtain);
	}

	private void uploadImpl(String uploadToken, UploadMgr uploadMgr,
			String url, String name) throws Exception {
		if (JSONConstant.IMAGE_TYPE.equals(mediumType)) {
			uploadPhoto(uploadToken, uploadMgr, url, name);
		} else {
			uploadFile(uploadToken, uploadMgr, url, name);
		}
	}

	private void uploadFile(String uploadToken, UploadMgr uploadMgr,
			String url, String name) throws InterruptedException {
		try {
			uploadMgr.uploadFile(url, name, uploadToken);
		} catch (Exception e) {
			e.printStackTrace();
			Thread.sleep(500);
			// 重试一次
			uploadMgr.uploadFile(url, name, uploadToken);
		}
	}

	// 图片需要先压缩，控制最大值
	private void uploadPhoto(String uploadToken, UploadMgr uploadMgr,
			String url, String name) throws InterruptedException {
		Bitmap bitmap = Utils.getLoacalBitmap(url, STANDARD_PIC);
		Log.d("DJC", "Size =" + bitmap.getRowBytes() * bitmap.getHeight());

		try {
			// uploadMgr.uploadPhoto(url, name, uploadToken);
			uploadMgr.uploadPhoto(bitmap, name, uploadToken);
		} catch (Exception e) {
			e.printStackTrace();
			Thread.sleep(500);
			// 重试一次
			uploadMgr.uploadPhoto(bitmap, name, uploadToken);
		}
	}

	protected void saveAndCompressNail(ExpInfo info) {
		String nail = info.serverUrlToLocalUrl(info.getServerUrls().get(0),
				true);
		Log.d("", "saveAndCompressNail url=" + info.getServerUrls().get(0));
		Log.d("", "saveAndCompressNail nail=" + nail);
		try {
			if (!new File(nail).exists()) {
				Bitmap nailbitmap = Utils.createVideoThumbnail(mediums.get(0));
				Utils.saveBitmapToSDCard(nailbitmap, nail);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 将图库中的照片保存到应用目录，注意要与ExpInfo中serverUrlToLocalUrl方法的自己发送目录相对应，这样就不用再到服务器上去下载
	private void saveBmpToSDCard() throws Exception {
		for (int i = 0; i < mediums.size(); i++) {
			String sdCardPath = mediums.get(i);
			Bitmap loacalBitmap = Utils.getLoacalBitmap(mediums.get(i),
					STANDARD_PIC);
			String path = Utils.getSDCardPicRootPath() + File.separator
					+ Utils.getExpRelativePath(sdCardPath);
			Utils.makeDirs(Utils.getDir(path));
			Utils.saveBitmapToSDCard(loacalBitmap, path);
			loacalBitmap.recycle();
		}
	}

}
