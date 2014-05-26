package com.cocobabys.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.SwipeInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;

public class SwipeCardMethod {

	// 需要获取的最大刷卡记录数，这里设置为1000，表示需要获取时间段内全部数据
	// 时间段都是按月的，所以1000足够了
	private static final int MOST = 1000;

	private SwipeCardMethod() {
	}

	public static SwipeCardMethod getMethod() {
		return new SwipeCardMethod();
	}

	public int getSwipecardRecord(long from, long to) throws Exception {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String url = createGetSwipeUrl(from, to);
		Log.e("DDDDD ", "createGetSwipeUrl cmd:" + url);
		result = HttpClientHelper.executeGet(url);
		bret = handlegetSwipecardRecordResult(result);
		return bret;
	}

	private String createGetSwipeUrl(long from, long to) {
		String url = String.format(ServerUrls.GET_SWIPE_RECORD, DataMgr
				.getInstance().getSchoolID(), DataMgr.getInstance()
				.getSelectedChild().getServer_id(), from, to, MOST);
		return url;
	}

	private int handlegetSwipecardRecordResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				List<SwipeInfo> list = new ArrayList<SwipeInfo>();
				JSONArray array = result.getJSONArray();
				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					list.add(SwipeInfo.toSwipeInfo(obj));
				}
				DataMgr.getInstance().addSwipeDataList(list);
				event = EventType.GET_SWIPE_RECORD_SUCCESS;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			event = EventType.SERVER_INNER_ERROR;
		}

		return event;
	}

}
