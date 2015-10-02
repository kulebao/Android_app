package com.cocobabys.activities;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.adapter.GalleryAdapter;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.NoticeAction;
import com.cocobabys.customview.CustomGallery;
import com.cocobabys.dlgmgr.DlgMgr;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.SendExpJob;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.ImageUtils;
import com.cocobabys.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SendExpActivity extends UmengStatisticsActivity{
    private static final int VIDEO_RECORD = 0;
    private static final int VIDEO_FILE   = 1;
    private GridView         gridGallery;
    private Handler          myhandler;
    private GalleryAdapter   adapter;
    // private ViewSwitcher viewSwitcher;d
    private ImageLoader      imageLoader;
    private Uri              uri;
    private EditText         exp_content;
    private ProgressDialog   dialog;
    private Uri              mVideoUri;
    private ImageView        videonail;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_exp);
        initImageLoader();
        initUI();
        initHander();
    }

    private void initDialog(){
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.sending));
    }

    private void initImageLoader(){
        imageLoader = ImageUtils.getImageLoader();
    }

    private void initHeader(){
        TextView topbarTitleView = (TextView)findViewById(R.id.topbarTitleView);
        topbarTitleView.setText(R.string.exp_content);
        topbarTitleView.setVisibility(View.VISIBLE);
    }

    protected boolean checkContentValid(){
        if(TextUtils.isEmpty(exp_content.getText().toString().trim()) && adapter.getCount() == 0){
            return false;
        }
        return true;
    }

    private void initHander(){
        myhandler = new MyHandler(this, dialog){
            @Override
            public void handleMessage(Message msg){
                if(SendExpActivity.this.isFinishing()){
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch(msg.what){
                    case EventType.POST_EXP_SUCCESS:
                        handSendExpSuccess();
                        break;
                    case EventType.POST_EXP_FAIL:
                        Utils.makeToast(SendExpActivity.this, R.string.send_fail);
                        break;
                    case EventType.UPLOAD_ICON_SUCCESS:
                        dialog.setProgress(msg.arg1);
                        break;
                    default:
                        break;
                }
            }

        };
    }

    private void handSendExpSuccess(){
        Utils.makeToast(SendExpActivity.this, R.string.send_success);
        clearAllContent();
    }

    private void initUI(){
        initHeader();
        initDialog();

        videonail = (ImageView)findViewById(R.id.videonail);

        exp_content = (EditText)findViewById(R.id.exp_content);
        initGallery();
        initBtn();
    }

    private void initGallery(){
        gridGallery = (GridView)findViewById(R.id.gridGallery);
        gridGallery.setFastScrollEnabled(true);
        adapter = new GalleryAdapter(getApplicationContext(), imageLoader);
        adapter.setMultiplePick(false);
        gridGallery.setAdapter(adapter);

        gridGallery.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Log.d("DDDDDD", "startToSlideGalleryActivity position=" + position);
                startToSlideGalleryActivity(position);
            }
        });

        // viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        // viewSwitcher.setDisplayedChild(1);
    }

    private void runSendExpJob(){
        List<CustomGallery> list = adapter.getData();
        List<String> mediaPaths = new ArrayList<String>();
        if(!list.isEmpty()){
            for(CustomGallery gallery : list){
                mediaPaths.add(gallery.getSdcardPath());
            }
        }
        if(!mediaPaths.isEmpty()){
            dialog.setMax(mediaPaths.size());
            dialog.setProgress(1);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        } else{
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        dialog.show();

        SendExpJob expJob = new SendExpJob(myhandler, exp_content.getText().toString(), mediaPaths,
                JSONConstant.IMAGE_TYPE);
        expJob.execute();
    }

    protected void startToSlideGalleryActivity(int position){
        Intent intent = new Intent(NoticeAction.ACTION_GALLERY_CAN_DELETE);
        List<String> allSelectedPath = adapter.getAllSelectedPath();
        if(!allSelectedPath.isEmpty()){
            intent.putExtra(NoticeAction.SELECTED_PATH, allSelectedPath.toArray(new String[allSelectedPath.size()]));
        }

        intent.putExtra(NoticeAction.EXP_TEXT, exp_content.getText().toString());
        intent.putExtra(NoticeAction.GALLERY_POSITION, position);
        startActivityForResult(intent, NoticeAction.SELECT_SLIDE_GALLERY);
    }

    private void initBtn(){
        Button camera = (Button)findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!Utils.isSdcardExisting()){
                    Toast.makeText(SendExpActivity.this, "未找到存储卡，无法保存图片！", Toast.LENGTH_LONG).show();
                    return;
                }

                if(adapter.checkMaxIconSelected()){
                    return;
                }

                chooseIconFromCamera();
            }
        });

        Button gallery = (Button)findViewById(R.id.gallery);
        gallery.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Intent i = new Intent(NoticeAction.ACTION_MULTIPLE_PICK);
                List<String> allSelectedPath = adapter.getAllSelectedPath();
                if(!allSelectedPath.isEmpty()){
                    i.putExtra(NoticeAction.SELECTED_PATH, allSelectedPath.toArray(new String[allSelectedPath.size()]));
                }
                startActivityForResult(i, NoticeAction.SELECT_GALLERY);
            }
        });

        Button video = (Button)findViewById(R.id.video);
        video.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                DlgMgr.getListDialog(SendExpActivity.this, R.array.video_choose, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        Log.d("initTitle ddd", "which =" + which);
                        handleClick(which);
                    }
                }).create().show();
            }
        });
    }

    private void handleClick(int which){
        switch(which){
            case VIDEO_RECORD:
                RecordVideo();
                break;
            case VIDEO_FILE:
                chooseVideoFile();
                break;
            default:
                break;
        }
    }

    private void chooseVideoFile(){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setType("video/*");
        startActivityForResult(i, NoticeAction.SELECT_VIDEO_FILE);
    }

    private void RecordVideo(){
        // Intent intent = new Intent(this, RecordVideoActivity.class);
        Intent intent = new Intent(this, RecordVideoActivity.class);
        startActivityForResult(intent, NoticeAction.VIDEO_CAPTURE_SELF);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK){
            Log.e("", "AAA requestCode=" + requestCode);
            return;
        }

        switch(requestCode){
            case NoticeAction.SELECT_CAMERA:
                handleTakePicSuccess();
                break;
            case NoticeAction.SELECT_GALLERY:
                String[] all_path = data.getStringArrayExtra(NoticeAction.ALL_PATH);
                Log.d("DJC", "path aaa =" + all_path[0]);
                changeView(all_path);
                break;
            case NoticeAction.SELECT_SLIDE_GALLERY:
                String[] changed_array = data.getStringArrayExtra(NoticeAction.PATH_AFTER_CHANGE);
                changeView(changed_array);
                break;
            case NoticeAction.SELECT_VIDEO:
                mVideoUri = data.getData();
                gridGallery.setVisibility(View.GONE);
                videonail.setVisibility(View.VISIBLE);
                videonail.setImageBitmap(DataUtils.createVideoThumbnail(mVideoUri.getPath()));
                break;
            case NoticeAction.SELECT_VIDEO_FILE:
                handleVideoFile(data);
                break;
            case NoticeAction.VIDEO_CAPTURE_SYS:
                handleVideoFile(data);
                break;
            case NoticeAction.VIDEO_CAPTURE_SELF:
                handleRecordVideoBySelf(data);
                break;

            case NoticeAction.SEND_VIDEO:
                // 发送视频成功返回,清空文字内容，因为文字内容随视频一起发送
                exp_content.setText("");
                break;

            default:
                break;
        }

    }

    // 添加视频到系统文件路径，从系统视频中可以选择播放
    private void AddVideoToSys(String path){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(new File(path)));
        this.sendBroadcast(mediaScanIntent);
    }

    private void handleRecordVideoBySelf(Intent data){
        String videoUrl = data.getStringExtra(ConstantValue.RECORD_FILE_NAME);
        AddVideoToSys(videoUrl);

        File file = new File(videoUrl);

        long size = file.length();

        showSize(size);

        startToSendVideo(videoUrl, size);
    }

    // for test
    private void showSize(long size){
        DecimalFormat format = new DecimalFormat("0.00");
        double rSize = size / (1024.0f * 1024.0f);
        Utils.makeToast(this, format.format(rSize) + "m");
    }

    private void handleVideoFile(Intent data){
        String path = DataUtils.getPathByIntent(data);
        File file = new File(path);

        if(!isValidSize(file.length())){
            DlgMgr.showSingleBtnResDlg(R.string.video_invalid, SendExpActivity.this);
            return;
        }

        startToSendVideo(path, file.length());
    }

    private void startToSendVideo(String url, long size){
        Intent intent = new Intent(SendExpActivity.this, ShowVideoActivity.class);
        intent.putExtra(NoticeAction.VIDEO_URL, url);
        intent.putExtra(NoticeAction.VIDEO_SIZE, size);
        intent.putExtra(NoticeAction.EXP_TEXT, exp_content.getText().toString());

        startActivityForResult(intent, NoticeAction.SEND_VIDEO);
        // startActivity(intent);
    }

    private boolean isValidSize(long size){
        return (double)size < 5.0 * 1024.0 * 1024.0;
    }

    private void clearAllContent(){
        exp_content.setText("");
        adapter.clearCache();
        adapter.clear();
    }

    private void handleTakePicSuccess(){
        try {
			Utils.addPicToGallery(uri);
			CustomGallery gallery = new CustomGallery();
			Log.d("DJC", "path bbb=" + uri.getPath());
			gallery.setSdcardPath(uri.getPath());
			gallery.setSeleted(true);
			adapter.insert(gallery);
		} catch (Exception e) {
			e.printStackTrace();
			Utils.makeToast(this, R.string.take_pic_failed);
		}
    }

    private void chooseIconFromCamera(){
        File file = getFile();
        uri = Uri.fromFile(file);
        Log.d("DJC", "path =" + uri.getPath());
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(cameraIntent, NoticeAction.SELECT_CAMERA);
    }

    private File getFile(){
        String path = Utils.getDefaultCameraDir();
        Utils.makeDirs(path);
        File file = new File(path, new Date().getTime() + ".jpg");
        return file;
    }

    private void changeView(String[] all_path){
        ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

        for(String string : all_path){
            CustomGallery customGallery = new CustomGallery();
            customGallery.setSdcardPath(string);
            customGallery.setSeleted(true);
            dataT.add(customGallery);
        }

        // viewSwitcher.setDisplayedChild(0);
        adapter.addAll(dataT);
    }

    @Override
    protected void onDestroy(){
        adapter.clearCache();
        super.onDestroy();
    }

    public void sendExp(View view){
        if(!checkContentValid()){
            Utils.makeToast(SendExpActivity.this, R.string.invalid_exp_content);
            return;
        }
        runSendExpJob();
    }
}
