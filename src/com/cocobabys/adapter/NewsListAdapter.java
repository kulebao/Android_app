package com.cocobabys.adapter;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.dbmgr.info.News;
import com.cocobabys.utils.ImageDownloader;
import com.cocobabys.utils.Utils;

public class NewsListAdapter extends BaseAdapter {
	private final Context context;
	private List<News> newsList;
	private static Map<String, SoftReference<Bitmap>> softMap = new HashMap<String, SoftReference<Bitmap>>();

	public void setLocationInfoList(List<News> list) {
		this.newsList = list;
	}

	public NewsListAdapter(Context activityContext, List<News> list) {
		this.context = activityContext;
		newsList = list;
	}

	public void clear() {
		newsList.clear();
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
					R.layout.notice_item, null);
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
			setDataToViews(position, flagholder);

			convertView.setBackgroundResource(R.drawable.news_item);
			convertView.setTag(flagholder);
		} else {
			convertView.setBackgroundResource(R.drawable.news_item);
			FlagHolder flagholder = (FlagHolder) convertView.getTag();
			if (flagholder != null) {
				setDataToViews(position, flagholder);
			}
		}

		return convertView;
	}

	private void setDataToViews(final int position, FlagHolder flagholder) {
		final News info = newsList.get(position);
		flagholder.titleView.setText(info.getTitle());
		flagholder.bodyView.setText(info.getContent());
		flagholder.timestampView.setText(Utils.formatChineseTime(info
				.getTimestamp()));
		flagholder.fromview.setText(info.getFrom());
		setIcon(flagholder.iconView, info);
		// flagholder.deleteView.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Log.d("DDD pos", "position =" + position);
		// Utils.showTwoBtnResDlg(R.string.delete_notice_confirm, context,
		// new android.content.DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog,
		// int which) {
		// }
		// });
		// }
		// });
	}

	private void setIcon(ImageView view, News info) {
		String localUrl = info.getNewsLocalIconPath();
		if (TextUtils.isEmpty(localUrl)) {
			view.setVisibility(View.GONE);
		} else {
			Bitmap loacalBitmap = getLocalBmp(localUrl);
			if (loacalBitmap != null) {
				Log.d("DJC", "setIcon url =" + localUrl);
				Utils.setImg(view, loacalBitmap);
			} else {
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
					ImageDownloader.getMaxPixWithDensity(40, 40));
			if (loacalBitmap != null) {
				Log.d("DJC", "getLoacalBitmap url =" + localUrl);
				softMap.put(localUrl, new SoftReference<Bitmap>(loacalBitmap));
			}
		}
		return loacalBitmap;
	}

	private class FlagHolder {
		public TextView titleView;
		public TextView bodyView;
		public TextView timestampView;
		public TextView fromview;
		public ImageView iconView;
	}
}