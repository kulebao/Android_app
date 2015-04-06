package com.cocobabys.activities;

import java.util.List;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.adapter.HomeworkListAdapter;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.Homework;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.taskmgr.GetHomeworkTask;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class HomeworkPullRefreshActivity extends UmengStatisticsActivity{
    private HomeworkListAdapter   adapter;
    private PullToRefreshListView msgListView;
    private Handler               myhandler;
    private GetHomeworkTask       getHomeworkTask;
    private List<Homework>        homeworkList;
    private boolean               bDataChanged = false;
    private ProgressDialog        dialog;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homework_pull_refresh_list);
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.homework_content);
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
        if(getHomeworkTask == null || getHomeworkTask.getStatus() != AsyncTask.Status.RUNNING){
            getHomeworkTask = new GetHomeworkTask(myhandler, ConstantValue.GET_HOMEWORK_MAX_COUNT, from, to, type);
            getHomeworkTask.execute();
        } else{
            bret = false;
            Log.d("djc", "should not getNewsImpl task already running!");
        }
        return bret;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        // 最多保存最新的25条通知
        if(bDataChanged){
            if(homeworkList.size() > ConstantValue.GET_HOMEWORK_MAX_COUNT){
                homeworkList = homeworkList.subList(0, ConstantValue.GET_HOMEWORK_MAX_COUNT);
            }

            DataMgr.getInstance().removeAllHomework();
            DataMgr.getInstance().addHomeworkList(homeworkList);
        }
        adapter.close();
    }

    private void initHander(){
        myhandler = new MyHandler(this, dialog){
            @Override
            public void handleMessage(Message msg){
                msgListView.onRefreshComplete();
                if(HomeworkPullRefreshActivity.this.isFinishing()){
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch(msg.what){
                    case EventType.SUCCESS:
                        handleSuccess(msg);
                        break;
                    case EventType.FAIL:
                        Toast.makeText(HomeworkPullRefreshActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    protected void handleSuccess(Message msg){
        @SuppressWarnings("unchecked")
        List<Homework> list = (List<Homework>)msg.obj;
        DataUtils.saveProp(ConstantValue.HAVE_HOMEWORK_NOTICE, "false");
        if(!list.isEmpty()){
            bDataChanged = true;
            if(msg.arg1 == ConstantValue.Type_GET_HEAD){
                addToHead(list);
            } else if(msg.arg1 == ConstantValue.Type_GET_TAIL){
                // 旧数据不保存数据库
                homeworkList.addAll(list);
            } else{
                Log.e("DDD", "handleSuccess bad param arg1=" + msg.arg1);
            }
            adapter.notifyDataSetChanged();
        } else{
            Toast.makeText(this, R.string.no_more_work, Toast.LENGTH_SHORT).show();
        }
    }

    private void addToHead(List<Homework> list){
        if(list.size() >= ConstantValue.GET_HOMEWORK_MAX_COUNT){
            homeworkList.clear();
            DataMgr.getInstance().removeAllHomework();
        }

        homeworkList.addAll(0, list);
        DataMgr.getInstance().addHomeworkList(list);
    }

    private void initCustomListView(){
        homeworkList = DataMgr.getInstance().getHomeworkWithLimite(ConstantValue.GET_HOMEWORK_MAX_COUNT);
        adapter = new HomeworkListAdapter(this, homeworkList);
        msgListView = (PullToRefreshListView)findViewById(R.id.homeworklist);// 继承ListActivity，id要写成android.R.id.list，否则报异常
        setRefreshListener();
        msgListView.setMode(Mode.BOTH);
        msgListView.setAdapter(adapter);
        setItemClickListener();
    }

    private void setRefreshListener(){
        // Set a listener to be invoked when the list should be refreshed.
        msgListView.setOnRefreshListener(new OnRefreshListener2<ListView>(){
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView){
                refreshHead();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView){
                refreshTail();
            }
        });

    }

    private void refreshHead(){
        long from = 0;
        if(!homeworkList.isEmpty()){
            try{
                from = homeworkList.get(0).getServer_id();
            } catch(NumberFormatException e){
                e.printStackTrace();
            }
        }

        boolean runtask = runGetNoticeTask(from, 0, ConstantValue.Type_GET_HEAD);
        if(!runtask){
            // 任务没有执行，立即去掉下拉显示
            msgListView.onRefreshComplete();
        }
    }

    private void setItemClickListener(){
        msgListView.setOnItemClickListener(new OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                // 自定义listview headview占了一个，所以真实数据从1开始
                int currentIndex = position - 1;
                if(currentIndex >= adapter.getCount()){
                    // 当底部条出现时，index会大于count造成数组越界异常，这里处理一下
                    return;
                }
                Homework info = (Homework)adapter.getItem(currentIndex);
                startTo(info);
            }
        });
    }

    private void refreshTail(){
        Log.d("djc", "on the end!!!!!!!!!!!!!!!!");
        long to = 0;
        if(!homeworkList.isEmpty()){
            try{
                to = homeworkList.get(homeworkList.size() - 1).getServer_id();
            } catch(NumberFormatException e){
                e.printStackTrace();
            }
        }
        boolean runtask = runGetNoticeTask(0, to, ConstantValue.Type_GET_TAIL);
    }

    private void startTo(Homework info){
        Intent intent = new Intent(this, NoticeActivity.class);
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(JSONConstant.NOTIFICATION_TITLE, info.getTitle());
        intent.putExtra(JSONConstant.NOTIFICATION_BODY, info.getContent());
        intent.putExtra(JSONConstant.TIME_STAMP, Utils.formatChineseTime(info.getTimestamp()));
        intent.putExtra(JSONConstant.PUBLISHER, info.getPublisher());
        intent.putExtra(JSONConstant.NET_URL, info.getIcon_url());
        intent.putExtra(JSONConstant.LOCAL_URL, info.getHomeWorkLocalIconPath());
        startActivity(intent);
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
                    DataMgr.getInstance().removeAllHomework();
                    adapter.clear();
                }
            });
        }
        return true;
    }

}
