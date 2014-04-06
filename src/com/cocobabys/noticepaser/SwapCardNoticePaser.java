package com.cocobabys.noticepaser;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.cocobabys.R;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.activities.SwipeDetailActivity;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.dbmgr.info.Notice;
import com.cocobabys.dbmgr.info.SwipeInfo;
import com.cocobabys.receiver.PushMessageReceiver;
import com.cocobabys.threadpool.MyThreadPoolMgr;
import com.cocobabys.utils.Utils;

public class SwapCardNoticePaser implements NoticePaser {
    private static String SWIPE_ICON = "swipe_icon";

    @Override
    public Notice saveData(JSONObject object) {
        try {
            final Context context = MyApplication.getInstance();
            String child_id = object.getString("child_id");
            final ChildInfo childinfo = DataMgr.getInstance().getChildByID(child_id);
            if (childinfo != null) {
                final SwipeInfo swipeInfo = saveSwipeInfo(object);
                final Notice notice = getNotice(swipeInfo, context, childinfo);
                Log.w("DDD", "saveData notice:" + notice.toString());

                final String record_url = swipeInfo.getUrl();
                if (!TextUtils.isEmpty(record_url)) {
                    MyThreadPoolMgr.getGenericService().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 就用通知时间搓作为文件名
                                Log.d("LIYI", "downloadIcon begain record_url=" + record_url);
                                downloadIcon(record_url, String.valueOf(swipeInfo.getTimestamp()));
                            } catch (Exception e) {
                                Log.d("LIYI", "downloadIcon exp:" + e.toString());
                                e.printStackTrace();
                            }
                            PushMessageReceiver.setNotification(notice, context);
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

    public Notice getNotice(SwipeInfo swipeInfo, Context context, ChildInfo childinfo)
            throws JSONException {
        int type = swipeInfo.getType();
        final Notice notice = new Notice();
        notice.setChild_id(swipeInfo.getChild_id());
        notice.setContent(swipeInfo.getNoticeBody(childinfo.getChild_nick_name()));
        notice.setTitle(swipeInfo.getNoticeTitle());
        notice.setTimestamp(swipeInfo.getFormattedTime());
        notice.setToClass(SwipeDetailActivity.class);
        String ticker = context.getResources().getString(R.string.swipcard_notice);
        notice.setType(type);
        notice.setTicker(ticker);

        // 保存数据库中，该条记录的行号，传递给activity使用
        notice.setId((int) swipeInfo.getId());
        return notice;
    }

    public static SwipeInfo saveSwipeInfo(JSONObject object) throws JSONException {
        SwipeInfo info = SwipeInfo.toSwipeInfo(object);
        long id = DataMgr.getInstance().addSwipeData(info);
        // 应该不会超过int最大值
        info.setId((int) id);
        return info;
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
        Utils.mkDirs(dir);
        String url = dir + iconname;
        Log.d("DDD", "getChildrenDefaultLocalIconPath url=" + url);
        return url;
    }
}
