package com.cocobabys.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.cocobabys.activities.CustomGalleryActivity;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.bean.AblumInfo;
import com.cocobabys.bean.AdInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.customview.CustomGallery;
import com.cocobabys.dbmgr.DataMgr;

public class DataUtils {

	private static int VERSION_CODE = Integer.MAX_VALUE;
	private static String AD_INFO = "AD_INFO";

	public static void saveCheckNewTime(long value) {
		SharedPreferences.Editor editor = DataUtils.getEditor();
		editor.putLong(ConstantValue.LATEST_CHECK_NEW_TIME, value);
		editor.commit();
	}

	public static long getCheckNewTime() {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI, Context.MODE_PRIVATE);
		return conf.getLong(ConstantValue.LATEST_CHECK_NEW_TIME, 0);
	}

	public static String getProp(String key) {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI, Context.MODE_PRIVATE);
		return conf.getString(key, "");
	}

	public static void saveProp(String key, String value) {
		SharedPreferences.Editor editor = DataUtils.getEditor();
		editor.putString(key, value);
		editor.commit();
	}

	public static String getProp(String key, String defaultValue) {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI, Context.MODE_PRIVATE);
		return conf.getString(key, defaultValue);
	}

	// 调用该接口保存的数据，退出登录后，不会清空
	public static void saveUndeleteableProp(String key, String value) {
		SharedPreferences.Editor editor = DataUtils.getEditor(ConstantValue.UNDELETEABLE_CONFIG);
		editor.putString(key, value);
		editor.commit();
	}

	public static String getUndeleteableProp(String key) {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(ConstantValue.UNDELETEABLE_CONFIG, Context.MODE_PRIVATE);
		return conf.getString(key, "");
	}

	public static boolean isFirstStart() {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(ConstantValue.UNDELETEABLE_CONFIG, Context.MODE_PRIVATE);
		return conf.getBoolean(ConstantValue.IS_FIRST_IN, true);
	}

	static SharedPreferences.Editor getEditor() {
		return DataUtils.getEditor(ConstantValue.CONF_INI);
	}

	static SharedPreferences.Editor getEditor(String name) {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = conf.edit();
		return editor;
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

	public static String getAccount() {
		return getProp(JSONConstant.ACCOUNT_NAME);
	}

	public static void deleteProp(String key) {
		SharedPreferences.Editor editor = getEditor();
		editor.putString(key, "");
		editor.commit();
	}

	// 获取AppKey
	public static String getMetaValue(Context context, String metaKey) {
		Bundle metaData = null;
		String apiKey = null;
		if (context == null || metaKey == null) {
			return null;
		}
		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
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
		if (DataUtils.VERSION_CODE == Integer.MAX_VALUE) {
			Context context = MyApplication.getInstance();
			try {
				PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				DataUtils.VERSION_CODE = info.versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}

		return DataUtils.VERSION_CODE;
	}

	public static void saveAdInfo(AdInfo info) {
		saveProp(AD_INFO, JSON.toJSONString(info));
	}

	public static AdInfo getAdInfo() {
		String prop = getProp(AD_INFO, "");

		if (!TextUtils.isEmpty(prop)) {
			return JSON.parseObject(prop, AdInfo.class);
		}

		return null;
	}

	public static boolean isFileExist(String path) {
		return new File(path).exists();
	}

	// 获取图库全部目录名称以及目录下的图片数量，以及最近一张图片的url
	public static List<AblumInfo> getGalleryPhotosDirs() {
		List<AblumInfo> list = new ArrayList<AblumInfo>();
		Cursor imagecursor = null;
		Context context = MyApplication.getInstance();
		try {
			final String[] columns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
					MediaStore.Images.Media.DATA };
			final String orderBy = MediaStore.Images.Media._ID + " DESC";

			String selection = MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?";
			String[] selectionArgs = new String[] { "image/jpeg", "image/png" };
			imagecursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
					selection, selectionArgs, orderBy);

			if (imagecursor != null && imagecursor.getCount() > 0) {

				while (imagecursor.moveToNext()) {
					int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
					String dir = imagecursor.getString(dataColumnIndex);

					dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
					String path = imagecursor.getString(dataColumnIndex);
					AblumInfo info = new AblumInfo();
					info.setDirName(dir);
					info.setDirCount(1);
					info.setLastestPicPath("file://" + path);

					if (!list.contains(info)) {
						list.add(info);
					} else {
						int indexOf = list.indexOf(info);
						AblumInfo ablumInfo = list.get(indexOf);
						ablumInfo.setDirCount(ablumInfo.getDirCount() + 1);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(imagecursor);
		}

		return list;
	}

	private static void closeCursor(Cursor imagecursor) {
		// if (Build.VERSION.SDK_INT < 14) {
		if (imagecursor != null) {
			imagecursor.close();
		}
		// }
	}

	public static ArrayList<CustomGallery> getGalleryPhotosByDir(String dir) {
		ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();
		Context context = MyApplication.getInstance();
		Cursor imagecursor = null;
		try {
			final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
			final String orderBy = MediaStore.Images.Media._ID + " DESC LIMIT "
					+ CustomGalleryActivity.MAX_PICS_SHOW_IN_GALLERY;

			String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?";
			String[] selectionArgs = new String[] { dir };

			imagecursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
					selection, selectionArgs, orderBy);

			if (imagecursor != null && imagecursor.getCount() > 0) {

				while (imagecursor.moveToNext()) {
					CustomGallery item = new CustomGallery();

					int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);

					String path = imagecursor.getString(dataColumnIndex);

					if (DataUtils.isValidFile(path)) {
						item.setSdcardPath(path);
						galleryList.add(item);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(imagecursor);
		}
		return galleryList;
	}

	public static ArrayList<CustomGallery> getRecentlyGalleryPhotos() {
		ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();
		Context context = MyApplication.getInstance();
		Cursor imagecursor = null;
		try {
			final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
			final String orderBy = MediaStore.Images.Media._ID + " DESC LIMIT "
					+ CustomGalleryActivity.MAX_PICS_SHOW_IN_GALLERY;

			imagecursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
					null, null, orderBy);

			if (imagecursor != null && imagecursor.getCount() > 0) {

				while (imagecursor.moveToNext()) {
					CustomGallery item = new CustomGallery();

					int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);

					String path = imagecursor.getString(dataColumnIndex);

					if (DataUtils.isValidFile(path)) {
						item.setSdcardPath(path);
						galleryList.add(item);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(imagecursor);
		}

		// show newest photo at beginning of the list
		// Collections.reverse(galleryList);
		return galleryList;
	}

	public static boolean isValidFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			if (file.length() > 0) {
				return true;
			}
		}
		return false;
	}
}
