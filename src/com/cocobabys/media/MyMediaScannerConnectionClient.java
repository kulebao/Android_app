package com.cocobabys.media;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.util.Log;

public class MyMediaScannerConnectionClient {
	private MediaScannerConnection msc;
	private Uri uri;

	public MyMediaScannerConnectionClient(Context context) {
		msc = new MediaScannerConnection(context,
				new PicMediaScannerConnectionClient());
	}

	public void addPicToGallery(Uri uri) {
		this.uri = uri;
		msc.connect();
	}

	private class PicMediaScannerConnectionClient implements
			MediaScannerConnectionClient {

		@Override
		public void onMediaScannerConnected() {
			Log.d("DDD", "onMediaScannerConnected");
			msc.scanFile(uri.getPath(), "image/jpeg");
		}

		@Override
		public void onScanCompleted(String path, Uri uri) {
			Log.d("DDD", "onScanCompleted");
			msc.disconnect();
		}

	}

}
