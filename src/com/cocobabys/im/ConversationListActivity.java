package com.cocobabys.im;

import java.util.ArrayList;
import java.util.List;

import com.cocobabys.R;
import com.cocobabys.activities.ActivityHelper;
import com.cocobabys.activities.ContactListActivity;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.utils.IMUtils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * Created by Bob on 15/8/18. 会话列表
 */
@SuppressLint("NewApi")
public class ConversationListActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.im_conversationlist);

		initUI();

		isReconnect();
	}

	private void initUI() {

		ImageView contact = (ImageView) findViewById(R.id.rightImage);
		contact.setVisibility(View.VISIBLE);
		contact.setImageResource(R.drawable.contactbtn);

		contact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				List<ChildInfo> allChildrenInfo = DataMgr.getInstance().getAllChildrenInfo();
				if (allChildrenInfo.size() > 1) {
					showListDlg();
				} else {
					startToContactListActivity(DataMgr.getInstance().getSelectedChild().getClass_id());
				}
			}
		});

		setActionBarTitle("家园互动");
	}

	protected void showListDlg() {
		AlertDialog.Builder builder = new AlertDialog.Builder(ConversationListActivity.this);
		builder.setIcon(R.drawable.small_logo);
		builder.setTitle("选择一个班级");
		final List<ChildInfo> allChildrenInfo = DataMgr.getInstance().getAllChildrenInfo();
		List<String> classnamelist = new ArrayList<>();
		for (ChildInfo childInfo : allChildrenInfo) {
			classnamelist.add(childInfo.getClass_name());
		}
		final String[] array = classnamelist.toArray(new String[classnamelist.size()]);
		builder.setItems(array, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String class_id = allChildrenInfo.get(which).getClass_id();
				startToContactListActivity(class_id);
			}
		});
		builder.show();
	}

	/**
	 * 设置 actionbar title
	 */
	private void setActionBarTitle(String title) {
		ActivityHelper.setTitle(this, title);
	}

	/**
	 * 加载 会话列表 ConversationListFragment
	 */
	private void enterFragment() {

		String lastPathSegment = getIntent().getData().getLastPathSegment();

		Log.d("", "enterFragment lastPathSegment=" + lastPathSegment);

		ConversationListFragment fragment = (ConversationListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.conversationlist);

		Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon().appendPath("conversationlist")
				.appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") // 设置私聊会话非聚合显示
				.appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")// 设置群组会话聚合显示
				.appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")// 设置讨论组会话非聚合显示
				.appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")// 设置系统会话非聚合显示
				.build();

		fragment.setUri(uri);
	}

	/**
	 * 判断消息是否是 push 消息
	 *
	 */
	private void isReconnect() {

		Intent intent = getIntent();

		String token = IMUtils.getToken();

		// push或通知过来
		if (intent != null && intent.getData() != null && intent.getData().getScheme().equals("rong")) {

			// 通过intent.getData().getQueryParameter("push") 为true，判断是否是push消息
			if (intent.getData().getQueryParameter("push") != null
					&& intent.getData().getQueryParameter("push").equals("true")) {
				reconnect(token);
			} else {
				// 程序切到后台，收到消息后点击进入,会执行这里
				if (RongIM.getInstance() == null || RongIM.getInstance().getRongIMClient() == null) {
					reconnect(token);
				} else {
					enterFragment();
				}
			}
		}

	}

	/**
	 * 重连
	 *
	 * @param token
	 */
	private void reconnect(String token) {

		if (getApplicationInfo().packageName.equals(MyApplication.getCurProcessName(getApplicationContext()))) {

			RongIM.connect(token, new RongIMClient.ConnectCallback() {
				@Override
				public void onTokenIncorrect() {

				}

				@Override
				public void onSuccess(String s) {
					enterFragment();
				}

				@Override
				public void onError(RongIMClient.ErrorCode errorCode) {

				}
			});
		}
	}

	private void startToContactListActivity(String class_id) {
		Intent intent = new Intent(ConversationListActivity.this, ContactListActivity.class);
		intent.putExtra(ConstantValue.CLASS_ID, class_id);
		intent.putExtra(ConstantValue.SHOW_GROUP_ENTRY, true);
		ConversationListActivity.this.startActivity(intent);
	}
}
