package com.cocobabys.net;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.im.IMHelper;

public class RelationshipMethod {
	private RelationshipMethod() {
	}

	public static RelationshipMethod getMethod() {
		return new RelationshipMethod();
	}

	public MethodResult getClassRelationship(String classid) throws Exception {
		MethodResult bret = new MethodResult(EventType.GET_CLASS_RELATIONSHIP_FAIL);
		HttpResult result = new HttpResult();
		String command = createCommand(classid);
		Log.d("", "getClassRelationship cmd=" + command);
		result = HttpClientHelper.executeGet(command);
		Log.d("", "getClassRelationship result=" + result.getContent());
		bret = handle(result);
		return bret;
	}

	private MethodResult handle(HttpResult result) throws Exception {
		MethodResult methodResult = new MethodResult(EventType.GET_CLASS_RELATIONSHIP_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			DataMgr.getInstance().addGroupInfo(result.getContent());
			methodResult.setResultType(EventType.GET_CLASS_RELATIONSHIP_SUCCESS);

			IMHelper.updateParentsInfoCache();
		}

		return methodResult;
	}

	private String createCommand(String classid) {
		String url = String.format(ServerUrls.GET_CLASS_RELATIONSHIP, DataMgr.getInstance().getSchoolID());
		url += "class_id=" + classid;
		return url;
	}
}
