package com.cocobabys.taskmgr;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.EventType;
import com.cocobabys.utils.Utils;

public class DownloadImgeJob {
	private int maxThreads = 2;
	private ExecutorService service = Executors.newFixedThreadPool(maxThreads);
	private Map<String, String> map = new HashMap<String, String>();
	private Handler hanlder;
	private boolean stop = false;
	private float defaultLimitWidth;
	private float defaultLimitHeight;

	public void setHanlder(Handler hanlder) {
		this.hanlder = hanlder;
	}

	public DownloadImgeJob() {
		DisplayMetrics dm = new DisplayMetrics();
		dm = MyApplication.getInstance().getResources().getDisplayMetrics();
		Log.d("DDD", "w = " + dm.widthPixels + " h=" + dm.heightPixels + " density=" + dm.density);
		defaultLimitWidth = dm.widthPixels * 0.7f;// * dm.density;
		defaultLimitHeight = dm.heightPixels * 0.7f;// * dm.density;
	}

	public synchronized void addTask(String imgaeUrl, String savePath) {
		if (stop || map.containsKey(imgaeUrl)) {
			Log.d("DDD", "runTask do nothing, stop =" + stop);
			return;
		}

		map.put(imgaeUrl, savePath);
		try {
			service.execute(new DownloadRunnable(imgaeUrl, savePath, defaultLimitWidth, defaultLimitHeight));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void addTask(String imgaeUrl, String savePath, float limitWidth, float limitHeight) {
		if (stop || map.containsKey(imgaeUrl)) {
			Log.d("DDD", "runTask do nothing, stop =" + stop);
			return;
		}

		map.put(imgaeUrl, savePath);
		try {
			service.execute(new DownloadRunnable(imgaeUrl, savePath, limitWidth, limitHeight));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class DownloadRunnable implements Runnable {
		private String imgaeUrl = "";
		private String savePath = "";
		private float limitWidth = 0;
		private float limitHeight = 0;

		public DownloadRunnable(String imgaeUrl, String savePath, float limitWidth, float limitHeight) {
			this.imgaeUrl = imgaeUrl;
			this.savePath = savePath;
			this.limitWidth = limitWidth;
			this.limitHeight = limitHeight;
		}

		@Override
		public void run() {
			int result = EventType.DOWNLOAD_IMG_FAILED;
			Bitmap bmp = null;
			try {
				Log.d("DDD", "downloadImgImpl url=" + imgaeUrl + " limitWidth=" + limitWidth + " limitHeight="
						+ limitHeight);
				bmp = Utils.downloadImgWithJudgement(imgaeUrl, limitWidth, limitHeight);
				if (bmp != null) {
					Log.d("DDD", "downloadImgImpl saveBitmapToSDCard");
					Utils.saveBitmapToSDCard(bmp, savePath);
					result = EventType.DOWNLOAD_IMG_SUCCESS;
				} else {
					result = EventType.DOWNLOAD_IMG_FAILED;
					Log.d("DDD", "downloadImgImpl failed url=" + imgaeUrl);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				sendMsg(result);
				if (bmp != null) {
					bmp.recycle();
				}
				map.remove(imgaeUrl);
			}
		}

		private void sendMsg(int result) {
			if (stop) {
				return;
			}
			Message msg = Message.obtain();
			msg.what = result;
			hanlder.sendMessage(msg);
		}
	}

	public void stopTask() {
		stop = true;
		hanlder.removeCallbacksAndMessages(null);
		service.shutdown();
	}
}
