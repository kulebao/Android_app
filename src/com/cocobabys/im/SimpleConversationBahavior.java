package com.cocobabys.im;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
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
	public boolean onUserPortraitClick(Context arg0, ConversationType arg1, UserInfo arg2) {
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
