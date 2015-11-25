package com.cocobabys.activities;

import java.util.ArrayList;
import java.util.List;

import com.cocobabys.R;
import com.cocobabys.adapter.ExpandableAdapter;
import com.cocobabys.bean.IMExpandInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.GetClassRelationShipJob;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;

public class ParentGroupActivity extends UmengStatisticsActivity {

	private ExpandableAdapter adapter;
	private ExpandableListView listView;
	private ProgressDialog dialog;
	private Handler myhandler;
	private String classid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parent_list);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.teacherList);

		classid = getIntent().getStringExtra(ConstantValue.CLASS_ID);

		initDialog();
		initHander();
		initListAdapter();
		runGetTeacherListTask();
	}

	private void initHander() {
		myhandler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				if (ParentGroupActivity.this.isFinishing()) {
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

		// 屏蔽群组点击事件，以免子列表收缩
		listView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				return true;
			}
		});

		adapter.notifyDataSetChanged();
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.loading_data));
	}

	private void runGetTeacherListTask() {
		dialog.show();
		GetClassRelationShipJob job = new GetClassRelationShipJob(myhandler, classid);
		job.execute();
	}

	private void initListAdapter() {
		List<IMExpandInfo> listinfo = new ArrayList<>();
		adapter = new ExpandableAdapter(this, listinfo);
		listView = (ExpandableListView) findViewById(R.id.expandList);
		listView.setAdapter(adapter);
		listView.setGroupIndicator(null);
	}

}
