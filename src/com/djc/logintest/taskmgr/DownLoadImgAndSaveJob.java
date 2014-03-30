package com.djc.logintest.taskmgr;

import android.graphics.Bitmap;
import android.util.Log;

import com.djc.logintest.threadpool.MyJob;
import com.djc.logintest.utils.Utils;

public class DownLoadImgAndSaveJob extends MyJob {

	private String filedir;
	private String url;
	private int minWidth;
	private int minHeight;

	// url 下载地址，filedir 保存文件的全路径
	public DownLoadImgAndSaveJob(String url, String filedir, int minWidth,
			int minHeight) {
		this.url = url;
		this.filedir = filedir;
		this.minWidth = minWidth;
		this.minHeight = minHeight;
	}

	@Override
	public void run() {
		super.run();
		downloadImpl();
	}

	protected void downloadImpl() {
		Log.d("DDD", "DownLoadImgAndSaveJob downloadImgImpl url=" + url);
		Bitmap bmp = Utils.downloadImgWithJudgement(url, minWidth, minHeight);
		if (bmp != null) {
			try {
				Log.d("DDD", "DownLoadImgAndSaveJob saveBitmapToSDCard");
				Utils.saveBitmapToSDCard(bmp, filedir);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Log.d("DDD", "DownLoadImgAndSaveJob failed url=" + url);
		}
	}

}
