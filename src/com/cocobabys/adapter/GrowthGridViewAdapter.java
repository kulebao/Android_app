package com.cocobabys.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.bean.GroupExpInfo;
import com.cocobabys.utils.ImageUtils;

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

	public void addAll(List<GroupExpInfo> list) {
		data.clear();
		data.addAll(list);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			FlagHolder flagholder = this.new FlagHolder();
			convertView = LayoutInflater.from(this.context).inflate(
					R.layout.exp_grid_item, null);
			flagholder.nameView = (TextView) convertView
					.findViewById(R.id.ItemText);
			flagholder.monthnameView = (TextView) convertView
					.findViewById(R.id.monthname);
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

		showText(flagholder, info);
		showIcon(flagholder, info);
	}

	private void showIcon(FlagHolder flagholder, GroupExpInfo info) {
		// Integer iconid = GroupExpInfo.getIconMap().get(info.getMonth());
		if (!TextUtils.isEmpty(info.getIconpath())) {
			Log.d("EXP_ICON", "path =" + info.getIconpath());
			// flagholder.headView.setImageBitmap(Utils.getLoacalBitmap(
			// info.getIconpath(), 200, 200));
			ImageUtils.getImageLoader()
					.displayImage(ImageUtils.wrapper(info.getIconpath()),
							flagholder.headView);
		} else {
			flagholder.headView.setImageResource(R.drawable.exp_default);
		}
	}

	private void showText(FlagHolder flagholder, GroupExpInfo info) {
		String content = info.getMonthName();// + "(" + info.getCount() + ")";
		// flagholder.nameView.setText(String.valueOf(info.getCount()));
		flagholder.monthnameView.setText(content);
	}

	private class FlagHolder {
		public TextView nameView;
		public TextView monthnameView;
		public ImageView headView;
	}

	public void updateList(List<GroupExpInfo> list) {
		for (GroupExpInfo info : list) {
			for (GroupExpInfo oldInfo : data) {
				if (oldInfo.getMonth().equals(info.getMonth())) {
					oldInfo.setCount(info.getCount());
				}
			}
		}
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
