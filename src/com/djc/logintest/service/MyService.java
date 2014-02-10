package com.djc.logintest.service;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.baidu.android.pushservice.PushSettings;
import com.djc.logintest.R;
import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.News;
import com.djc.logintest.dbmgr.info.Notice;
import com.djc.logintest.handler.PushEventHandler;
import com.djc.logintest.net.GetNormalNewsMethod;
import com.djc.logintest.utils.Utils;

public class MyService extends Service {
    private static final int CHECK_NEWS = 1;
    private CheckNewsTask checkNewsTask;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("DJC 10-16", "MyService onCreate");
        PushSettings.enableDebugMode(this, true);
        PushEventHandler.getPushEventHandler().start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra(ConstantValue.COMMAND_CHECK_NOTICE, -1);

        switch (command) {
        case ConstantValue.COMMAND_TYPE_CHECK_NOTICE:
            Log.d("ddd", "onStartCommand COMMAND_TYPE_CHECK_NOTICE");
            checkNews();
            break;

        default:
            break;
        }

        return START_STICKY;
    }

    private void checkNews() {
        long from = 0;
        List<News> list = DataMgr.getInstance().getNewsByType(JSONConstant.NOTICE_TYPE_NORMAL, 1);
        if (!list.isEmpty()) {
            from = list.get(0).getNews_server_id();
        }

        if (checkNewsTask == null || checkNewsTask.getStatus() != AsyncTask.Status.RUNNING) {
            checkNewsTask = new CheckNewsTask(from);
            checkNewsTask.execute();
        } else {
            Log.d("djc", "should not getNewsImpl task already running!");
        }
    }

    public static void setNotification() {
        Context context = MyApplication.getInstance();
        // look up the notification manager service
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // Intent intent = new Intent(context, notice.getToClass());
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // intent.putExtra(JSONConstant.NOTIFICATION_TITLE, notice.getTitle());

        // 每次调用到这句时，第二个参数一定要不同，否则多个通知同时存在时，对于同一个id的通知
        // 点击后，始终会使用最后一次发送时的intent，这样导致每次打开activity看到的都是最后一次
        // 通知的内容
        // PendingIntent contentIntent = PendingIntent.getActivity(context,
        // notify_id++, intent,
        // PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notif = new Notification(R.drawable.small_logo, "您有新的公告",
                System.currentTimeMillis());

        notif.setLatestEventInfo(context, "您有新的公告", "", null);
        notif.defaults |= Notification.DEFAULT_SOUND;
        notif.defaults |= Notification.DEFAULT_VIBRATE;
        // long[] vibrate = { 0, 250 };
        // notif.vibrate = vibrate;
        notif.flags |= Notification.FLAG_AUTO_CANCEL; // 在通知栏上点击此通知后自动清除此通知
        notif.icon = R.drawable.tiny_logo;
        // if (!notice.isClear()) {
        // notif.flags |= Notification.FLAG_NO_CLEAR; //
        // 表明在点击了通知栏中的"清除通知"后，此通知不清除，
        // }

        nm.notify(CHECK_NEWS, notif);
    }

    class CheckNewsTask extends AsyncTask<Void, Void, Void> {

        private long from;
        private List<News> list;

        public CheckNewsTask(long from) {
            this.from = from;
        }

        @Override
        protected Void doInBackground(Void... params) {
            boolean networkConnected = Utils.isNetworkConnected(MyApplication.getInstance());
            if (networkConnected) {
                GetNormalNewsMethod method = GetNormalNewsMethod.getMethod();
                try {
                    list = method.getNormalNews(1, from, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (list != null && !list.isEmpty()) {
                setNotification();
            }
        }
    }
}
