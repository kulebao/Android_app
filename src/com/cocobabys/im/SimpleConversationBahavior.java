package com.cocobabys.im;

import io.rong.imkit.RongIM;
import io.rong.imkit.RongIM.ConversationBehaviorListener;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ImageMessage;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.cocobabys.event.EmptyEvent;

import de.greenrobot.event.EventBus;

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
	public boolean onUserPortraitClick(Context context, ConversationType arg1, UserInfo userInfo) {
		// 通知ContactListActivity这里发起了私聊，等会直接退出到主界面
		EventBus.getDefault().post(new EmptyEvent());
		RongIM.getInstance().startPrivateChat(context, userInfo.getUserId(), userInfo.getName());
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
