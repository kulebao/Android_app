package com.cocobabys.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.MediaController;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.NoticeAction;
import com.cocobabys.customview.CustomDialog.Builder;
import com.cocobabys.customview.FullScreenVideoView;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.dlgmgr.DlgMgr;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.jobs.SendExpJob;
import com.cocobabys.threadpool.MyThreadPoolMgr;
import com.cocobabys.utils.Utils;

public class ShowVideoActivity extends UmengStatisticsActivity{
    private Bitmap              bitmap;
    private String              videoUrl;
    private FullScreenVideoView videoView;
    private ImageView           play;
    private ImageView           nailview;

    private static final int    NULL_EXP_ID         = -1;
    private int                 mPositionWhenPaused = -1;

    private MediaController     mMediaController;
    private String              content;
    private long                size;
    private long                expid;
    private ExpInfo             info;
    private Handler             handler;
    private ProgressDialog      dialog;
    private String              serverUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_video);

        // Create media controller
        mMediaController = new MediaController(this);

        getExpID();
        initDialog();
        initHandler();

        if(isSendPriview()){
            // 发送视频前预览
            goSendVideo();
        } else{
            // 查看本地视频记录
            goSeeVideo();
        }
        Log.d("", "ShowVideoActivity onCreate");
    }

    // 是否是发送前预览
    private boolean isSendPriview(){
        return expid == NULL_EXP_ID;
    }

    private void initDialog(){
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.dling));
    }

    private void initHandler(){
        handler = new MyHandler(this, dialog){
            @Override
            public void handleMessage(Message msg){
                if(ShowVideoActivity.this.isFinishing()){
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch(msg.what){
                    case EventType.DOWNLOAD_FILE_SUCCESS:
                        initUI();
                        playVideo();
                        break;
                    case EventType.DOWNLOAD_FILE_FAILED:
                        Builder singleBtnDlg = DlgMgr
                                .getSingleBtnDlg(ShowVideoActivity.this,
                                                 new android.content.DialogInterface.OnClickListener(){

                                                     @Override
                                                     public void onClick(DialogInterface dialog, int which){
                                                         dialog.dismiss();
                                                         ShowVideoActivity.this.finish();
                                                     }
                                                 });
                        singleBtnDlg.setMessage(R.string.dl_failed);

                        singleBtnDlg.create().show();
                        break;
                    case EventType.POST_EXP_SUCCESS:
                        handSendExpSuccess();
                        break;
                    case EventType.POST_EXP_FAIL:
                        Utils.makeToast(ShowVideoActivity.this, R.string.send_fail);
                        break;
                    default:
                        break;
                }
            }

        };
    }

    protected void handSendExpSuccess(){
        Utils.makeToast(this, R.string.send_success);
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void goSeeVideo(){
        info = DataMgr.getInstance().getExpInfoByID(expid);

        List<String> serverUrls = info.getServerUrls();

        serverUrl = serverUrls.get(0);

        videoUrl = info.serverUrlToLocalUrl(serverUrl, false);

        Log.d("", "saveAndCompressNail videoUrl=" + videoUrl);
        if(!new File(videoUrl).exists()){
            showDlg(serverUrl);
        } else{
            saveAndCompressNail();
            handler.sendEmptyMessage(EventType.DOWNLOAD_FILE_SUCCESS);
        }
    }

    private void showDlg(final String serverUrl){
        final Builder builder = DlgMgr.getTwoBtnDlg(this, new android.content.DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int which){
                dialog.show();
                downloadVideo(serverUrl);
            }
        }, new android.content.DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
                ShowVideoActivity.this.finish();
            }
        });
        builder.setMessage(Utils.getResString(R.string.dl_video));
        builder.createTwoBtn().show();
    }

    private void downloadVideo(final String serverUrl){
        MyThreadPoolMgr.getGenericService().execute(new Runnable(){
            @Override
            public void run(){
                try{
                    HttpClientHelper.downloadFile(serverUrl, videoUrl);

                    saveAndCompressNail();
                    handler.sendEmptyMessage(EventType.DOWNLOAD_FILE_SUCCESS);
                } catch(Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(EventType.DOWNLOAD_FILE_FAILED);
                }
            }
        });
    }

    protected void saveAndCompressNail(){
        String nail = info.serverUrlToLocalUrl(serverUrl, true);
        Log.d("", "saveAndCompressNail serverUrl=" + serverUrl);
        Log.d("", "saveAndCompressNail nail=" + videoUrl);
        try{
            if(!new File(nail).exists()){
                Bitmap nailbitmap = Utils.createVideoThumbnail(videoUrl);
                Utils.saveBitmapToSDCard(nailbitmap, nail);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void goSendVideo(){
        initData();
        initUI();

        findViewById(R.id.cancel).setVisibility(View.VISIBLE);
        findViewById(R.id.send).setVisibility(View.VISIBLE);
    }

    private void getExpID(){
        expid = getIntent().getLongExtra(ConstantValue.EXP_ID, NULL_EXP_ID);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        Log.d("", "ShowVideoActivity onConfigurationChanged");
    }

    private void initUI(){
        if(!isSendPriview()){
            initSaveBtn();
        }

        nailview = (ImageView)findViewById(R.id.nailview);
        bitmap = Utils.createVideoThumbnail(videoUrl);
        nailview.setImageBitmap(bitmap);

        initVideoView();

        play = (ImageView)findViewById(R.id.play);
        play.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                playVideo();
            }

        });

    }

    private void initSaveBtn(){
        ImageView saveBtn = (ImageView)findViewById(R.id.save);
        saveBtn.setVisibility(View.VISIBLE);
        saveBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                Utils.addVideoToGallery(Uri.fromFile(new File(videoUrl)));
                Utils.makeToast(ShowVideoActivity.this, R.string.save_to_phone);
            }
        });
    }

    private void initVideoView(){
        videoView = (FullScreenVideoView)findViewById(R.id.videoView);
        videoView.setMediaController(mMediaController);
        videoView.setVideoPath(videoUrl);
        videoView.setOnPreparedListener(new OnPreparedListener(){
            @Override
            public void onPrepared(MediaPlayer mp){
                Log.d("", "onPrepared  ");
                play.setVisibility(View.GONE);
                videoView.setVideoWidth(mp.getVideoWidth());
                videoView.setVideoHeight(mp.getVideoHeight());
            }
        });

        videoView.setOnCompletionListener(new OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp){
                pauseVideo();
            }
        });
    }

    private void pauseVideo(){
        videoView.pause();

        play.setVisibility(View.VISIBLE);
    }

    private void playVideo(){
        nailview.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);

        videoView.start();
        play.setVisibility(View.GONE);
    }

    private void initData(){
        videoUrl = getIntent().getStringExtra(NoticeAction.VIDEO_URL);
        size = getIntent().getLongExtra(NoticeAction.VIDEO_SIZE, 0);
        content = getIntent().getStringExtra(NoticeAction.EXP_TEXT);
    }

    public void onPause(){
        if(videoView != null){
            // Stop video when the activity is pause.
            mPositionWhenPaused = videoView.getCurrentPosition();
            videoView.stopPlayback();
            Log.d("", "OnStop: mPositionWhenPaused = " + mPositionWhenPaused);
            Log.d("", "OnStop: getDuration  = " + videoView.getDuration());
        }

        super.onPause();
    }

    public void onResume(){
        // Resume video player
        if(mPositionWhenPaused >= 0 && videoView != null){
            videoView.seekTo(mPositionWhenPaused);
            mPositionWhenPaused = -1;
        }

        super.onResume();
    }

    @Override
    protected void onDestroy(){
        if(bitmap != null){
            bitmap.recycle();
        }
        super.onDestroy();
    }

    public void cancel(View view){
        this.finish();
    }

    public void send(View view){
        dialog.setMessage(getResources().getString(R.string.sending));
        dialog.show();

        videoView.stopPlayback();
        List<String> list = new ArrayList<String>(1);
        list.add(videoUrl);

        SendExpJob expJob = new SendExpJob(handler, content, list, JSONConstant.VIDEO_TYPE);
        expJob.execute();
    }

}
