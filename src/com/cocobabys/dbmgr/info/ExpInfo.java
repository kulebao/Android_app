package com.cocobabys.dbmgr.info;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.cocobabys.R;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.upload.UploadFactory;
import com.cocobabys.utils.Utils;

public class ExpInfo {
	public static final String TEACHER_TYPE = "t";
	public static final String PARENT_TYPE = "p";

	public static final String ID = "_id";
	public static final String CONTENT = "content";
	public static final String TIMESTAMP = "timestamp";
	public static final String EXP_ID = "exp_id";
	public static final String CHILD_ID = "child_id";
	public static final String MEDIA_URL = "media_url";
	public static final String MEDIA_TYPE = "media_type";
	public static final String SENDER_TYPE = "sender_type";
	public static final String SENDER_ID = "sender_id";
	public static final String MEDIUM = "medium";
	private static final String THUMBNAIL = "thumbnail";

	private String content = "";
	private long timestamp = 0;
	private String sender_id = "";
	private String medium = "";
	private String sender_type = "";
	private String child_id = "";
	private int id = 0;
	private long exp_id = 0;

	public long getExp_id() {
		return exp_id;
	}

	public void setExp_id(long exp_id) {
		this.exp_id = exp_id;
	}

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

	public String getMedium() {
		return medium;
	}

	public void setMedium(String medium) {
		this.medium = medium;
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

	public int getLayoutID() {
		int layoutid = 0;
		if (!TEACHER_TYPE.equals(sender_type)) {
			layoutid = R.layout.chat_item_right;
		} else {
			layoutid = R.layout.chat_item_left;
		}

		return layoutid;
	}

	public boolean isSendBySelf() {
		return TEACHER_TYPE.equals(sender_type);
	}

	public static List<ExpInfo> parseFromJsonArray(JSONArray array) throws JSONException {
		List<ExpInfo> list = new ArrayList<ExpInfo>();
		for (int i = 0; i < array.length(); i++) {
			list.add(parseFromJsonObj(array.getJSONObject(i)));
		}
		return list;
	}

	public static ExpInfo parseFromJsonObj(JSONObject object) throws JSONException {
		ExpInfo info = new ExpInfo();

		info.setExp_id(object.getLong("id"));
		info.setChild_id(object.getString(JSONConstant.TOPIC));
		info.setContent(object.getString(CONTENT));
		info.setSender_id(object.getJSONObject(JSONConstant.SENDER).getString(JSONConstant.ID));
		info.setSender_type(object.getJSONObject(JSONConstant.SENDER).getString(JSONConstant.TYPE));
		info.setTimestamp(object.getLong(TIMESTAMP));
		info.setMedium(object.getJSONArray(MEDIUM).toString());
		return info;
	}

	public String getMediumType() {
		String type = JSONConstant.IMAGE_TYPE;
		if (!"".equals(medium)) {
			try {
				JSONArray array = new JSONArray(medium);
				if (array.length() > 0) {
					type = array.getJSONObject(0).getString(JSONConstant.TYPE);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return type;
	}

	public List<String> getLocalUrls(boolean bThumbnail) {
		List<String> list = new ArrayList<String>();
		if (!"".equals(medium)) {
			try {
				JSONArray array = new JSONArray(medium);
				for (int i = 0; i < array.length(); i++) {
					String url = array.getJSONObject(i).getString(JSONConstant.URL);
					String localUrl = serverUrlToLocalUrl(url, bThumbnail);
					list.add(localUrl);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public List<String> getServerUrls() {
		List<String> list = new ArrayList<String>();
		if (!"".equals(medium)) {
			try {
				JSONArray array = new JSONArray(medium);
				for (int i = 0; i < array.length(); i++) {
					String url = array.getJSONObject(i).getString(JSONConstant.URL);
					list.add(url);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public String serverUrlToLocalUrl(String serverUrl, boolean bThumbnail) {
		String localUrl = "";
		if ("".equals(serverUrl)) {
			return "";
		}

		if (sender_id.equals(DataMgr.getInstance().getSelfInfoByPhone().getParent_id())) {
			// 自己发的图片，就从本地读，以免再次去服务器下载
			localUrl = serverUrl.replace(UploadFactory.getUploadHost(), Utils.getSDCardPicRootPath()
					+ File.separator);

			// 如果文件不存在，则区分是否是缩略图
			if (!new File(localUrl).exists() && bThumbnail) {
				String name = Utils.getName(localUrl);
				localUrl = Utils.getDir(localUrl) + File.separator + THUMBNAIL + File.separator + name;
			}
		} else {
			String dir = "";
			if (bThumbnail) {
				dir = getThumbnailDir();
			} else {
				dir = getOriginalDir();
			}

			localUrl = dir + Utils.getName(serverUrl);
		}

		Utils.mkDirs(Utils.getDir(localUrl));
		Log.d("DDD", "serverUrlToLocalUrl =" + localUrl);
		return localUrl;
	}

	public String getOriginalDir() {
		return Utils.getExpIconDir(child_id) + exp_id + File.separator;
	}

	public String getThumbnailDir() {
		return Utils.getExpIconDir(child_id) + exp_id + File.separator + THUMBNAIL + File.separator;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (exp_id ^ (exp_id >>> 32));
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
		ExpInfo other = (ExpInfo) obj;
		if (exp_id != other.exp_id)
			return false;
		return true;
	}

}
