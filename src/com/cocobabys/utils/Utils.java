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
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import com.cocobabys.media.MyMediaScannerConnectionClient;
import com.cocobabys.push.PushModel;

public class Utils {
	public static final int NETWORK_NOT_CONNECTED = -1;
	public static final String TAG = "Utils";
	public static final String APP_DIR_ROOT = "cocobaby";
	public static final String APP_DIR_TMP = "cocobaby/tmp";
	public static final String OLD_APP_DIR_PIC = "cocobaby/pic";
	public static final String APP_DIR_PIC = "cocobaby/images";

	// public static final String APP_DIR_PIC_NOMEIDA = "cocobaby/pic/.nomedia";
	public static final String APP_DIR_VOI = "cocobaby/voi";
	public static final String APP_DIR_VID = "cocobaby/video";
	public static final String APP_LOGS = "cocobaby/logs";
	public static final int LIMIT_WIDTH = 320;
	public static final int LIMIT_HEIGHT = 480;
	private static String CHILD_PHOTO = "child_photo";
	public static String CHAT_ICON = "chat_icon";
	public static String VIDEO_PIC = "video_pic";
	public static String EXP_ICON = "exp_icon";
	public static String CHAT_VOICE = "chat_voice";
	public static final String DEFAULT_VIDEO_ENDS = ".mp4";
	public static final String AD_FORMAT = "%s 提醒您，";
	public static String JPG_EXT = ".jpg";
	public static String PNG_EXT = ".png";

	public static String getResString(int resID) {
		Resources resources = MyApplication.getInstance().getResources();
		return resources.getString(resID);
	}

	public static ColorStateList getResColor(int colorID) {
		Resources resources = MyApplication.getInstance().getResources();
		return resources.getColorStateList(colorID);
	}

	public static void makeToast(Context context, int resID) {
		Toast.makeText(context, resID, Toast.LENGTH_SHORT).show();
	}

	public static void makeToast(Context context, String content) {
		Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
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

	public static String getAdNotice(String adContent) {
		return String.format(Utils.AD_FORMAT, adContent);
	}

	// record_url 下载地址,iconname 下载成功后保存的文件名
	public static void downloadIcon(String record_url, String path)
			throws Exception {
		Bitmap bmp = getBitmapFromUrl(record_url, 2);
		if (bmp != null) {
			Log.d("LIYI", "saveBitmapToSDCard path=" + path);
			saveBitmapToSDCard(bmp, path);
		}
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
		return ConstantValue.VOICE_OPEN.equals(DataUtils.getProp(
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
			// 让pic目录下的图片不显示在系统图库里
			// makeAppDirInSDCard(APP_DIR_PIC_NOMEIDA);
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
		} else if (JSONConstant.VIDEO_TYPE.equals(type)) {
			dir = new File(getAppSDCardPath(), APP_DIR_VID).getAbsolutePath();
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
				+ JPG_EXT;
	}

	public static String getChatIconUrl(long timestamp) {
		String dir = CHAT_ICON + File.separator
				+ DataMgr.getInstance().getSchoolID() + File.separator
				+ DataUtils.getAccount();
		return dir + File.separator + timestamp + JPG_EXT;
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

	public static String getExpRelativePathExt(String realName) {
		String name = DataMgr.getInstance().getSchoolID() + File.separator
				+ EXP_ICON + File.separator
				+ DataMgr.getInstance().getSelectedChild().getServer_id()
				+ File.separator + realName;
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
		DataUtils.saveProp(TEST_HOST, enable);
	}

	public static boolean isTestHost() {
		String prop = DataUtils.getProp(TEST_HOST, "false");
		return "true".equals(prop);
	}

	private static final String IS_MY_VIDEO = "is_my_video";

	public static boolean isMyVideo() {
		String prop = DataUtils.getProp(IS_MY_VIDEO, "false");
		return "true".equals(prop);
	}

	public static void setVideo(String enable) {
		DataUtils.saveProp(IS_MY_VIDEO, enable);
	}

	public static String getExpVideoPath(long timestamp) {
		String parentid = DataMgr.getInstance().getSelfInfoByPhone()
				.getParent_id();

		String dir = getSDCardMediaRootPath(JSONConstant.VIDEO_TYPE)
				+ File.separator + parentid;

		File file = new File(dir);
		if (!file.exists()) {
			file.mkdirs();
		}

		String name = dir + File.separator + timestamp + DEFAULT_VIDEO_ENDS;
		return name;
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
	public static void addPicToGallery(Uri uri) {
		// Context context =
		// MyApplication.getInstance().getApplicationContext();
		// Intent mediaScanIntent = new
		// Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		// mediaScanIntent.setData(uri);
		// context.sendBroadcast(mediaScanIntent);
		MyMediaScannerConnectionClient mediaScannerConnectionClient = MyApplication
				.getInstance().getMediaScannerConnectionClient();
		mediaScannerConnectionClient.addPicToGallery(uri);
	}

	// 向图库里添加文件路径，可以让视频显示在图库里
	public static void addVideoToGallery(Uri uri) {
		MyMediaScannerConnectionClient mediaScannerConnectionClient = MyApplication
				.getInstance().getMediaScannerConnectionClient();
		mediaScannerConnectionClient.addVideoToGallery(uri);
	}

	public static void closeKeyBoard(Activity activity) {
		View view = activity.getWindow().peekDecorView();
		if (view != null) {
			InputMethodManager inputmanger = (InputMethodManager) activity
					.getSystemService(Activity.INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public static Bitmap createVideoThumbnail(String filePath) {
		Bitmap bitmap = null;
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		try {
			mmr.setDataSource(filePath);
			bitmap = mmr.getFrameAtTime();
			mmr.release();
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
		} finally {
			try {
				mmr.release();
			} catch (RuntimeException ex) {
			}
		}
		return bitmap;
	}

	// 修改pic目录名称，主要为了老版本和新版本兼容，把老版本的目录改名为新版本目录名
	public static void renamePicDir() {
		File fromFile = new File(getAppSDCardPath(), Utils.OLD_APP_DIR_PIC);
		if (fromFile.exists()) {
			File toFile = new File(getAppSDCardPath(), Utils.APP_DIR_PIC);
			if (!fromFile.renameTo(toFile)) {
				Log.d("", "renameDirectory failed! ");
			}
			Utils.removeAllPicExt();
		}

	}

	private static void removeAllPicExt() {
		File file = new File(getAppSDCardPath(), APP_DIR_PIC);
		removeImpl(file);
	}

	private static void removeImpl(File file) {
		if (file.isFile()
				&& (file.getPath().toLowerCase().endsWith(JPG_EXT) || file
						.getPath().toLowerCase().endsWith(PNG_EXT))) {
			String picFileNameNoExt = getPicFileNameNoExt(file.getPath()
					.toLowerCase());
			File toFile = new File(picFileNameNoExt);
			file.renameTo(toFile);
			return;
		}

		if (file.isDirectory()) {
			File[] childFile = file.listFiles();
			for (File f : childFile) {
				removeImpl(f);
			}
		}
	}

	private static int getPicIndex(String filename) {
		int dot = filename.lastIndexOf(JPG_EXT);
		if (dot <= -1) {
			dot = filename.lastIndexOf(PNG_EXT);
		}
		return dot;
	}

	// 去掉.png或.jpg或.mp4扩展名，不显示图片到系统图库
	public static String getPicFileNameNoExt(String filename) {
		if ((filename != null) && (filename.length() > 0)
				&& (filename.endsWith(JPG_EXT) || filename.endsWith(PNG_EXT))) {
			int dot = Utils.getPicIndex(filename);
			if ((dot > -1) && (dot < (filename.length()))) {
				return filename.substring(0, dot);
			}
		}
		return filename;
	}

	// 去掉媒体文件的扩展名，不显示在本地系统中
	public static String getMediaFileNameNoExt(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf(".");
			if ((dot > -1) && (dot < (filename.length()))) {
				return filename.substring(0, dot);
			}
		}
		return filename;
	}
}