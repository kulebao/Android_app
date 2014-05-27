package com.cocobabys.adapter;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.activities.ShowIconActivity;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChatInfo;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.dlgmgr.DlgMgr;
import com.cocobabys.taskmgr.DownloadImgeJob;
import com.cocobabys.utils.ImageDownloader;
import com.cocobabys.utils.Utils;

public class ChatListAdapter extends BaseAdapter {
	// 最小显示时间间隔为2分钟
	private static final long MIN_TIME_LIMIT = 2 * 60 * 1000L;
	private static final int LEFT = 0;
	private static final int RIGHT = 1;

	// private static Map<String, Bitmap> map = new HashMap<String, Bitmap>();
	private static Map<String, SoftReference<Bitmap>> softMap = new HashMap<String, SoftReference<Bitmap>>();

	LruCache<String, Bitmap> lruCache;
	private final Context context;
	private List<ChatInfo> dataList;
	private DownloadImgeJob task;
	private Handler handler;

	public ChatListAdapter(Context activityContext, List<ChatInfo> list,
			DownloadImgeJob task) {
		this.context = activityContext;
		this.dataList = list;
		this.task = task;

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
				if (msg.what == EventType.DOWNLOAD_IMG_SUCCESS) {
					notifyDataSetChanged();
				}
			}

		};
		this.task.setHanlder(handler);
	}

	public void clear() {
		dataList.clear();
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
		ChatInfo chatInfo = dataList.get(position);
		if (TextUtils.isEmpty(chatInfo.getSender())) {
			return RIGHT;
		} else {
			return LEFT;
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
			ChatInfo info = (ChatInfo) getItem(position);

			if (getItemViewType(position) == LEFT) {
				convertView = LayoutInflater.from(this.context).inflate(
						R.layout.chat_item_left, null);
			} else {
				convertView = LayoutInflater.from(this.context).inflate(
						R.layout.chat_item_right, null);
			}

			flagholder = new FlagHolder();

			flagholder.bLeft = (info.getLayoutID() == R.layout.chat_item_left);
			flagholder.sendView = (TextView) convertView
					.findViewById(R.id.sender);
			flagholder.bodyView = (TextView) convertView
					.findViewById(R.id.content);
			flagholder.timestampView = (TextView) convertView
					.findViewById(R.id.timestamp);
			flagholder.resendView = (ImageView) convertView
					.findViewById(R.id.resend);
			flagholder.headiconView = (ImageView) convertView
					.findViewById(R.id.headicon);
			flagholder.chaticonView = (ImageView) convertView
					.findViewById(R.id.chat_icon);
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
		final ChatInfo info = dataList.get(position);
		flagholder.sendView.setText(info.getSender());
		setContent(flagholder, info);
		setTimeView(position, flagholder);
		setResendView(position, flagholder);
		setHeadIcon(info, flagholder);
		Log.d("DJCDDD", "exist getLayoutID="
				+ (flagholder.bLeft ? "left" : "right"));
		Log.d("DJCDDD", "exist sender=" + info.getSender());
	}

	private void setHeadIcon(ChatInfo info, FlagHolder flagholder) {
		Bitmap bitmap = null;
		if (info.isSendBySelf()) {
			bitmap = getLocalIcon(softMap, DataMgr.getInstance()
					.getSelectedChild().getLocal_url(), 50, 50);
		} else {
			String url = Teacher.getLocalIconPath(info.getPhone());
			bitmap = Utils.getLoacalBitmap(url,
					ImageDownloader.getMaxPixWithDensity(50, 50));
		}

		if (bitmap != null) {
			Utils.setImg(flagholder.headiconView, bitmap);
		} else {
			flagholder.headiconView.setImageResource(R.drawable.chat_head_icon);
		}
	}

	private Bitmap getLocalIcon(Map<String, SoftReference<Bitmap>> map,
			String local_url, int limitWidth, int limitHeight) {
		Bitmap loacalBitmap = null;
		if (TextUtils.isEmpty(local_url)) {
			return null;
		}

		loacalBitmap = lruCache.get(local_url);

		if (loacalBitmap == null) {
			loacalBitmap = Utils.getLoacalBitmap(local_url, ImageDownloader
					.getMaxPixWithDensity(limitWidth, limitHeight));

			if (loacalBitmap != null) {
				int height = loacalBitmap.getHeight();
				int width = loacalBitmap.getWidth();
				int roow = loacalBitmap.getRowBytes();
				Log.d("DJC", "getLoacalBitmap height =" + height + " width="
						+ width + " roow" + roow);
				Log.d("DJC", "getLoacalBitmap url =" + local_url);
				// map.put(local_url, new SoftReference<Bitmap>(loacalBitmap));
				lruCache.put(local_url, loacalBitmap);
			}
		}
		return loacalBitmap;
	}

	private void setContent(FlagHolder flagholder, final ChatInfo info) {
		if (TextUtils.isEmpty(info.getIcon_url())) {
			flagholder.bodyView.setVisibility(View.VISIBLE);
			flagholder.bodyView.setText(info.getContent());
			flagholder.chaticonView.setVisibility(View.GONE);
		} else {
			flagholder.bodyView.setVisibility(View.GONE);
			flagholder.chaticonView.setVisibility(View.VISIBLE);
			setIcon(flagholder.chaticonView, info);
		}
	}

	private void setIcon(ImageView view, ChatInfo info) {
		String localUrl = info.getLocalUrl();

		Bitmap loacalBitmap = getLocalIcon(softMap, localUrl, 70, 70);

		if (loacalBitmap != null) {
			Log.d("DJC", "setIcon url =" + localUrl);
			Utils.setImg(view, loacalBitmap);
		} else {
			downloadIcon(view, info);
		}
	}

	public void releaseCache() {
		lruCache.evictAll();
	}

	private void downloadIcon(ImageView view, ChatInfo info) {
		view.setImageResource(R.drawable.default_icon);
		// 本地路径按照学校id+家长手机号码+时间搓.jpg保存
		String savePath = info.getLocalUrl();
		String dir = Utils.getDir(savePath);
		Utils.makeDirs(dir);
		Log.d("DDD", "savePath =" + savePath);
		// if (!"".equals(info.getSender())) {
		// task.addTask(info.getIcon_url(), savePath);
		// }
		task.addTask(info.getIcon_url(), savePath);
	}

	private void setTimeView(final int position, FlagHolder flagholder) {
		final ChatInfo info = dataList.get(position);
		final ChatInfo preinfo = getPreChatinfo(position);
		if (info.getSend_result() == ChatInfo.SEND_FAIL) {
			flagholder.timestampView.setVisibility(View.GONE);
		} else if (preinfo == null
				|| preinfo.getSend_result() == ChatInfo.SEND_FAIL
				|| (info.getTimestamp() - preinfo.getTimestamp()) > MIN_TIME_LIMIT) {
			flagholder.timestampView.setVisibility(View.VISIBLE);
			flagholder.timestampView.setText(info.getFormattedTime());

		} else {
			flagholder.timestampView.setVisibility(View.GONE);
		}
	}

	private ChatInfo getPreChatinfo(int position) {
		int prePosition = position - 1;
		if (prePosition >= 0) {
			return dataList.get(prePosition);
		}
		return null;
	}

	private void setResendView(final int position, FlagHolder flagholder) {
		final ChatInfo info = dataList.get(position);
		flagholder.resendView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				DlgMgr.getListDialog(context, R.array.resend_items,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								Log.d("resend", "which =" + position);
							}
						}).create().show();

				return true;
			}
		});

		if (info.getSend_result() == ChatInfo.SEND_FAIL) {
			flagholder.resendView.setVisibility(View.VISIBLE);
		} else {
			flagholder.resendView.setVisibility(View.GONE);
		}

		flagholder.chaticonView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String iconurl = ((ChatInfo) getItem(position)).getLocalUrl();
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
		public ImageView resendView;
		public ImageView headiconView;
		public ImageView chaticonView;
		public boolean bLeft;
	}
}