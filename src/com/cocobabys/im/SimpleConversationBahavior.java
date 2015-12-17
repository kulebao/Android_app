package com.cocobabys.im;

import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ParentInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongIM.ConversationBehaviorListener;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ImageMessage;

public class SimpleConversationBahavior implements ConversationBehaviorListener {

	@Override
	public boolean onMessageClick(Context context, View view, Message message) {
		if (message.getContent() instanceof ImageMessage) {
			ImageMessage imageMessage = (ImageMessage) message.getContent();
			Intent intent = new Intent(context, PhotoActivity.class);

			intent.putExtra("photo",
					imageMessage.getLocalUri() == null ? imageMessage.getRemoteUri() : imageMessage.getLocalUri());
			if (imageMessage.getThumUri() != null)
				intent.putExtra("thumbnail", imageMessage.getThumUri());

			context.startActivity(intent);
		}

		Log.d("Begavior", message.getObjectName() + ":" + message.getMessageId());

		return false;
	}

	@Override
	public boolean onMessageLongClick(Context arg0, View arg1, Message arg2) {
		return false;
	}

	@Override
	public boolean onUserPortraitClick(Context context, ConversationType conversationType, UserInfo userInfo) {
		try {
			ParentInfo selfInfoByPhone = DataMgr.getInstance().getSelfInfoByPhone();
			if (conversationType == ConversationType.GROUP
					&& !selfInfoByPhone.getIMUserid().equals(userInfo.getUserId())) {
				// EventBus.getDefault().post(new EmptyEvent());
				RongIM.getInstance().startPrivateChat(context, userInfo.getUserId(), userInfo.getName());
				Activity activity = (Activity) context;
				activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean onUserPortraitLongClick(Context arg0, ConversationType arg1, UserInfo arg2) {
		return false;
	}

	@Override
	public boolean onMessageLinkClick(Context arg0, String arg1) {
		return false;
	}

}
