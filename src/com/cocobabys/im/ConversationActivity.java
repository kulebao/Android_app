package com.cocobabys.im;

import java.util.Locale;

import com.cocobabys.R;
import com.cocobabys.activities.ActivityHelper;
import com.cocobabys.activities.BaseEventFragmentActivity;
import com.cocobabys.activities.GroupSettingActivity;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.IMGroupInfo;
import com.cocobabys.event.EmptyEvent;
import com.cocobabys.utils.IMUtils;
import com.cocobabys.utils.Utils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.fragment.MessageListFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Conversation.ConversationType;

/**
 * Created by Bob on 15/8/18. 会话页面
 */
public class ConversationActivity extends BaseEventFragmentActivity {

	private String mTargetId;

	/**
	 * 刚刚创建完讨论组后获得讨论组的id 为targetIds，需要根据 为targetIds 获取 targetId
	 */
	private String mTargetIds;

	/**
	 * 会话类型
	 */
	private Conversation.ConversationType mConversationType;
	private String title;

	private boolean bCloseWhenResume = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		getIntentDate(intent);

		if (mConversationType.equals(ConversationType.SYSTEM)) {
			setContentView(R.layout.im_messagelist);
		} else {
			setContentView(R.layout.im_conversation);
		}

		Log.d("", "ZZZZZ ConversationActivity oncreate bCloseWhenResume=" + bCloseWhenResume);

		initUI();

		isReconnect(intent);
	}

	public void onEvent(EmptyEvent emptyEvent) {
		if (mConversationType.equals(ConversationType.GROUP)) {
			Log.d("", "ZZZZZ group receive emptyEvent");
			bCloseWhenResume = true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (bCloseWhenResume && mConversationType.equals(ConversationType.GROUP)) {
			Log.d("", "ZZZZZ ConversationActivity onResume group finish self");
			bCloseWhenResume = false;
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mConversationType.equals(ConversationType.GROUP)) {
			Log.d("", "ZZZZZ ConversationActivity onDestroy");
			bCloseWhenResume = false;
			finish();
		}

	}

	/**
	 * 展示如何从 Intent 中得到 融云会话页面传递的 Uri
	 */
	private void getIntentDate(Intent intent) {

		mTargetId = intent.getData().getQueryParameter("targetId");
		mTargetIds = intent.getData().getQueryParameter("targetIds");
		title = intent.getData().getQueryParameter("title");
		// intent.getData().getLastPathSegment();//获得当前会话类型
        mConversationType = Conversation.ConversationType.valueOf(intent.getData().getLastPathSegment()
                .toUpperCase(Locale.getDefault()));
	}

	private void initUI() {
		enterFragment(mConversationType, mTargetId);

		if (Conversation.ConversationType.GROUP.equals(mConversationType)) {
			Log.d("", "GROUP ");
			// 刷新群组信息
			// IMHelper.updateGroupInfoCache(mTargetId);

			ImageView groupMember = (ImageView) findViewById(R.id.rightImage);
			groupMember.setVisibility(View.VISIBLE);
			groupMember.setImageResource(R.drawable.contactbtn);

			groupMember.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					IMGroupInfo group = DataMgr.getInstance().getIMGroupInfoByGroupID(mTargetId);
					if (group == null) {
						Utils.makeToast(ConversationActivity.this, "获取群组信息失败！");
						return;
					}
					Log.d("", "group =" + group.toString());
					Intent intent = new Intent(ConversationActivity.this, GroupSettingActivity.class);
					intent.putExtra(ConstantValue.CLASS_ID, group.getClass_id() + "");
					intent.putExtra(ConstantValue.IM_GROUP_ID, mTargetId);
					intent.putExtra(ConstantValue.IM_GROUP_NAME, title);
					startActivity(intent);
				}
			});
		} else {
			Log.d("", "Private ");
		}

		setActionBarTitle(title);
	}

    /**
     * 加载会话页面 ConversationFragment
     *
     * @param mConversationType
     * @param mTargetId
     */
    @SuppressLint("NewApi")
    private void enterFragment(Conversation.ConversationType mConversationType, String mTargetId){

		Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon().appendPath("conversation")
				.appendPath(mConversationType.getName().toLowerCase()).appendQueryParameter("targetId", mTargetId)
				.build();

		if (mConversationType.equals(ConversationType.SYSTEM)) {
			MessageListFragment fragment = (MessageListFragment) getSupportFragmentManager()
					.findFragmentById(R.id.messagelist);
			fragment.setUri(uri);
		} else {
			ConversationFragment fragment = (ConversationFragment) getSupportFragmentManager()
					.findFragmentById(R.id.conversation);
			fragment.setUri(uri);
		}

	}

	/**
	 * 判断消息是否是 push 消息
	 */
	private void isReconnect(Intent intent) {

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
					enterFragment(mConversationType, mTargetId);
				}
			}
		}
	}

	/**
	 * 设置 actionbar title
	 */
	private void setActionBarTitle(String title) {
		ActivityHelper.setTitle(this, title);
	}

	/**
	 * 重连
	 *
	 * @param token
	 */
	private void reconnect(final String token) {

		if (getApplicationInfo().packageName.equals(MyApplication.getCurProcessName(getApplicationContext()))) {

			RongIM.connect(token, new RongIMClient.ConnectCallback() {
				@Override
				public void onTokenIncorrect() {
					Log.e("", "reconnect token invalid :" + token);
				}

				@Override
				public void onSuccess(String s) {
					enterFragment(mConversationType, mTargetId);
				}

				@Override
				public void onError(RongIMClient.ErrorCode errorCode) {
					Log.e("", "reconnect error :" + errorCode);
				}
			});
		}
	}
}
