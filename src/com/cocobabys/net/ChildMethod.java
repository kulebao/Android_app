package com.cocobabys.net;

import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.Utils;

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
		bret = handleGetChildInfoResult(result);
		return bret;
	}

	private String createAllGetChildrenInfoUrl() {
		String url = String.format(ServerUrls.GET_ALL_CHILDREN_INFO, DataMgr
				.getInstance().getSchoolID(), Utils
				.getProp(JSONConstant.ACCOUNT_NAME));
		return url;
	}

	private int handleGetChildInfoResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONArray array = result.getJSONArray();
				Log.d("DDD handleGetChildInfoResult",
						"str : " + array.toString());
				event = checkUpdate(array);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			event = EventType.SERVER_INNER_ERROR;
		}

		return event;
	}

	private int checkUpdate(JSONArray array) throws JSONException {
		DataMgr instance = DataMgr.getInstance();
		int event = EventType.CHILDREN_INFO_IS_LATEST;

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
		}
		return event;
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

}
