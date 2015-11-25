package com.cocobabys.activities;

import java.util.ArrayList;
import java.util.List;

import com.cocobabys.R;
import com.cocobabys.adapter.TeacherListAdapter;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.GetTeacherListJob;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;

public class TeacherGroupActivity extends UmengStatisticsActivity {

	private TeacherListAdapter adapter;
	private ListView listView;
	private ProgressDialog dialog;
	private Handler myhandler;
	private String classid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.teacher_list);
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
				if (TeacherGroupActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.GET_TEACHER_SUCCESS:
					@SuppressWarnings("unchecked")
					List<Teacher> list = (List<Teacher>) msg.obj;
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

	protected void refreshList(List<Teacher> list) {
		adapter.refresh(list);
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.loading_data));
	}

	private void runGetTeacherListTask() {
		dialog.show();
		GetTeacherListJob getTeacherListJob = new GetTeacherListJob(myhandler, classid);
		getTeacherListJob.execute();
	}

	private void initListAdapter() {
		List<Teacher> listinfo = new ArrayList<Teacher>();
		adapter = new TeacherListAdapter(this, listinfo);
		listView = (ListView) findViewById(R.id.teacher_list);
		listView.setAdapter(adapter);
	}

}
