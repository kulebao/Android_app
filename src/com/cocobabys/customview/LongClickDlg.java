package com.cocobabys.customview;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.cocobabys.R;
import com.cocobabys.bean.ShareInfo;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dlgmgr.DlgMgr;
import com.cocobabys.share.WeiXinUtils;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

public class LongClickDlg{
    private DeleteChatListener       deleteChatListener       = null;
    private OnDeleteBtnClickListener onDeleteBtnClickListener = null;
    private Context                  context;
    private String[]                 items;
    private ProgressDialog           dialog;

    private ShareInfo                info                     = new ShareInfo();

    public ShareInfo getInfo(){
        return info;
    }

    public void setInfo(ShareInfo info){
        this.info = info;
    }

    public LongClickDlg(Context context){
        this.context = context;

        initProgressDlg(context);
        initDeleteChatListener();
    }

    public DeleteChatListener getDeleteChatListener(){
        return deleteChatListener;
    }

    private void initDeleteChatListener(){
        deleteChatListener = new DeleteChatListener(){

            @Override
            public void onDeleteSuccess(){
                Utils.makeToast(context, "删除数据成功");
                dialog.cancel();
            }

            @Override
            public void onDeleteFail(){
                dialog.cancel();
                Utils.makeToast(context, "删除数据失败");
            }

            @Override
            public void onDeleteBegain(){
                dialog.show();
            }
        };
    }

    private void initProgressDlg(Context context){
        dialog = new ProgressDialog(context);
        dialog.setMessage(Utils.getResString(R.string.deleting_data));
        dialog.setCancelable(false);
    }

    public void setOnDeleteBtnClickListener(OnDeleteBtnClickListener onDeleteBtnClickListener){
        this.onDeleteBtnClickListener = onDeleteBtnClickListener;
    }

    public void showDlg(){
        List<String> list = new ArrayList<String>();
        if(!TextUtils.isEmpty(info.getContent())){
            list.add(Utils.getResString(R.string.copy));
        }

        if(info.isValidShareType() && !TextUtils.isEmpty(info.getLocalUrl()) && new File(info.getLocalUrl()).exists()){

            if(JSONConstant.IMAGE_TYPE.equals(info.getMediaType())){
                list.add(Utils.getResString(R.string.save_to_gallery));
            }

            // 暂时屏蔽长按显示分享的选项
            // if(MyApplication.getInstance().isForTest()){
            // // 微信目前不支持分享视频到朋友圈
            // if(!JSONConstant.VIDEO_TYPE.equals(info.getMediaType())){
            // list.add(Utils.getResString(R.string.share_to_wexin_circle));
            // }
            // list.add(Utils.getResString(R.string.share_to_wexin_friends));
            // }
        }

        if(onDeleteBtnClickListener != null){
            list.add(Utils.getResString(R.string.delete));
        }

        if(list.isEmpty()){
            return;
        }

        items = list.toArray(new String[list.size()]);

        DlgMgr.getListDialog(context, items, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                Log.d("initTitle ddd", "which =" + which);
                handleClick(items, which);
            }
        }).create().show();
    }

    protected void handleClick(String[] items, int which){
        String btnName = items[which];
        if(Utils.getResString(R.string.copy).equals(btnName)){
            handleCopy();
        } else if(Utils.getResString(R.string.save_to_gallery).equals(btnName)){
            handleAddToGallery();
        } else if(Utils.getResString(R.string.delete).equals(btnName)){
            if(onDeleteBtnClickListener != null){
                onDeleteBtnClickListener.onDeleteClicked();
            }
        } else if(Utils.getResString(R.string.share_to_wexin_circle).equals(btnName)){
            copyFile();
            Log.d("", "share_to_wexin_circle shareUrl=" + info.getMediaUrl());
            WeiXinUtils.getInstance().shareFile("", "", info.getMediaUrl(), Platform.SHARE_IMAGE, WechatMoments.NAME);
        } else if(Utils.getResString(R.string.share_to_wexin_friends).equals(btnName)){
            copyFile();
            Log.d("", "share_to_wexin_friends shareUrl=" + info.getMediaUrl());
            WeiXinUtils.getInstance().shareFile("测试", "测试", info.getMediaUrl(), getShareType(), Wechat.NAME);
        }
    }

    private int getShareType(){
        if(JSONConstant.IMAGE_TYPE.equals(info.getMediaType())){
            return Platform.SHARE_IMAGE;
        } else if(JSONConstant.VIDEO_TYPE.equals(info.getMediaType())){
            return Platform.SHARE_VIDEO;
        }

        return Platform.SHARE_FILE;
    }

    private void handleCopy(){
        Utils.copy(info.getContent());
        Utils.makeToast(context, R.string.copy_to_clipboard);
    }

    private void handleAddToGallery(){
        try{
            File file = new File(info.getLocalUrl());
            Utils.addPicToGallery(Uri.fromFile(file));
            Utils.makeToast(context, R.string.copy_to_gallery);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public interface OnDeleteBtnClickListener{
        public void onDeleteClicked();
    }

    public interface DeleteChatListener{
        public void onDeleteBegain();

        public void onDeleteSuccess();

        public void onDeleteFail();
    }

    // 分享本地文件的方式，可以省服务器流量，但是微信的，不绕过审核的视频分享不支持该模式
    // 微信绕过审核的视频可以分享，暂时不用
    private void copyFile(){
        String mediaUrl = info.getLocalUrl();
        Log.d("", "mediaUrl =" + mediaUrl);

        // 如果本地文件带扩展名，则可以直接传给微信
        if(mediaUrl.endsWith(Utils.PNG_EXT) || mediaUrl.endsWith(Utils.JPG_EXT)
                || mediaUrl.endsWith(Utils.DEFAULT_VIDEO_ENDS)){
            info.setMediaUrl(mediaUrl);
            return;
        }

        // 本地保存的文件是不带扩展名的，为了不显示在本地相册
        // 而微信上需要扩展名，否则打不开文件,这里需要拷贝一份分享文件到本地
        String shareFileName = Utils.getName(mediaUrl);

        Log.d("", "shareFileName =" + shareFileName);

        if(JSONConstant.IMAGE_TYPE.equals(info.getMediaType())){
            shareFileName += Utils.PNG_EXT;
        } else if(JSONConstant.VIDEO_TYPE.equals(info.getMediaType())){
            shareFileName += Utils.DEFAULT_VIDEO_ENDS;
        }

        Log.d("", "shareFileName =" + shareFileName);

        // File to = new File(Utils.getSDCardFileDir(Utils.APP_DIR_SHARE),
        // shareFileName);
        File to = new File(Utils.getDefaultCameraDir() + shareFileName);
        if(!to.exists()){
            Log.d("", "setShareUrl to =" + to.getAbsolutePath());
            DataUtils.copyFile(new File(mediaUrl), to);
        }

        // 必须加入本地图库数据库，因为微信是通过这个去获取视频的缩略图，播放时间等信息
        // 而且，如果不这样做，ios手机可能会无法播放视频。。。
        Utils.addVideoToGallery(Uri.fromFile(to));

        info.setMediaUrl(to.getAbsolutePath());
    }
}
