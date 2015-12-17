package com.cocobabys.media;

import com.cocobabys.utils.DataUtils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.util.Log;

public class MyMediaScannerConnectionClient{
    private MediaScannerConnection picScannerConnection;
    private MediaScannerConnection videoScannerConnection;
    private Uri                    uri;

    public MyMediaScannerConnectionClient(Context context){
        picScannerConnection = new MediaScannerConnection(context, new PicMediaScannerConnectionClient());

        videoScannerConnection = new MediaScannerConnection(context, new VideoMediaScannerConnectionClient());
    }

    public void addPicToGallery(Uri uri){
        this.uri = uri;
        picScannerConnection.connect();
    }

    public void addVideoToGallery(Uri uri){
        this.uri = uri;
        videoScannerConnection.connect();
    }

    private class PicMediaScannerConnectionClient implements MediaScannerConnectionClient{

        @Override
        public void onMediaScannerConnected(){
            Log.d("DDD", "onMediaScannerConnected");
            try{
                picScannerConnection.scanFile(DataUtils.getPath(uri), "image/jpeg");
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onScanCompleted(String path, Uri uri){
            Log.d("DDD", "onScanCompleted");
            picScannerConnection.disconnect();
        }

    }

    private class VideoMediaScannerConnectionClient implements MediaScannerConnectionClient{

        @Override
        public void onMediaScannerConnected(){
            Log.d("DDD", "onMediaScannerConnected");
            try{
                videoScannerConnection.scanFile(DataUtils.getPath(uri), "video/mpeg");
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onScanCompleted(String path, Uri uri){
            Log.d("DDD", "onScanCompleted");
            videoScannerConnection.disconnect();
        }

    }

}
