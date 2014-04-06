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

	public int getTeacherInfo(String phones) throws Exception {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String url = createGetTeacherInfoUrl(phones);
		Log.e("DDDDD ", "createGetChildrenInfoUrl cmd:" + url);
		result = HttpClientHelper.executeGet(url);
		bret = handleGetTeacherResult(result);
		return bret;
	}

	private String createGetTeacherInfoUrl(String phones) {
		String url = String.format(ServerUrls.GET_TEACHER_INFO, DataMgr
				.getInstance().getSchoolID(), phones);
		return url;
	}

	private int handleGetTeacherResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONArray array = result.getJSONArray();
				Log.d("DDD handleGetTeacherResult", "str : " + array.toString());
				event = checkUpdate(array);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			event = EventType.SERVER_INNER_ERROR;
		}

		return event;
	}

	private int checkUpdate(JSONArray array) throws JSONException {
		DataMgr instance = DataMgr.getInstance();
		int event = EventType.SUCCESS;

		List<Teacher> fromnetTeachers = Teacher.toTeacherList(array);

		handleIncomingTeacher(instance, fromnetTeachers);
		return event;
	}

	private void handleIncomingTeacher(DataMgr instance,
			List<Teacher> fromnetTeachers) {
		for (Teacher teacher : fromnetTeachers) {
			boolean result = instance.handleIncomingTeacher(teacher);
			if (result) {
				// 头像大小最多50*50
				Bitmap bmp = Utils.downloadImgWithJudgement(
						teacher.getHead_icon(), ConstantValue.HEAD_ICON_WIDTH,
						ConstantValue.HEAD_ICON_HEIGHT);
				if (bmp != null) {
					try {
						Utils.saveBitmapToSDCard(bmp,
								teacher.getLocalIconPath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
