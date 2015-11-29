package com.cocobabys.im;

import com.cocobabys.R;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.IMGroupInfo;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.utils.IMUtils;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.CameraInputProvider;
import io.rong.imkit.widget.provider.ImageInputProvider;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Conversation.ConversationNotificationStatus;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

public class IMHelper
		implements RongIM.UserInfoProvider, RongIMClient.OnReceiveMessageListener, RongIM.GroupInfoProvider {

	@Override
	public UserInfo getUserInfo(String userId) {
		UserInfo bret = null;
		try {
			if (userId.toLowerCase().startsWith("t_")) {
				// 老师
				String id = getID(userId);
				Teacher teacher = DataMgr.getInstance().getTeacherByInternalID(Integer.parseInt(id));
				if (teacher != null) {
					bret = new UserInfo(userId, teacher.getName(), Uri.parse(teacher.getHead_icon()));
				} else {
					bret = new UserInfo(userId, "教师", null);
				}

			} else if (userId.toLowerCase().startsWith("p_")) {
				// 家长
				String id = getID(userId);
				ParentInfo parent = DataMgr.getInstance().getParentByInternalID(Integer.parseInt(id));

				if (parent != null) {
					bret = new UserInfo(userId, parent.getName(), Uri.parse(parent.getPortrait()));
				} else {
					bret = new UserInfo(userId, "家长", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.d("", "getUserInfo  userId=" + userId + " info=" + bret);
		return bret;
	}

	// 返回()中间的字符,只能判断最后一个括号内容，不过对当前业务来看足够了
	public static String getID(String str) {
		String r = str.replaceAll("^.*\\(", "").replaceAll("\\).*", "");
		return r;
	}

	@Override
	public boolean onReceived(Message message, int left) {
		ConversationType conversationType = message.getConversationType();
		String targetId = message.getTargetId();

		// 返回true表示自己处理消息通知，这里直接返回就表示不处理
		if (IMUtils.isMessageDisturbEnable(targetId) && conversationType.equals(ConversationType.GROUP)) {
			Log.d("", "onReceived message but disturb it! targetId=" + targetId);
			return true;
		}
		return false;
	}

	@Override
	public Group getGroupInfo(String groupId) {
		Group group = null;
		IMGroupInfo imGroupInfo = DataMgr.getInstance().getIMGroupInfoByGroupID(groupId);
		Resources r = MyApplication.getInstance().getResources();

		Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
				+ r.getResourcePackageName(R.drawable.small_logo) + "/" + r.getResourceTypeName(R.drawable.small_logo)
				+ "/" + r.getResourceEntryName(R.drawable.small_logo));

		if (imGroupInfo != null) {
			group = new Group(groupId, imGroupInfo.getGroup_name(), uri);
		}

		Log.d("imGroupInfo", "imGroupInfo =" + imGroupInfo.toString());

		return group;
	}

	public static void setGroupMessageNotificationStatus(ConversationType conversationType, String groupID,
			ConversationNotificationStatus status) {
		RongIM.getInstance().getRongIMClient().setConversationNotificationStatus(conversationType, groupID, status,
				new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {

					@Override
					public void onSuccess(Conversation.ConversationNotificationStatus status) {
						Log.e("", "SetConversationNotificationFragment onError--  onSuccess ");
					}

					@Override
					public void onError(RongIMClient.ErrorCode errorCode) {
						Log.e("",
								"SetConversationNotificationFragment onError--  errorCode =" + errorCode.getMessage());
					}
				});
	}

}
