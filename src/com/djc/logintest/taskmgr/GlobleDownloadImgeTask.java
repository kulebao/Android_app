package com.djc.logintest.taskmgr;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.utils.Utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;

public class GlobleDownloadImgeTask {
	private int maxThreads = 2;
	private ExecutorService service = Executors.newFixedThreadPool(maxThreads);
	private Map<String, String> map = new HashMap<String, String>();
	private float limitWidth = 0;
	private float limitHeight = 0;
	private Handler hanlder;
	private boolean stop = false;

	public void setHanlder(Handler hanlder) {
		this.hanlder = hanlder;
	}

	public GlobleDownloadImgeTask() {
		DisplayMetrics dm = new DisplayMetrics();
		dm = MyApplication.getInstance().getResources().getDisplayMetrics();

		Log.d("DDD", "w = " + dm.widthPixels + " h=" + dm.heightPixels + " density=" + dm.density);
		limitWidth = dm.widthPixels * 0.7f;// * dm.density;
		limitHeight = dm.heightPixels * 0.7f;// * dm.density;
	}

	public synchronized void addTask(String imgaeUrl, String savePath) {
		if (stop || map.containsKey(imgaeUrl)) {
			Log.d("DDD", "runTask do nothing, stop =" + stop);
			return;
		}

		map.put(imgaeUrl, savePath);
		try {
			service.execute(new DownloadRunnable(imgaeUrl, savePath));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class DownloadRunnable implements Runnable {
		private String imgaeUrl = "";
		private String savePath = "";

		public DownloadRunnable(String imgaeUrl, String savePath) {
			this.imgaeUrl = imgaeUrl;
			this.savePath = savePath;
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
					result = EventType.SUCCESS;
				} else {
					result = EventType.FAIL;
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
