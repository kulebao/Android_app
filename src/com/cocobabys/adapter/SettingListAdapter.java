package com.cocobabys.adapter;

import java.util.List;

import com.cocobabys.R;
import com.cocobabys.bean.SettingInfo;
import com.cocobabys.utils.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingListAdapter extends BaseAdapter {
	private final Context context;
	private List<SettingInfo> mList;

	public void setLocationInfoList(List<SettingInfo> list) {
		this.mList = list;
	}

	public SettingListAdapter(Context activityContext, List<SettingInfo> list) {
		this.context = activityContext;
		mList = list;
	}

	public void clear() {
		mList.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public SettingInfo getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			FlagHolder flagholder = this.new FlagHolder();
			convertView = LayoutInflater.from(this.context).inflate(R.layout.setting_item, null);
			flagholder.content = (TextView) convertView.findViewById(R.id.content);
			flagholder.headView = (ImageView) convertView.findViewById(R.id.headView);
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
		final SettingInfo settingInfo = mList.get(position);
		flagholder.content.setText(Utils.getResString(settingInfo.getNameid()));

		flagholder.headView.setImageResource(settingInfo.getPicid());

	}

	private class FlagHolder {
		public TextView content;
		public ImageView headView;
	}
}