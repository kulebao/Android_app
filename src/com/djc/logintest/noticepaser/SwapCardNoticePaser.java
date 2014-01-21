package com.djc.logintest.noticepaser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.djc.logintest.R;
import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.activities.SwipeDetailActivity;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.ChildInfo;
import com.djc.logintest.dbmgr.info.Notice;
import com.djc.logintest.receiver.IntentPaser;
import com.djc.logintest.threadpool.MyThreadPoolMgr;
import com.djc.logintest.utils.Utils;

public class SwapCardNoticePaser implements NoticePaser {
    public static final String SWIPE_CARD_TITLE = "尊敬的用户 %s 您好:";

    public static final String SWIPE_CARD_IN_BODY = "您的小孩 %s 已于%s刷卡入园!";
    public static final String SWIPE_CARD_OUT_BODY = "您的小孩 %s 已于%s刷卡离园!";

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("MM月dd日 HH时mm分");

    private long timestamp;

    private static String SWIPE_ICON = "swipe_icon";

    @Override
    public Notice saveData(JSONObject object) {
        try {
            final Context context = MyApplication.getInstance();
            String child_id = object.getString("child_id");
            final ChildInfo childinfo = DataMgr.getInstance().getChildByID(child_id);
            if (childinfo != null) {
                final Notice notice = getNotice(object, context, childinfo);
                Log.w("DDD", "saveData notice:" + notice.toString());
                final long id = DataMgr.getInstance().addNotice(notice);
                // 保存数据库中，该条记录的行号，传递给activity使用
                notice.setId((int) id);

                final String record_url = object.getString("record_url");
                if (record_url != null) {
                    MyThreadPoolMgr.getGenericService().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 就用通知id作为文件名
                                Log.d("LIYI", "downloadIcon begain record_url=" + record_url);
                                downloadIcon(record_url, String.valueOf(id));
                            } catch (Exception e) {
                                Log.d("LIYI", "downloadIcon exp:" + e.toString());
                                e.printStackTrace();
                            }
                            IntentPaser.setNotification(notice, context);
                        }
                    });
                    // 此时不要设置通知，等到图片下载完毕后再设置
                    return null;
                }
                return notice;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DDD", "saveData error e:" + e.toString());
        }
        return null;
    }

    public Notice getNotice(JSONObject object, Context context, ChildInfo childinfo)
            throws JSONException {
        int type = object.getInt(JSONConstant.NOTIFICATION_TYPE);
        final Notice notice = new Notice();
        timestamp = Long.valueOf(object.getString(JSONConstant.TIME_STAMP));
        notice.setChild_id(childinfo.getServer_id());

        String sample = (type == JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN ? SWIPE_CARD_IN_BODY
                : SWIPE_CARD_OUT_BODY);

        String body = String.format(sample, childinfo.getChild_nick_name(),
                FORMAT.format(new Date(timestamp)));
        notice.setContent(body);
        String title = String.format(SWIPE_CARD_TITLE, Utils.getProp(JSONConstant.USERNAME));
        notice.setTitle(title);
        notice.setTimestamp(Utils.convertTime(timestamp));

        notice.setToClass(SwipeDetailActivity.class);
        String ticker = context.getResources().getString(R.string.swipcard_notice);
        notice.setType(type);
        notice.setTicker(ticker);

        return notice;
    }

    // record_url 下载地址,iconname 下载成功后保存的文件名
    public void downloadIcon(String record_url, String iconname) throws Exception {
        Bitmap bmp = Utils.getBitmapFromUrl(record_url, 2);
        if (bmp != null) {
            String path = createSwipeIconPath(iconname);
            Log.d("LIYI", "saveBitmapToSDCard path=" + path);
            Utils.saveBitmapToSDCard(bmp, path);
        }
    }

    public static String createSwipeIconPath(String iconname) {
        String dir = Utils.getSDCardPicRootPath() + File.separator + SWIPE_ICON + File.separator;
        Utils.mkDir(dir);
        String url = dir + iconname;
        Log.d("DDD", "getChildrenDefaultLocalIconPath url=" + url);
        return url;
    }
}
