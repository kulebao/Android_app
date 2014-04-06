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
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.Homework;
import com.cocobabys.utils.ImageDownloader;
import com.cocobabys.utils.Utils;

public class HomeworkListAdapter extends BaseAdapter {
	private final Context context;
	private List<Homework> dataList;
	private static Map<String, SoftReference<Bitmap>> softMap = new HashMap<String, SoftReference<Bitmap>>();

	public void setLocationInfoList(List<Homework> list) {
		this.dataList = list;
	}

	public HomeworkListAdapter(Context activityContext, List<Homework> list) {
		this.context = activityContext;
		dataList = list;
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
			flagholder.fromView = (TextView) convertView
					.findViewById(R.id.fromview);
			flagholder.iconView = (ImageView) convertView
					.findViewById(R.id.iconView);
			setDataToViews(position, flagholder);
			convertView.setBackgroundResource(R.drawable.homework_item);
			convertView.setTag(flagholder);
		} else {
			FlagHolder flagholder = (FlagHolder) convertView.getTag();
			convertView.setBackgroundResource(R.drawable.homework_item);
			if (flagholder != null) {
				setDataToViews(position, flagholder);
			}
		}

		return convertView;
	}

	private void setDataToViews(final int position, FlagHolder flagholder) {
		final Homework info = dataList.get(position);
		flagholder.titleView.setText(info.getTitle());
		flagholder.bodyView.setText(info.getContent());
		String timestr = Utils.formatChineseTime(info.getTimestamp());
		flagholder.timestampView.setText(timestr);

		flagholder.fromView.setText(DataMgr.getInstance()
				.getClassNameByClassID(info.getClass_id()));
		setIcon(flagholder.iconView, info);
	}

	private void setIcon(ImageView view, Homework info) {
		String localUrl = info.getHomeWorkLocalIconPath();
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
		public TextView fromView;
		public ImageView iconView;
	}
}