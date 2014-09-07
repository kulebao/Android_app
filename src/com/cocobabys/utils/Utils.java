package com.cocobabys.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventMap;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.customview.CustomDialog;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dlgmgr.DlgMgr;
import com.cocobabys.push.PushModel;

public class Utils {
	public static final int NETWORK_NOT_CONNECTED = -1;
	public static final String TAG = "Utils";
	public static final String APP_DIR_ROOT = "cocobaby";
	public static final String APP_DIR_TMP = "cocobaby/tmp";
	public static final String APP_DIR_PIC = "cocobaby/pic";
	public static final String APP_DIR_VOI = "cocobaby_tclient/voi";
	public static final String APP_LOGS = "cocobaby/logs";
	public static final int LIMIT_WIDTH = 320;
	public static final int LIMIT_HEIGHT = 480;
	private static String CHILD_PHOTO = "child_photo";
	public static String CHAT_ICON = "chat_icon";
	public static String VIDEO_PIC = "video_pic";
	public static String EXP_ICON = "exp_icon";
	public static String CHAT_VOICE = "chat_voice";

	private static int versionCode = Integer.MAX_VALUE;

	public static String getResString(int resID) {
		Resources resources = MyApplication.getInstance().getResources();
		return resources.getString(resID);
	}

	public static void makeToast(Context context, int resID) {
		Toast.makeText(context, resID, Toast.LENGTH_SHORT).show();
	}

	public static void makeToast(Context context, String content) {
		Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
	}

	// 获取AppKey
	public static String getMetaValue(Context context, String metaKey) {
		Bundle metaData = null;
		String apiKey = null;
		if (context == null || metaKey == null) {
			return null;
		}
		try {
			ApplicationInfo ai = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);
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
		if (versionCode == Integer.MAX_VALUE) {
			Context context = MyApplication.getInstance();
			try {
				PackageInfo info = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0);
				versionCode = info.versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}

		return versionCode;
	}

	public static void showSingleBtnEventDlg(int errorEventType, Context context) {
		CustomDialog.Builder builder = DlgMgr.getSingleBtnDlg(context);
		builder.setMessage(EventMap.getErrorResID(errorEventType));
		builder.create().show();
	}

	public static void showSingleBtnResDlg(int resID, Context context) {
		CustomDialog.Builder builder = DlgMgr.getSingleBtnDlg(context);
		builder.setMessage(context.getResources().getString(resID));
		builder.create().show();
	}

	public static void showSingleBtnResDlg(String content, Context context) {
		CustomDialog.Builder builder = DlgMgr.getSingleBtnDlg(context);
		builder.setMessage(content);
		builder.create().show();
	}

	public static void showSingleBtnResDlg(int resID, Context context,
			OnClickListener configListener) {
		CustomDialog.Builder builder = DlgMgr.getSingleBtnDlg(context,
				configListener);
		builder.setMessage(context.getResources().getString(resID));
		builder.create().show();
	}

	public static void showTwoBtnResDlg(int resID, Context context,
			OnClickListener configListener) {
		CustomDialog.Builder builder = DlgMgr.getTwoBtnDlg(context,
				configListener);
		builder.setMessage(context.getResources().getString(resID));
		builder.createTwoBtn().show();
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
		Editor editor = getEditor(ConstantValue.UNDELETEABLE_CONFIG);
		// 存入数据
		editor.putBoolean(ConstantValue.IS_FIRST_IN, false);
		// 提交修改
		editor.commit();
	}

	public static boolean isFirstStart() {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(
				ConstantValue.UNDELETEABLE_CONFIG, Context.MODE_PRIVATE);
		return conf.getBoolean(ConstantValue.IS_FIRST_IN, true);
	}

	public static String getProp(String key, String defaultValue) {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(
				ConstantValue.CONF_INI, Context.MODE_PRIVATE);
		return conf.getString(key, defaultValue);
	}

	public static String getProp(String key) {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(
				ConstantValue.CONF_INI, Context.MODE_PRIVATE);
		return conf.getString(key, "");
	}

	public static void saveCheckNewTime(long value) {
		SharedPreferences.Editor editor = getEditor();
		editor.putLong(ConstantValue.LATEST_CHECK_NEW_TIME, value);
		editor.commit();
	}

	public static long getCheckNewTime() {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(
				ConstantValue.CONF_INI, Context.MODE_PRIVATE);
		return conf.getLong(ConstantValue.LATEST_CHECK_NEW_TIME, 0);
	}

	public static void saveProp(String key, String value) {
		SharedPreferences.Editor editor = getEditor();
		editor.putString(key, value);
		editor.commit();
	}

	// 调用该接口保存的数据，退出登录后，不会清空
	public static void saveUndeleteableProp(String key, String value) {
		SharedPreferences.Editor editor = getEditor(ConstantValue.UNDELETEABLE_CONFIG);
		editor.putString(key, value);
		editor.commit();
	}

	public static String getUndeleteableProp(String key) {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(
				ConstantValue.UNDELETEABLE_CONFIG, Context.MODE_PRIVATE);
		return conf.getString(key, "");
	}

	private static SharedPreferences.Editor getEditor() {
		return getEditor(ConstantValue.CONF_INI);
	}

	private static SharedPreferences.Editor getEditor(String name) {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(name,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = conf.edit();
		return editor;
	}

	public static void clearProp() {
		SharedPreferences.Editor editor = getEditor(ConstantValue.CONF_INI);
		editor.clear();
		editor.commit();
		// editor = getEditor(ConstantValue.PUSH_CONFIG);
		// editor.clear();
		// editor.commit();
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

	public static synchronized void bindPush() {
		try {
			if (!Utils.isNetworkConnected(MyApplication.getInstance())) {
				return;
			}

			PushModel pushModel = PushModel.getPushModel();

			if (!pushModel.isBinded()) {
				Log.w("DJC", "not bind,do it now!");
				pushModel.bind();
			} else {
				Log.w("DJC", "aleady bind!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String convertTime(long timestamp) {
		SimpleDateFormat fomat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.CHINESE);
		return fomat.format(new Date(timestamp));
	}

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
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
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
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
				return mNetworkInfo.getType();
			}
		}
		return NETWORK_NOT_CONNECTED;
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

	public static String getFixedUrl(String originalUrl, int limitwidth,
			int limitheight) {
		String fixedUrl = originalUrl
				+ String.format("?imageView/2/w/%d/h/%d", limitwidth,
						limitheight);
		Log.d("III", "getFixedUrl  fixedUrl=" + fixedUrl);
		return fixedUrl;
	}

	// 月日 时分
	public static String formatChineseTime(long time) {
		SimpleDateFormat format = new SimpleDateFormat("MM月dd日 HH:mm",
				Locale.CHINESE);
		String timestr = format.format(new Date(time));
		if (timestr.startsWith("0")) {
			timestr = timestr.replaceFirst("0", "");
		}
		return timestr;
	}

	public static boolean isVoiceOn() {
		return ConstantValue.VOICE_OPEN.equals(getProp(
				ConstantValue.VOICE_CONFIG, ConstantValue.VOICE_OPEN));
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

	private static File getAppSDCardPath() {
		return Environment.getExternalStorageDirectory();
		// return
		// MyApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
	}

	public static File getSDCardFileDir(String dir) {
		return new File(getAppSDCardPath(), dir);
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
		return new File(getAppSDCardPath(), APP_DIR_PIC).getAbsolutePath();
	}

	public static String getSDCardMediaRootPath(String type) {
		String dir = "";
		if (JSONConstant.IMAGE_TYPE.equals(type)) {
			dir = new File(getAppSDCardPath(), APP_DIR_PIC).getAbsolutePath();
		} else if (JSONConstant.VOICE_TYPE.equals(type)) {
			dir = new File(getAppSDCardPath(), APP_DIR_VOI).getAbsolutePath();
		}
		return dir;
	}

	public static boolean makeDirs(String dir) {
		File file = new File(dir);
		if (!file.exists()) {
			return file.mkdirs();
		}
		return false;
	}

	public static String getDir(String path) {
		if (TextUtils.isEmpty(path)) {
			return "";
		}
		int lastindex = path.lastIndexOf(File.separator);
		if (lastindex != -1) {
			return path.substring(0, lastindex);
		}
		return path;
	}

	public static boolean makeAppDirInSDCard(String dir) {
		if (isSdcardExisting()) {
			File file = new File(getAppSDCardPath(), dir);
			if (!file.exists()) {
				return file.mkdirs();
			}
		}
		return false;
	}

	public static void clearSDFolder() {
		if (isSdcardExisting()) {
			File file = new File(getAppSDCardPath(), APP_DIR_ROOT);
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

	public static void saveBitmapToSDCard(Bitmap bitmap, String name)
			throws Exception {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(name);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
				out.flush();
			}
		} finally {
			close(out);
		}
	}

	public static InputStream Bitmap2InputStream(Bitmap bm, int quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, quality, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}

	// 上传到oss的小孩照片固定地址
	public static String getUploadChildUrl() {
		return CHILD_PHOTO + File.separator
				+ DataMgr.getInstance().getSchoolID() + File.separator
				+ DataMgr.getInstance().getSelectedChild().getServer_id()
				+ File.separator
				+ DataMgr.getInstance().getSelectedChild().getServer_id()
				+ ".jpg";
	}

	public static String getChatIconUrl(long timestamp) {
		String dir = CHAT_ICON + File.separator
				+ DataMgr.getInstance().getSchoolID() + File.separator
				+ Utils.getAccount();
		return dir + File.separator + timestamp + ".jpg";
	}

	// 获取自己发送的图片的地址，避免自己发送的还从服务器去下
	public static String getChatMediaUrl(long timestamp, String type) {
		String url = "";
		if (JSONConstant.IMAGE_TYPE.equals(type)) {
			url = getChatIconUrl(timestamp);
		} else if (JSONConstant.VOICE_TYPE.equals(type)) {
			url = getChatVoiceUrl(timestamp);
		}
		return url;
	}

	public static String getChatVoiceUrl(long timestamp) {
		String dir = CHAT_VOICE + File.separator
				+ DataMgr.getInstance().getSelfInfoByPhone().getParent_id();
		return dir + File.separator + timestamp
				+ ConstantValue.DEFAULT_VOICE_TYPE;
	}

	public static String getVideoPicPath(String filename) {
		return getSDCardPicRootPath() + File.separator + VIDEO_PIC
				+ File.separator + filename;
	}

	public static String getChatIconDir(String childid) {
		return getSDCardPicRootPath() + File.separator + CHAT_ICON
				+ File.separator + childid + File.separator;
	}

	public static String getExpIconDir(String childid) {
		return getSDCardPicRootPath() + File.separator + EXP_ICON
				+ File.separator + childid + File.separator;
	}

	// url是针对sd卡，应用保存图片路径+name
	public static Bitmap getLoacalBitmapByName(String name) {
		// url 类似mnt/sdcard/cocobaby/pic name 类似school_logo
		String url = Utils.getSDCardFileDir(Utils.APP_DIR_PIC).getPath()
				+ File.separator + name;
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

	public static Bitmap getLoacalBitmap(String url, int maxpix) {
		Bitmap bmp = null;
		try {
			bmp = ImageDownloader.getResizedBmp(maxpix, url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bmp;
	}

	public static Bitmap getLoacalBitmap(String url, int maxHeight, int maxWidth) {
		Bitmap bmp = null;
		try {
			bmp = ImageDownloader.getResizedBmp(url, maxHeight, maxWidth);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bmp;
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
		Bitmap bitmap = null;
		try {
			// bitmap = downloader.download();
			bitmap = downloadImgImpl(Utils.getFixedUrl(url, LIMIT_WIDTH,
					LIMIT_HEIGHT));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	public static String getExpRelativePath(String sdCardPath) {
		String name = DataMgr.getInstance().getSchoolID() + File.separator
				+ EXP_ICON + File.separator
				+ DataMgr.getInstance().getSelectedChild().getServer_id()
				+ File.separator + getName(sdCardPath);
		return name;
	}

	public static Bitmap downloadImgWithJudgement(String url, float limitWidth,
			float limitHeight) {
		Bitmap bitmap = null;
		try {
			bitmap = downloadImgImpl(Utils.getFixedUrl(url, (int) limitWidth,
					(int) limitHeight));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	public static void mkDirs(String dir) {
		File file = new File(dir);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public static void saveInSDCard(String log) throws FileNotFoundException,
			IOException {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String filename = getSDCardFileDir(APP_LOGS).getPath()
					+ File.separator + "logs.txt";
			File file = new File(filename);
			if (!file.exists()) {
				file.createNewFile();
			}

			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(log.getBytes());
			fos.close();
		}
	}

	public static void saveInSDCard(StringBuffer sb, String fileName)
			throws FileNotFoundException, IOException {
		saveInSDCard(sb.toString(), fileName);
	}

	// 测试服务器地址
	private static final String TEST_HOST = "test_host";

	public static void setToTestHost(String enable) {
		saveProp(TEST_HOST, enable);
	}

	public static boolean isTestHost() {
		String prop = Utils.getProp(TEST_HOST, "false");
		return "true".equals(prop);
	}

	private static final String IS_MY_VIDEO = "is_my_video";

	public static boolean isMyVideo() {
		String prop = Utils.getProp(IS_MY_VIDEO, "false");
		return "true".equals(prop);
	}
	
	public static void setVideo(String enable) {
		saveProp(IS_MY_VIDEO, enable);
	}

	public static void saveInSDCard(String str, String fileName) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
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

	public static String getName(String fullpath) {
		int separatorIndex = fullpath.lastIndexOf(File.separator);
		return (separatorIndex < 0) ? fullpath : fullpath.substring(
				separatorIndex + 1, fullpath.length());
	}

	public static String getDefaultCameraDir() {
		String path = Environment.getExternalStorageDirectory().getPath()
				+ "/DCIM/Camera/";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}

	public static void goNextActivity(Activity activity, Class<?> toClass,
			boolean closeSelf) {
		Intent intent = new Intent();
		intent.setClass(activity, toClass);
		activity.startActivity(intent);
		if (closeSelf) {
			activity.finish();
		}
	}

	/**
	 * 实现文本复制功能 add by wangqianzhou
	 * 
	 * @param content
	 */
	public static void copy(String content) {
		if (TextUtils.isEmpty(content)) {
			return;
		}
		// 得到剪贴板管理器
		Context context = MyApplication.getInstance().getApplicationContext();
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		cmb.setText(content.trim());
	}

	// 向图库里添加文件路径，可以让图片显示在图库里
	public static void galleryAddPic(Uri uri) {
		Context context = MyApplication.getInstance().getApplicationContext();
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(uri);
		context.sendBroadcast(mediaScanIntent);
	}

	public static void closeKeyBoard(Activity activity) {
		View view = activity.getWindow().peekDecorView();
		if (view != null) {
			InputMethodManager inputmanger = (InputMethodManager) activity
					.getSystemService(Activity.INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
}