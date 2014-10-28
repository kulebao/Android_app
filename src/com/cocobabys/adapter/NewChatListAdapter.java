package com.cocobabys.adapter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.activities.ShowIconActivity;
import com.cocobabys.bean.IconInfo;
import com.cocobabys.bean.SenderInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.customview.LongClickDlg;
import com.cocobabys.customview.LongClickDlg.OnDeleteBtnClickListener;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.dbmgr.info.NewChatInfo;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.jobs.DeleteChatJob;
import com.cocobabys.jobs.GetSenderInfoJob;
import com.cocobabys.media.MediaMgr;
import com.cocobabys.taskmgr.DownloadImgeJob;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

public class NewChatListAdapter extends BaseAdapter {
	private static final String SELF_NAME = "我";
	private static final String DEFAULT_PARENT_NAME = "家长";
	private static final String DEFAULT_TEACHER_NAME = "匿名老师";
	// 最小显示时间间隔为2分钟
	private static final long MIN_TIME_LIMIT = 2 * 60 * 1000L;
	private static final int LEFT = 0;
	private static final int RIGHT = 1;

	LruCache<String, Bitmap> lruCache;
	private final Context context;
	private List<NewChatInfo> dataList;
	private DownloadImgeJob downloadImgeJob;
	private Handler handler;
	private int deletePos = -1;
	private Map<String, String> senderMap = new HashMap<String, String>();
	private GetSenderInfoJob getTeacherInfoJob;
	private LongClickDlg longClickDlg;
	private AnimHelper animHelper;

	public NewChatListAdapter(Context activityContext, List<NewChatInfo> list,
			DownloadImgeJob downloadImgeTask, GetSenderInfoJob getTeacherInfoJob) {
		this.context = activityContext;
		this.dataList = list;
		this.downloadImgeJob = downloadImgeTask;
		this.getTeacherInfoJob = getTeacherInfoJob;

		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		lruCache = new LruCache<String, Bitmap>(maxMemory) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight() / 1024;
			}
		};

		handler = new InnerHandler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				switch (msg.what) {
				case EventType.DOWNLOAD_FILE_SUCCESS:
					notifyDataSetChanged();
					break;
				case EventType.GET_SENDER_SUCCESS:
					handleGetSenderSuccess(msg);
					break;
				case EventType.DELETE_CHAT_SUCCESS:
					handleDeleteSuccess();
					break;
				case EventType.DELETE_CHAT_FAIL:
					longClickDlg.getDeleteChatListener().onDeleteFail();
					break;
				default:
					break;
				}
			}

		};
		this.downloadImgeJob.setHanlder(handler);
		this.getTeacherInfoJob.setHanlder(handler);
	}

	private void handleDeleteSuccess() {
		if (deletePos != -1) {
			NewChatInfo item = getItem(deletePos);
			DataMgr.getInstance().deleteChat(item.getChat_id());
			dataList.remove(deletePos);
			notifyDataSetChanged();
		}
		longClickDlg.getDeleteChatListener().onDeleteSuccess();
	}

	private void handleGetSenderSuccess(Message msg) {
		SenderInfo info = (SenderInfo) msg.obj;
		String name = "";
		if (SenderInfo.TEACHER_TYPE.equals(info.getSenderType())) {
			name = getTeacherName(info.getSenderID());
		} else {
			name = getParentName(info.getSenderID());
		}
		senderMap.put(info.getSenderID(), name);
		notifyDataSetChanged();
	}

	public void clear() {
		dataList.clear();
		lruCache.evictAll();
		senderMap.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public NewChatInfo getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	// 必须带下面2个override的方法，否则系统会无法识别左右布局，导致显示混乱
	@Override
	public int getItemViewType(int position) {
		NewChatInfo chatInfo = dataList.get(position);
		if (chatInfo.isSendByTeacher()) {
			return LEFT;
		} else {
			return RIGHT;
		}
	}

	// 获取项的类型数
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FlagHolder flagholder = null;
		if (convertView == null) {
			flagholder = new FlagHolder();

			if (getItemViewType(position) == LEFT) {
				convertView = LayoutInflater.from(this.context).inflate(
						R.layout.chat_item_left, null);
				flagholder.bLeft = true;
			} else {
				convertView = LayoutInflater.from(this.context).inflate(
						R.layout.chat_item_right, null);
				flagholder.bLeft = false;
			}

			flagholder.sendView = (TextView) convertView
					.findViewById(R.id.sender);
			flagholder.bodyView = (TextView) convertView
					.findViewById(R.id.content);
			flagholder.timestampView = (TextView) convertView
					.findViewById(R.id.timestamp);
			flagholder.headiconView = (ImageView) convertView
					.findViewById(R.id.headicon);
			flagholder.mediaView = (ImageView) convertView
					.findViewById(R.id.chat_icon);
			flagholder.durationView = (TextView) convertView
					.findViewById(R.id.duration);
			flagholder.contentlayout = (RelativeLayout) convertView
					.findViewById(R.id.contentlayout);
			convertView.setTag(flagholder);
			convertView.setId(position);
		} else {
			flagholder = (FlagHolder) convertView.getTag();
		}

		if (flagholder != null) {
			setDataToViews(position, flagholder);
		}

		return convertView;
	}

	private void setDataToViews(final int position, FlagHolder flagholder) {
		final NewChatInfo info = dataList.get(position);

		flagholder.sendView.setText(getSenderName(info));

		setContent(flagholder, info);
		setTimeView(position, flagholder);
		setIconClickListener(position, flagholder);
		setHeadIcon(info, flagholder);
		Log.d("DJCDDD", "exist getLayoutID="
				+ (flagholder.bLeft ? "left" : "right"));
	}

	private String getSenderName(NewChatInfo info) {
		String name = senderMap.get(info.getSender_id());
		if (name == null) {
			if (NewChatInfo.TEACHER_TYPE.equals(info.getSender_type())) {
				name = getTeacherName(info.getSender_id());
			} else {
				name = getParentName(info.getSender_id());
			}
			senderMap.put(info.getSender_id(), name);
		}

		// 没有获取到名字，表示发送者还没有获取过资料
		if (DEFAULT_TEACHER_NAME.equals(name)
				|| DEFAULT_PARENT_NAME.equals(name)) {
			SenderInfo senderInfo = new SenderInfo();
			senderInfo.setSenderID(info.getSender_id());
			senderInfo.setSenderType(info.getSender_type());
			getTeacherInfoJob.addTask(info.getSender_id(), senderInfo);
		}

		return name;
	}

	private String getParentName(String senderid) {
		String name = DEFAULT_PARENT_NAME;
		try {
			ParentInfo parent = DataMgr.getInstance().getParentByID(senderid);
			if (parent != null) {
				if (DataUtils.getAccount().equals(parent.getPhone())) {
					name = SELF_NAME;
				} else {
					name = parent.getName();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	private String getTeacherName(String senderid) {
		String name = DEFAULT_TEACHER_NAME;
		Teacher teacher = DataMgr.getInstance().getTeacherByID(senderid);

		if (teacher != null) {
			name = teacher.getName();
		}
		return name;
	}

	private void setHeadIcon(NewChatInfo info, FlagHolder flagholder) {
		Bitmap bitmap = null;
		IconInfo iconInfo = getIconInfo(info);

		bitmap = getLocalIcon(iconInfo, ConstantValue.HEAD_ICON_WIDTH,
				ConstantValue.HEAD_ICON_HEIGHT);

		if (bitmap != null) {
			Utils.setImg(flagholder.headiconView, bitmap);
		} else {
			downloadImgeJob.addTask(iconInfo.getNetPath(),
					iconInfo.getLocalPath(), ConstantValue.HEAD_ICON_WIDTH,
					ConstantValue.HEAD_ICON_HEIGHT);
			flagholder.headiconView
					.setImageResource(R.drawable.default_small_icon);
		}
	}

	private IconInfo getIconInfo(NewChatInfo info) {
		IconInfo iconInfo = new IconInfo();
		try {
			if (NewChatInfo.PARENT_TYPE.equals(info.getSender_type())) {
				// 家长有头像就用家长头像，否则就用小孩头像
				// ParentInfo parent = DataMgr.getInstance().getParentByID(
				// info.getSender_id());
				// if (parent != null &&
				// !TextUtils.isEmpty(parent.getPortrait())) {
				// iconInfo.setLocalPath(ParentInfo
				// .getParentLocalIconPath(info.getSender_id()));
				// iconInfo.setNetPath(parent.getPortrait());
				// } else {
				// ChildInfo childByID = DataMgr.getInstance().getChildByID(
				// info.getChild_id());
				// iconInfo.setLocalPath(childByID.getLocal_url());
				// iconInfo.setNetPath(childByID.getServer_url());
				// }

				// 只用小孩头像
				ChildInfo childByID = DataMgr.getInstance().getChildByID(
						info.getChild_id());
				iconInfo.setLocalPath(childByID.getLocal_url());
				iconInfo.setNetPath(childByID.getServer_url());

			} else {
				Teacher teacherByID = DataMgr.getInstance().getTeacherByID(
						info.getSender_id());
				iconInfo.setLocalPath(teacherByID.getLocalIconPath());
				iconInfo.setNetPath(teacherByID.getHead_icon());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return iconInfo;
	}

	private Bitmap getLocalIcon(IconInfo iconInfo, int limitWidth,
			int limitHeight) {
		Bitmap loacalBitmap = null;
		if (TextUtils.isEmpty(iconInfo.getNetPath())) {
			return null;
		}

		loacalBitmap = lruCache.get(iconInfo.getLocalPath());

		if (loacalBitmap == null) {
			loacalBitmap = Utils.getLoacalBitmap(iconInfo.getLocalPath(),
					limitHeight, limitWidth);

			if (loacalBitmap != null) {
				lruCache.put(iconInfo.getLocalPath(), loacalBitmap);
			}
		}
		return loacalBitmap;
	}

	private void setContent(FlagHolder flagholder, final NewChatInfo info) {
		if (TextUtils.isEmpty(info.getLocalUrl())) {
			flagholder.bodyView.setVisibility(View.VISIBLE);
			flagholder.bodyView.setText(info.getContent());
			flagholder.mediaView.setVisibility(View.GONE);
			flagholder.durationView.setVisibility(View.GONE);
		} else {
			flagholder.bodyView.setVisibility(View.GONE);
			flagholder.mediaView.setVisibility(View.VISIBLE);
			setChatIcon(flagholder.mediaView, info, flagholder.durationView);
		}
	}

	private void setChatIcon(ImageView view, NewChatInfo info, TextView textView) {
		if (JSONConstant.IMAGE_TYPE.equals(info.getMedia_type())) {
			textView.setVisibility(View.GONE);
			IconInfo iconinfo = new IconInfo();
			iconinfo.setLocalPath(info.getLocalUrl());
			iconinfo.setNetPath(info.getMedia_url());
			Bitmap loacalBitmap = getLocalIcon(iconinfo, 160, 160);

			if (loacalBitmap != null) {
				Drawable drawable = new BitmapDrawable(loacalBitmap);
				view.setBackgroundDrawable(drawable);
			} else {
				view.setBackgroundResource(R.drawable.default_small_icon);
				downloadIcon(view, info);
			}
		} else {
			try {
				File file = new File(info.getLocalUrl());
				if (file.exists()) {
					textView.setText(MediaMgr.getDuration(context,
							Uri.fromFile(file))
							+ "``");
					textView.setVisibility(View.VISIBLE);
				} else {
					textView.setVisibility(View.GONE);
					// 实际下载的是音频文件
					downloadIcon(view, info);
				}
			} catch (Exception e) {
			}

			if (info.isSendByTeacher()) {
				// 显示在屏幕左边
				view.setBackgroundResource(R.drawable.playing_3);
			} else {
				// 显示在屏幕右边
				view.setBackgroundResource(R.drawable.playing_3_r);
			}
		}
	}

	public void releaseCache() {
		lruCache.evictAll();
		senderMap.clear();
		if (animHelper != null) {
			animHelper.stopAnimation();
		}
	}

	private void downloadIcon(ImageView view, NewChatInfo info) {
		String savePath = info.getLocalUrl();
		String dir = Utils.getDir(savePath);
		Utils.makeDirs(dir);
		Log.d("DDD downloadIcon", "savePath =" + savePath);
		if (JSONConstant.IMAGE_TYPE.equals(info.getMedia_type())) {
			downloadImgeJob.addTask(info.getMedia_url(), savePath);
		} else {
			downloadImgeJob.addTaskExt(info.getMedia_url(), savePath);
		}
	}

	private void setTimeView(final int position, FlagHolder flagholder) {
		final NewChatInfo info = dataList.get(position);
		final NewChatInfo preinfo = getPreChatinfo(position);
		if (preinfo == null
				|| (info.getTimestamp() - preinfo.getTimestamp()) > MIN_TIME_LIMIT) {
			flagholder.timestampView.setVisibility(View.VISIBLE);
			flagholder.timestampView.setText(info.getFormattedTime());
		} else {
			flagholder.timestampView.setVisibility(View.GONE);
		}
	}

	private NewChatInfo getPreChatinfo(int position) {
		int prePosition = position - 1;
		if (prePosition >= 0) {
			return dataList.get(prePosition);
		}
		return null;
	}

	private void handleMediaClick(final int position,
			final FlagHolder flagholder) {
		NewChatInfo newChatInfo = getItem(position);
		String iconurl = newChatInfo.getLocalUrl();
		File file = new File(iconurl);
		if (file.exists()) {
			String media_type = newChatInfo.getMedia_type();
			Log.d("DDD", "media_type =" + media_type);
			if (JSONConstant.IMAGE_TYPE.equals(media_type)) {
				// 文件存在才显示大图
				startToShowIconActivity(iconurl);
			} else if (JSONConstant.VOICE_TYPE.equals(media_type)) {
				Log.d("DDD", "playMediaNow");
				// handleVoiceBtnClick(flagholder, file);
				handleVoiceBtnClick(flagholder, file);
			}
		}
	}

	private void setIconClickListener(final int position,
			final FlagHolder flagholder) {
		flagholder.contentlayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleMediaClick(position, flagholder);
			}
		});

		flagholder.mediaView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleMediaClick(position, flagholder);
			}
		});

		flagholder.contentlayout
				.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						// showDlg(position);
						showDlgEx(position);
						return false;
					}
				});

		flagholder.mediaView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// showDlg(position);
				showDlgEx(position);
				return false;
			}
		});
	}

	private void handleVoiceBtnClick(final FlagHolder flagholder, File file) {
		if (animHelper == null) {
			animHelper = new AnimHelper(flagholder.mediaView, flagholder.bLeft);
		} else if (!animHelper.isSameView(flagholder.mediaView)) {
			// 如果是另外一个音频图标被点击，先停止当前正在播放的动画和音频,再创建新的animHelper
			animHelper.stopAnimation();
			MediaMgr.close();
			animHelper = new AnimHelper(flagholder.mediaView, flagholder.bLeft);
		}

		boolean isPlaying = MediaMgr.isPlaying();
		Log.d("DDD", "playMediaNow isplaying=" + isPlaying);
		if (isPlaying) {
			Log.d("DDD", "stopAnimation");
			animHelper.stopAnimation();
			MediaMgr.close();
		} else {
			Log.d("DDD", "startAnimation");
			animHelper.startAnimation();
			MediaMgr.playMediaNow(context, Uri.fromFile(file),
					new MediaPlayCompleteListener() {
						@Override
						public void onComplete() {
							if (animHelper != null) {
								animHelper.stopAnimation();
							}
						}
					});
		}
	}

	private void showDlgEx(final int pos) {
		NewChatInfo newChatInfo = getItem(pos);
		longClickDlg = new LongClickDlg(context);

		longClickDlg.setTextContent(newChatInfo.getContent());

		if (!TextUtils.isEmpty(newChatInfo.getLocalUrl())
				&& new File(newChatInfo.getLocalUrl()).exists()
				&& JSONConstant.IMAGE_TYPE.equals(newChatInfo.getMedia_type())) {
			longClickDlg.setImageUrl(newChatInfo.getLocalUrl());
		}

		if (DataMgr.getInstance().getSelfInfoByPhone().getParent_id()
				.equals(newChatInfo.getSender_id())) {
			longClickDlg
					.setOnDeleteBtnClickListener(new OnDeleteBtnClickListener() {

						@Override
						public void onDeleteClicked() {
							NewChatInfo item = getItem(pos);
							DeleteChatJob deleteChatJob = new DeleteChatJob(
									handler, item.getChat_id(), DataMgr
											.getInstance().getSelectedChild()
											.getServer_id());
							deletePos = pos;
							longClickDlg.getDeleteChatListener()
									.onDeleteBegain();
							deleteChatJob.execute();
						}
					});
		}

		longClickDlg.showDlg();
	}

	protected void startToShowIconActivity(String iconUrl) {
		Intent intent = new Intent(context, ShowIconActivity.class);
		intent.putExtra(ConstantValue.LOCAL_URL, iconUrl);
		context.startActivity(intent);
	}

	private class FlagHolder {
		public TextView durationView;
		public TextView sendView;
		public TextView bodyView;
		public TextView timestampView;
		public ImageView headiconView;
		public ImageView mediaView;
		public RelativeLayout contentlayout;
		public boolean bLeft;
	}

	public interface DeleteChatListener {
		public void onDeleteBegain();

		public void onDeleteSuccess();

		public void onDeleteFail();
	}

	public static class InnerHandler extends Handler {

	}

	public interface MediaPlayCompleteListener {
		public void onComplete();
	}
}