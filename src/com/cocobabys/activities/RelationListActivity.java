package com.cocobabys.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.cocobabys.R;
import com.cocobabys.adapter.RelationListAdapter;
import com.cocobabys.bean.FamilyInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.RelationJob;
import com.cocobabys.utils.Utils;

public class RelationListActivity extends UmengStatisticsActivity{

    private static final int    START_INVITATION = 100;
    private ListView            list;
    private RelationListAdapter adapter;
    private ProgressDialog      dialog;
    private Handler             handler;
    private List<ParentInfo>    listinfo;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.relation_list);
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.invitation);
        initHeader();
        initDlg();
        initHandler();
        initListAdapter();
        runGetRalationTask();
    }

    private void initHeader(){
        TextView share = (TextView)findViewById(R.id.rightBtn);
        share.setVisibility(View.VISIBLE);
        share.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                Utils.goNextActivity(RelationListActivity.this, ShowIconExActivity.class, false);
            }
        });
    }

    private void initListAdapter(){
        listinfo = new ArrayList<ParentInfo>();
        adapter = new RelationListAdapter(this, listinfo);
        ParentInfo info = new ParentInfo();
        info.setRelationship(Utils.getResString(R.string.more_relation));
        listinfo.add(info);

        list = (ListView)findViewById(R.id.relation_list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                ParentInfo item = adapter.getItem(position);

                // 真实家长的电话不会为空
                if(TextUtils.isEmpty(item.getPhone())){
                    startToInvitationActivity();
                }
            }
        });
    }

    private void initDlg(){
        dialog = new ProgressDialog(this);
        dialog.setMessage(Utils.getResString(R.string.enrolling));
        dialog.setCancelable(true);
    }

    private void runGetRalationTask(){
        dialog.show();
        RelationJob relationJob = new RelationJob(handler);
        relationJob.execute();
    }

    private void initHandler(){
        handler = new MyHandler(this, dialog){
            @Override
            public void handleMessage(Message msg){
                if(RelationListActivity.this.isFinishing()){
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch(msg.what){
                    case EventType.GET_RELATIONSHIP_FAIL:
                        Utils.makeToast(RelationListActivity.this, R.string.getRaltionFail);
                        break;
                    case EventType.GET_RELATIONSHIP_SUCCESS:
                        handleGetRaltionSuccess(msg);
                        break;
                    default:
                        break;
                }
            }

        };
    }

    private void handleGetRaltionSuccess(Message msg){
        @SuppressWarnings("unchecked")
        List<ParentInfo> list = (List<ParentInfo>)msg.obj;
        listinfo.addAll(0, list);
        adapter.notifyDataSetChanged();
    }

    private void startToInvitationActivity(){
        Intent intent = new Intent();
        intent.setClass(this, InvitationActivity.class);
        startActivityForResult(intent, START_INVITATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK){
            return;
        }

        Utils.goNextActivity(RelationListActivity.this, ShowIconExActivity.class, false);

        String info = data.getStringExtra(ConstantValue.RELATION_INFO);
        if(!TextUtils.isEmpty(info)){
            FamilyInfo simpleRelaiton = JSONObject.parseObject(info, FamilyInfo.class);
            ParentInfo parent = new ParentInfo();
            parent.setName(simpleRelaiton.getName());
            parent.setPhone(simpleRelaiton.getPhone());
            parent.setRelationship(simpleRelaiton.getRelation());

            listinfo.add(0, parent);
            adapter.notifyDataSetChanged();
        }
    }

    public void invitation(View view){
        startToInvitationActivity();
    }
}