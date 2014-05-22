package com.cocobabys.taskmgr;

import android.graphics.Bitmap;
import android.util.Log;

import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.Utils;

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
		Log.d("EEE", "DownLoadImgAndSaveJob downloadImgImpl url=" + url);
		Bitmap bmp = Utils.downloadImgWithJudgement(url, minWidth, minHeight);
		if (bmp != null) {
			try {
				Log.d("EEE", "DownLoadImgAndSaveJob saveBitmapToSDCard");
				Utils.saveBitmapToSDCard(bmp, filedir);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Log.d("EEE", "DownLoadImgAndSaveJob failed url=" + url);
		}
	}

}
