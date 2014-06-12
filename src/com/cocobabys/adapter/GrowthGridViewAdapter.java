package com.cocobabys.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.bean.GroupExpInfo;

public class GrowthGridViewAdapter extends BaseAdapter {
	private Context context = null;
	private List<GroupExpInfo> data = new ArrayList<GroupExpInfo>();

	public GrowthGridViewAdapter(Context context, List<GroupExpInfo> data) {
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public GroupExpInfo getItem(int position) {
		return data.get(position);
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
					R.layout.grid_item, null);
			flagholder.newDataSymble = (ImageView) convertView
					.findViewById(R.id.noticeImg);
			flagholder.nameView = (TextView) convertView
					.findViewById(R.id.ItemText);
			flagholder.headView = (ImageView) convertView
					.findViewById(R.id.ItemImage);
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
		GroupExpInfo info = getItem(position);
		flagholder.nameView.setText(info.getMonth() + "/"
				+ String.valueOf(info.getCount()));
		flagholder.headView.setImageResource(R.drawable.growth);
	}

	private class FlagHolder {
		public ImageView newDataSymble;
		public TextView nameView;
		public ImageView headView;
	}

	public void addAll(List<GroupExpInfo> list) {
		data.clear();
		data.addAll(list);
		notifyDataSetChanged();
	}

	public void clear() {
		data.clear();
		notifyDataSetChanged();
	}

	public void changeCount(int expCountInMonth, String selectedMonth) {
		for (GroupExpInfo info : data) {
			if (info.getMonth().equals(selectedMonth)) {
				info.setCount(expCountInMonth);
				notifyDataSetChanged();
				return;
			}
		}
	}

}
