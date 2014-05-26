package com.cocobabys.jobs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.NewChatInfo;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.net.HttpResult;

public class GetTeacherInfoJob {
	private int maxThreads = 2;
	private ExecutorService service = Executors.newFixedThreadPool(maxThreads);
	private Map<String, NewChatInfo> map = new HashMap<String, NewChatInfo>();
	private Handler hanlder;
	private boolean stop = false;

	public void setHanlder(Handler hanlder) {
		this.hanlder = hanlder;
	}

	public GetTeacherInfoJob() {
	}

	public synchronized void addTask(String imgaeUrl, NewChatInfo info) {
		if (stop || map.containsKey(imgaeUrl)) {
			Log.d("DDD", "runTask do nothing, stop =" + stop);
			return;
		}

		map.put(imgaeUrl, info);
		try {
			service.execute(new GetTeacherRunnable(imgaeUrl, info));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class GetTeacherRunnable implements Runnable {
		private String imgaeUrl = "";
		private NewChatInfo info;

		public GetTeacherRunnable(String imgaeUrl, NewChatInfo info) {
			this.imgaeUrl = imgaeUrl;
			this.info = info;
		}

		@Override
		public void run() {
			int ret = EventType.GET_SENDER_FAIL;
			try {
				HttpResult result = new HttpResult();
				String url = createGetTeacherInfoUrl(info);
				Log.e("DDDDD ", "GetTeacherRunnable cmd:" + url);
				result = HttpClientHelper.executeGet(url);
				ret = handleGetTeacherResult(result);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				sendMsg(ret, info.getSender_id());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				map.remove(imgaeUrl);
			}
		}

		private void sendMsg(int result, String senderid) {
			if (stop) {
				return;
			}
			Message msg = Message.obtain();
			msg.what = result;
			msg.obj = senderid;
			hanlder.sendMessage(msg);
		}
	}

	public void stopTask() {
		stop = true;
		hanlder.removeCallbacksAndMessages(null);
		service.shutdown();
	}

	private String createGetTeacherInfoUrl(NewChatInfo info) {
		String url = String
				.format(ServerUrls.GET_SENDER_INFO, DataMgr.getInstance().getSchoolID(), info.getSender_id());
		url += "type=" + info.getSender_type();
		return url;
	}

	private int handleGetTeacherResult(HttpResult result) {
		int event = EventType.GET_SENDER_FAIL;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONObject jsonObject = result.getJsonObject();
				Teacher teacher = Teacher.toTeacher(jsonObject);
				DataMgr.getInstance().addTeacher(teacher);
				event = EventType.GET_SENDER_SUCCESS;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return event;
	}
}
