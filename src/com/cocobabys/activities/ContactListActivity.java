package com.cocobabys.activities;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.IMGroupInfo;
import com.cocobabys.fragment.ParentListFragment;
import com.cocobabys.fragment.TeacherListFragment;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import io.rong.imkit.RongIM;

public class ContactListActivity extends FragmentActivity {
	private static final int PARAENT = 0;
	private static final int TEACHER = 1;

	private int currentIndex = TEACHER;

	private ImageView parentListView;
	private ImageView teacherListView;
	private ParentListFragment parentListFragment;
	private TeacherListFragment teacherListFragment;

	private Fragment currentFragment;
	private String classid = "";
	private String className;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact);
		Log.d("", "EEEE ContactListActivity onCreate");

		initGroupEntry();

		initView();

		parentListFragment = new ParentListFragment();
		teacherListFragment = new TeacherListFragment();

		moveToTeacherList();
	}

	private void initGroupEntry() {
		boolean entry = getIntent().getBooleanExtra(ConstantValue.SHOW_GROUP_ENTRY, false);
		classid = getIntent().getStringExtra(ConstantValue.CLASS_ID);

		if (entry) {
			Button groupEntry = (Button) findViewById(R.id.groupEntry);
			groupEntry.setVisibility(View.VISIBLE);
			className = DataMgr.getInstance().getClassName(classid);
			if (TextUtils.isEmpty(className)) {
				groupEntry.setText("发消息给班级群");
			} else {
				groupEntry.setText("发消息给" + className);
			}
		}
	}

	public void enterGroup(View view) {
		try {
			DataMgr instance = DataMgr.getInstance();
			int intClassid = Integer.parseInt(classid);
			IMGroupInfo imGroupInfo = instance.getIMGroupInfo(intClassid);
			if (imGroupInfo == null) {
				// 默认的groupid构建方式
				String group_id = instance.getSchoolID() + "_" + classid;
				imGroupInfo = new IMGroupInfo(intClassid, group_id, className);
			}

			RongIM.getInstance().startGroupChat(this, imGroupInfo.getGroup_id(), imGroupInfo.getGroup_name());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this); // 统计时长
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void initView() {
		parentListView = (ImageView) findViewById(R.id.parentListView);
		teacherListView = (ImageView) findViewById(R.id.teacherListView);
	}

	private void moveToParentList() {
		switchContent(teacherListFragment, parentListFragment);
	}

	private void moveToTeacherList() {
		switchContent(parentListFragment, teacherListFragment);
	}

	public void switchContent(Fragment from, Fragment to) {
		if (currentFragment != to) {
			currentFragment = to;
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			if (!to.isAdded()) { // 先判断是否被add过
				transaction.hide(from).add(R.id.contact_fragment, to);
				transaction.show(to);
				transaction.commit(); // 隐藏当前的fragment，add下一个到Activity中
			} else {
				transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
			}
		}
	}

	// public void onEvent(EmptyEvent emptyEvent) {
	// Log.d("", "XXXX receive emptyEvent");
	// bDirectToMain = true;
	// }

	public void parentList(View view) {
		Log.d("", "action currentIndex=" + currentIndex);
		if (currentIndex == PARAENT) {
			return;
		}

		parentListView.setImageResource(R.drawable.parent_btn_down);
		teacherListView.setImageResource(R.drawable.teacher_btn_nor);

		moveToParentList();
		currentIndex = PARAENT;
	}

	public void teacherList(View view) {
		Log.d("", "shop currentIndex=" + currentIndex);

		if (currentIndex == TEACHER) {
			return;
		}

		parentListView.setImageResource(R.drawable.parent_btn_nor);
		teacherListView.setImageResource(R.drawable.teacher_btn_down);

		moveToTeacherList();
		currentIndex = TEACHER;
	}

	// @Override
	// public void onBackPressed() {
	// if (bDirectToMain == true) {
	// Log.d("", "ZZZZZ start FLAG_ACTIVITY_CLEAR_TOP");
	// Intent intent = new Intent(this, SchoolNoticeActivity.class);
	// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	// intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	//
	// startActivity(intent);
	// } else {
	// Log.d("", "ZZZZZ finish myself");
	// super.onBackPressed();
	// }
	// bDirectToMain = false;
	// }

}