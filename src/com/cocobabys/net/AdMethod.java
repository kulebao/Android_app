package com.cocobabys.net;

import java.util.List;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.cocobabys.bean.AdInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.DataUtils;

public class AdMethod {
	private AdMethod() {
	}

	public static AdMethod getMethod() {
		return new AdMethod();
	}

	public MethodResult getInfo() throws Exception {
		HttpResult result = new HttpResult();
		String command = createCommand();
		Log.d("DDD AdMethod getInfo", " str : " + command);
		result = HttpClientHelper.executeGet(command);
		return getResult(result);
	}

	private MethodResult getResult(HttpResult result) throws Exception {
		MethodResult methodResult = new MethodResult(EventType.GET_AD_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			List<AdInfo> list = JSON.parseArray(result.getContent(),
					AdInfo.class);

			for (AdInfo info : list) {
				DataUtils.saveAdInfo(info);
				// 暂时只记录一个广告位
				break;
			}

			methodResult.setResultType(EventType.GET_AD_SUCCESS);
		}
		return methodResult;
	}

	private String createCommand() {
		String cmd = String.format(ServerUrls.GET_AD_INFO, DataMgr
				.getInstance().getSchoolID());
		return cmd;
	}
}
