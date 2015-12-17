package com.cocobabys.adapter;

import com.cocobabys.constant.EventType;
import com.cocobabys.taskmgr.DownloadImgeJob;

import android.os.Handler;
import android.os.Message;

public class DonwloadModule {
	private DownloadImgeJob downloadImgeJob = new DownloadImgeJob();
	private Handler handler;
	private DownloadListener downloadListener;

	public DonwloadModule() {
		handler = new InnerHandler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				switch (msg.what) {
				case EventType.DOWNLOAD_FILE_SUCCESS:
					if (downloadListener != null) {
						downloadListener.downloadSuccess();
					}
					break;
				default:
					break;
				}
			}

		};
		this.downloadImgeJob.setHanlder(handler);
	}

	public void setDownloadListener(DownloadListener downloadListener) {
		this.downloadListener = downloadListener;
	}

	public void addTask(String iconUrl, String savePath) {
		downloadImgeJob.addTask(iconUrl, savePath);
	}

	public void addTask(String iconUrl, String savePath, int limitWidth,
			int limitHeight) {
		downloadImgeJob.addTask(iconUrl, savePath, limitWidth, limitHeight);
	}

	public void close() {
		downloadImgeJob.stopTask();
	}

	private static class InnerHandler extends Handler {

	}

	public interface DownloadListener {
		public void downloadSuccess();
	}
}
