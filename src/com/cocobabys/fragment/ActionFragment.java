/*
 * Copyright 2014 The Android Open Source Project Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.cocobabys.fragment;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.cocobabys.R;
import com.cocobabys.activities.ActionActivity;
import com.cocobabys.adapter.ActionListAdapter;
import com.cocobabys.bean.ActionInfo;
import com.cocobabys.bean.PullToRefreshListInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.jobs.GetActionJob;
import com.cocobabys.listener.MyPullToRefreshOnItemClickListener;
import com.cocobabys.utils.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ActionFragment extends MyListFragment{

    private List<ActionInfo>  actionInfos = new ArrayList<ActionInfo>();

    private ActionListAdapter listAdapter;

    public ActionFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.action, container, false);
        msgListView = (PullToRefreshListView)view.findViewById(R.id.pulltorefreshlist);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        initListView(view);
    }

    @Override
    public void runLoadDataTask(){
        dialog.show();

        PullToRefreshListInfo info = new PullToRefreshListInfo();
        doGet(info);
    }

    private void initListView(View view){
        listAdapter = new ActionListAdapter(getActivity(), actionInfos);

        msgListView.setAdapter(listAdapter);

        msgListView.setOnItemClickListener(new MyPullToRefreshOnItemClickListener(){
            @Override
            public void handleClick(int realPosition){
                startToActionDetailActivity(realPosition);
            }
        });

    }

    private void startToActionDetailActivity(int position){
        ActionInfo item = (ActionInfo)listAdapter.getItem(position);

        Intent intent = new Intent();
        FragmentActivity activity = getActivity();
        intent.setClass(activity, ActionActivity.class);
        intent.putExtra(ConstantValue.ACTION_DETAIL, JSON.toJSONString(item));
        activity.startActivity(intent);
    }

    @Override
    public void handleMsg(Message msg){
        Log.d("DDD ActionMethod getResult", " result : " + msg.what);
        switch(msg.what){
            case EventType.ACTION_GET_FAIL:
                Utils.makeToast(getActivity(), Utils.getResString(R.string.get_action_fail));
                break;
            case EventType.ACTION_GET_SUCCESS:
                handleGetActionSuccess(msg);
                break;
            default:
                break;
        }
    }

    private void handleGetActionSuccess(Message msg){
        @SuppressWarnings("unchecked")
        List<ActionInfo> list = (List<ActionInfo>)msg.obj;
        if(list.isEmpty()){
            Utils.makeToast(getActivity(), Utils.getResString(R.string.get_action_empty));
        } else{
            if(msg.arg1 == ConstantValue.TYPE_GET_HEAD){
                actionInfos.addAll(0, list);
            } else{
                actionInfos.addAll(list);
            }

            listAdapter.notifyDataSetChanged();
        }
    }

    private void doGet(PullToRefreshListInfo info){
        GetActionJob actionJob = new GetActionJob(myhandler, info);
        actionJob.execute();
    }

    @Override
    public void refreshTail(){
        PullToRefreshListInfo info = new PullToRefreshListInfo();
        if(!actionInfos.isEmpty()){
            info.setTo(actionInfos.get(actionInfos.size() - 1).getId());
        }
        info.setType(ConstantValue.TYPE_GET_TAIL);
        doGet(info);
    }

    @Override
    public void refreshHead(){
        PullToRefreshListInfo info = new PullToRefreshListInfo();
        if(!actionInfos.isEmpty()){
            info.setFrom(actionInfos.get(0).getId());
        }
        info.setType(ConstantValue.TYPE_GET_HEAD);
        doGet(info);
    }

}
