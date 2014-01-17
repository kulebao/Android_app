package com.djc.logintest.taskmgr;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.utils.Utils;

public class DownLoadImgAndSaveTask extends AsyncTask<Void, Void, Integer> {

    private Handler hander;
    private String filedir;
    private String url;

    // url 下载地址，filedir 保存文件的全路径
    public DownLoadImgAndSaveTask(Handler handler, String url, String filedir) {
        this.hander = handler;
        this.url = url;
        this.filedir = filedir;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int result = EventType.DOWNLOAD_IMG_FAILED;
        Log.d("DDD", "DownLoadImgAndSaveTask downloadImgImpl url=" + url);
        // Bitmap bmp = Utils.downloadImgImpl(url);
        Bitmap bmp = Utils.downloadImgWithJudgement(url);
        if (bmp != null) {
            try {
                Log.d("DDD", "downloadImgImpl saveBitmapToSDCard");
                Utils.saveBitmapToSDCard(bmp, filedir);
                result = EventType.DOWNLOAD_IMG_SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.d("DDD", "downloadImgImpl failed url=" + url);
        }
        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Message msg = Message.obtain();
        msg.what = result;
        msg.obj = filedir;
        hander.sendMessage(msg);
    }

}
