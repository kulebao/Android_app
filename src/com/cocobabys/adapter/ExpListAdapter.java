package com.cocobabys.adapter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.bean.SenderInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.jobs.GetSenderInfoJob;
import com.cocobabys.taskmgr.DownloadImgeJob;
import com.cocobabys.utils.ImageDownloader;
import com.cocobabys.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ExpListAdapter extends BaseAdapter {
	private static final String SELF_NAME = "我";
	private static final String DEFAULT_PARENT_NAME = "家长";

	LruCache<String, Bitmap> lruCache;
	private final Context context;
	private List<ExpInfo> dataList;
	private DownloadImgeJob downloadImgeJob;
	private Handler handler;

	private Map<String, String> senderMap = new HashMap<String, String>();
	private GetSenderInfoJob getTeacherInfoJob;
	private ImageLoader imageLoader;
	private static final String ANONYMOUS_TEACHER_NAME = "匿名老师";

	public ExpListAdapter(Context activityContext, List<ExpInfo> list,
			DownloadImgeJob downloadImgeTask,
			GetSenderInfoJob getTeacherInfoJob, ImageLoader imageLoader) {
		this.context = activityContext;
		this.dataList = list;
		this.downloadImgeJob = downloadImgeTask;
		this.getTeacherInfoJob = getTeacherInfoJob;
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
				case EventType.DOWNLOAD_IMG_SUCCESS:
					notifyDataSetChanged();
					break;
				case EventType.GET_SENDER_SUCCESS:
					String senderid = (String) msg.obj;
					senderMap.put(senderid, getTeacherName(senderid));
					notifyDataSetChanged();
					break;

				default:
					break;
				}
			}

		};
		this.downloadImgeJob.setHanlder(handler);
		this.getTeacherInfoJob.setHanlder(handler);
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
			flagholder.gridview = (GridView) convertView
					.findViewById(R.id.gridview);
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
		setIcon(flagholder, info);
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
			getTeacherInfoJob.addTask(info.getSender_id(), senderInfo);
		}

		return name;
	}

	private String getParentName(String senderid) {
		String name = DEFAULT_PARENT_NAME;
		try {
			ParentInfo parent = DataMgr.getInstance().getParentByID(senderid);
			if (parent != null) {
				if (Utils.getAccount().equals(parent.getPhone())) {
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
		String headUrl = "";
		try {
			if (ExpInfo.PARENT_TYPE.equals(info.getSender_type())) {
				headUrl = DataMgr.getInstance()
						.getChildByID(info.getChild_id()).getLocal_url();
			} else {
				headUrl = DataMgr.getInstance()
						.getTeacherByID(info.getSender_id()).getLocalIconPath();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		bitmap = getLocalIcon(headUrl, ConstantValue.HEAD_ICON_WIDTH,
				ConstantValue.HEAD_ICON_HEIGHT);

		if (bitmap != null) {
			Utils.setImg(flagholder.headiconView, bitmap);
		} else {
			flagholder.headiconView
					.setImageResource(R.drawable.default_small_icon);
		}
	}

	private Bitmap getLocalIcon(String local_url, int limitWidth,
			int limitHeight) {
		Bitmap loacalBitmap = null;
		if (TextUtils.isEmpty(local_url)) {
			return null;
		}

		loacalBitmap = lruCache.get(local_url);

		if (loacalBitmap == null) {
			loacalBitmap = Utils.getLoacalBitmap(local_url, ImageDownloader
					.getMaxPixWithDensity(limitWidth, limitHeight));

			if (loacalBitmap != null) {
				lruCache.put(local_url, loacalBitmap);
			}
		}
		return loacalBitmap;
	}

	private void setContent(FlagHolder flagholder, final ExpInfo info) {
		flagholder.contentView.setText(info.getContent());
	}

	private void setIcon(FlagHolder flagholder, ExpInfo info) {
		List<String> localUrls = info.getLocalUrls();
		if (localUrls.isEmpty()) {
			flagholder.gridview.setVisibility(View.GONE);
		} else {
			addToDownloadTask(info);

			SimpleGridViewAdapter adapter = new SimpleGridViewAdapter(context,
					imageLoader, localUrls);
			flagholder.gridview.setVisibility(View.VISIBLE);
			flagholder.gridview.setAdapter(adapter);
		}

	}

	private void addToDownloadTask(ExpInfo info) {
		List<String> serverUrls = info.getServerUrls();
		for (String serverUrl : serverUrls) {
			String localUrl = info.serverUrlToLocalUrl(serverUrl);
			if (!new File(localUrl).exists()) {
				downloadImgeJob.addTask(serverUrl, localUrl, 50f, 50f);
			}
		}
	}

	public void releaseCache() {
		lruCache.evictAll();
		senderMap.clear();
		imageLoader.clearMemoryCache();
		imageLoader.clearDiscCache();
	}

	private void downloadIcon(ImageView view, ExpInfo info) {
	}

	public void addAll(List<ExpInfo> list) {
		dataList.clear();
		dataList.addAll(list);
		notifyDataSetChanged();
	}

	private class FlagHolder {
		public TextView nameView;
		public TextView contentView;
		public TextView timestampView;
		public ImageView headiconView;
		public GridView gridview;
	}
}