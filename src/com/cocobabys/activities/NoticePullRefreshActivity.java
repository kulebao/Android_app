package com.cocobabys.activities;

import java.util.List;

import com.cocobabys.R;
import com.cocobabys.adapter.NewsListAdapter;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.News;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.listener.MyPullToRefreshOnItemClickListener;
import com.cocobabys.taskmgr.GetNormalNewsTask;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.MethodUtils;
import com.cocobabys.utils.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class NoticePullRefreshActivity extends UmengStatisticsActivity{
    private static final int      START_NOTICE_DETAIL = 10;
    private NewsListAdapter       adapter;
    private PullToRefreshListView msgListView;
    private Handler               myhandler;
    private GetNormalNewsTask     getNoticeTask;
    private List<News>            newsList;
    private ProgressDialog        dialog;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_pull_refresh_list);
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.noticeTitle);
        initDialog();
        initHander();
        initCustomListView();
        loadNewData();
    }

    public void loadNewData(){
        dialog.show();
        refreshHead();
    }

    private void initDialog(){
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.loading_data));
    }

    private boolean runGetNoticeTask(long from, long to, int type){
        boolean bret = true;
        if(getNoticeTask == null || getNoticeTask.getStatus() != AsyncTask.Status.RUNNING){
            getNoticeTask = new GetNormalNewsTask(myhandler, ConstantValue.GET_NORMAL_NOTICE_MAX_COUNT, from, to, type);
            getNoticeTask.execute();
        } else{
            bret = false;
            Log.d("djc", "should not getNewsImpl task already running!");
        }
        return bret;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        adapter.close();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("", "DDD onResume count=" + adapter.getCount());

        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    private void initHander(){
        myhandler = new MyHandler(this, dialog){
            @Override
            public void handleMessage(Message msg){
                msgListView.onRefreshComplete();
                if(NoticePullRefreshActivity.this.isFinishing()){
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch(msg.what){
                    case EventType.GET_NOTICE_SUCCESS:
                        handleSuccess(msg);
                        break;
                    case EventType.GET_NOTICE_FAILED:
                        Utils.makeToast(NoticePullRefreshActivity.this, "获取公告消息失败");
                        break;
                    default:
                        break;
                }
            }
        };
    }

    protected void handleSuccess(Message msg){
        MethodUtils.removeNewsNotification();
        @SuppressWarnings("unchecked")
        List<News> list = (List<News>)msg.obj;
        Log.d("", "handleSuccess saveProp false");
        DataUtils.saveProp(ConstantValue.HAVE_NEWS_NOTICE, "false");
        if(!list.isEmpty()){
            // 刷出新公告了，去掉有新公告的标志
            if(msg.arg1 == ConstantValue.TYPE_GET_HEAD){
                addToHead(list);
            } else if(msg.arg1 == ConstantValue.TYPE_GET_TAIL){
                newsList.addAll(list);
            } else{
                Log.e("DDD", "handleSuccess bad param arg1=" + msg.arg1);
            }
            adapter.notifyDataSetChanged();
        } else{
            Toast.makeText(this, R.string.no_more_news, Toast.LENGTH_SHORT).show();
        }
    }

    private void addToHead(List<News> list){
        // 如果大于等于25条，就说明很可能还有公告没有一次性获取完，为了获取
        // 到连续的公告数据，避免排序和获取复杂化，在界面上删除旧的全部公告，只保留最新的25条
        if(list.size() >= ConstantValue.GET_NORMAL_NOTICE_MAX_COUNT){
            adapter.clear();
        }
        newsList.addAll(0, list);
    }

    private void initCustomListView(){
        newsList = DataMgr.getInstance().getNewsByType(JSONConstant.NOTICE_TYPE_NORMAL,
                                                       ConstantValue.GET_NORMAL_NOTICE_MAX_COUNT);
        adapter = new NewsListAdapter(this, newsList);
        msgListView = (PullToRefreshListView)findViewById(R.id.noticelist);
        msgListView.setAdapter(adapter);
        // msgListView.setScrollingWhileRefreshingEnabled(true);
        msgListView.setMode(Mode.BOTH);

        // 是否打开惯性特效，默认
        // msgListView.setPullToRefreshOverScrollEnabled(true);

        setRefreshListener();
        setItemClickListener();
    }

    @TargetApi(11)
    private void setListScrollSpeed(){
        // 设置listview 滑动的速度，friction越大滑动速度越慢
        if(Build.VERSION.SDK_INT >= 11){
            // Note that scroll speed decreases as friction increases. To
            // decrease scroll momentum by a suitable amount, I ended up using a
            // friction scale factor of 10.
            // msgListView.getRefreshableView().setFriction(ViewConfiguration.getScrollFriction()
            // * friction);
        }
    }

    private void setRefreshListener(){
        // Set a listener to be invoked when the list should be refreshed.
        msgListView.setOnRefreshListener(new OnRefreshListener2<ListView>(){
            /**
             * onPullDownToRefresh will be called only when the user has Pulled
             * from the start, and released.
             */
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView){
                // Do work to refresh the list here.
                refreshHead();
            }

            /**
             * onPullUpToRefresh will be called only when the user has Pulled
             * from the end, and released.
             */
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView){
                refreshTail();
            }
        });

    }

    private void refreshHead(){
        long from = 0;
        if(!newsList.isEmpty()){
            try{
                from = newsList.get(0).getTimestamp();
            } catch(NumberFormatException e){
                e.printStackTrace();
            }
        }

        Log.d("DDD", "refreshHead from=" + from);
        boolean runtask = runGetNoticeTask(from, 0, ConstantValue.TYPE_GET_HEAD);
        if(!runtask){
            // 任务没有执行，立即去掉下拉显示
            msgListView.onRefreshComplete();
        } else{
            // Toast.makeText(NoticePullRefreshActivity.this, "Head Head Head!",
            // Toast.LENGTH_SHORT)
            // .show();
        }
    }

    private void setItemClickListener(){
        msgListView.setOnItemClickListener(new MyPullToRefreshOnItemClickListener(){
            @Override
            public void handleClick(int realPosition){
                try{
                    News info = (News)adapter.getItem(realPosition);
                    startTo(info);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void refreshTail(){
        // 判断是否滚动到底部
        long to = 0;
        if(!newsList.isEmpty()){
            try{
                to = newsList.get(newsList.size() - 1).getTimestamp();
            } catch(NumberFormatException e){
                e.printStackTrace();
            }
        }
        Log.d("djc", "refreshTail to=" + to);
        runGetNoticeTask(0, to, ConstantValue.TYPE_GET_TAIL);
    }

    private void startTo(News info){
        Intent intent = new Intent(this, NoticeActivity.class);
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // intent.putExtra(JSONConstant.NOTIFICATION_TITLE, info.getTitle());
        // intent.putExtra(JSONConstant.NOTIFICATION_BODY, info.getContent());
        // intent.putExtra(JSONConstant.TIME_STAMP,
        // Utils.formatChineseTime(info.getTimestamp()));
        // intent.putExtra(JSONConstant.PUBLISHER, info.getFrom());
        // intent.putExtra(JSONConstant.NET_URL, info.getIcon_url());
        // intent.putExtra(JSONConstant.LOCAL_URL, info.getNewsLocalIconPath());

        intent.putExtra(JSONConstant.NOTIFICATION_ID, info.getNews_server_id());
        startActivityForResult(intent, START_NOTICE_DETAIL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d("", "onActivityResult resultCode" + resultCode);
        if(resultCode == RESULT_OK){
            int id = data.getIntExtra(JSONConstant.NOTIFICATION_ID, -1);
            Log.d("", "onActivityResult id" + id);
            adapter.updateFeedbackState(id);
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        if(MyApplication.getInstance().isForTest()){
            menu.add(1, // 组号
                     Menu.FIRST, // 唯一的ID号
                     Menu.FIRST, // 排序号
                     "清空"); // 标题
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == Menu.FIRST){
            Utils.showTwoBtnResDlg(R.string.delete_all_notice_confirm, this, new OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    DataMgr.getInstance().removeAllNewsByType(JSONConstant.NOTICE_TYPE_NORMAL);
                    DataMgr.getInstance().clearReceipts();
                    adapter.clear();
                }
            });
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        Activity parent = getParent();
        if(parent != null){
            if(parent.getIntent() == null){
                Log.d("", "onNewIntent parent is not null loadNewData");
                loadNewData();
            }
            parent.setIntent(null);
        } else{
            Log.d("", "onNewIntent parent is null loadNewData");
            loadNewData();
        }
    }

}
