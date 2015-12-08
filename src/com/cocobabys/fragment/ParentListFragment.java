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

import com.cocobabys.R;
import com.cocobabys.adapter.ExpandableAdapter;
import com.cocobabys.bean.IMExpandInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.GroupParentInfo;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.event.EmptyEvent;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.GetClassRelationShipJob;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import de.greenrobot.event.EventBus;
import io.rong.imkit.RongIM;

public class ParentListFragment extends Fragment {

	private ExpandableAdapter adapter;
	private ExpandableListView listView;
	private ProgressDialog dialog;
	private Handler myhandler;
	private String classid;

	public ParentListFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("", "EEEE ParentListFragment onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.parent_list_fragment, container, false);
		Log.d("", "EEEE ParentListFragment onCreateView");
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Log.d("", "EEEE ParentListFragment onViewCreated");
		classid = getActivity().getIntent().getStringExtra(ConstantValue.CLASS_ID);

		initDialog();
		initHander();
		initListAdapter(view);
		runGetClassRelationShipTask();
	}

	private void initHander() {
		myhandler = new MyHandler(getActivity(), dialog) {

			@Override
			public void handleMessage(Message msg) {
				if (getActivity().isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);

				switch (msg.what) {
				case EventType.GET_CLASS_RELATIONSHIP_SUCCESS:
				case EventType.GET_CLASS_RELATIONSHIP_FAIL:
					List<IMExpandInfo> classMemberInfo = DataMgr.getInstance().getClassMemberInfo(classid);
					refreshList(classMemberInfo);
					break;
				default:
					break;
				}
			}

		};
	}

	protected void refreshList(List<IMExpandInfo> list) {
		adapter.refresh(list);

		// 默认展开所有群组
		for (int i = 0; i < adapter.getGroupCount(); i++) {
			listView.expandGroup(i);
		}

		// 屏蔽群组点击事件，以阻止列表收缩
		listView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				return true;
			}
		});

		adapter.notifyDataSetChanged();
	}

	private void initDialog() {
		dialog = new ProgressDialog(getActivity());
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.loading_data));
	}

	private void initListAdapter(View view) {
		List<IMExpandInfo> listinfo = new ArrayList<>();
		adapter = new ExpandableAdapter(getActivity(), listinfo);
		listView = (ExpandableListView) view.findViewById(R.id.expandList);
		listView.setAdapter(adapter);
		listView.setGroupIndicator(null);

		listView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition,
					long id) {

				ParentInfo self = DataMgr.getInstance().getSelfInfoByPhone();

				final GroupParentInfo child = adapter.getChild(groupPosition, childPosition);

				if (!self.getParent_id().equals(child.getParent_id())) {
					Log.d("", "start im id=" + child.getIMUserid() + " name =" + child.getName());
					// 通知ContactListActivity这里发起了私聊，等会直接退出到主界面
					EventBus.getDefault().post(new EmptyEvent());
					RongIM.getInstance().startPrivateChat(getActivity(), child.getIMUserid(), child.getName());
				}
				return true;
			}
		});
	}

	private void runGetClassRelationShipTask() {
		dialog.show();
		GetClassRelationShipJob job = new GetClassRelationShipJob(myhandler, classid);
		job.execute();
	}

}
