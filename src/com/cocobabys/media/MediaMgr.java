package com.cocobabys.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.util.Log;

public class MediaMgr {
    private static MediaPlayer mMediaPlayer;
    private static final String MEDIA_LOG = "MEDIA_LOG";

    public synchronized static boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }

        return false;
    }

    public static synchronized void playMediaNow(Context context, Uri uri,
            final MediaPlayCompleteListener listener) {
        try {
            // if (uri.equals(current_uri)) {
            // Log.d(MEDIA_LOG, "same file already playing ! return!");
            // current_uri = null;
            // return;
            // }
            // 播放之前，先关闭正在播放的音频
            close();

            mMediaPlayer = MediaPlayer.create(context, uri);
            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    listener.onComplete();
                }
            });
            Log.d(MEDIA_LOG, "MediaMgr playing! resID=" + uri);
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getDuration(Context context, Uri uri) {
        return Math.round(MediaPlayer.create(context, uri).getDuration() / 1000f);
    }

    public synchronized static void close() {
        try {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                Log.d(MEDIA_LOG, "MediaMgr release!");
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface MediaPlayCompleteListener {
        public void onComplete();
    }

}
