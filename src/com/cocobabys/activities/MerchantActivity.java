package com.cocobabys.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.model.LatLng;
import com.cocobabys.R;
import com.cocobabys.adapter.ActionSmallListAdapter;
import com.cocobabys.bean.ActionInfo;
import com.cocobabys.bean.MerchantInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.GetMerchantActionJob;
import com.cocobabys.listener.MyPullToRefreshOnItemClickListener;
import com.cocobabys.utils.ImageUtils;
import com.cocobabys.utils.Utils;
import com.cocobabys.utils.ViewUtils;

public class MerchantActivity extends NavigationActivity{
    private Handler                handler;
    private TextView               titleView;
    private TextView               contactView;
    private TextView               detailView;
    private MerchantInfo           merchantInfo;
    private ListView               listView;
    private ActionSmallListAdapter adapter;
    private List<ActionInfo>       actioninfos = new ArrayList<ActionInfo>();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.merchant_detail);

        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.merchant_detail);

        initData();

        initView();

        initHandler();

        setEndPoint(new LatLng(merchantInfo.getLocation().getLatitude(), merchantInfo.getLocation().getLongitude()));

        runGetActionListTask();
    }

    private void runGetActionListTask(){
        GetMerchantActionJob actionJob = new GetMerchantActionJob(handler, merchantInfo.getId());
        actionJob.execute();
    }

    private void initData(){
        String detail = getIntent().getStringExtra(ConstantValue.MERCHANT_DETAIL);
        merchantInfo = JSON.parseObject(detail, MerchantInfo.class);
    }

    private void initHandler(){
        handler = new MyHandler(this, null){
            @Override
            public void handleMessage(Message msg){
                if(MerchantActivity.this.isFinishing()){
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch(msg.what){
                    case EventType.ACTION_GET_FAIL:
                        Log.d("", "merchant get action failed!");
                        break;
                    case EventType.ACTION_GET_SUCCESS:
                        handleGetActionSuccess(msg);
                    default:
                        break;
                }
            }
        };
    }

    private void handleGetActionSuccess(Message msg){
        @SuppressWarnings("unchecked")
        List<ActionInfo> list = (List<ActionInfo>)msg.obj;
        if(list.isEmpty()){
            Log.d("", "merchant get action empty!");
        } else{
            // 只会执行一次
            actioninfos.addAll(list);
            initListView();
            adapter.notifyDataSetChanged();
        }
    }

    private void initView(){
        setLogo();
        initContent();
    }

    private void initListView(){
        listView = (ListView)findViewById(R.id.action_list);
        listView.setVisibility(View.VISIBLE);

        adapter = new ActionSmallListAdapter(this, actioninfos);
        listView.setAdapter(adapter);

        // 必须要在list.setAdapter(adapter) 之后调用
        ViewUtils.setListViewHeightBasedOnChildren(listView);

        listView.setOnItemClickListener(new MyPullToRefreshOnItemClickListener(){

            @Override
            public void handleClick(int realPosition){
                startToActionDetailActivity(realPosition);
            }
        });
    }

    private void startToActionDetailActivity(int position){
        ActionInfo item = (ActionInfo)adapter.getItem(position);

        Intent intent = new Intent();
        intent.setClass(this, ActionActivity.class);
        intent.putExtra(ConstantValue.ACTION_DETAIL, JSON.toJSONString(item));
        startActivity(intent);
    }

    private void initContent(){
        setTitle();

        setContact();

        setAddress();

        setDetail();
    }

    private void setAddress(){
        TextView address = (TextView)findViewById(R.id.address);
        address.setText(merchantInfo.getAddress());
    }

    private void setDetail(){
        detailView = (TextView)findViewById(R.id.detail);
        detailView.setText(merchantInfo.getDetail());
    }

    private void setContact(){
        contactView = (TextView)findViewById(R.id.contact);
        contactView.setText(merchantInfo.getContact());
    }

    public void contact(View view){
        Utils.startToCall(this, merchantInfo.getContact());
    }

    public void navigation(View view){
        startRoutePlanDriving();
    }

    private void setTitle(){
        titleView = (TextView)findViewById(R.id.title);
        titleView.setText(merchantInfo.getTitle());
    }

    private void setLogo(){
        ImageView actionImageView = (ImageView)findViewById(R.id.actionImage);
        if(!TextUtils.isEmpty(merchantInfo.getLogo())){
            ImageUtils.displayEx(merchantInfo.getLogo(), actionImageView, ConstantValue.ACTION_PIC_MAX_WIDTH,
                                 ConstantValue.ACTION_PIC_MAX_HEIGHT);
        }
    }

}
