package com.cocobabys.net;

import java.util.List;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.cocobabys.bean.LocationInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.httpclientmgr.HttpClientHelper;

public class LbsMethod {
	public static final String FAKE_DEVICE = "1451351909";

	private LbsMethod() {
	}

	public static LbsMethod getMethod() {
		return new LbsMethod();
	}

	public MethodResult getLastLocation(String deviceid) throws Exception {
		HttpResult result = new HttpResult();
		String command = createGetLastLocationCommand(deviceid);
		Log.d("DDD getLastLocation", " str : " + command);
		result = HttpClientHelper.executeGet(command);
		return getResult(result);
	}

	private MethodResult getResult(HttpResult result) throws Exception {
		MethodResult methodResult = new MethodResult(
				EventType.GET_LAST_LOCATION_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			Log.d("DDD getLastLocation ", " str : " + result.getContent());
			methodResult.setResultType(EventType.GET_LAST_LOCATION_SUCCESS);
			List<LocationInfo> parseArray = JSON.parseArray(
					result.getContent(), LocationInfo.class);
			methodResult.setResultObj(parseArray.get(0));
		}
		return methodResult;
	}

	private String createGetLastLocationCommand(String deviceid) {
		// 测试设备号先写死索鸟的设备 "1451351909"
		String cmd = String.format(ServerUrls.GET_LAST_LOCATION, deviceid);
		return cmd;
	}
}
