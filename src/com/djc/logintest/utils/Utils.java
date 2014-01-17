package com.djc.logintest.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventMap;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.customview.CustomDialog;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dlgmgr.DlgMgr;
import com.djc.logintest.push.PushModel;

public class Utils {
    public static final String TAG = "Utils";
    private static SimpleDateFormat fomat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final String APP_DIR_ROOT = "cocobaby";
    public static final String APP_DIR_TMP = "cocobaby/tmp";
    public static final String APP_DIR_PIC = "cocobaby/pic";
    public static final String APP_LOGS = "cocobaby/logs";

    // 获取AppKey
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {

        }
        return apiKey;
    }

    public static int getVersionCode() {
        int versionCode = 1000;
        Context context = MyApplication.getInstance();
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0);
            versionCode = info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static void showSingleBtnEventDlg(int errorEventType, Context context) {
        AlertDialog.Builder builder = DlgMgr.getSingleBtnDlg(context);
        builder.setMessage(EventMap.getErrorResID(errorEventType));
        builder.create().show();
    }

    public static void showSingleBtnResDlg(int resID, Context context) {
        AlertDialog.Builder builder = DlgMgr.getSingleBtnDlg(context);
        builder.setMessage(context.getResources().getString(resID));
        builder.create().show();
    }

    public static void showSingleBtnResDlg(String content, Context context) {
        AlertDialog.Builder builder = DlgMgr.getSingleBtnDlg(context);
        builder.setMessage(content);
        builder.create().show();
    }

    public static void showSingleBtnResDlg(int resID, Context context,
            OnClickListener configListener) {
        AlertDialog.Builder builder = DlgMgr.getSingleBtnDlg(context, configListener);
        builder.setMessage(context.getResources().getString(resID));
        builder.create().show();
    }

    public static void showTwoBtnResDlg(int resID, Context context, OnClickListener configListener) {
        CustomDialog.Builder builder = DlgMgr.getTwoBtnDlg(context, configListener);
        builder.setMessage(context.getResources().getString(resID));
        builder.create().show();
    }

    public static void deleteProp(String key) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(key, "");
        editor.commit();
    }

    public static String getAccount() {
        return getProp(JSONConstant.ACCOUNT_NAME);
    }

    /**
     * 
     * method desc：设置已经引导过了，下次启动不用再次引导
     */
    public static void setGuided() {
        Editor editor = getEditor();
        // 存入数据
        editor.putBoolean(ConstantValue.IS_FIRST_IN, false);
        // 提交修改
        editor.commit();
    }

    public static boolean isFirstStart() {
        Context context = MyApplication.getInstance().getApplicationContext();
        SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI,
                Context.MODE_PRIVATE);
        return conf.getBoolean(ConstantValue.IS_FIRST_IN, true);
    }

    public static String getProp(String key, String defaultValue) {
        Context context = MyApplication.getInstance().getApplicationContext();
        SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI,
                Context.MODE_PRIVATE);
        return conf.getString(key, defaultValue);
    }

    public static String getProp(String key) {
        Context context = MyApplication.getInstance().getApplicationContext();
        SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI,
                Context.MODE_PRIVATE);
        return conf.getString(key, "");
    }

    public static void saveCheckNewTime(long value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putLong(ConstantValue.LATEST_CHECK_NEW_TIME, value);
        editor.commit();
    }

    public static long getCheckNewTime() {
        Context context = MyApplication.getInstance().getApplicationContext();
        SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI,
                Context.MODE_PRIVATE);
        return conf.getLong(ConstantValue.LATEST_CHECK_NEW_TIME, 0);
    }

    public static void saveProp(String key, String value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(key, value);
        editor.commit();
    }

    private static SharedPreferences.Editor getEditor() {
        Context context = MyApplication.getInstance().getApplicationContext();
        SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = conf.edit();
        return editor;
    }

    public static void clearProp() {
        SharedPreferences.Editor editor = getEditor();
        editor.clear();
        editor.commit();
    }

    // 应用是否登录 true 未登录 false已经登录
    public static boolean isLoginout() {
        return DataMgr.getInstance().getSchoolInfo() == null;
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 简单判断一下是否是手机号码
    // 判断依据，1开头的11位数字
    public static boolean checkPhoneNum(String phonenum) {
        String regexExp = "^[1][0-9]{10}$";
        return phonenum.matches(regexExp);
    }

    // 判断验证码是否合法，6位数字
    public static boolean checkAuthCode(String phonenum) {
        String regexExp = "^[0-9]{6}$";
        return phonenum.matches(regexExp);
    }

    // 检查密码格式是否正确,6-16位，数字或英文字母
    public static boolean checkPWD(String pwd) {
        String regexExp = "^[0-9a-zA-Z]{6,16}$";
        return pwd.matches(regexExp);
    }

    public static void bindPushTags() {
        PushModel pushModel = PushModel.getPushModel();
        pushModel.bind();

        // 如果没有设置默认tag，则设置tag
        if (pushModel.getTags().isEmpty()) {
            pushModel.setAllDefaultTag();
        }
    }

    public static String convertTime(long timestamp) {
        return fomat.format(new Date(timestamp));
    }

    // public static boolean isWiFiActive(Context inContext) {
    // Context context = inContext.getApplicationContext();
    // ConnectivityManager connectivity = (ConnectivityManager) context
    // .getSystemService(Context.CONNECTIVITY_SERVICE);
    // if (connectivity != null) {
    // NetworkInfo[] info = connectivity.getAllNetworkInfo();
    // if (info != null) {
    // for (int i = 0; i < info.length; i++) {
    // if (info[i].getTypeName().equalsIgnoreCase("WIFI") &&
    // info[i].isConnected()) {
    // return true;
    // }
    // }
    // }
    // }
    // return false;
    // }

    public static boolean isWiFiActive(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

    public boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static boolean isVoiceOn() {
        return ConstantValue.VOICE_OPEN.equals(getProp(ConstantValue.VOICE_CONFIG,
                ConstantValue.VOICE_OPEN));
    }

    public static Calendar getMonDayCalendar() {
        Calendar c = Calendar.getInstance();

        c.setFirstDayOfWeek(Calendar.MONDAY);// 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        int day = c.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天

        if (day == Calendar.SUNDAY) {
            // 如果刚好是周日，需要减去一周的时期，也就是7，否则周一算出来会是周日后面的日期
            c.add(Calendar.DATE, -7);
        }

        System.out.println("day：" + day);
        c.add(Calendar.DATE, c.getFirstDayOfWeek() - day);// 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        return c;
    }

    public static void setImg(ImageView view, Bitmap loacalBitmap) {
        if (loacalBitmap != null) {
            Drawable drawable = new BitmapDrawable(loacalBitmap);
            view.setImageDrawable(drawable);
        }
    }

    public static File getSDCardFileDir(String dir) {
        return new File(Environment.getExternalStorageDirectory(), dir);
    }

    public static boolean isSdcardExisting() {
        final String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static void makeDefaultDirInSDCard() {
        if (isSdcardExisting()) {
            makeAppDirInSDCard(APP_DIR_ROOT);
            makeAppDirInSDCard(APP_DIR_PIC);
            makeAppDirInSDCard(APP_DIR_TMP);
            makeAppDirInSDCard(APP_LOGS);
        }
    }

    public static String getSDCardPicRootPath() {
        return new File(Environment.getExternalStorageDirectory(), APP_DIR_PIC).getAbsolutePath();
    }

    public static boolean makeAppDirInSDCard(String dir) {
        if (isSdcardExisting()) {
            File file = new File(Environment.getExternalStorageDirectory(), dir);
            if (!file.exists()) {
                return file.mkdirs();
            }
        }
        return false;
    }

    public static void clearSDFolder() {
        if (isSdcardExisting()) {
            File file = new File(Environment.getExternalStorageDirectory(), APP_DIR_ROOT);
            if (file.exists()) {
                RecursionDeleteFile(file);
            }
        }
    }

    public static void RecursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }

    public static void saveBitmapToSDCard(Bitmap bitmap, String name) throws Exception {
        // File sdCardFileDir = getSDCardFileDir(Utils.APP_DIR_PIC);
        // File file = new File(sdCardFileDir, name);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(name);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)) {
                out.flush();
            }
        } finally {
            close(out);
        }
    }

    // url是针对sd卡，应用保存图片路径+name
    public static Bitmap getLoacalBitmapByName(String name) {
        // url 类似mnt/sdcard/cocobaby/pic name 类似school_logo
        String url = Utils.getSDCardFileDir(Utils.APP_DIR_PIC).getPath() + File.separator + name;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(fis);
        }
        return null;
    }

    // url是全路径
    public static Bitmap getLoacalBitmap(String url) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(fis);
        }
        return null;
    }

    public static Bitmap getBitmapFromUrl(String url) {
        return getBitmapFromUrl(url, 1);
    }

    public static Bitmap getBitmapFromUrl(String url, int retryTimes) {
        Bitmap bitmap = null;
        for (int i = 0; i < retryTimes; i++) {
            // bitmap = downloadImgImpl(url);
            bitmap = downloadImgWithJudgement(url);
            if (bitmap != null) {
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static Bitmap downloadImgImpl(String url) {
        URL uri;
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            uri = new URL(url);
            URLConnection conn = uri.openConnection();

            is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(is);
        }
        return bitmap;
    }

    public static Bitmap downloadImgWithJudgement(String url) {
        ImageDownloader downloader = new ImageDownloader(url);
        return downloader.download();
    }

    public static void mkDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void saveInSDCard(String log) throws FileNotFoundException, IOException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String filename = getSDCardFileDir(APP_LOGS).getPath() + File.separator + "logs.txt";
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(log.getBytes());
            fos.close();
        }
    }

    public static void saveInSDCard(StringBuffer sb, String fileName) throws FileNotFoundException,
            IOException {
        saveInSDCard(sb.toString(), fileName);
    }

    public static void saveInSDCard(String str, String fileName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String path = getSDCardFileDir(APP_LOGS).getPath();
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String absFileName = path + fileName;
            File file = new File(absFileName);

            try {
                if (!file.exists()) {
                    file.createNewFile();
                }

                FileOutputStream fos = new FileOutputStream(absFileName, true);
                fos.write(str.getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
