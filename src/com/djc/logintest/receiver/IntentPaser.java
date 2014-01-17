package com.djc.logintest.receiver;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.djc.logintest.R;
import com.djc.logintest.activities.LocationActivity;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.BindedNumInfo;
import com.djc.logintest.dbmgr.info.LocationInfo;
import com.djc.logintest.dbmgr.info.Notice;
import com.djc.logintest.handler.PushEventHandler;
import com.djc.logintest.noticepaser.NoticePaser;
import com.djc.logintest.noticepaser.NoticePaserFactory;
import com.djc.logintest.push.info.PushEvent;
import com.djc.logintest.utils.Utils;

public class IntentPaser {
    public static final String TAG = IntentPaser.class.getSimpleName();

    public static int notify_id = 0;

    private IntentPaser() {
    }

    public static IntentPaser getIntentPaser() {
        return new IntentPaser();
    }

    //应用受到的PushConstants.ACTION_MESSAGE类型消息都是应用服务器自己发送的
    //收到的PushConstants.ACTION_RECEIVE类型消息是百度服务器发送的账号绑定相关
    public PushEvent paraseIntent(final Context context, Intent intent) {
        PushEvent event = new PushEvent();
        if (intent.getAction().equals(PushConstants.ACTION_MESSAGE)) {
            if (Utils.isLoginout()) {
                // 未登录情况下，不接收任何消息,baidu绑定消息是通过ACTION_RECEIVE来通知应用的
                Log.i("DDD", "logout do not receive any msg");
                return null;
            }
            event.setType(PushEvent.TYPE_MESSAGE);
            // 获取消息内容
            String message = intent.getExtras().getString(PushConstants.EXTRA_PUSH_MESSAGE_STRING);
            event.setMessage(message);
            // 消息的用户自定义内容读取方式
            Log.i("DDD", "onMessage: " + message);

            // 自定义内容的json串
            Log.d("DDD", "EXTRA_EXTRA = " + intent.getStringExtra(PushConstants.EXTRA_EXTRA));
            handleMsg(message, context);
        } else if (intent.getAction().equals(PushConstants.ACTION_RECEIVE)) {
            event.setType(PushEvent.TYPE_NOTIFICATION);
            // 处理绑定等方法的返回数据
            // PushManager.startWork()的返回值通过PushConstants.METHOD_BIND得到

            // 获取方法
            final String method = intent.getStringExtra(PushConstants.EXTRA_METHOD);
            // 方法返回错误码。若绑定返回错误（非0），则应用将不能正常接收消息。
            // 绑定失败的原因有多种，如网络原因，或access token过期。
            // 请不要在出错时进行简单的startWork调用，这有可能导致死循环。
            // 可以通过限制重试次数，或者在其他时机重新调用来解决。
            int errorCode = intent.getIntExtra(PushConstants.EXTRA_ERROR_CODE,
                    PushConstants.ERROR_SUCCESS);
            String content = "";
            if (intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT) != null) {
                // 返回内容
                content = new String(intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));
            }

            String custom = intent.getStringExtra(PushConstants.EXTRA_EXTRA);
            Log.d("bbind", "method =" + method + " errorCode=" + errorCode + " content=" + content
                    + " custom=" + custom);

            event.setMethod(method);
            event.setMessage(content);
            event.setErrorCode(errorCode);
        } else if (intent.getAction().equals(PushConstants.ACTION_RECEIVER_NOTIFICATION_CLICK)) {
            // 可选。通知用户点击事件处理
            // handleClick(context, intent);
        }

        return event;
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

    private void setLocNotification(LocationInfo info, JSONObject object, Context context) {
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

            BindedNumInfo bindedNumInfo = DataMgr.getInstance().getBindedNumInfoByNum(
                    info.getLbs_num());
            if (bindedNumInfo != null) {
                title = bindedNumInfo.toString();
            }

            PendingIntent contentIntent = PendingIntent
                    .getActivity(context, notify_id++, intent, 0);

            Notification notif = new Notification(R.drawable.ic_launcher, context.getResources()
                    .getString(R.string.locationInfo), System.currentTimeMillis());

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
            long timestamp = Long.valueOf(object.getString(JSONConstant.TIME_STAMP));
            // 经纬度和位置属性是放在自定义字段
            JSONObject custom = new JSONObject(object.getString(JSONConstant.PUSH_CUSTOM));
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
        PendingIntent contentIntent = PendingIntent.getActivity(context, notify_id++, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notif = new Notification(R.drawable.small_logo, notice.getTicker(),
                System.currentTimeMillis());

        String content = notice.getContent();
        Log.d("DDD ", "content =" + content);
        if (notice.getType() == JSONConstant.NOTICE_TYPE_COOKBOOK) {
            // 食谱太长，就不在通知栏显示了
            content = "";
        }

        notif.setLatestEventInfo(context, notice.getTitle(), content, contentIntent);
        setSound(notif);
        notif.defaults |= Notification.DEFAULT_VIBRATE;
        // long[] vibrate = { 0, 250 };
        // notif.vibrate = vibrate;
        notif.flags |= Notification.FLAG_AUTO_CANCEL; // 在通知栏上点击此通知后自动清除此通知
        notif.icon = R.drawable.small_logo;
        if (!notice.isClear()) {
            notif.flags |= Notification.FLAG_NO_CLEAR; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，
        }

        nm.notify(notify_id, notif);
    }

}
