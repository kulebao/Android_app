package com.cocobabys.net;

import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;

import android.graphics.Bitmap;
import android.util.Log;

import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.Utils;

public class TeacherMethod {

	private TeacherMethod() {
	}

	public static TeacherMethod getMethod() {
		return new TeacherMethod();
	}

	public MethodResult getTeacherListByClassID(String classid) throws Exception {
		MethodResult methodResult = new MethodResult(EventType.NET_WORK_INVALID);
		HttpResult result = new HttpResult();
		String url = createGetTeacherListUrl(classid);
		Log.e("DDDDD ", "getTeacherListByClassID cmd:" + url);
		result = HttpClientHelper.executeGet(url);
		Log.e("DDDDD ", "getTeacherListByClassID result:" + result.getContent());
		methodResult = handleGetTeacherResult(result);
		return methodResult;
	}

	private String createGetTeacherListUrl(String classid) {
		String url = String.format(ServerUrls.GET_TEACHER_LIST, DataMgr.getInstance().getSchoolID(), classid);
		return url;
	}

	public MethodResult getTeacherInfo(String phones) throws Exception {
		MethodResult methodResult = new MethodResult(EventType.NET_WORK_INVALID);
		HttpResult result = new HttpResult();
		String url = createGetTeacherInfoUrl(phones);
		Log.e("DDDDD ", "getTeacherInfo cmd:" + url);
		result = HttpClientHelper.executeGet(url);
		Log.e("DDDDD ", "getTeacherInfo result:" + result.getContent());
		methodResult = handleGetTeacherResult(result);
		return methodResult;
	}

	private String createGetTeacherInfoUrl(String phones) {
		String url = String.format(ServerUrls.GET_TEACHER_INFO, DataMgr.getInstance().getSchoolID(), phones);
		return url;
	}

	private MethodResult handleGetTeacherResult(HttpResult result) {
		MethodResult methodResult = new MethodResult(EventType.GET_TEACHER_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONArray array = result.getJSONArray();
				Log.d("DDD handleGetTeacherResult", "str : " + array.toString());
				methodResult = checkUpdate(array);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return methodResult;
	}

	private MethodResult checkUpdate(JSONArray array) throws JSONException {
		MethodResult methodResult = new MethodResult(EventType.GET_TEACHER_SUCCESS);
		DataMgr instance = DataMgr.getInstance();

		List<Teacher> fromnetTeachers = Teacher.toTeacherList(array);
		methodResult.setResultObj(fromnetTeachers);
		handleIncomingTeacher(instance, fromnetTeachers);
		return methodResult;
	}

	private void handleIncomingTeacher(DataMgr instance, List<Teacher> fromnetTeachers) {
		for (Teacher teacher : fromnetTeachers) {
			boolean result = instance.handleIncomingTeacher(teacher);
			if (result) {
				// 头像大小最多50*50
				Bitmap bmp = Utils.downloadImgWithJudgement(teacher.getHead_icon(), ConstantValue.HEAD_ICON_WIDTH,
						ConstantValue.HEAD_ICON_HEIGHT);
				if (bmp != null) {
					try {
						Utils.saveBitmapToSDCard(bmp, teacher.getLocalIconPath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
