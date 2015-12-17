package com.cocobabys.net;

import java.util.List;

import org.apache.http.HttpStatus;

import com.alibaba.fastjson.JSON;
import com.cocobabys.bean.BusinessSummary;
import com.cocobabys.bean.MerchantInfo;
import com.cocobabys.bean.PullToRefreshListInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.MethodUtils;

import android.text.TextUtils;
import android.util.Log;

public class MerchantMethod {
	private MerchantMethod() {
	}

	public static MerchantMethod getMethod() {
		return new MerchantMethod();
	}

	public MethodResult getInfo(PullToRefreshListInfo info, int category) throws Exception {
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
			List<MerchantInfo> list = JSON.parseArray(result.getContent(), MerchantInfo.class);

			methodResult.setResultObj(list);
			methodResult.setResultType(EventType.MECHANT_GET_SUCCESS);
		}
		return methodResult;
	}

	private String createCommand(PullToRefreshListInfo info, int category) {
		String cmd = String.format(ServerUrls.GET_MERCHANT_LIST, DataMgr.getInstance().getSchoolID());

		String createFromToParams = MethodUtils.createFromToParams(info);

		String cate = "&category=" + category;

		return cmd + createFromToParams + cate;
	}

	// 判断客户端是否显示商户平台，如果已经显示过了，则一直显示
	public void updateBusineeState() {
		String value = DataUtils.getUndeleteableProp(ConstantValue.BUSINESS_STATE);

		if (TextUtils.isEmpty(value)) {
			HttpResult result = new HttpResult();
			String url = String.format(ServerUrls.GET_BUSINESS_STATE, DataMgr.getInstance().getSchoolID());
			Log.d("", "updateBusineeState url =" + url);
			try {
				result = HttpClientHelper.executeGet(url);
				Log.d("", "updateBusineeState content =" + result.getContent());

				if (result.getResCode() == HttpStatus.SC_OK) {
					BusinessSummary summary = JSON.parseObject(result.getContent(), BusinessSummary.class);
					if (summary.isValid()) {
						DataUtils.saveUndeleteableProp(ConstantValue.BUSINESS_STATE, ConstantValue.BUSINESS_VISIBLE);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
