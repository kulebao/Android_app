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
import com.cocobabys.activities.MyApplication;
import com.cocobabys.dlgmgr.DlgMgr;
import com.cocobabys.share.WeiXinUtils;
import com.cocobabys.utils.Utils;

public class LongClickDlg {
	private String textContent = "";
	// 本地路径，用来保存到图库
	private String imageUrl = "";

	// 服务器路径，用来分享到微信微博
	private String shareUrl = "";

	private DeleteChatListener deleteChatListener = null;
	private OnDeleteBtnClickListener onDeleteBtnClickListener = null;
	private Context context;
	private String[] items;
	private ProgressDialog dialog;

	public String getShareUrl() {
		return shareUrl;
	}

	public void setShareUrl(String shareUrl) {
		this.shareUrl = shareUrl;
	}

	public LongClickDlg(Context context) {
		this.context = context;

		initProgressDlg(context);
		initDeleteChatListener();
	}

	public DeleteChatListener getDeleteChatListener() {
		return deleteChatListener;
	}

	private void initDeleteChatListener() {
		deleteChatListener = new DeleteChatListener() {

			@Override
			public void onDeleteSuccess() {
				Utils.makeToast(context, "删除数据成功");
				dialog.cancel();
			}

			@Override
			public void onDeleteFail() {
				dialog.cancel();
				Utils.makeToast(context, "删除数据失败");
			}

			@Override
			public void onDeleteBegain() {
				dialog.show();
			}
		};
	}

	private void initProgressDlg(Context context) {
		dialog = new ProgressDialog(context);
		dialog.setMessage(Utils.getResString(R.string.deleting_data));
		dialog.setCancelable(false);
	}

	public void setOnDeleteBtnClickListener(
			OnDeleteBtnClickListener onDeleteBtnClickListener) {
		this.onDeleteBtnClickListener = onDeleteBtnClickListener;
	}

	public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void showDlg() {
		List<String> list = new ArrayList<String>();
		if (!TextUtils.isEmpty(textContent)) {
			list.add(Utils.getResString(R.string.copy));
		}

		if (!TextUtils.isEmpty(imageUrl) && new File(imageUrl).exists()) {
			list.add(Utils.getResString(R.string.save_to_gallery));

			if (MyApplication.getInstance().isForTest()) {
				// 微信目前不支持分享视频到朋友圈
				if (!shareUrl.endsWith(Utils.DEFAULT_VIDEO_ENDS)) {
					list.add(Utils.getResString(R.string.share_to_wexin_circle));
				}
				list.add(Utils.getResString(R.string.share_to_wexin_friends));
			}
		}

		if (onDeleteBtnClickListener != null) {
			list.add(Utils.getResString(R.string.delete));
		}

		if (list.isEmpty()) {
			return;
		}

		items = list.toArray(new String[list.size()]);

		DlgMgr.getListDialog(context, items,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Log.d("initTitle ddd", "which =" + which);
						handleClick(items, which);
					}
				}).create().show();
	}

	protected void handleClick(String[] items, int which) {
		String btnName = items[which];
		if (Utils.getResString(R.string.copy).equals(btnName)) {
			handleCopy();
		} else if (Utils.getResString(R.string.save_to_gallery).equals(btnName)) {
			handleAddToGallery();
		} else if (Utils.getResString(R.string.delete).equals(btnName)) {
			if (onDeleteBtnClickListener != null) {
				onDeleteBtnClickListener.onDeleteClicked();
			}
		} else if (Utils.getResString(R.string.share_to_wexin_circle).equals(
				btnName)) {
			Log.d("", "share_to_wexin_circle shareUrl=" + shareUrl);
			WeiXinUtils.getInstance().share("", "", shareUrl,
					Platform.SHARE_IMAGE, WechatMoments.NAME);
		} else if (Utils.getResString(R.string.share_to_wexin_friends).equals(
				btnName)) {
			Log.d("", "share_to_wexin_friends shareUrl=" + shareUrl);
			WeiXinUtils.getInstance().share("", "", shareUrl,
					Platform.SHARE_IMAGE, Wechat.NAME);
		}
	}

	private void handleCopy() {
		Utils.copy(textContent);
		Utils.makeToast(context, R.string.copy_to_clipboard);
	}

	private void handleAddToGallery() {
		try {
			File file = new File(imageUrl);
			Utils.addPicToGallery(Uri.fromFile(file));
			Utils.makeToast(context, R.string.copy_to_gallery);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public interface OnDeleteBtnClickListener {
		public void onDeleteClicked();
	}

	public interface DeleteChatListener {
		public void onDeleteBegain();

		public void onDeleteSuccess();

		public void onDeleteFail();
	}
}
