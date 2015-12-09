package com.cocobabys.im;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Conversation.ConversationNotificationStatus;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

import java.util.List;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.util.Log;

import com.cocobabys.R;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.GroupParentInfo;
import com.cocobabys.dbmgr.info.IMGroupInfo;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.utils.IMUtils;

public class IMHelper implements RongIM.UserInfoProvider, RongIMClient.OnReceiveMessageListener,
        RongIM.GroupInfoProvider{

    private static final String IM_SYSTEM_ADMIN = "im_system_admin";

    @Override
    public UserInfo getUserInfo(String userId){
        // String currentGroupID =
        // MyApplication.getInstance().getCurrentGroupID();
        UserInfo bret = null;
        try{
            if(userId.toLowerCase().startsWith("t_")){
                // 老师
                String id = getID(userId);
                Teacher teacher = DataMgr.getInstance().getTeacherByInternalID(Integer.parseInt(id));
                if(teacher != null){
                    bret = new UserInfo(userId, teacher.getName() + "老师", Uri.parse(teacher.getHead_icon()));
                } else{
                    bret = new UserInfo(userId, "老师", null);
                }

            } else if(userId.toLowerCase().startsWith("p_")){
                // 家长
                String id = getID(userId);
                GroupParentInfo groupParentInfo = DataMgr.getInstance().getGroupParentInfo(Integer.parseInt(id));
                // ParentInfo parent =
                // DataMgr.getInstance().getParentByInternalID(Integer.parseInt(id));

                if(groupParentInfo != null){
                    bret = new UserInfo(userId, groupParentInfo.getNick_name(),
                            Uri.parse(groupParentInfo.getPortrait()));
                } else{
                    bret = new UserInfo(userId, "家长", null);
                }
            } else if(IM_SYSTEM_ADMIN.equalsIgnoreCase(userId)){
                bret = new UserInfo(userId, "系统管理员", null);
            }
        } catch(Exception e){
            e.printStackTrace();
        }

        if(bret != null){
            Log.d("",
                  "AAAA getUserInfo  userId=" + userId + " uri=" + bret.getPortraitUri() + " name=" + bret.getName());
        } else{
            Log.d("", "AAAA getUserInfo  userId=" + userId + " info is null!!!");
        }
        return bret;
    }

    // 返回()中间的字符,只能判断最后一个括号内容，不过对当前业务来看足够了
    public static String getID(String str){
        String r = str.replaceAll("^.*\\(", "").replaceAll("\\).*", "");
        return r;
    }

    @Override
    public boolean onReceived(Message message, int left){
        ConversationType conversationType = message.getConversationType();
        String targetId = message.getTargetId();

        // 返回true表示自己处理消息通知，这里直接返回就表示不处理
        if(IMUtils.isMessageDisturbEnable(targetId) && conversationType.equals(ConversationType.GROUP)){
            Log.d("", "onReceived message but disturb it! targetId=" + targetId);
            return true;
        }
        return false;
    }

    @Override
    public Group getGroupInfo(String groupId){
        Group group = getGroup(groupId);

        return group;
    }

    private static Group getGroup(String groupId){
        Group group = null;
        IMGroupInfo imGroupInfo;
        try{
            imGroupInfo = DataMgr.getInstance().getIMGroupInfoByGroupID(groupId);
            Resources r = MyApplication.getInstance().getResources();

            Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                    + r.getResourcePackageName(R.drawable.small_logo) + "/"
                    + r.getResourceTypeName(R.drawable.small_logo) + "/"
                    + r.getResourceEntryName(R.drawable.small_logo));

            if(imGroupInfo != null){
                group = new Group(groupId, imGroupInfo.getGroup_name(), uri);
                Log.d("imGroupInfo", "AAAA imGroupInfo =" + imGroupInfo.toString());
            }
        } catch(NotFoundException e){
            e.printStackTrace();
        }

        return group;
    }

    public static void updateGroupInfoCache(){
        DataMgr instance = DataMgr.getInstance();
        List<String> allClassID = instance.getAllClassID();
        String schoolID = instance.getSchoolID();
        for(String classid : allClassID){
            Group group = getGroup(schoolID + "_" + classid);
            if(group != null){
                RongIM.getInstance().refreshGroupInfoCache(group);
            }
        }
    }

    public static void updateParentsInfoCache(){
        List<GroupParentInfo> allGroupParentsInfo = DataMgr.getInstance().getAllGroupParentsInfoWithNick();

        for(GroupParentInfo groupParentInfo : allGroupParentsInfo){
            Uri uri = null;
            String imUserid = groupParentInfo.getIMUserid();
            if(!groupParentInfo.getPortrait().isEmpty()){
                uri = Uri.parse(groupParentInfo.getPortrait());
            }
            /**
             * 刷新用户缓存数据。
             *
             * @param userInfo 需要更新的用户缓存数据。
             */
            Log.d("", "AAAA updateParentsInfoCache pid=" + imUserid + " name=" + groupParentInfo.getNick_name());
            RongIM.getInstance().refreshUserInfoCache(new UserInfo(imUserid, groupParentInfo.getNick_name(), uri));
        }
    }

    public static void updateTeacherInfoCache(){
        List<Teacher> allTeachers = DataMgr.getInstance().getAllTeachers();

        for(Teacher teacher : allTeachers){
            Uri uri = null;
            String imUserid = teacher.getIMUserid();
            if(!teacher.getHead_icon().isEmpty()){
                uri = Uri.parse(teacher.getHead_icon());
            }
            /**
             * 刷新用户缓存数据。
             *
             * @param userInfo 需要更新的用户缓存数据。
             */
            Log.d("", "AAAA updateTeacherInfoCache tid=" + imUserid + " name=" + teacher.getName());
            RongIM.getInstance().refreshUserInfoCache(new UserInfo(imUserid, teacher.getName() + "老师", uri));
        }
    }

    public static void clearChatRecord(ConversationType conversionType, String targetID,
            ResultCallback<Boolean> callback){
        RongIM.getInstance().getRongIMClient().clearMessages(conversionType, targetID, callback);
    }

    public static void setGroupMessageNotificationStatus(ConversationType conversationType, String groupID,
            ConversationNotificationStatus status){
        RongIM.getInstance()
                .getRongIMClient()
                .setConversationNotificationStatus(conversationType,
                                                   groupID,
                                                   status,
                                                   new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>(){

                                                       @Override
                                                       public void onSuccess(
                                                               Conversation.ConversationNotificationStatus status){
                                                           Log.e("",
                                                                 "SetConversationNotificationFragment onError--  onSuccess ");
                                                       }

                                                       @Override
                                                       public void onError(RongIMClient.ErrorCode errorCode){
                                                           Log.e("",
                                                                 "SetConversationNotificationFragment onError--  errorCode ="
                                                                         + errorCode.getMessage());
                                                       }
                                                   });
    }

}
