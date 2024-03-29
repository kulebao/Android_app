package com.cocobabys.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cocobabys.bean.RelationInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.DataUtils;

import android.util.Log;

public class ChildMethod {

	private ChildMethod() {
	}

	public static ChildMethod getMethod() {
		return new ChildMethod();
	}

	public MethodResult getRelationshipByChild(String childid) throws Exception {
		HttpResult result = new HttpResult();
		String url = createGetRelationshipByChildUrl(childid);
		Log.e("DDDDD ", "getRelationshipByChild cmd:" + url);
		result = HttpClientHelper.executeGet(url);
		MethodResult methodResult = handleRelationshipResultEx(result);
		return methodResult;
	}

	private MethodResult handleRelationshipResultEx(HttpResult result) {
		MethodResult methodResult = new MethodResult(EventType.GET_RELATIONSHIP_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONArray array = result.getJSONArray();
				Log.d("DDD handleRelationshipResultEx", "str : " + array.toString());
				List<ParentInfo> list = new ArrayList<ParentInfo>();
				for (int i = 0; i < array.length(); i++) {
					JSONObject jsonObject = array.getJSONObject(i);
					ParentInfo info = ParentInfo.parseFromRelationship(jsonObject);
					list.add(info);
				}

				methodResult.setResultObj(list);
				methodResult.setResultType(EventType.GET_RELATIONSHIP_SUCCESS);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return methodResult;
	}

	public int getRelationship() throws Exception {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String url = createGetRelationshipUrl();
		Log.e("DDDDD ", "createGetChildrenInfoUrl cmd:" + url);
		result = HttpClientHelper.executeGet(url);
		bret = handleRelationshipResult(result);
		return bret;
	}

	private int handleRelationshipResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONArray array = result.getJSONArray();
				Log.d("DDD handleRelationshipResult", "str : " + array.toString());
				event = parseParentFromArray(array);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			event = EventType.SERVER_INNER_ERROR;
		}

		return event;
	}

	private int parseParentFromArray(JSONArray jsonArray) throws JSONException {
		int event = EventType.CHILDREN_INFO_IS_LATEST;
		List<ParentInfo> list = new ArrayList<ParentInfo>();
		JSONArray childArray = new JSONArray();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			ParentInfo info = ParentInfo.parseFromRelationship(jsonObject);

			list.add(info);
			JSONObject childObj = jsonObject.getJSONObject("child");
			childArray.put(childObj);

			// 因为有多个小孩的情况，将小孩id作为key，把卡号和关系id保存起来，改卡时需要使用
			RelationInfo relationInfo = new RelationInfo();

			relationInfo.setCardnum(info.getCard());
			relationInfo.setChildid(childObj.optString("child_id"));
			relationInfo.setRelationid(jsonObject.getString("id"));
			relationInfo.setRelationship(info.getRelationship());
			DataUtils.saveRelationInfo(relationInfo);
		}
		DataMgr.getInstance().addParentList(list);

		List<ChildInfo> childList = ChildInfo.jsonArrayToList(childArray);

		event = checkUpdate(childList);
		return event;
	}

	private int checkUpdate(List<ChildInfo> childList) {
		DataMgr instance = DataMgr.getInstance();
		// int event = EventType.CHILDREN_INFO_IS_LATEST;

		ChildInfo selectedChild = instance.getSelectedChild();
		// if (selectedChild == null || (instance.getAllChildrenInfo().size() !=
		// childList.size())) {
		// updateAll(childList, selectedChild);
		// event = EventType.UPDATE_CHILDREN_INFO;
		// } else {
		// long latestChildTimestamp =
		// Long.parseLong(instance.getLatestChildTimestamp());
		// long curLatest = getLatestTime(childList);
		//
		// if (curLatest > latestChildTimestamp) {
		// updateAll(childList, selectedChild);
		// event = EventType.UPDATE_CHILDREN_INFO;
		// }
		// }
		updateAll(childList, selectedChild);
		return EventType.UPDATE_CHILDREN_INFO;
	}

	private void updateAll(List<ChildInfo> childList, ChildInfo selectedChild) {
		// DataMgr.getInstance().clearChildInfo();

		if (selectedChild == null) {
			// 首次使用，把全部小孩数据更新到数据库
			DataMgr.getInstance().addChildrenInfoList(childList);
		} else {
			DataMgr.getInstance().addChildrenInfoList(childList);
			int setSelectedChild = DataMgr.getInstance().setSelectedChild(selectedChild.getServer_id());
			Log.d("DDD", "updateAll setSelectedChild=" + setSelectedChild);
			// 如果刷新数据后，之前的选中小孩不存在了，随便设置一个小孩为选中小孩
			if (setSelectedChild < 1) {
				DataMgr.getInstance().setSelectedChild(childList.get(0).getServer_id());
			}
		}
	}

	private long getLatestTime(List<ChildInfo> childList) {
		long latest = 0;
		for (ChildInfo info : childList) {
			if (info.getTimestamp() > latest) {
				latest = info.getTimestamp();
			}
		}
		return latest;
	}

	private String createGetRelationshipUrl() {
		String url = String.format(ServerUrls.GET_RELATIONSHIP, DataMgr.getInstance().getSchoolID());
		url += "parent=" + DataUtils.getAccount();
		return url;
	}

	private String createGetRelationshipByChildUrl(String childid) {
		String url = String.format(ServerUrls.GET_RELATIONSHIP_BY_CHILD, DataMgr.getInstance().getSchoolID());
		url += "child=" + childid;
		return url;
	}

}
