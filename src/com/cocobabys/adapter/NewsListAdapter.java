package com.cocobabys.adapter;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cocobabys.R;
import com.cocobabys.adapter.DonwloadModule.DownloadListener;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.News;
import com.cocobabys.dbmgr.info.ReceiptInfo;
import com.cocobabys.jobs.GetReceiptStateJob;
import com.cocobabys.threadpool.JobManager;
import com.cocobabys.utils.ImageDownloader;
import com.cocobabys.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NewsListAdapter extends BaseAdapter {
	private final Context context;
	private List<News> newsList;
	private static Map<String, SoftReference<Bitmap>> softMap = new HashMap<String, SoftReference<Bitmap>>();
	private DonwloadModule donwloadModule;
	private Handler handler;
	private Map<Integer, FeedbackState> map = new ConcurrentHashMap<Integer, NewsListAdapter.FeedbackState>();
	private JobManager jobManager;
	private boolean closed = false;
	private final int PADDING = 10;

	public void setLocationInfoList(List<News> list) {
		this.newsList = list;
	}

	public NewsListAdapter(Context activityContext, List<News> list) {
		this.context = activityContext;
		newsList = list;
		donwloadModule = new DonwloadModule();
		donwloadModule.setDownloadListener(new DownloadListener() {
			@Override
			public void downloadSuccess() {
				notifyDataSetChanged();
			}
		});
		initHandler();
		jobManager = new JobManager();
		jobManager.start();
	}

	private void initHandler() {
		handler = new InnerHandler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (closed) {
					Log.d("", "closed do nothing!");
					return;
				}

				try {
					switch (msg.what) {
					case EventType.POST_RECEIPT_FAIL:
						Utils.makeToast(context,
								R.string.send_news_feedback_fail);
						map.put(msg.arg1, FeedbackState.NEED_FEEDBACK);
						NewsListAdapter.this.notifyDataSetChanged();
						break;
					case EventType.POST_RECEIPT_SUCCESS:
						map.put(msg.arg1, FeedbackState.ALREADY_FEEDBACK);
						NewsListAdapter.this.notifyDataSetChanged();
						Log.d("", "POST_RECEIPT_SUCCESS id=" + msg.arg1);
						break;
					case EventType.GET_RECEIPT_SUCCESS:
						NewsListAdapter.this.notifyDataSetChanged();
						break;

					default:
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
	};

	public void clear() {
		newsList.clear();
		map.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return newsList.size();
	}

	@Override
	public Object getItem(int position) {
		return newsList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			FlagHolder flagholder = this.new FlagHolder();
			convertView = LayoutInflater.from(this.context).inflate(
					R.layout.new_notice_item, null);
			flagholder.tagView = (TextView) convertView
					.findViewById(R.id.tagView);
			flagholder.titleView = (TextView) convertView
					.findViewById(R.id.titleView);
			flagholder.bodyView = (TextView) convertView
					.findViewById(R.id.bodyView);
			flagholder.timestampView = (TextView) convertView
					.findViewById(R.id.timeStampView);
			flagholder.iconView = (ImageView) convertView
					.findViewById(R.id.iconView);
			flagholder.fromview = (TextView) convertView
					.findViewById(R.id.fromview);
			flagholder.feedBackView = (TextView) convertView
					.findViewById(R.id.feedback);
			setDataToViews(position, flagholder);

			convertView.setTag(flagholder);
		} else {
			FlagHolder flagholder = (FlagHolder) convertView.getTag();
			if (flagholder != null) {
				setDataToViews(position, flagholder);
			}
		}

		return convertView;
	}

	private void setDataToViews(final int position, FlagHolder flagholder) {
		final News info = newsList.get(position);
		setTagView(flagholder, info);
		flagholder.titleView.setText(info.getTitle());
		flagholder.bodyView.setText(info.getContent());
		flagholder.timestampView.setText(Utils.formatChineseTime(info
				.getTimestamp()));
		flagholder.fromview.setText(info.getFrom());
		setFeedBackBtn(flagholder, info);
		setIcon(flagholder.iconView, info);
	}

	private void setTagView(FlagHolder flagholder, News info) {
		String tags = info.getTags();

		// tags不为空则显示标签
		if (!TextUtils.isEmpty(tags)
				&& tags.contains(ConstantValue.TAGS_HOMEWORK)) {
			setTextviewProp(flagholder.tagView, R.drawable.homework_back,
					R.string.homework, true);
			return;
		}

		// classid对于0表示范围是学校
		if (info.getClass_id() == 0) {
			setTextviewProp(flagholder.tagView, R.drawable.school_notice_back,
					R.string.pnotice, true);
		} else {
			setTextviewProp(flagholder.tagView, R.drawable.class_notice_back,
					R.string.class_notice, true);
		}
	}

	private void setFeedBackBtn(final FlagHolder flagholder, final News info) {
		int need_receipt = info.getNeed_receipt();
		if (need_receipt != 0) {
			flagholder.feedBackView.setVisibility(View.VISIBLE);
			final int news_server_id = info.getNews_server_id();
			FeedbackState feedbackState = getFeedbackState(news_server_id);

			switch (feedbackState) {
			case ALREADY_FEEDBACK:
				setTextviewProp(flagholder.feedBackView,
						R.drawable.already_feedback,
						R.string.news_already_feedback, false);
				break;
			case NEED_FEEDBACK:
				setTextviewProp(flagholder.feedBackView,
						R.drawable.need_feedback, R.string.news_not_feedback,
						true);

				// 去掉在列表界面，点击反馈的功能
				// flagholder.feedBackView
				// .setOnClickListener(new OnClickListener() {
				// @Override
				// public void onClick(View v) {
				// map.put(news_server_id,
				// FeedbackState.SENDING_FEEDBACK);
				// flagholder.feedBackView
				// .setText(R.string.send_news_feedback);
				// // send feedback
				// SendReceiptJob expJob = new SendReceiptJob(
				// handler, news_server_id);
				// expJob.execute();
				// }
				// });
				break;
			case SENDING_FEEDBACK:
				setTextviewProp(flagholder.feedBackView,
						R.drawable.already_feedback,
						R.string.send_news_feedback, false);
				break;

			default:
				break;
			}

		} else {
			flagholder.feedBackView.setVisibility(View.GONE);
		}
	}

	private FeedbackState getFeedbackState(final int news_server_id) {
		ReceiptInfo receiptInfo = DataMgr.getInstance().getReceiptInfo(
				news_server_id);

		FeedbackState feedbackState = map.get(news_server_id);

		Log.d("", "setFeedBackBtn  id=" + news_server_id + " feedbackState="
				+ feedbackState);

		if (feedbackState == null) {

			if (receiptInfo != null && receiptInfo.getReceipt_state() != 0) {
				feedbackState = FeedbackState.ALREADY_FEEDBACK;
			} else {
				feedbackState = FeedbackState.NEED_FEEDBACK;
			}

			if (receiptInfo == null) {
				Log.d("", "addJob  news_server_id=" + news_server_id);
				jobManager.addJob(String.valueOf(news_server_id),
						new GetReceiptStateJob(handler, news_server_id));
			} else {
				map.put(news_server_id, feedbackState);
			}
		}
		return feedbackState;
	}

	private void setIcon(ImageView view, News info) {
		if (TextUtils.isEmpty(info.getIcon_url())) {
			view.setVisibility(View.GONE);
		} else {
			String localUrl = info.getNewsLocalMiniIconPath();
			Bitmap loacalBitmap = getLocalBmp(localUrl);
			if (loacalBitmap != null) {
				Log.d("DJC", "setIcon url =" + localUrl);
				Utils.setImg(view, loacalBitmap);
			} else {
				donwloadModule.addTask(info.getIcon_url(),
						info.getNewsLocalMiniIconPath(),
						ConstantValue.MINI_PIC_SIZE,
						ConstantValue.MINI_PIC_SIZE);
				view.setImageResource(R.drawable.default_icon);

			}
			view.setVisibility(View.VISIBLE);
		}
	}

	private Bitmap getLocalBmp(String localUrl) {
		Bitmap loacalBitmap = null;
		if (softMap.containsKey(localUrl)) {
			loacalBitmap = softMap.get(localUrl).get();
		}

		if (loacalBitmap == null) {
			loacalBitmap = Utils.getLoacalBitmap(localUrl,
					ImageDownloader.getMaxPixWithDensity(160, 160));
			if (loacalBitmap != null) {
				Log.d("DJC", "getLoacalBitmap url =" + localUrl);
				softMap.put(localUrl, new SoftReference<Bitmap>(loacalBitmap));
			}
		}
		return loacalBitmap;
	}

	// 在通知详情里面点击回执后，这里要更新回执状态
	public void updateFeedbackState(int newid) {
		for (News news : newsList) {
			if (news.getNews_server_id() == newid) {
				Log.d("", "notifyDataSetChanged newid" + newid);
				map.put(news.getNews_server_id(),
						FeedbackState.ALREADY_FEEDBACK);
				notifyDataSetChanged();
				break;
			}
		}
	}

	private void setTextviewProp(TextView view, int backResID, int textID,
			boolean clickable) {
		view.setText(textID);
		view.setBackgroundResource(backResID);
		// 默认padding是10dp
		view.setPadding(PADDING, PADDING, PADDING, PADDING);
		view.setClickable(clickable);
		if (!clickable) {
			view.setTextColor(Utils.getResColor(R.color.gray));
		} else {
			view.setTextColor(Utils.getResColor(R.color.white));
		}
	}

	public void close() {
		clear();
		donwloadModule.close();
		jobManager.stopTask();
		closed = true;
	}

	private class FlagHolder {
		public TextView tagView;
		public TextView titleView;
		public TextView feedBackView;
		public TextView bodyView;
		public TextView timestampView;
		public TextView fromview;
		public ImageView iconView;
	}

	public enum FeedbackState {
		NEED_FEEDBACK, ALREADY_FEEDBACK, SENDING_FEEDBACK;
	}

	public static class InnerHandler extends Handler {

	}
}