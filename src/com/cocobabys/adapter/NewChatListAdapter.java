package com.cocobabys.adapter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.activities.ShowIconActivity;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.NewChatInfo;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.jobs.GetTeacherInfoJob;
import com.cocobabys.taskmgr.GlobleDownloadImgeTask;
import com.cocobabys.utils.ImageDownloader;
import com.cocobabys.utils.Utils;

public class NewChatListAdapter extends BaseAdapter {
	private static final String SELF_NAME = "我";
	private static final String DEFAULT_PARENT_NAME = "家长";
	// 最小显示时间间隔为2分钟
	private static final long MIN_TIME_LIMIT = 2 * 60 * 1000L;
	private static final int LEFT = 0;
	private static final int RIGHT = 1;

	LruCache<String, Bitmap> lruCache;
	private final Context context;
	private List<NewChatInfo> dataList;
	private GlobleDownloadImgeTask downloadImgeJob;
	private Handler handler;

	private Map<String, String> senderMap = new HashMap<String, String>();
	private GetTeacherInfoJob getTeacherInfoJob;
	private static final String ANONYMOUS_TEACHER_NAME = "匿名老师";

	public NewChatListAdapter(Context activityContext, List<NewChatInfo> list, GlobleDownloadImgeTask downloadImgeTask,
			GetTeacherInfoJob getTeacherInfoJob) {
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

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.SUCCESS:
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
	public Object getItem(int position) {
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
			NewChatInfo info = (NewChatInfo) getItem(position);

			if (getItemViewType(position) == LEFT) {
				convertView = LayoutInflater.from(this.context).inflate(R.layout.chat_item_left, null);
			} else {
				convertView = LayoutInflater.from(this.context).inflate(R.layout.chat_item_right, null);
			}

			flagholder = new FlagHolder();

			flagholder.bLeft = (info.getLayoutID() == R.layout.chat_item_left);
			flagholder.sendView = (TextView) convertView.findViewById(R.id.sender);
			flagholder.bodyView = (TextView) convertView.findViewById(R.id.content);
			flagholder.timestampView = (TextView) convertView.findViewById(R.id.timestamp);
			flagholder.headiconView = (ImageView) convertView.findViewById(R.id.headicon);
			flagholder.chaticonView = (ImageView) convertView.findViewById(R.id.chat_icon);
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
		Log.d("DJCDDD", "exist getLayoutID=" + (flagholder.bLeft ? "left" : "right"));
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

		if (ANONYMOUS_TEACHER_NAME.equals(name)) {
			// 获取老师信息
			getTeacherInfoJob.addTask(info.getSender_id(), info);
		}

		return name;
	}

	private String getParentName(String senderid) {
		String name = DEFAULT_PARENT_NAME;
		try {
			ParentInfo parent = DataMgr.getInstance().getParentByID(senderid);
			if(parent != null){
				if(Utils.getAccount().equals(parent.getPhone())){
					name = SELF_NAME;
				}else{
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

	private void setHeadIcon(NewChatInfo info, FlagHolder flagholder) {
		Bitmap bitmap = null;
		String headUrl = "";
		try {
			if (NewChatInfo.PARENT_TYPE.equals(info.getSender_type())) {
				headUrl = DataMgr.getInstance().getChildByID(info.getChild_id()).getLocal_url();
			} else {
				headUrl = DataMgr.getInstance().getTeacherByID(info.getSender_id()).getLocalIconPath();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		bitmap = getLocalIcon(headUrl, ConstantValue.HEAD_ICON_WIDTH, ConstantValue.HEAD_ICON_HEIGHT);

		if (bitmap != null) {
			Utils.setImg(flagholder.headiconView, bitmap);
		} else {
			flagholder.headiconView.setImageResource(R.drawable.default_small_icon);
		}
	}

	private Bitmap getLocalIcon(String local_url, int limitWidth, int limitHeight) {
		Bitmap loacalBitmap = null;
		if (TextUtils.isEmpty(local_url)) {
			return null;
		}

		loacalBitmap = lruCache.get(local_url);

		if (loacalBitmap == null) {
			loacalBitmap = Utils.getLoacalBitmap(local_url,
					ImageDownloader.getMaxPixWithDensity(limitWidth, limitHeight));

			if (loacalBitmap != null) {
				int height = loacalBitmap.getHeight();
				int width = loacalBitmap.getWidth();
				int roow = loacalBitmap.getRowBytes();
				Log.d("DJC", "getLoacalBitmap height =" + height + " width=" + width + " roow" + roow);
				Log.d("DJC", "getLoacalBitmap url =" + local_url);
				// map.put(local_url, new SoftReference<Bitmap>(loacalBitmap));
				lruCache.put(local_url, loacalBitmap);
			}
		}
		return loacalBitmap;
	}

	private void setContent(FlagHolder flagholder, final NewChatInfo info) {
		if (TextUtils.isEmpty(info.getLocalUrl())) {
			flagholder.bodyView.setVisibility(View.VISIBLE);
			flagholder.bodyView.setText(info.getContent());
			flagholder.chaticonView.setVisibility(View.GONE);
		} else {
			flagholder.bodyView.setVisibility(View.GONE);
			flagholder.chaticonView.setVisibility(View.VISIBLE);
			setIcon(flagholder.chaticonView, info);
		}
	}

	private void setIcon(ImageView view, NewChatInfo info) {
		String localUrl = info.getLocalUrl();

		Bitmap loacalBitmap = getLocalIcon(localUrl, 70, 70);

		if (loacalBitmap != null) {
			Log.d("DJC", "setIcon url =" + localUrl);
			Utils.setImg(view, loacalBitmap);
		} else {
			downloadIcon(view, info);
		}
	}

	public void releaseCache() {
		lruCache.evictAll();
		senderMap.clear();
	}

	private void downloadIcon(ImageView view, NewChatInfo info) {
		view.setImageResource(R.drawable.default_icon);
		String savePath = info.getLocalUrl();
		String dir = Utils.getDir(savePath);
		Utils.makeDirs(dir);
		Log.d("DDD downloadIcon", "savePath =" + savePath);
		downloadImgeJob.addTask(info.getMedia_url(), savePath);
	}

	private void setTimeView(final int position, FlagHolder flagholder) {
		final NewChatInfo info = dataList.get(position);
		final NewChatInfo preinfo = getPreChatinfo(position);
		if (preinfo == null || (info.getTimestamp() - preinfo.getTimestamp()) > MIN_TIME_LIMIT) {
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

	private void setIconClickListener(final int position, FlagHolder flagholder) {
		flagholder.chaticonView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String iconurl = ((NewChatInfo) getItem(position)).getLocalUrl();
				// 文件存在才显示大图
				if (new File(iconurl).exists()) {
					startToShowIconActivity(iconurl);
				}
			}
		});
	}

	protected void startToShowIconActivity(String iconUrl) {
		Intent intent = new Intent(context, ShowIconActivity.class);
		intent.putExtra(ConstantValue.LOCAL_URL, iconUrl);
		context.startActivity(intent);
	}

	private class FlagHolder {
		public TextView sendView;
		public TextView bodyView;
		public TextView timestampView;
		public ImageView headiconView;
		public ImageView chaticonView;
		public boolean bLeft;
	}
}