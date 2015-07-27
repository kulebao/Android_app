package com.cocobabys.net;

import java.util.List;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.cocobabys.bean.MerchantInfo;
import com.cocobabys.bean.PullToRefreshListInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.MethodUtils;

public class MerchantMethod {
	private MerchantMethod() {
	}

	public static MerchantMethod getMethod() {
		return new MerchantMethod();
	}

	public MethodResult getInfo(PullToRefreshListInfo info, int category)
			throws Exception {
		HttpResult result = new HttpResult();
		String command = createCommand(info, category);
		Log.d("DDD MerchantMethod getInfo", " str : " + command);
		result = HttpClientHelper.executeGet(command);
		return getResult(result);
	}

	private MethodResult getResult(HttpResult result) throws Exception {
		Log.d("DDD MerchantMethod getResult", " result : " + result.toString());
		MethodResult methodResult = new MethodResult(EventType.MECHANT_GET_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			List<MerchantInfo> list = JSON.parseArray(result.getContent(),
					MerchantInfo.class);

			methodResult.setResultObj(list);
			methodResult.setResultType(EventType.MECHANT_GET_SUCCESS);
		}
		return methodResult;
	}

	private String createCommand(PullToRefreshListInfo info, int category) {
		String cmd = String.format(ServerUrls.GET_MERCHANT_LIST, DataMgr
				.getInstance().getSchoolID());

		String createFromToParams = MethodUtils.createFromToParams(info);

		String cate = "&category=" + category;

		return cmd + createFromToParams + cate;
	}
}
