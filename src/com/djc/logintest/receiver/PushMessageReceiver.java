package com.djc.logintest.receiver;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baidu.frontia.api.FrontiaPushMessageReceiver;
import com.djc.logintest.R;
import com.djc.logintest.activities.LocationActivity;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.BindedNumInfo;
import com.djc.logintest.dbmgr.info.LocationInfo;
import com.djc.logintest.dbmgr.info.Notice;
import com.djc.logintest.noticepaser.NoticePaser;
import com.djc.logintest.noticepaser.NoticePaserFactory;
import com.djc.logintest.utils.Utils;

/**
 * Push消息处理receiver
 */
public class PushMessageReceiver extends FrontiaPushMessageReceiver {
	public static final String TAG = PushMessageReceiver.class.getSimpleName();
	public static int notify_id = 0;

	/**
	 * 调用PushManager.startWork后，sdk将对push
	 * server发起绑定请求，这个过程是异步的。绑定请求的结果通过onBind返回。 如果您需要用单播推送，需要把这里获取的channel
	 * id和user id上传到应用server中，再调用server接口用channel id和user id给单个手机或者用户推送。
	 * 
	 * @param context
	 *            BroadcastReceiver的执行Context
	 * @param errorCode
	 *            绑定接口返回值，0 - 成功
	 * @param appid
	 *            应用id。errorCode非0时为null
	 * @param userId
	 *            应用user id。errorCode非0时为null
	 * @param channelId
	 *            应用channel id。errorCode非0时为null
	 * @param requestId
	 *            向服务端发起的请求id。在追查问题时有用；
	 * @return none
	 */
	@Override
	public void onBind(Context context, int errorCode, String appid,
			String userId, String channelId, String requestId) {
		String responseString = "onBind errorCode=" + errorCode + " appid="
				+ appid + " userId=" + userId + " channelId=" + channelId
				+ " requestId=" + requestId;
		Log.d(TAG, responseString);

		// 绑定成功，设置已绑定flag，可以有效的减少不必要的绑定请求
		if (errorCode == 0) {
			Utils.savePushProp(JSONConstant.CHANNEL_ID, channelId);
			Utils.savePushProp(JSONConstant.USER_ID, userId);
		}
	}

	/**
	 * 接收透传消息的函数。
	 * 
	 * @param context
	 *            上下文
	 * @param message
	 *            推送的消息
	 * @param customContentString
	 *            自定义内容,为空或者json字符串
	 */
	@Override
	public void onMessage(Context context, String message,
			String customContentString) {
		String messageString = "透传消息 message=\"" + message
				+ "\" customContentString=" + customContentString;
		Log.d(TAG, messageString);
		if (Utils.isLoginout()) {
			// 未登录情况下，不接收任何消息,baidu绑定消息是通过ACTION_RECEIVE来通知应用的
			Log.i("DDD", "logout do not receive any msg");
			return;
		}

		handleMsg(message, context);
		// 自定义内容获取方式，mykey和myvalue对应透传消息推送时自定义内容中设置的键和值
		// if (customContentString != null
		// & TextUtils.isEmpty(customContentString)) {
		// JSONObject customJson = null;
		// try {
		// customJson = new JSONObject(customContentString);
		// String myvalue = null;
		// if (customJson.isNull("mykey")) {
		// myvalue = customJson.getString("mykey");
		// }
		// } catch (JSONException e) {
		// e.printStackTrace();
		// }
		// }

	}

	protected void handleMsg(String msg, Context context) {
		try {
			JSONObject object = new JSONObject(msg);
			int type = object.getInt(JSONConstant.NOTIFICATION_TYPE);
			if (type == JSONConstant.NOTICE_TYPE_LOCATION) {
				LocationInfo info = getLocationInfo(object);
				saveLocationInfo(info);
				setLocNotification(info, object, context);
				return;
			}

			NoticePaser paser = NoticePaserFactory.createNoticePaser(type);
			if (paser != null) {
				Notice notice = paser.saveData(object);
				if (notice != null) {
					setNotification(notice, context);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void setLocNotification(LocationInfo info, JSONObject object,
			Context context) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(context, LocationActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		try {
			String title = object.getString(JSONConstant.NOTIFICATION_TITLE);
			// 注意，这里进行了修正，使用address做body
			String body = info.getAddress();

			intent.putExtra(JSONConstant.LATITUDE, info.getLatitude());
			intent.putExtra(JSONConstant.LONGITUDE, info.getLongitude());
			intent.putExtra(JSONConstant.ADDRESS, info.getAddress());
			intent.putExtra(JSONConstant.TIME_STAMP, info.getTimestamp());
			intent.putExtra(JSONConstant.LBS_NUM, info.getLbs_num());

			BindedNumInfo bindedNumInfo = DataMgr.getInstance()
					.getBindedNumInfoByNum(info.getLbs_num());
			if (bindedNumInfo != null) {
				title = bindedNumInfo.toString();
			}

			PendingIntent contentIntent = PendingIntent.getActivity(context,
					notify_id++, intent, 0);

			Notification notif = new Notification(R.drawable.ic_launcher,
					context.getResources().getString(R.string.locationInfo),
					System.currentTimeMillis());

			notif.setLatestEventInfo(context, title, body, contentIntent);
			setSound(notif);
			notif.defaults |= Notification.DEFAULT_VIBRATE;
			notif.flags |= Notification.FLAG_AUTO_CANCEL; // 在通知栏上点击此通知后自动清除此通知
			notif.flags |= Notification.FLAG_NO_CLEAR; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，

			nm.notify(notify_id, notif);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setSound(Notification notif) {
		// 通过设置控制提示音打开关闭
		if (Utils.isVoiceOn()) {
			notif.defaults |= Notification.DEFAULT_SOUND;
		}
	}

	private void saveLocationInfo(LocationInfo info) {
		DataMgr.getInstance().addLocationInfo(info);
	}

	private LocationInfo getLocationInfo(JSONObject object) {
		LocationInfo info = null;
		try {
			// 注意timestamp是放在通用字段里面的
			long timestamp = Long.valueOf(object
					.getString(JSONConstant.TIME_STAMP));
			// 经纬度和位置属性是放在自定义字段
			JSONObject custom = new JSONObject(
					object.getString(JSONConstant.PUSH_CUSTOM));
			String lat = custom.getString(JSONConstant.LATITUDE);
			String lot = custom.getString(JSONConstant.LONGITUDE);
			String address = custom.getString(JSONConstant.ADDRESS);
			String lbs_num = custom.getString(JSONConstant.LBS_NUM);

			info = new LocationInfo();
			info.setLatitude(lat);
			info.setLongitude(lot);
			info.setTimestamp(Utils.convertTime(timestamp));
			info.setAddress(address);
			info.setLbs_num(lbs_num);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}

	public static void setNotification(Notice notice, Context context) {
		// look up the notification manager service
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(context, notice.getToClass());
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(JSONConstant.NOTIFICATION_TITLE, notice.getTitle());
		intent.putExtra(JSONConstant.NOTIFICATION_BODY, notice.getContent());
		intent.putExtra(JSONConstant.NOTIFICATION_TYPE, notice.getType());
		intent.putExtra(JSONConstant.TIME_STAMP, notice.getTimestamp());
		intent.putExtra(JSONConstant.PUBLISHER, notice.getPublisher());
		intent.putExtra(JSONConstant.NOTIFICATION_ID, notice.getId());

		// 每次调用到这句时，第二个参数一定要不同，否则多个通知同时存在时，对于同一个id的通知
		// 点击后，始终会使用最后一次发送时的intent，这样导致每次打开activity看到的都是最后一次
		// 通知的内容
		PendingIntent contentIntent = PendingIntent.getActivity(context,
				notify_id++, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notif = new Notification(R.drawable.small_logo,
				notice.getTicker(), System.currentTimeMillis());

		String content = notice.getContent();
		Log.d("DDD ", "content =" + content);
		if (notice.getType() == JSONConstant.NOTICE_TYPE_COOKBOOK) {
			// 食谱太长，就不在通知栏显示了
			content = "";
		}

		notif.setLatestEventInfo(context, notice.getTitle(), content,
				contentIntent);
		setSound(notif);
		notif.defaults |= Notification.DEFAULT_VIBRATE;
		// long[] vibrate = { 0, 250 };
		// notif.vibrate = vibrate;
		notif.flags |= Notification.FLAG_AUTO_CANCEL; // 在通知栏上点击此通知后自动清除此通知
		notif.icon = R.drawable.tiny_logo;
		if (!notice.isClear()) {
			notif.flags |= Notification.FLAG_NO_CLEAR; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，
		}

		nm.notify(notify_id, notif);
	}

	/**
	 * 接收通知点击的函数。注：推送通知被用户点击前，应用无法通过接口获取通知的内容。
	 * 
	 * @param context
	 *            上下文
	 * @param title
	 *            推送的通知的标题
	 * @param description
	 *            推送的通知的描述
	 * @param customContentString
	 *            自定义内容，为空或者json字符串
	 */
	@Override
	public void onNotificationClicked(Context context, String title,
			String description, String customContentString) {
	}

	/**
	 * setTags() 的回调函数。
	 * 
	 * @param context
	 *            上下文
	 * @param errorCode
	 *            错误码。0表示某些tag已经设置成功；非0表示所有tag的设置均失败。
	 * @param successTags
	 *            设置成功的tag
	 * @param failTags
	 *            设置失败的tag
	 * @param requestId
	 *            分配给对云推送的请求的id
	 */
	@Override
	public void onSetTags(Context context, int errorCode,
			List<String> sucessTags, List<String> failTags, String requestId) {
		String responseString = "onSetTags errorCode=" + errorCode
				+ " sucessTags=" + sucessTags + " failTags=" + failTags
				+ " requestId=" + requestId;
		Log.d(TAG, responseString);
		Utils.savePushProp(JSONConstant.PUSH_TAGS, listToString(sucessTags));
	}

	private String listToString(List<String> list) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			if (i == list.size() - 1)// 当循环到最后一个的时候 就不添加逗号,
			{
				str.append(list.get(i));
			} else {
				str.append(list.get(i));
				str.append(",");
			}
		}
		return str.toString();
	}

	/**
	 * delTags() 的回调函数。
	 * 
	 * @param context
	 *            上下文
	 * @param errorCode
	 *            错误码。0表示某些tag已经删除成功；非0表示所有tag均删除失败。
	 * @param successTags
	 *            成功删除的tag
	 * @param failTags
	 *            删除失败的tag
	 * @param requestId
	 *            分配给对云推送的请求的id
	 */
	@Override
	public void onDelTags(Context context, int errorCode,
			List<String> sucessTags, List<String> failTags, String requestId) {
		String responseString = "onDelTags errorCode=" + errorCode
				+ " sucessTags=" + sucessTags + " failTags=" + failTags
				+ " requestId=" + requestId;
		Log.d(TAG, responseString);

	}

	/**
	 * listTags() 的回调函数。
	 * 
	 * @param context
	 *            上下文
	 * @param errorCode
	 *            错误码。0表示列举tag成功；非0表示失败。
	 * @param tags
	 *            当前应用设置的所有tag。
	 * @param requestId
	 *            分配给对云推送的请求的id
	 */
	@Override
	public void onListTags(Context context, int errorCode, List<String> tags,
			String requestId) {
		String responseString = "onListTags errorCode=" + errorCode + " tags="
				+ tags;
		Log.d(TAG, responseString);
	}

	/**
	 * PushManager.stopWork() 的回调函数。
	 * 
	 * @param context
	 *            上下文
	 * @param errorCode
	 *            错误码。0表示从云推送解绑定成功；非0表示失败。
	 * @param requestId
	 *            分配给对云推送的请求的id
	 */
	@Override
	public void onUnbind(Context context, int errorCode, String requestId) {
		String responseString = "onUnbind errorCode=" + errorCode
				+ " requestId = " + requestId;
		Log.d(TAG, responseString);

		// 解绑定成功，设置未绑定flag，
		if (errorCode == 0) {
			// Utils.setBind(context, false);
		}
	}
}
