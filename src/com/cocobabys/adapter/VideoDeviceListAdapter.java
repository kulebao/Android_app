package com.cocobabys.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.video.VideoDeviceInfo;

public class VideoDeviceListAdapter extends BaseAdapter {
	private final Context context;
	private List<VideoDeviceInfo> deviceList;

	public void setLocationInfoList(List<VideoDeviceInfo> list) {
		this.deviceList = list;
	}

	public VideoDeviceListAdapter(Context activityContext,
			List<VideoDeviceInfo> list) {
		this.context = activityContext;
		deviceList = list;
	}

	@Override
	public int getCount() {
		return deviceList.size();
	}

	@Override
	public VideoDeviceInfo getItem(int position) {
		return deviceList.get(position);
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
					R.layout.device_item, null);
			flagholder.titleView = (TextView) convertView
					.findViewById(R.id.titleView);
			flagholder.bodyView = (TextView) convertView
					.findViewById(R.id.bodyView);
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
		final VideoDeviceInfo info = deviceList.get(position);
		String content = getContent(info);

		flagholder.bodyView.setText(content);
	}

	private String getContent(VideoDeviceInfo info) {
		String onlineState = info.isOnline() ? "在线" : "离线";
		String content = info.getDeviceName() + "(" + onlineState + ")";
		return content;
	}

	private class FlagHolder {
		public TextView titleView;
		public TextView bodyView;
	}
}