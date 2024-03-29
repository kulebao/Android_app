package com.cocobabys.dbmgr.info;

import java.io.File;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.upload.UploadFactory;
import com.cocobabys.utils.Utils;

import android.text.TextUtils;
import android.util.Log;

public class NewChatInfo {
	public static final String NEW_CHAT_ICON_BIG_WITDH = "720";
	public static final String NEW_CHAT_ICON_BIG_HEIGHT = "1080";

	public static final String TEACHER_TYPE = "t";
	public static final String PARENT_TYPE = "p";

	public static final String ID = "_id";
	public static final String CONTENT = "content";
	public static final String TIMESTAMP = "timestamp";
	public static final String CHAT_ID = "chat_id";
	public static final String CHILD_ID = "child_id";
	public static final String MEDIA_URL = "media_url";
	public static final String MEDIA_TYPE = "media_type";
	public static final String SENDER_TYPE = "sender_type";
	public static final String SENDER_ID = "sender_id";

	private String content = "";
	private long timestamp = 0;
	private String media_url = "";
	private String media_type = "";
	private String sender_id = "";
	private String sender_type = "";
	private String child_id = "";
	private int id = 0;
	private long chat_id = 0;

	public String getChild_id() {
		return child_id;
	}

	public void setChild_id(String child_id) {
		this.child_id = child_id;
	}

	public String getSender_type() {
		return sender_type;
	}

	public void setSender_type(String sender_type) {
		this.sender_type = sender_type;
	}

	public String getSender_id() {
		return sender_id;
	}

	public void setSender_id(String sender_id) {
		this.sender_id = sender_id;
	}

	public long getChat_id() {
		return chat_id;
	}

	public void setChat_id(long chat_id) {
		this.chat_id = chat_id;
	}

	public String getMedia_url() {
		return media_url;
	}

	public void setMedia_url(String media_url) {
		this.media_url = media_url;
	}

	public String getMedia_type() {
		return media_type;
	}

	public void setMedia_type(String media_type) {
		this.media_type = media_type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getFormattedTime() {
		String ret = "";
		try {
			ret = Utils.convertTime(timestamp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public boolean isSendByTeacher() {
		return TEACHER_TYPE.equals(sender_type);
	}

	public String getLocalUrl() {
		String localUrl = "";
		if (!"".equals(media_url)) {
			if (sender_id.equals(DataMgr.getInstance().getSelfInfoByPhone()
					.getParent_id())) {

				NativeMediumInfo nativeMediumInfo = DataMgr.getInstance()
						.getNativeMediumInfo(Utils.getName(media_url));
				// 不为空表示该资源是由用户上传过，并记录到本地数据库
				if (nativeMediumInfo != null) {
					String value = nativeMediumInfo.getValue();
					// 如果文件存在才返回，否则继续
					if (new File(value).exists()) {
						return value;
					}
				}

				// 自己发的图片，就从本地读，以免再次去服务器下载
				localUrl = media_url.replace(UploadFactory.getUploadHost(),
						Utils.getSDCardPicRootPath() + File.separator);

				// 这里必须增加容错处理，如果服务器上传图片的保存路径与客户端不一致，那么
				// replace会失败，这里就必须手动设置一下
				if (localUrl.toLowerCase(Locale.US).startsWith("http")) {
					localUrl = getDefaultPath();
				}

				if (!TextUtils.isEmpty(localUrl)) {
					Utils.mkDirs(Utils.getDir(localUrl));
				}

				// 如果文件存在，则直接返回
				if (new File(localUrl).exists()) {
					return localUrl;
				}

			}
			
			// 文件不存在，则需要从网上下载，此时命名规则以我为准.不考虑是从哪儿上传的
			{
				localUrl = getDefaultPath();
			}

			localUrl = Utils.getPicFileNameNoExt(localUrl);
		}

		Log.d("DDD", "getLocalUrl =" + localUrl);
		return localUrl;
	}

	private String getDefaultPath() {
		String dir = Utils.getChatIconDir(child_id);
		Utils.mkDirs(dir);
		String localUrl = dir + chat_id + getMediaExtName(media_type);
		return localUrl;
	}

	private String getMediaExtName(String type) {
		String extName = ".un";
		if (JSONConstant.IMAGE_TYPE.equals(type)) {
			extName = ".png";
		} else if (JSONConstant.VOICE_TYPE.equals(type)) {
			extName = ".amr";
		}

		return extName;
	}

	public static NewChatInfo parseFromJson(JSONObject object)
			throws JSONException {
		NewChatInfo info = new NewChatInfo();

		info.setChat_id(object.getLong("id"));
		info.setChild_id(object.getString(JSONConstant.TOPIC));
		info.setContent(object.getString(CONTENT));
		info.setMedia_type(object.getJSONObject(JSONConstant.MEDIA).getString(
				"type"));
		info.setMedia_url(object.getJSONObject(JSONConstant.MEDIA).getString(
				"url"));
		info.setSender_id(object.getJSONObject(JSONConstant.SENDER).getString(
				"id"));
		info.setSender_type(object.getJSONObject(JSONConstant.SENDER)
				.getString("type"));
		info.setTimestamp(object.getLong(TIMESTAMP));
		return info;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (chat_id ^ (chat_id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NewChatInfo other = (NewChatInfo) obj;
		if (chat_id != other.chat_id)
			return false;
		return true;
	}

}
