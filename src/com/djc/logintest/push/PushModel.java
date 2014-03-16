package com.djc.logintest.push;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.android.pushservice.PushSettings;
import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.ChildInfo;
import com.djc.logintest.utils.Utils;

public class PushModel {

	private PushModel() {

	}

	public void enableDebug(boolean enable) {
		PushSettings.enableDebugMode(MyApplication.getInstance(), enable);
	}

	public static PushModel getPushModel() {
		return new PushModel();
	}

	public void bind() {
		Log.d("bbind", "do bind!");
		PushManager.startWork(MyApplication.getInstance(),
				PushConstants.LOGIN_TYPE_API_KEY, Utils.getMetaValue(
						MyApplication.getInstance(), ConstantValue.API_KEY));
	}

	public void unBind() {
		PushManager.stopWork(MyApplication.getInstance());
	}

	public void setTag(List<String> tags) {
		PushManager.setTags(MyApplication.getInstance(), tags);
	}

	// 设置学校id和班级id为默认tag,注意调用时机为，学校信息和小孩信息都同时获取到之后
	public void setAllDefaultTag() {
		try {
			List<String> tags = new ArrayList<String>();
			String schoolTag = DataMgr.getInstance().getSchoolID();
			List<ChildInfo> allChildrenInfo = DataMgr.getInstance()
					.getAllChildrenInfo();

			if (!allChildrenInfo.isEmpty() && !"".equals(schoolTag)) {
				for (ChildInfo info : allChildrenInfo) {
					if (!TextUtils.isEmpty(info.getClass_id())) {
						tags.add(info.getClass_id());
					}
				}
				tags.add(schoolTag);
				Log.d("DJC 10-16", "setTag tags=" + tags);
				PushModel.getPushModel().setTag(tags);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isBinded() {
		return PushManager.isPushEnabled(MyApplication.getInstance());
	}

	public List<String> getTags() {
		List<String> tags = new ArrayList<String>();
		String tagsStr = Utils.getUndeleteableProp(JSONConstant.PUSH_TAGS);
		Log.d("DJC 10-16", "tags =" + tagsStr);
		if (!"".equals(tagsStr)) {
			String[] split = tagsStr.split(",");
			tags = Arrays.asList(split);
		}

		return tags;
	}
}
