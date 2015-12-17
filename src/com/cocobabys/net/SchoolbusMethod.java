package com.cocobabys.net;

import org.apache.http.HttpStatus;

import com.alibaba.fastjson.JSON;
import com.cocobabys.bean.BusLocation;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.httpclientmgr.HttpClientHelper;

import android.util.Log;

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
		MethodResult methodResult = new MethodResult(
				EventType.GET_LAST_BUS_LOCATION_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			methodResult.setResultType(EventType.GET_LAST_BUS_LOCATION_SUCCESS);
			BusLocation info = JSON.parseObject(result.getContent(),
					BusLocation.class);
			methodResult.setResultObj(info);
		} else if (result.getResCode() == HttpStatus.SC_NOT_FOUND) {
			int errorCode = result.getErrorCode();
			if (errorCode == 1) {
				// 班车未出发
				methodResult
						.setResultType(EventType.GET_LAST_BUS_LOCATION_NOT_RUN);
			} else if (errorCode == 2) {
				// 小孩已经上午下车
				methodResult
						.setResultType(EventType.GET_LAST_BUS_LOCATION_CHILD_GETOFF);
			} else if (errorCode == 4) {
				// 小孩已经下午下车
				methodResult
						.setResultType(EventType.GET_LAST_BUS_LOCATION_CHILD_GETOFF);
			}
		}
		return methodResult;
	}

	private String createCommand(String childid) {
		String cmd = String.format(ServerUrls.GET_SCHOOLBUS_LAST_LOCATION,
				DataMgr.getInstance().getSchoolID(), childid);
		return cmd;
	}
}
