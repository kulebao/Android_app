package com.cocobabys.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.EducationInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;

public class EducationMethod {

	private static final String MOST = "most";
	private static final String FROM = "from";
	private static final String TO = "to";

	private EducationMethod() {
	}

	public static EducationMethod getMethod() {
		return new EducationMethod();
	}

	public List<EducationInfo> getEdus(int most, long from, long to)
			throws Exception {
		List<EducationInfo> list = new ArrayList<EducationInfo>();
		HttpResult result = new HttpResult();
		String command = createCommand(most, from, to);
		Log.e("DDDDD ", "getSchoolInfo cmd:" + command);
		result = HttpClientHelper.executeGet(command);
		list = handleGetEduResult(result);
		return list;
	}

	private List<EducationInfo> handleGetEduResult(HttpResult result)
			throws JSONException {
		List<EducationInfo> list = new ArrayList<EducationInfo>();
		if (result.getResCode() == HttpStatus.SC_OK) {
			JSONArray array = result.getJSONArray();
			list = EducationInfo.jsonArrayToList(array);
		}

		return list;
	}

	private String createCommand(int most, long from, long to) {
		if (most == 0) {
			most = ConstantValue.GET_EDU_MAX_COUNT;
		}
		String cmd = String.format(ServerUrls.GET_EDUCATION, DataMgr
				.getInstance().getSchoolID(), DataMgr.getInstance()
				.getSelectedChild().getServer_id());

		cmd += MOST + "=" + most;
		if (from != 0) {
			cmd += "&" + FROM + "=" + from;
		}

		if (to != 0) {
			cmd += "&" + TO + "=" + to;
		}
		Log.d("DDD", "createCommand cmd=" + cmd);
		return cmd;
	}
}
