package com.cocobabys.net;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.httpclientmgr.HttpClientHelper;

public class SchoolbusMethod {
	private SchoolbusMethod() {
	}

	public static SchoolbusMethod getMethod() {
		return new SchoolbusMethod();
	}

	public MethodResult getLocation(String childid) throws Exception {
		HttpResult result = new HttpResult();
		String command = createCommand(childid);
		Log.d("DDD getLocation ", " str : " + command);
		result = HttpClientHelper.executeGet(command);
		return getResult(result);
	}

	private MethodResult getResult(HttpResult result) throws Exception {
		MethodResult methodResult = new MethodResult(EventType.GET_LAST_BUS_LOCATION_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			methodResult.setResultType(EventType.GET_LAST_BUS_LOCATION_SUCCESS);
		}
		return methodResult;
	}

	private String createCommand(String childid) {
		String cmd = String.format(ServerUrls.GET_SCHOOLBUS_LAST_LOCATION, DataMgr
				.getInstance().getSchoolID(),childid);
		return cmd;
	}
}
