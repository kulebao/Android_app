package com.cocobabys.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.util.Log;

public class MediaMgr {
	private static MediaPlayer mMediaPlayer;
	private static final String MEDIA_LOG = "Media_logs";
	private static Uri current_uri = null;

	public static void playMedia(Context context, int resID) {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				Log.w(MEDIA_LOG, "MediaMgr playing! do nothing!");
				return;
			}
			mMediaPlayer.release();
		}
		mMediaPlayer = MediaPlayer.create(context, resID);
		mMediaPlayer.start();
	}

	public static void playMediaNow(Context context, int resID) {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				Log.w(MEDIA_LOG, "MediaMgr playing! stop it!");
				mMediaPlayer.stop();
			}
			mMediaPlayer.release();
		}
		mMediaPlayer = MediaPlayer.create(context, resID);
		Log.w(MEDIA_LOG, "MediaMgr playing! resID=" + resID);
		mMediaPlayer.start();
	}

	public static synchronized void playMediaNow(Context context, Uri uri) {
		try {
			close();
			if (uri.equals(current_uri)) {
				Log.d(MEDIA_LOG, "same file already playing ! return!");
				current_uri = null;
				return;
			}
			
			mMediaPlayer = MediaPlayer.create(context, uri);
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					current_uri = null;
				}
			});
			Log.d(MEDIA_LOG, "MediaMgr playing! resID=" + uri);
			current_uri = uri;
			mMediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getDuration(Context context, Uri uri) {
		return Math
				.round(MediaPlayer.create(context, uri).getDuration() / 1000f);
	}

	public static void close() {
		try {
			if (mMediaPlayer != null) {
				if (mMediaPlayer.isPlaying()) {
					Log.d(MEDIA_LOG, "MediaMgr playing! stop it!");
					mMediaPlayer.stop();
				}
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
