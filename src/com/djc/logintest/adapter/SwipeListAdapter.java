package com.djc.logintest.adapter;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.djc.logintest.R;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.Homework;
import com.djc.logintest.dbmgr.info.SwipeInfo;
import com.djc.logintest.noticepaser.SwapCardNoticePaser;
import com.djc.logintest.utils.ImageDownloader;
import com.djc.logintest.utils.Utils;

public class SwipeListAdapter extends BaseAdapter {
	private final Context context;
	private List<SwipeInfo> list;
	private String nick;
	private static Map<String, SoftReference<Bitmap>> softMap = new HashMap<String, SoftReference<Bitmap>>();

	public void setLocationInfoList(List<SwipeInfo> list) {
		this.list = list;
	}

	public SwipeListAdapter(Context activityContext, List<SwipeInfo> list) {
		this.context = activityContext;
		this.list = list;
		getNick();
	}

	public void getNick() {
		nick = DataMgr.getInstance().getSelectedChild().getChild_nick_name();
	}

	public void clear() {
		list.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
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
			convertView.setTag(flagholder);
		} else {
			FlagHolder flagholder = (FlagHolder) convertView.getTag();
			if (flagholder != null) {
				setDataToViews(position, flagholder);
			}
		}

		return convertView;
	}

	private void setIcon(ImageView view, SwipeInfo info) {
		String localUrl = SwapCardNoticePaser.createSwipeIconPath(String
				.valueOf(info.getTimestamp()));
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

	private void setDataToViews(final int position, FlagHolder flagholder) {
		final SwipeInfo info = list.get(position);
		flagholder.titleView.setText(info.getNoticeTitle());
		flagholder.bodyView.setText(info.getNoticeBody(nick));
		flagholder.timestampView.setText(info.getFormattedTime());
		flagholder.fromView.setText(DataMgr.getInstance().getSchoolInfo()
				.getSchool_name());
		setIcon(flagholder.iconView, info);
	}

	private class FlagHolder {
		public TextView titleView;
		public TextView bodyView;
		public TextView timestampView;
		public TextView fromView;
		public ImageView iconView;
	}
}