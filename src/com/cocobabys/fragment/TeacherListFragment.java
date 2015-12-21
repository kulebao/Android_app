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

import io.rong.imkit.RongIM;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.cocobabys.R;
import com.cocobabys.adapter.TeacherListAdapter;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.GetTeacherListJob;

public class TeacherListFragment extends Fragment{

    private TeacherListAdapter adapter;
    private ListView           listView;
    private ProgressDialog     dialog;
    private Handler            myhandler;
    private String             classid;

    public TeacherListFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d("", "EEEE TeacherListFragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.teacher_list_fragment, container, false);
        Log.d("", "EEEE TeacherListFragment onCreateView");
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Log.d("", "EEEE TeacherListFragment onViewCreated");

        classid = getActivity().getIntent().getStringExtra(ConstantValue.CLASS_ID);

        initDialog();
        initHander();
        initListAdapter(view);
        runGetTeacherListTask();
    }

    private void initHander(){
        myhandler = new MyHandler(getActivity(), dialog){
            @Override
            public void handleMessage(Message msg){
                if(getActivity().isFinishing()){
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch(msg.what){
                    case EventType.GET_TEACHER_SUCCESS:
                        @SuppressWarnings("unchecked")
                        List<Teacher> list = (List<Teacher>)msg.obj;
                        refreshList(list);
                        break;
                    case EventType.GET_TEACHER_FAIL:
                        List<Teacher> allTeachers = DataMgr.getInstance().getAllTeachers();
                        refreshList(allTeachers);
                        break;
                    default:
                        break;
                }
            }

        };
    }

    protected void refreshList(List<Teacher> list){
        adapter.refresh(list);
    }

    private void initDialog(){
        dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.loading_data));
    }

    private void runGetTeacherListTask(){
        dialog.show();
        GetTeacherListJob getTeacherListJob = new GetTeacherListJob(myhandler, classid);
        getTeacherListJob.execute();
    }

    private void initListAdapter(View view){
        List<Teacher> listinfo = new ArrayList<Teacher>();
        adapter = new TeacherListAdapter(getActivity(), listinfo);
        listView = (ListView)view.findViewById(R.id.teacher_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Teacher teacher = adapter.getItem(position);
                Log.d("", "start im id=" + teacher.getIMUserid() + " name =" + teacher.getName());
                RongIM.getInstance().startPrivateChat(getActivity(), teacher.getIMUserid(), teacher.getName());
            }
        });

    }

}
