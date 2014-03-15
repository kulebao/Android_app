package com.djc.logintest.adapter;

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

import com.djc.logintest.R;
import com.djc.logintest.activities.ShowIconActivity;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.ChatInfo;
import com.djc.logintest.dlgmgr.DlgMgr;
import com.djc.logintest.taskmgr.GlobleDownloadImgeTask;
import com.djc.logintest.utils.ImageDownloader;
import com.djc.logintest.utils.Utils;

public class ChatListAdapter extends BaseAdapter {
	// 最小显示时间间隔为2分钟
	private static final long MIN_TIME_LIMIT = 2 * 60 * 1000L;
	// private static Map<String, Bitmap> map = new HashMap<String, Bitmap>();
	private static Map<String, SoftReference<Bitmap>> softMap = new HashMap<String, SoftReference<Bitmap>>();
	private final Context context;
	private List<ChatInfo> dataList;
	private GlobleDownloadImgeTask task;
	private Handler handler;

	public ChatListAdapter(Context activityContext, List<ChatInfo> list,
			GlobleDownloadImgeTask task) {
		this.context = activityContext;
		this.dataList = list;
		this.task = task;
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == EventType.SUCCESS) {
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// if (convertView == null) {
		ChatInfo info = (ChatInfo) getItem(position);
		convertView = LayoutInflater.from(this.context).inflate(
				info.getLayoutID(), null);

		Log.d("DJCDDD", "null getLayoutID="
				+ ((info.getLayoutID() == R.layout.chat_item_left) ? "left"
						: "right"));
		Log.d("DJCDDD", "null sender=" + info.getSender());

		FlagHolder flagholder = new FlagHolder();

		flagholder.bLeft = (info.getLayoutID() == R.layout.chat_item_left);
		flagholder.sendView = (TextView) convertView.findViewById(R.id.sender);
		flagholder.bodyView = (TextView) convertView.findViewById(R.id.content);
		flagholder.timestampView = (TextView) convertView
				.findViewById(R.id.timestamp);
		flagholder.resendView = (ImageView) convertView
				.findViewById(R.id.resend);
		flagholder.headiconView = (ImageView) convertView
				.findViewById(R.id.headicon);
		flagholder.chaticonView = (ImageView) convertView
				.findViewById(R.id.chat_icon);
		setDataToViews(position, flagholder);
		convertView.setTag(flagholder);
		convertView.setId(position);
		// } else {
		// FlagHolder flagholder = (FlagHolder) convertView.getTag();
		// if (flagholder != null) {
		// setDataToViews(position, flagholder);
		// }
		//
		// }
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
		Bitmap bitmap = getHeadIcon(info);
		if (bitmap != null) {
			Utils.setImg(flagholder.headiconView, bitmap);
		} else {
			flagholder.headiconView.setImageResource(R.drawable.chat_head_icon);
		}
	}

	private Bitmap getHeadIcon(ChatInfo info) {
		Bitmap loacalBitmap = null;
		if (info.isSendBySelf()
				&& !TextUtils.isEmpty(DataMgr.getInstance().getSelectedChild()
						.getLocal_url())) {
			loacalBitmap = Utils.getLoacalBitmap(DataMgr.getInstance()
					.getSelectedChild().getLocal_url());
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

		Bitmap loacalBitmap = getLocalBmp(localUrl);

		if (loacalBitmap != null) {
			Log.d("DJC", "setIcon url =" + localUrl);
			Utils.setImg(view, loacalBitmap);
		} else {
			downloadIcon(view, info);
		}
	}

	private Bitmap getLocalBmp(String localUrl) {
		Bitmap loacalBitmap = null;
		if (softMap.containsKey(localUrl)) {
			loacalBitmap = softMap.get(localUrl).get();
		}

		if (loacalBitmap == null) {
			loacalBitmap = Utils.getLoacalBitmap(localUrl,
					ImageDownloader.getMaxPixWithDensity(100, 100));
			if (loacalBitmap != null) {
				Log.d("DJC", "getLoacalBitmap url =" + localUrl);
				softMap.put(localUrl, new SoftReference<Bitmap>(loacalBitmap));
			}
		}
		return loacalBitmap;
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