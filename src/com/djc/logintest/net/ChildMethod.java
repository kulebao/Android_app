package com.djc.logintest.net;

import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.ChildInfo;
import com.djc.logintest.httpclientmgr.HttpClientHelper;
import com.djc.logintest.utils.Utils;

public class ChildMethod {

	private ChildMethod() {
	}

	public static ChildMethod getMethod() {
		return new ChildMethod();
	}

	public int getChildrenInfo() throws Exception {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String url = createAllGetChildrenInfoUrl();
		Log.e("DDDDD ", "createGetChildrenInfoUrl cmd:" + url);
		result = HttpClientHelper.executeGet(url);
		bret = handleGetChildInfoResultEx(result);
		return bret;
	}

	private String createAllGetChildrenInfoUrl() {
		String url = String.format(ServerUrls.GET_ALL_CHILDREN_INFO, DataMgr
				.getInstance().getSchoolID(), Utils
				.getProp(JSONConstant.ACCOUNT_NAME));
		return url;
	}

	private int handleGetChildInfoResultEx(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONArray array = result.getJSONArray();
				Log.d("DDD handleGetChildInfoResult",
						"str : " + array.toString());
				event = checkUpdateEx(array);
				// if (errorcode == 0) {
				// event = checkUpdate(jsonObject);
				// } else {
				// event = EventType.GET_CHILDREN_INFO_FAILED;
				// }
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			event = EventType.SERVER_INNER_ERROR;
		}

		return event;
	}

	private int checkUpdateEx(JSONArray array) throws JSONException {
		DataMgr instance = DataMgr.getInstance();
		int event = EventType.CHILDREN_INFO_IS_LATEST;

		ChildInfo selectedChild = instance.getSelectedChild();
		if (selectedChild == null
				|| (instance.getAllChildrenInfo().size() != array.length())) {
			updateAllEx(array, selectedChild);
			event = EventType.UPDATE_CHILDREN_INFO;
		} else {
			long latestChildTimestamp = Long.parseLong(instance
					.getLatestChildTimestamp());
			long curLatest = getLatestTime(array);

			if (curLatest > latestChildTimestamp) {
				updateAllEx(array, selectedChild);
				event = EventType.UPDATE_CHILDREN_INFO;
			}
		}
		return event;
	}

	private void updateAllEx(JSONArray array, ChildInfo selectedChild) {
		DataMgr.getInstance().clearChildInfo();

		if (selectedChild == null) {
			// 首次使用，把全部小孩数据更新到数据库
			List<ChildInfo> list = ChildInfo.jsonArrayToList(array);
			DataMgr.getInstance().addChildrenInfoList(list);
		} else {
			List<ChildInfo> list = ChildInfo.jsonArrayToList(array);
			DataMgr.getInstance().addChildrenInfoList(list);
			int setSelectedChild = DataMgr.getInstance().setSelectedChild(
					selectedChild.getServer_id());
			Log.d("DDD", "updateAll setSelectedChild=" + setSelectedChild);
			// 如果刷新数据后，之前的选中小孩不存在了，随便设置一个小孩为选中小孩
			if (setSelectedChild < 1) {
				DataMgr.getInstance().setSelectedChild(
						list.get(0).getServer_id());
			}
		}
	}

	private int handleGetChildInfoResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONObject jsonObject = result.getJsonObject();
				Log.d("DDD handleGetChildInfoResult",
						"str : " + jsonObject.toString());
				int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);
				// 登录成功，保存token
				if (errorcode == 0) {
					event = checkUpdate(jsonObject);
				} else {
					event = EventType.GET_CHILDREN_INFO_FAILED;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			event = EventType.SERVER_INNER_ERROR;
		}

		return event;
	}

	private int checkUpdate(JSONObject jsonObject) throws JSONException {
		DataMgr instance = DataMgr.getInstance();
		int event = EventType.CHILDREN_INFO_IS_LATEST;
		String jsonSrc = jsonObject.getString("children");
		JSONArray array = new JSONArray(jsonSrc);

		ChildInfo selectedChild = instance.getSelectedChild();
		if (selectedChild == null
				|| (instance.getAllChildrenInfo().size() != array.length())) {
			updateAll(array, selectedChild);
			event = EventType.UPDATE_CHILDREN_INFO;
		} else {
			long latestChildTimestamp = Long.parseLong(instance
					.getLatestChildTimestamp());
			long curLatest = getLatestTime(array);

			if (curLatest > latestChildTimestamp) {
				updateAll(array, selectedChild);
				event = EventType.UPDATE_CHILDREN_INFO;
			}
			// event = compareSelecteChild(instance, event, array,
			// selectedChild);
		}
		// Utils.bindPushTags();
		return event;
	}

	private long getLatestTime(JSONArray array) {
		long latest = 0;
		List<ChildInfo> list = ChildInfo.jsonArrayToList(array);
		for (ChildInfo info : list) {
			if (info.getTimestamp() > latest) {
				latest = info.getTimestamp();
			}
		}
		return latest;
	}

	private void updateAll(JSONArray array, ChildInfo selectedChild) {
		DataMgr.getInstance().clearChildInfo();

		if (selectedChild == null) {
			// 首次使用，把全部小孩数据更新到数据库
			List<ChildInfo> list = ChildInfo.jsonArrayToList(array);
			DataMgr.getInstance().addChildrenInfoList(list);
		} else {
			List<ChildInfo> list = ChildInfo.jsonArrayToList(array);
			DataMgr.getInstance().addChildrenInfoList(list);
			int setSelectedChild = DataMgr.getInstance().setSelectedChild(
					selectedChild.getServer_id());
			Log.d("DDD", "updateAll setSelectedChild=" + setSelectedChild);
			// 如果刷新数据后，之前的选中小孩不存在了，随便设置一个小孩为选中小孩
			if (setSelectedChild < 1) {
				DataMgr.getInstance().setSelectedChild(
						list.get(0).getServer_id());
			}
		}
	}

}
