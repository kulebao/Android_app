package com.cocobabys.activities;

import com.cocobabys.R;
import com.cocobabys.fragment.ParentListFragment;
import com.cocobabys.fragment.TeacherListFragment;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ContactListActivity extends FragmentActivity {
	private static final int PARAENT = 0;
	private static final int TEACHER = 1;

	private int currentIndex = TEACHER;


	private ImageView parentListView;
	private ImageView teacherListView;
	private ParentListFragment parentListFragment;
	private TeacherListFragment teacherListFragment;

	private Fragment currentFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact);
		Log.d("", "EEEE ContactListActivity onCreate");

		initView();

		parentListFragment = new ParentListFragment();
		teacherListFragment = new TeacherListFragment();

		moveToTeacherList();
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