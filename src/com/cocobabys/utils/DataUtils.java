package com.cocobabys.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.model.LatLng;
import com.cocobabys.activities.CustomGalleryActivity;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.bean.AblumInfo;
import com.cocobabys.bean.AdInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.customview.CustomGallery;
import com.cocobabys.dbmgr.DataMgr;

public class DataUtils{

    private static int    VERSION_CODE = Integer.MAX_VALUE;
    private static String AD_INFO      = "AD_INFO";

    public static boolean needCheckNotice(){
        long checkNewTime = DataUtils.getCheckNoticeTime();
        long currentTime = System.currentTimeMillis();
        if((currentTime - checkNewTime) >= ConstantValue.CHECK_NOTICE_TIME_SPAN){
            saveCheckNoticeTime(currentTime);
            return true;
        }
        return false;
    }

    private static void saveCheckNoticeTime(long value){
        SharedPreferences.Editor editor = DataUtils.getEditor();
        editor.putLong(ConstantValue.LATEST_CHECK_NOTICE_TIME, value);
        editor.commit();
    }

    private static long getCheckNoticeTime(){
        Context context = MyApplication.getInstance().getApplicationContext();
        SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI, Context.MODE_PRIVATE);
        return conf.getLong(ConstantValue.LATEST_CHECK_NOTICE_TIME, 0);
    }

    public static void saveCheckNewTime(long value){
        SharedPreferences.Editor editor = DataUtils.getEditor();
        editor.putLong(ConstantValue.LATEST_CHECK_NEW_TIME, value);
        editor.commit();
    }

    public static long getCheckNewTime(){
        Context context = MyApplication.getInstance().getApplicationContext();
        SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI, Context.MODE_PRIVATE);
        return conf.getLong(ConstantValue.LATEST_CHECK_NEW_TIME, 0);
    }

    public static String getProp(String key){
        Context context = MyApplication.getInstance().getApplicationContext();
        SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI, Context.MODE_PRIVATE);
        return conf.getString(key, "");
    }

    public static void saveProp(String key, String value){
        SharedPreferences.Editor editor = DataUtils.getEditor();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getProp(String key, String defaultValue){
        Context context = MyApplication.getInstance().getApplicationContext();
        SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI, Context.MODE_PRIVATE);
        return conf.getString(key, defaultValue);
    }

    // 调用该接口保存的数据，退出登录后，不会清空
    public static void saveUndeleteableProp(String key, String value){
        SharedPreferences.Editor editor = DataUtils.getEditor(ConstantValue.UNDELETEABLE_CONFIG);
        editor.putString(key, value);
        editor.commit();
    }

    public static String getUndeleteableProp(String key){
        Context context = MyApplication.getInstance().getApplicationContext();
        SharedPreferences conf = context.getSharedPreferences(ConstantValue.UNDELETEABLE_CONFIG, Context.MODE_PRIVATE);
        return conf.getString(key, "");
    }

    public static boolean isFirstStart(){
        Context context = MyApplication.getInstance().getApplicationContext();
        SharedPreferences conf = context.getSharedPreferences(ConstantValue.UNDELETEABLE_CONFIG, Context.MODE_PRIVATE);
        return conf.getBoolean(ConstantValue.IS_FIRST_IN, true);
    }

    static SharedPreferences.Editor getEditor(){
        return DataUtils.getEditor(ConstantValue.CONF_INI);
    }

    static SharedPreferences.Editor getEditor(String name){
        Context context = MyApplication.getInstance().getApplicationContext();
        SharedPreferences conf = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = conf.edit();
        return editor;
    }

    /**
     * 
     * method desc：设置已经引导过了，下次启动不用再次引导
     */
    public static void setGuided(){
        Editor editor = getEditor(ConstantValue.UNDELETEABLE_CONFIG);
        // 存入数据
        editor.putBoolean(ConstantValue.IS_FIRST_IN, false);
        // 提交修改
        editor.commit();
    }

    public static void clearProp(){
        SharedPreferences.Editor editor = getEditor(ConstantValue.CONF_INI);
        editor.clear();
        editor.commit();
        // editor = getEditor(ConstantValue.PUSH_CONFIG);
        // editor.clear();
        // editor.commit();
    }

    // 应用是否登录 true 未登录 false已经登录
    public static boolean isLoginout(){
        return DataMgr.getInstance().getSchoolInfo() == null;
    }

    public static String getAccount(){
        return getProp(JSONConstant.ACCOUNT_NAME);
    }

    public static void deleteProp(String key){
        SharedPreferences.Editor editor = getEditor();
        editor.putString(key, "");
        editor.commit();
    }

    // 获取AppKey
    public static String getMetaValue(Context context, String metaKey){
        Bundle metaData = null;
        String apiKey = null;
        if(context == null || metaKey == null){
            return null;
        }
        try{
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                                                                                PackageManager.GET_META_DATA);
            if(null != ai){
                metaData = ai.metaData;
            }
            if(null != metaData){
                apiKey = metaData.getString(metaKey);
            }
        } catch(NameNotFoundException e){

        }
        return apiKey;
    }

    public static int getVersionCode(){
        if(DataUtils.VERSION_CODE == Integer.MAX_VALUE){
            Context context = MyApplication.getInstance();
            try{
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                DataUtils.VERSION_CODE = info.versionCode;
            } catch(NameNotFoundException e){
                e.printStackTrace();
            }
        }

        return DataUtils.VERSION_CODE;
    }

    public static void saveAdInfo(AdInfo info){
        saveProp(AD_INFO, JSON.toJSONString(info));
    }

    public static AdInfo getAdInfo(){
        String prop = getProp(AD_INFO, "");

        if(!TextUtils.isEmpty(prop)){
            return JSON.parseObject(prop, AdInfo.class);
        }

        return null;
    }

    public static boolean isFileExist(String path){
        return new File(path).exists();
    }

    // 获取图库全部目录名称以及目录下的图片数量，以及最近一张图片的url
    public static List<AblumInfo> getGalleryPhotosDirs(){
        List<AblumInfo> list = new ArrayList<AblumInfo>();
        Cursor imagecursor = null;
        Context context = MyApplication.getInstance();
        try{
            final String[] columns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATA };
            final String orderBy = MediaStore.Images.Media._ID + " DESC";

            String selection = MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?";
            String[] selectionArgs = new String[] { "image/jpeg", "image/png" };
            imagecursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                                                             selection, selectionArgs, orderBy);

            if(imagecursor != null && imagecursor.getCount() > 0){

                while(imagecursor.moveToNext()){
                    int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                    String dir = imagecursor.getString(dataColumnIndex);

                    dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    String path = imagecursor.getString(dataColumnIndex);
                    AblumInfo info = new AblumInfo();
                    info.setDirName(dir);
                    info.setDirCount(1);
                    info.setLastestPicPath("file://" + path);

                    if(!list.contains(info)){
                        list.add(info);
                    } else{
                        int indexOf = list.indexOf(info);
                        AblumInfo ablumInfo = list.get(indexOf);
                        ablumInfo.setDirCount(ablumInfo.getDirCount() + 1);
                    }
                }

            }
        } catch(Exception e){
            e.printStackTrace();
        }
        finally{
            closeCursor(imagecursor);
        }

        return list;
    }

    public static void closeCursor(Cursor imagecursor){
        // if (Build.VERSION.SDK_INT < 14) {
        if(imagecursor != null){
            imagecursor.close();
        }
        // }
    }

    public static ArrayList<CustomGallery> getGalleryPhotosByDir(String dir){
        ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();
        Context context = MyApplication.getInstance();
        Cursor imagecursor = null;
        try{
            final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
            final String orderBy = MediaStore.Images.Media._ID + " DESC LIMIT "
                    + CustomGalleryActivity.MAX_PICS_SHOW_IN_GALLERY;

            String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?";
            String[] selectionArgs = new String[] { dir };

            imagecursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                                                             selection, selectionArgs, orderBy);

            if(imagecursor != null && imagecursor.getCount() > 0){

                while(imagecursor.moveToNext()){
                    CustomGallery item = new CustomGallery();

                    int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);

                    String path = imagecursor.getString(dataColumnIndex);

                    if(DataUtils.isValidFile(path)){
                        item.setSdcardPath(path);
                        galleryList.add(item);
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        finally{
            closeCursor(imagecursor);
        }
        return galleryList;
    }

    public static ArrayList<CustomGallery> getRecentlyGalleryPhotos(){
        ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();
        Context context = MyApplication.getInstance();
        Cursor imagecursor = null;
        try{
            final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
            final String orderBy = MediaStore.Images.Media._ID + " DESC LIMIT "
                    + CustomGalleryActivity.MAX_PICS_SHOW_IN_GALLERY;

            imagecursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                                                             null, null, orderBy);

            if(imagecursor != null && imagecursor.getCount() > 0){

                while(imagecursor.moveToNext()){
                    CustomGallery item = new CustomGallery();

                    int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);

                    String path = imagecursor.getString(dataColumnIndex);

                    if(DataUtils.isValidFile(path)){
                        item.setSdcardPath(path);
                        galleryList.add(item);
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        finally{
            closeCursor(imagecursor);
        }

        // show newest photo at beginning of the list
        // Collections.reverse(galleryList);
        return galleryList;
    }

    public static boolean isValidFile(String path){
        File file = new File(path);
        if(file.exists()){
            if(file.length() > 0){
                return true;
            }
        }
        return false;
    }

    // 原始数据包含度和分，要转换为度
    public static LatLng getCoor(double lat, double lon){
        double baiduLat = getBaiduLat(lat);
        double baidulon = getBaiduLon(lon);

        Log.d("", "baiduLat =" + baiduLat + " baidulon=" + baidulon);

        LatLng latLng = new LatLng(baiduLat, baidulon);

        return latLng;
    }

    // 获取纬度，以度为单位
    private static double getBaiduLat(double original){
        // latitude：纬度，格式 DDFF.FFFF, DD：纬度的度（00 ~ 90）,FF.FFFF：纬度的
        // 分（00.0000 ~ 59.9999），保留四位小数
        String str = String.valueOf(original);

        double lat = Double.valueOf(str.substring(0, 2));

        // 将分转为度
        double fen = Double.valueOf(str.substring(2, str.length())) / 60;

        BigDecimal b = new BigDecimal(fen);

        // 最终保留6位小数
        lat = lat + b.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        return lat;
    }

    // 获取经度，以度为单位
    private static double getBaiduLon(double original){
        // longitude：经度，格式 DDDFF.FFFF，DDD：经度的度（000 ~ 180），FF.FFFF：
        // 经度的分（00.0000 ~ 59.9999），保留四位小数

        String str = String.valueOf(original);

        double lon = Double.valueOf(str.substring(0, 3));

        // 将分转为度
        double fen = Double.valueOf(str.substring(3, str.length())) / 60;

        BigDecimal b = new BigDecimal(fen);

        // 最终保留6位小数
        lon = lon + b.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        return lon;
    }

    // 将节转换为公里/小时
    public static double convertSpeed(double jie){
        BigDecimal b = new BigDecimal(jie * 1.852);
        return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static Bitmap createVideoThumbnail(String filePath){
        Bitmap bitmap = null;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try{
            mmr.setDataSource(filePath);
            bitmap = mmr.getFrameAtTime();
            mmr.release();
        } catch(IllegalArgumentException ex){
            // Assume this is a corrupt video file
        } catch(RuntimeException ex){
            // Assume this is a corrupt video file.
        }
        finally{
            try{
                mmr.release();
            } catch(RuntimeException ex){
                // Ignore failures while cleaning up.
            }
        }
        return bitmap;
    }

    public static void copyFile(File from, File to){
        if(null == from || !from.exists()){
            return;
        }
        if(null == to){
            return;
        }
        FileInputStream is = null;
        FileOutputStream os = null;
        try{
            is = new FileInputStream(from);
            if(!to.exists()){
                to.createNewFile();
            }
            os = new FileOutputStream(to);
            copyFileFast(is, os);
        } catch(Exception e){
            throw new RuntimeException(DataUtils.class.getClass().getName(), e);
        }
        finally{
            closeIO(is, os);
        }
    }

    private static void copyFileFast(FileInputStream is, FileOutputStream os) throws IOException{
        FileChannel in = null;
        FileChannel out = null;
        in = is.getChannel();
        out = os.getChannel();
        in.transferTo(0, in.size(), out);
    }

    public static void closeIO(Closeable... closeables){
        if(null == closeables || closeables.length <= 0){
            return;
        }
        for(Closeable cb : closeables){
            try{
                if(null == cb){
                    continue;
                }
                cb.close();
            } catch(IOException e){
                throw new RuntimeException(DataUtils.class.getClass().getName(), e);
            }
        }
    }

    public static String getPathByIntent(Intent data){
        return getPath(data.getData());
    }

    public static String getPath(final Uri uri){
        if(uri == null){
            return "";
        }

        final Context context = MyApplication.getInstance();
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if(isKitKat && DocumentsContract.isDocumentUri(context, uri)){
            // ExternalStorageProvider
            if(isExternalStorageDocument(uri)){
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if("primary".equalsIgnoreCase(type)){
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if(isDownloadsDocument(uri)){

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                                                                  Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if(isMediaDocument(uri)){
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if("image".equals(type)){
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if("video".equals(type)){
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if("audio".equals(type)){
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if("content".equalsIgnoreCase(uri.getScheme())){

            // Return the remote address
            if(isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if("file".equalsIgnoreCase(uri.getScheme())){
            return uri.getPath();
        }

        return "";
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     * 
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs){

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try{
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if(cursor != null && cursor.moveToFirst()){
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }
        finally{
            if(cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri){
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri){
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri){
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri){
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
