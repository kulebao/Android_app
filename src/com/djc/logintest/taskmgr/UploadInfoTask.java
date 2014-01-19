package com.djc.logintest.taskmgr;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.net.UploadChildInfoMethod;
import com.djc.logintest.upload.OSSMgr;

public class UploadInfoTask extends AsyncTask<Void, Void, Integer> {
    private Handler hander;
    private Bitmap bitmap = null;
    private String content;

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public UploadInfoTask(Handler handler,String content) {
        this.hander = handler;
        this.content = content;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int bret = EventType.UPLOAD_FAILED;
        if (bitmap != null) {
            try {
                OSSMgr.UploadPhoto(bitmap);
            } catch (Exception e) {
                // 如果上传文件失败，直接返回错误
                e.printStackTrace();
                return bret;
            }
        }
        bret = UploadChildInfoMethod.getMethod().uploadChildInfo(content);
        return bret;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Message msg = Message.obtain();
        msg.what = result;
        msg.obj = bitmap;
        hander.sendMessage(msg);
    }

}
