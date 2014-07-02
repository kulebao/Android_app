package com.cocobabys.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

public class MediaMgr {
	private static MediaPlayer mMediaPlayer;
	private static final String MEDIA_LOG = "Media_logs";

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

	public static void playMediaNow(Context context, Uri uri) {
		try {
			if (mMediaPlayer != null) {
				if (mMediaPlayer.isPlaying()) {
					Log.d(MEDIA_LOG, "MediaMgr playing! stop it!");
					mMediaPlayer.stop();
				}
				mMediaPlayer.release();
			}
			mMediaPlayer = MediaPlayer.create(context, uri);
			Log.d(MEDIA_LOG, "MediaMgr playing! resID=" + uri);
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
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
}
