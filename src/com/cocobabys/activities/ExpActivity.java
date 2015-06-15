package com.cocobabys.activities;

import java.util.Calendar;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.adapter.ExpGridViewAdapter;
import com.cocobabys.bean.GroupExpInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.GetExpCountJob;
import com.cocobabys.utils.Utils;

public class ExpActivity extends UmengStatisticsActivity{
    private static final int   START_TO_EXP_LIST = 1;
    private TextView           titleView;
    private int                currentYear       = Calendar.getInstance().get(Calendar.YEAR);
    private ImageButton        leftBtn;
    private ImageButton        rightBtn;
    private GridView           gridview;
    private ExpGridViewAdapter adapter;
    private ProgressDialog     dialog;
    private Handler            handler;
    private String             selectedMonth;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grouth);
        initUI();
        initHandler();
        runGetExpCountJob();
    }

    private void runGetExpCountJob(){
        dialog.show();
        GetExpCountJob getExpCountJob = new GetExpCountJob(handler, currentYear);
        getExpCountJob.execute();
    }

    private void initHandler(){
        handler = new MyHandler(this, dialog){

            @Override
            public void handleMessage(Message msg){
                if(ExpActivity.this.isFinishing()){
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch(msg.what){
                    case EventType.GET_EXP_COUNT_FAIL:
                        Utils.makeToast(ExpActivity.this, "获取数量失败");
                        break;
                    case EventType.GET_EXP_COUNT_SUCCESS:
                        handleGetCountSuccess((List<GroupExpInfo>)msg.obj);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    protected void handleGetCountSuccess(List<GroupExpInfo> list){
        adapter.updateList(list);
    }

    private void initDialog(){
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.refresh_data));
    }

    private void initUI(){
        initDialog();
        titleView = (TextView)findViewById(R.id.topYear);
        setTitle();
        initBtn();
        initGridView();
    }

    public void initGridView(){
        gridview = (GridView)findViewById(R.id.gridview);
        List<GroupExpInfo> list = getData();
        adapter = new ExpGridViewAdapter(this, list);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                startToExpListActivity(position);
            }
        });

        adapter.setClickListener(new LayoutClickListener(){
            @Override
            public void onLayoutClickListener(int pos){
                startToExpListActivity(pos);
            }
        });
    }

    @Override
    // 处理从图库和拍照返回的照片
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == START_TO_EXP_LIST){
            List<GroupExpInfo> list = getData();
            adapter.addAll(list);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startToExpListActivity(int position){
        Intent intent = new Intent();
        intent.putExtra(ConstantValue.EXP_YEAR, currentYear);
        selectedMonth = adapter.getItem(position).getMonth();
        intent.putExtra(ConstantValue.EXP_MONTH, selectedMonth);
        intent.setClass(this, ExpListActivity.class);
        startActivityForResult(intent, START_TO_EXP_LIST);
    }

    private List<GroupExpInfo> getData(){
        return DataMgr.getInstance().getExpCountGroupByMonthPerYear(currentYear);
    }

    private void setTitle(){
        titleView.setText(String.valueOf(currentYear));
    }

    private void initBtn(){
        leftBtn = (ImageButton)findViewById(R.id.leftArrow);
        leftBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(currentYear <= 2012){
                    return;
                }
                currentYear--;
                setTitle();
                adapter.addAll(getData());
                // runGetExpCountJob();
            }
        });

        rightBtn = (ImageButton)findViewById(R.id.rightArrow);

        rightBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(currentYear >= 2200){
                    return;
                }
                currentYear++;
                setTitle();
                adapter.addAll(getData());
                // runGetExpCountJob();
            }
        });

        // from topbar
        TextView send = (TextView)findViewById(R.id.rightBtn);
        send.setVisibility(View.VISIBLE);
        send.setText(R.string.add);
        send.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                Utils.goNextActivity(ExpActivity.this, SendExpActivity.class, false);
            }
        });

        TextView topbarTitle = (TextView)findViewById(R.id.topbarTitleView);
        topbarTitle.setVisibility(View.VISIBLE);
        topbarTitle.setText(R.string.experence);

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
            // DataMgr.getInstance().clearExp();
            // adapter.clear();
        }
        return true;
    }

    public interface LayoutClickListener{
        public void onLayoutClickListener(int pos);
    }

}
