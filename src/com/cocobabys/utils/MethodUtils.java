package com.cocobabys.utils;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.cocobabys.R;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.activities.NoticePullRefreshActivity;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.EducationInfo;
import com.cocobabys.dbmgr.info.Homework;
import com.cocobabys.dbmgr.info.NewChatInfo;
import com.cocobabys.dbmgr.info.News;
import com.cocobabys.net.CookbookMethod;
import com.cocobabys.net.EducationMethod;
import com.cocobabys.net.HomeworkMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.NewChatMethod;
import com.cocobabys.net.NewsMethod;
import com.cocobabys.net.ScheduleMethod;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.service.MyService;

public class MethodUtils {

	public static final int CHECK_NEWS = 1001;

	// 检查是否有新公告
	public static boolean checkNews() {
		boolean has_new = false;
		boolean networkConnected = Utils.isNetworkConnected(MyApplication
				.getInstance());
		if (networkConnected) {
			NewsMethod method = NewsMethod.getMethod();
			try {
				long from = getMaxNewsTimestamp();
				has_new = !method.getNormalNews(1, from, 0,
						ConstantValue.Type_CHECK_NEW).isEmpty();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return has_new;
	}

	// 检查是否有新亲子作业
	public static boolean checkHomework() {
		boolean has_new = false;
		boolean networkConnected = Utils.isNetworkConnected(MyApplication
				.getInstance());
		if (networkConnected) {
			HomeworkMethod method = HomeworkMethod.getMethod();
			try {
				long from = getMaxHomeworkID();
				has_new = !method.getGetHomework(1, from, 0).isEmpty();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return has_new;
	}

	// 检查是否有新在园表现评价
	public static boolean checkEdu() {
		boolean has_new = false;
		boolean networkConnected = Utils.isNetworkConnected(MyApplication
				.getInstance());
		if (networkConnected) {
			EducationMethod method = EducationMethod.getMethod();
			try {
				long from = getMaxEducationID();
				has_new = !method.getEdus(1, from, 0).isEmpty();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return has_new;
	}

	// 检查是否有新教师留言
	public static boolean checkNewChat() {
		boolean has_new = false;
		boolean networkConnected = Utils.isNetworkConnected(MyApplication
				.getInstance());
		if (networkConnected) {
			NewChatMethod method = NewChatMethod.getMethod();
			try {
				long from = getMaxNewChatID();
				MethodResult chatInfo = method.getChatInfo(1, from, 0, DataMgr
						.getInstance().getSelectedChild().getServer_id());
				List<NewChatInfo> resultObj = (List<NewChatInfo>) chatInfo
						.getResultObj();
				has_new = !resultObj.isEmpty();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return has_new;
	}

	// 检查是否有新食谱
	public static boolean checkCookBook() {
		boolean has_new = false;
		boolean networkConnected = Utils.isNetworkConnected(MyApplication
				.getInstance());
		if (networkConnected) {
			CookbookMethod method = CookbookMethod.getMethod();
			try {
				has_new = method.checkCookBook();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return has_new;
	}

	// 检查是否有新课程表
	public static boolean checkSchedule() {
		boolean has_new = false;
		boolean networkConnected = Utils.isNetworkConnected(MyApplication
				.getInstance());
		if (networkConnected) {
			ScheduleMethod method = ScheduleMethod.getMethod();
			try {
				if (method.checkSchedule() == EventType.GET_SCHEDULE_SUCCESS) {
					has_new = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return has_new;
	}

	private static long getMaxNewChatID() {
		long from = 0;
		List<NewChatInfo> list = DataMgr
				.getInstance()
				.getNewChatInfoWithLimite(1,
						DataMgr.getInstance().getSelectedChild().getServer_id());
		if (!list.isEmpty()) {
			from = list.get(0).getChat_id();
		}
		return from;
	}

	private static long getMaxHomeworkID() {
		long from = 0;
		List<Homework> list = DataMgr.getInstance().getHomeworkWithLimite(1);
		if (!list.isEmpty()) {
			from = list.get(0).getServer_id();
		}
		return from;
	}

	private static long getMaxEducationID() {
		long from = 0;
		List<EducationInfo> list = DataMgr.getInstance()
				.getSelectedChildEduRecord();
		if (!list.isEmpty()) {
			from = list.get(0).getServer_id();
		}
		return from;
	}

	private static long getMaxNewsTimestamp() {
		long from = 0;
		List<News> list = DataMgr.getInstance().getNewsByType(
				JSONConstant.NOTICE_TYPE_NORMAL, 1);
		if (!list.isEmpty()) {
			from = list.get(0).getTimestamp();
		}
		return from;
	}

	public static void removeNewsNotification() {
		Context context = MyApplication.getInstance();
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(CHECK_NEWS);
	}

	public static void setNotification(int noticeid, String noticetitle,
			Class<?> toClass) {
		Context context = MyApplication.getInstance();
		// look up the notification manager service
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(context, toClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// 每次调用到这句时，第二个参数一定要不同，否则多个通知同时存在时，对于同一个id的通知
		// 点击后，始终会使用最后一次发送时的intent，这样导致每次打开activity看到的都是最后一次
		// 通知的内容
		PendingIntent contentIntent = PendingIntent.getActivity(context,
				noticeid, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notif = new Notification(R.drawable.small_logo,
				noticetitle, System.currentTimeMillis());

		notif.setLatestEventInfo(context, noticetitle, "", contentIntent);
		notif.defaults |= Notification.DEFAULT_SOUND;
		notif.defaults |= Notification.DEFAULT_VIBRATE;
		// long[] vibrate = { 0, 250 };
		// notif.vibrate = vibrate;
		notif.flags |= Notification.FLAG_AUTO_CANCEL; // 在通知栏上点击此通知后自动清除此通知
		notif.icon = R.drawable.tiny_logo;

		nm.notify(noticeid, notif);
	}

	// 设置新公告通知栏提示
	public static void setNewsNotification() {
		Context context = MyApplication.getInstance();
		// look up the notification manager service
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(context, NoticePullRefreshActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// 每次调用到这句时，第二个参数一定要不同，否则多个通知同时存在时，对于同一个id的通知
		// 点击后，始终会使用最后一次发送时的intent，这样导致每次打开activity看到的都是最后一次
		// 通知的内容
		PendingIntent contentIntent = PendingIntent.getActivity(context,
				MethodUtils.CHECK_NEWS, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notif = new Notification(R.drawable.small_logo, "您有新的公告",
				System.currentTimeMillis());

		notif.setLatestEventInfo(context, "您有新的公告", "", contentIntent);
		notif.defaults |= Notification.DEFAULT_SOUND;
		notif.defaults |= Notification.DEFAULT_VIBRATE;
		// long[] vibrate = { 0, 250 };
		// notif.vibrate = vibrate;
		notif.flags |= Notification.FLAG_AUTO_CANCEL; // 在通知栏上点击此通知后自动清除此通知
		notif.icon = R.drawable.tiny_logo;

		nm.notify(MethodUtils.CHECK_NEWS, notif);
	}

	public static void executeCheckNewsCommand(Context context) {
		Intent myintent = new Intent(context, MyService.class);
		myintent.putExtra(ConstantValue.SERVICE_COMMAND,
				ConstantValue.COMMAND_TYPE_CHECK_NOTICE);
		context.startService(myintent);
	}

	public static void executeCheckHomeworkCommand(Context context) {
		Intent myintent = new Intent(context, MyService.class);
		myintent.putExtra(ConstantValue.SERVICE_COMMAND,
				ConstantValue.COMMAND_TYPE_CHECK_HOMEWORK);
		context.startService(myintent);
	}

	public static void executeCheckCookbookCommand(Context context) {
		Intent myintent = new Intent(context, MyService.class);
		myintent.putExtra(ConstantValue.SERVICE_COMMAND,
				ConstantValue.COMMAND_TYPE_CHECK_COOKBOOK);
		context.startService(myintent);
	}

	public static void executeCheckScheduleCommand(Context context) {
		Intent myintent = new Intent(context, MyService.class);
		myintent.putExtra(ConstantValue.SERVICE_COMMAND,
				ConstantValue.COMMAND_TYPE_CHECK_SCHEDULE);
		context.startService(myintent);
	}

	public static void executeCheckChatCommand(Context context) {
		Intent myintent = new Intent(context, MyService.class);
		myintent.putExtra(ConstantValue.SERVICE_COMMAND,
				ConstantValue.COMMAND_TYPE_CHECK_CHAT);
		context.startService(myintent);
	}

	public static void executeCheckEducationCommand(Context context) {
		Intent myintent = new Intent(context, MyService.class);
		myintent.putExtra(ConstantValue.SERVICE_COMMAND,
				ConstantValue.COMMAND_TYPE_CHECK_EDU);
		context.startService(myintent);
	}

	public static String getAllFormatedClassid() {
		String classIDs = "";
		List<String> class_ids = DataMgr.getInstance().getAllClassID();
		for (String id : class_ids) {
			classIDs += id + ",";
		}
		return classIDs.substring(0, classIDs.length() - 1);
	}

	// 临时处理一下，本来应该都返回MethodResult，但是有大量老接口，还是使用int做返回，这里做一下适配
	public static MethodResult getBindResult(MyProxyImpl bind) throws Exception {
		MethodResult bret = null;
		Object obj = null;
		try {
			obj = bind.handle();
			bret = (MethodResult) obj;
		} catch (ClassCastException e) {
			bret = new MethodResult();
			bret.setResultType((Integer) obj);
		}
		return bret;
	}
}
