package com.djc.logintest.net;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.CookBookInfo;
import com.djc.logintest.dbmgr.info.InfoHelper;
import com.djc.logintest.httpclientmgr.HttpClientHelper;
import com.djc.logintest.utils.Utils;

public class GetCookbookMethod {

	private GetCookbookMethod() {
	}

	public static GetCookbookMethod getMethod() {
		return new GetCookbookMethod();
	}

	public int getCookBook() {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String url = createCookbookPreviewUrl();
		Log.e("DDDDD ", "createGetChildrenInfoUrl cmd:" + url);
		try {
			result = HttpClientHelper.executeGet(url);
			bret = handleCheckCookbookResult(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bret;
	}

	public boolean checkCookBook() {
		boolean bret = false;
		HttpResult result = new HttpResult();
		String url = createCookbookPreviewUrl();
		Log.e("DDDDD ", "createGetChildrenInfoUrl cmd:" + url);
		try {
			result = HttpClientHelper.executeGet(url);
			CookBookPreview cookBookPreview = paraseCookBookPreview(result);
			bret = compareTime(cookBookPreview);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bret;
	}

	private boolean compareTime(CookBookPreview cookBookPreview) {
		boolean bret = false;
		if (cookBookPreview.errorcode == 0) {
			CookBookInfo cookBookInfo = DataMgr.getInstance().getCookBookInfo();
			if (cookBookInfo == null
					|| (cookBookPreview.timestamp > Long.parseLong(cookBookInfo
							.getTimestamp()))) {
				bret = true;
			}
		}
		return bret;
	}

	private String createCookbookPreviewUrl() {
		String url = String.format(ServerUrls.COOKBOOK_PRIVIEW, DataMgr
				.getInstance().getSchoolID());
		return url;
	}

	private CookBookPreview paraseCookBookPreview(HttpResult result) {
		CookBookPreview cookBookPreview = new CookBookPreview();
		if (result.getResCode() == HttpStatus.SC_OK) {
			// 返回单一元素数组，保存最新食谱
			try {
				JSONArray array = result.getJSONArray();
				JSONObject jsonObject = array.getJSONObject(0);
				cookBookPreview.cookbookid = jsonObject
						.getString(CookBookInfo.COOKBOOK_ID);
				cookBookPreview.timestamp = Long.parseLong(jsonObject
						.getString(InfoHelper.TIMESTAMP));
				cookBookPreview.errorcode = jsonObject
						.getInt(JSONConstant.ERROR_CODE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return cookBookPreview;
	}

	private int handleCheckCookbookResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				CookBookPreview cookBookPreview = paraseCookBookPreview(result);
				boolean bret = compareTime(cookBookPreview);
				if (bret) {
					event = getCookbook(cookBookPreview.cookbookid);
				} else {
					event = EventType.GET_COOKBOOK_FAILED;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			event = EventType.SERVER_INNER_ERROR;
		}

		return event;
	}

	public int handleSuccess(JSONObject jsonObject) throws JSONException {
		String newtimestamp = jsonObject.getString(InfoHelper.TIMESTAMP);
		long newTime = Long.parseLong(newtimestamp);
		CookBookInfo cookBookInfo = DataMgr.getInstance().getCookBookInfo();

		if (cookBookInfo == null
				|| (newTime > Long.parseLong(cookBookInfo.getTimestamp()))) {
			return getCookbook(jsonObject.getString(CookBookInfo.COOKBOOK_ID));
		}
		return EventType.GET_COOKBOOK_LATEST;
	}

	public int getCookbook(String cookbookID) {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String url = createGetCookbookUrl(cookbookID);
		Log.e("DDDDD ", "getSchedule cmd:" + url);
		try {
			result = HttpClientHelper.executeGet(url);
			bret = handleGetCookbookResult(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bret;
	}

	private int handleGetCookbookResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONObject jsonObject = result.getJsonObject();
				Log.d("DDD handleGetChildInfoResult",
						"str : " + jsonObject.toString());
				int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);
				if (errorcode == 0) {
					saveCookbook(jsonObject);
					return EventType.GET_COOKBOOK_SUCCESS;
				} else {
					event = EventType.GET_COOKBOOK_FAILED;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			event = EventType.SERVER_INNER_ERROR;
		}
		return event;
	}

	private String createGetCookbookUrl(String cookbookID) {
		String url = String.format(ServerUrls.COOKBOOK_DETAIL, DataMgr
				.getInstance().getSchoolID(), cookbookID);
		return url;
	}

	private void saveCookbook(JSONObject jsonObject) throws JSONException {
		CookBookInfo info = new CookBookInfo();
		info.setTimestamp(jsonObject.getString(InfoHelper.TIMESTAMP));
		info.setCookbook_id(jsonObject.getString(CookBookInfo.COOKBOOK_ID));
		info.setCookbook_content(jsonObject.getString(InfoHelper.WEEK_DETAIL));
		DataMgr.getInstance().updateCookBookInfo(info);
		// 获取到新的食谱，将新食谱的标志置为false
		Utils.saveProp(ConstantValue.HAVE_COOKBOOK_NOTICE, "false");
	}

	private class CookBookPreview {
		long timestamp = 0;
		int errorcode = -1;
		String cookbookid = null;
	}
}
