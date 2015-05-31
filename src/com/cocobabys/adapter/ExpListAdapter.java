package com.cocobabys.adapter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.activities.ShowVideoActivity;
import com.cocobabys.bean.IconInfo;
import com.cocobabys.bean.SenderInfo;
import com.cocobabys.bean.ShareInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.NoticeAction;
import com.cocobabys.customview.LongClickDlg;
import com.cocobabys.customview.LongClickDlg.OnDeleteBtnClickListener;
import com.cocobabys.customview.MyGridView;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.jobs.DeleteExpJob;
import com.cocobabys.jobs.GetSenderInfoJob;
import com.cocobabys.taskmgr.DownloadImgeJob;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.ImageDownloader;
import com.cocobabys.utils.ImageUtils;
import com.cocobabys.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ExpListAdapter extends BaseAdapter {
	private static final String SELF_NAME = "我";
	private static final String DEFAULT_PARENT_NAME = "家长";

	private LruCache<String, Bitmap> lruCache;
	private final Context context;
	private List<ExpInfo> dataList;
	private DownloadImgeJob downloadImgeJob;
	private Handler handler;

	private Map<String, String> senderMap = new HashMap<String, String>();
	private GetSenderInfoJob getSenderInfoJob;
	private ImageLoader imageLoader;
	private LongClickDlg longClickDlg;
	private static final String ANONYMOUS_TEACHER_NAME = "匿名老师";
	private int deletePos = -1;

	public ExpListAdapter(Context activityContext, List<ExpInfo> list,
			DownloadImgeJob downloadImgeTask,
			GetSenderInfoJob getTeacherInfoJob, ImageLoader imageLoader) {
		this.context = activityContext;
		this.dataList = list;
		this.downloadImgeJob = downloadImgeTask;
		this.getSenderInfoJob = getTeacherInfoJob;
		this.imageLoader = imageLoader;

		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		lruCache = new LruCache<String, Bitmap>(maxMemory) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight() / 1024;
			}
		};

		handler = new Handler() {
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
				case EventType.DELETE_EXP_SUCCESS:
					handleDeleteSuccess();
					break;
				case EventType.DELETE_EXP_FAIL:
					longClickDlg.getDeleteChatListener().onDeleteFail();
					break;
				default:
					break;
				}
			}

		};
		this.downloadImgeJob.setHanlder(handler);
		this.getSenderInfoJob.setHanlder(handler);
	}

	private void handleDeleteSuccess() {
		if (deletePos != -1) {
			ExpInfo item = getItem(deletePos);
			DataMgr.getInstance().deleteExpInfoByID(item.getExp_id());
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
	public ExpInfo getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FlagHolder flagholder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(this.context).inflate(
					R.layout.exp_item, null);
			flagholder = new FlagHolder();
			flagholder.nameView = (TextView) convertView
					.findViewById(R.id.name);
			flagholder.contentView = (TextView) convertView
					.findViewById(R.id.content);
			flagholder.timestampView = (TextView) convertView
					.findViewById(R.id.time);
			flagholder.headiconView = (ImageView) convertView
					.findViewById(R.id.headicon);
			flagholder.gridview = (MyGridView) convertView
					.findViewById(R.id.gridview);
			flagholder.layout = (LinearLayout) convertView
					.findViewById(R.id.linearLayout);
			flagholder.videonail = (ImageView) convertView
					.findViewById(R.id.videonail);

			convertView.setTag(flagholder);
		} else {
			flagholder = (FlagHolder) convertView.getTag();
		}

		if (flagholder != null) {
			setDataToViews(position, flagholder);
		}

		return convertView;
	}

	private void setDataToViews(final int position, FlagHolder flagholder) {
		ExpInfo info = getItem(position);
		flagholder.nameView.setText(getSenderName(info));
		flagholder.timestampView.setText(info.getFormattedTime());

		setHeadIcon(flagholder, info);
		setContent(flagholder, info);

		if (JSONConstant.IMAGE_TYPE.equals(info.getMediumType())) {
			setIcon(flagholder, info);
		} else if (JSONConstant.VIDEO_TYPE.equals(info.getMediumType())) {
			Log.d("DDDE", "DDDE setVideoNail medium=" + info.getMedium()
					+ "  type=" + info.getMediumType());
			setVideoNail(flagholder, info);
		} else {
			flagholder.videonail.setVisibility(View.GONE);
			flagholder.gridview.setVisibility(View.GONE);
		}

		setOnLongClickListener(flagholder, position);
	}

	private void setVideoNail(final FlagHolder flagholder, final ExpInfo info) {
		flagholder.gridview.setVisibility(View.GONE);
		flagholder.videonail.setVisibility(View.VISIBLE);

		List<String> serverUrls = info.getServerUrls();
		final String nail = info.serverUrlToLocalUrl(serverUrls.get(0), true);
		Log.d("DDDE", "setVideoNail AAA =" + nail);
		if (new File(nail).exists()) {
			ImageSize minImageSize = new ImageSize(100, 100);

			imageLoader.loadImage(ImageUtils.wrapper(nail), minImageSize,
					new SimpleImageLoadingListener() {
						@SuppressWarnings("deprecation")
						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							Log.d("DDDE", "onLoadingComplete AAA =" + nail);
							flagholder.videonail.setBackgroundDrawable(new BitmapDrawable(
									context.getResources(), loadedImage));
							flagholder.videonail
									.setImageResource(R.drawable.pvideo);
							// notifyDataSetChanged();
						}

						@Override
						public void onLoadingStarted(String imageUri, View view) {
							super.onLoadingStarted(imageUri, view);
							setDefaultNail(flagholder);
						}

					});

		} else {
			setDefaultNail(flagholder);
		}

		flagholder.videonail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, ShowVideoActivity.class);
				intent.putExtra(ConstantValue.EXP_ID, info.getExp_id());
				context.startActivity(intent);
			}
		});
	}

	private void setDefaultNail(final FlagHolder flagholder) {
		flagholder.videonail.setBackgroundColor(context.getResources()
				.getColor(R.color.black));
		flagholder.videonail.setImageResource(R.drawable.pvideo);
	}

	private void setOnLongClickListener(FlagHolder flagholder,
			final int position) {
		// flagholder.layout.setOnLongClickListener(new OnLongClickListener(){
		// @Override
		// public boolean onLongClick(View v){
		// showDlgEx(position, 0);
		// return false;
		// }
		// });

		flagholder.contentView
				.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						showDlgEx(position, 0);
						return false;
					}
				});

		flagholder.gridview
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int pos, long id) {
						Log.d("", "DDDA onItemLongClick");
						showDlgEx(position, pos);
						return false;
					}
				});

		flagholder.videonail.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				ExpInfo info = getItem(position);
				// 视频已经下载到本地才允许分享
				if (info.isVideoFileExist()) {
					showDlgEx(position, 0);
				}
				return false;
			}
		});
	}

	private void showDlgEx(final int pos, int mediumIndex) {
		ExpInfo info = getItem(pos);
		longClickDlg = new LongClickDlg(context);

		ShareInfo shareInfo = getShareInfo(mediumIndex, info);

		longClickDlg.setInfo(shareInfo);

		// 只能删除自己发的
		if (DataMgr.getInstance().getSelfInfoByPhone().getParent_id()
				.equals(info.getSender_id())) {
			longClickDlg
					.setOnDeleteBtnClickListener(new OnDeleteBtnClickListener() {

						@Override
						public void onDeleteClicked() {
							ExpInfo item = getItem(pos);
							DeleteExpJob deleteExpJob = new DeleteExpJob(
									handler, item.getExp_id(), DataMgr
											.getInstance().getSelectedChild()
											.getServer_id());
							deletePos = pos;
							longClickDlg.getDeleteChatListener()
									.onDeleteBegain();
							deleteExpJob.execute();
						}
					});
		}

		longClickDlg.showDlg();
	}

	private ShareInfo getShareInfo(int mediumIndex, ExpInfo info) {
		List<String> localUrls = info.getLocalUrls(false);
		ShareInfo shareInfo = new ShareInfo();
		shareInfo.setContent(info.getContent());

		if (!localUrls.isEmpty()) {
			shareInfo.setLocalUrl(localUrls.get(mediumIndex));
		}

		shareInfo.setMediaType(info.getMediumType());
		return shareInfo;
	}

	private String getSenderName(ExpInfo info) {
		String name = senderMap.get(info.getSender_id());
		if (name == null) {
			if (ExpInfo.TEACHER_TYPE.equals(info.getSender_type())) {
				name = getTeacherName(info.getSender_id());
			} else {
				name = getParentName(info.getSender_id());
			}
			senderMap.put(info.getSender_id(), name);
		}

		if (ANONYMOUS_TEACHER_NAME.equals(name)) {
			// 获取老师信息
			SenderInfo senderInfo = new SenderInfo();
			senderInfo.setSenderID(info.getSender_id());
			senderInfo.setSenderType(info.getSender_type());
			getSenderInfoJob.addTask(info.getSender_id(), senderInfo);
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
		String name = ANONYMOUS_TEACHER_NAME;
		Teacher teacher = DataMgr.getInstance().getTeacherByID(senderid);

		if (teacher != null) {
			name = teacher.getName();
		}
		return name;
	}

	private void setHeadIcon(FlagHolder flagholder, ExpInfo info) {
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
			flagholder.headiconView.setImageResource(R.drawable.chat_head_icon);
		}
	}

	private IconInfo getIconInfo(ExpInfo info) {
		IconInfo iconInfo = new IconInfo();
		try {
			if (ExpInfo.PARENT_TYPE.equals(info.getSender_type())) {
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
					ImageDownloader.getMaxPixWithDensity(limitWidth,
							limitHeight));

			if (loacalBitmap != null) {
				lruCache.put(iconInfo.getLocalPath(), loacalBitmap);
			}
		}
		return loacalBitmap;
	}

	private void setContent(FlagHolder flagholder, final ExpInfo info) {
		if (TextUtils.isEmpty(info.getContent())) {
			// 避免空字符占用UI布局
			flagholder.contentView.setVisibility(View.GONE);
		} else {
			flagholder.contentView.setVisibility(View.VISIBLE);
		}
		flagholder.contentView.setText(info.getContent());
	}

	private void setIcon(FlagHolder flagholder, final ExpInfo info) {
		flagholder.videonail.setVisibility(View.GONE);

		final List<String> localUrls = info.getLocalUrls(true);
		if (localUrls.isEmpty()) {
			flagholder.gridview.setVisibility(View.GONE);
		} else {
			addToDownloadTask(info);

			SimpleGridViewAdapter adapter = new SimpleGridViewAdapter(context,
					imageLoader, localUrls);
			flagholder.gridview.setVisibility(View.VISIBLE);
			flagholder.gridview
					.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							if (checkFileExist(localUrls)) {
								startToSlideGalleryActivity(info.getExp_id(),
										position);
							}
						}
					});
			flagholder.gridview.setAdapter(adapter);
		}
	}

	private void startToSlideGalleryActivity(long id, int position) {
		Intent intent = new Intent(NoticeAction.ACTION_GALLERY_READ_ONLY);
		intent.putExtra(ConstantValue.EXP_ID, id);
		intent.putExtra(NoticeAction.GALLERY_POSITION, position);
		context.startActivity(intent);
	}

	// 查看是否所有小图都下载完毕，只有确认全部完毕才允许查看大图
	protected boolean checkFileExist(List<String> localUrls) {
		for (String path : localUrls) {
			if (!new File(path).exists()) {
				return false;
			}
		}

		return true;
	}

	private void addToDownloadTask(ExpInfo info) {
		List<String> serverUrls = info.getServerUrls();
		for (String serverUrl : serverUrls) {
			String localUrl = info.serverUrlToLocalUrl(serverUrl, true);
			if (!new File(localUrl).exists()) {
				downloadImgeJob.addTask(serverUrl, localUrl, 100f, 100f);
			}
		}
	}

	public void releaseCache() {
		lruCache.evictAll();
		senderMap.clear();
		// imageLoader.clearMemoryCache();
	}

	public void addAll(List<ExpInfo> list) {
		dataList.clear();
		dataList.addAll(list);
		notifyDataSetChanged();
	}

	private class FlagHolder {
		public ImageView videonail;
		public TextView nameView;
		public TextView contentView;
		public TextView timestampView;
		public ImageView headiconView;
		public GridView gridview;
		public LinearLayout layout;
	}
}