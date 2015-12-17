package com.cocobabys.activities;

import java.io.File;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.News;
import com.cocobabys.dbmgr.info.ReceiptInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.SendReceiptJob;
import com.cocobabys.taskmgr.DownLoadImgAndSaveTask;
import com.cocobabys.utils.ImageUtils;
import com.cocobabys.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.ProgressDialog;
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
    private TextView                       rightBtn;
    private ProgressDialog                 dialog;
    private News                           news;
    private boolean                        bUpdateState = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice);
        Log.d("DDD JJJ", "NoticeActivity onCreate");
        initView();
        initDlg();
        initHandler();
        setData(getIntent());

        // 要在setData之后调用
        initTopbar();
    }

    private void setData(Intent intent){
        int noticeId = intent.getIntExtra(JSONConstant.NOTIFICATION_ID, -1);

        news = DataMgr.getInstance().getNewsByID(noticeId);

        if(news != null){
            String title = news.getTitle();
            String content = news.getContent();
            String time = Utils.formatChineseTime(news.getTimestamp());
            String publisher = news.getFrom();

            net_url = news.getIcon_url();
            local_url = news.getNewsLocalIconPath();

            setIcon();

            signView.setText(publisher);
            timeView.setText(time);

            titleView.setText(title);
            contentView.setText(content);

            ActivityHelper.setTitle(this, news.getNoticeTypeStr());
        }
    }

    private void initHandler(){
        handler = new MyHandler(this, dialog){
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
                    case EventType.POST_RECEIPT_FAIL:
                        Utils.makeToast(NoticeActivity.this, R.string.send_news_feedback_fail);
                        break;
                    case EventType.POST_RECEIPT_SUCCESS:
                        bUpdateState = true;
                        rightBtn.setText(R.string.news_already_feedback);
                        rightBtn.setClickable(false);
                        break;
                    default:
                        break;
                }
            }

        };
    }

    private void initDlg(){
        dialog = new ProgressDialog(this);
        dialog.setTitle(R.string.sending_feedback);
        dialog.setCancelable(false);
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
    public void onBackPressed(){
        if(news != null && bUpdateState){
            Log.d("", "onBackPressed id =" + news.getNews_server_id());
            Intent data = new Intent();
            data.putExtra(JSONConstant.NOTIFICATION_ID, news.getNews_server_id());
            setResult(RESULT_OK, data);
        }
        finish();
    }

    private void initTopbar(){

        if(news != null){
            int need_receipt = news.getNeed_receipt();

            // 需要回执
            if(need_receipt != 0){
                rightBtn = (TextView)findViewById(R.id.rightBtn);
                rightBtn.setVisibility(View.VISIBLE);

                ReceiptInfo receiptInfo = DataMgr.getInstance().getReceiptInfo(news.getNews_server_id());

                if(receiptInfo != null && receiptInfo.getReceipt_state() != 0){
                    rightBtn.setText(R.string.news_already_feedback);
                } else{
                    rightBtn.setText(R.string.send_feedback);
                    rightBtn.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v){
                            Log.d("", "expJob runnning!");
                            dialog.show();
                            SendReceiptJob expJob = new SendReceiptJob(handler, news.getNews_server_id());
                            expJob.execute();
                        }
                    });
                }
            }
        }
    }

    // @Override
    // protected void onNewIntent(Intent intent){
    // super.onNewIntent(intent);
    // Log.d("DDD JJJ", "NoticeActivity onNewIntent");
    // setIntent(intent);
    // setData(intent);
    // }

}
