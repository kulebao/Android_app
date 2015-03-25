package com.cocobabys.activities;

import java.io.File;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.taskmgr.DownLoadImgAndSaveTask;
import com.cocobabys.utils.ImageUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class NoticeActivity extends UmengStatisticsActivity{
    private TextView                       contentView;
    private ImageView                      noticeiconView;
    private TextView                       signView;
    private TextView                       timeView;
    private AsyncTask<Void, Void, Integer> downloadIconTask;
    // 图片在服务器上路径
    private String                         net_url;
    // 图片本地保存路径
    private String                         local_url;
    private Handler                        handler;
    private TextView                       titleView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice);
        // ActivityHelper.setBackKeyLitsenerOnTopbar(this,
        // R.string.notice_title);
        Log.d("DDD JJJ", "NoticeActivity onCreate");
        initView();
        initHandler();
        setData(getIntent());
    }

    private void setData(Intent intent){
        String title = intent.getStringExtra(JSONConstant.NOTIFICATION_TITLE);
        String content = intent.getStringExtra(JSONConstant.NOTIFICATION_BODY);
        String time = intent.getStringExtra(JSONConstant.TIME_STAMP);
        String publisher = intent.getStringExtra(JSONConstant.PUBLISHER);

        net_url = intent.getStringExtra(JSONConstant.NET_URL);
        local_url = intent.getStringExtra(JSONConstant.LOCAL_URL);

        setIcon();

        signView.setText(publisher);
        timeView.setText(time);

        titleView.setText(title);
        contentView.setText(content);
    }

    private void initHandler(){
        handler = new MyHandler(this, null){
            @Override
            public void handleMessage(Message msg){
                if(NoticeActivity.this.isFinishing()){
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch(msg.what){
                    case EventType.DOWNLOAD_FILE_SUCCESS:
                        setIcon();
                        break;
                    default:
                        break;
                }
            }

        };
    }

    private void setIcon(){
        if(!TextUtils.isEmpty(local_url)){
            if(new File(local_url).exists()){
            	ImageLoader imageLoader = ImageUtils.getImageLoader();
                noticeiconView.setVisibility(View.VISIBLE);
                String path = "file://" + local_url;
                imageLoader.displayImage(path, noticeiconView);
                noticeiconView.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        startToShowIcon();
                    }

                });
            } else{
                // 如果本地图片有路径却没有文件，那么从服务器重新下载并保存到本地
                if(!TextUtils.isEmpty(net_url)){
                    runDownloadIconTask();
                    noticeiconView.setVisibility(View.VISIBLE);
                    noticeiconView.setImageResource(R.drawable.default_icon);
                }
            }

        }
    }

    private void startToShowIcon(){
        Intent intent = new Intent(this, ShowIconActivity.class);
        intent.putExtra(ConstantValue.LOCAL_URL, local_url);
        startActivity(intent);
    }

    private void runDownloadIconTask(){
        if(downloadIconTask != null && downloadIconTask.getStatus() == AsyncTask.Status.RUNNING){
            // 有任务执行，则返回
            Log.d("DDD", "NoticeActivity DownloadIconTask already running!");
            return;
        }

        downloadIconTask = new DownLoadImgAndSaveTask(handler, net_url, local_url).execute();
    }

    private void initView(){
        signView = (TextView)findViewById(R.id.sign);
        timeView = (TextView)findViewById(R.id.time);
        contentView = (TextView)findViewById(R.id.noticecontent);
        noticeiconView = (ImageView)findViewById(R.id.noticeicon);
        titleView = (TextView)findViewById(R.id.title);
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        Log.d("DDD JJJ", "NoticeActivity onNewIntent");
        setIntent(intent);
        setData(intent);
    }

}
