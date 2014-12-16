package com.cocobabys.net;

import java.util.List;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.cocobabys.bean.LocationInfo;
import com.cocobabys.bean.LocatorPower;
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

	public MethodResult getPower(String deviceid) throws Exception {
		HttpResult result = new HttpResult();
		String command = createGetPowerCommand(deviceid);
		Log.d("DDD getLastLocation", " str : " + command);
		result = HttpClientHelper.executeGet(command);
		return getPowerResult(result);
	}

	private MethodResult getPowerResult(HttpResult result) {
		MethodResult methodResult = new MethodResult(EventType.GET_LOCATOR_POWER_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			Log.d("DDD getPowerResult ", " getPowerResult : " + result.getContent());
			methodResult.setResultType(EventType.GET_LOCATOR_POWER_SUCCESS);
			LocatorPower parseObject = JSON.parseObject(result.getContent(), LocatorPower.class);
			methodResult.setResultObj(parseObject);
		}
		return methodResult;
	}

	public MethodResult getHistoryLocation(String deviceid) throws Exception {
		HttpResult result = new HttpResult();
		String command = createGetHistoryLocationCommand(deviceid);
		Log.d("DDD getLastLocation", " str : " + command);
		result = HttpClientHelper.executeGet(command);
		return getHistoryResult(result);
	}

	public MethodResult getLastLocation(String deviceid) throws Exception {
		HttpResult result = new HttpResult();
		String command = createGetLastLocationCommand(deviceid);
		Log.d("DDD getLastLocation", " str : " + command);
		result = HttpClientHelper.executeGet(command);
		return getLastResult(result);
	}

	private MethodResult getHistoryResult(HttpResult result) throws Exception {
		MethodResult methodResult = new MethodResult(EventType.GET_HISTORY_LOCATION_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			Log.d("DDD getLastLocation ", " str : " + result.getContent());
			methodResult.setResultType(EventType.GET_HISTORY_LOCATION_SUCCESS);
			List<LocationInfo> parseArray = JSON.parseArray(result.getContent(), LocationInfo.class);
			methodResult.setResultObj(parseArray);
		}
		return methodResult;
	}

	private MethodResult getLastResult(HttpResult result) throws Exception {
		MethodResult methodResult = new MethodResult(EventType.GET_LAST_LOCATION_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			Log.d("DDD getLastLocation ", " str : " + result.getContent());
			methodResult.setResultType(EventType.GET_LAST_LOCATION_SUCCESS);
			List<LocationInfo> parseArray = JSON.parseArray(result.getContent(), LocationInfo.class);
			methodResult.setResultObj(parseArray.get(0));
		}
		return methodResult;
	}

	private String createGetPowerCommand(String deviceid) {
		String cmd = String.format(ServerUrls.GET_LOCATOR_POWER, deviceid);
		return cmd;
	}

	private String createGetHistoryLocationCommand(String deviceid) {
		String cmd = String.format(ServerUrls.GET_HISTORY_LOCATION, deviceid);
		return cmd;
	}

	private String createGetLastLocationCommand(String deviceid) {
		String cmd = String.format(ServerUrls.GET_LAST_LOCATION, deviceid);
		return cmd;
	}
}
