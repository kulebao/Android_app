package com.djc.logintest.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.djc.logintest.R;
import com.djc.logintest.activities.SchoolNoticeActivity;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.utils.Utils;

public class MyGridViewAdapter extends BaseAdapter {
	private Context context = null;
	private List<? extends Map<String, ?>> data;

	public MyGridViewAdapter(Context context,
			List<? extends Map<String, ?>> data) {
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
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
		setNoticeImg(position, flagholder);

		HashMap<String, Object> map = (HashMap<String, Object>) getItem(position);
		int object = (Integer) map.get("ItemImage");
		flagholder.headView.setBackgroundResource(object);

		String content = (String) map.get("ItemText");
		flagholder.nameView.setText(content);
	}

	private void setNoticeImg(final int position, FlagHolder flagholder) {
		if (position == SchoolNoticeActivity.NORMAL_NOTICE) {
			String prop = Utils.getProp(ConstantValue.HAVE_NEWS_NOTICE);
			if ("true".equals(prop)) {
				flagholder.newDataSymble.setVisibility(View.VISIBLE);
			} else {
				flagholder.newDataSymble.setVisibility(View.GONE);
			}
		} else if (position == SchoolNoticeActivity.COOK_NOTICE) {
			String prop = Utils.getProp(ConstantValue.HAVE_COOKBOOK_NOTICE);
			if ("true".equals(prop)) {
				flagholder.newDataSymble.setVisibility(View.VISIBLE);
			} else {
				flagholder.newDataSymble.setVisibility(View.GONE);
			}
		} else if (position == SchoolNoticeActivity.HOMEWORK) {
			String prop = Utils.getProp(ConstantValue.HAVE_HOMEWORK_NOTICE);
			if ("true".equals(prop)) {
				flagholder.newDataSymble.setVisibility(View.VISIBLE);
			} else {
				flagholder.newDataSymble.setVisibility(View.GONE);
			}
		}else if (position == SchoolNoticeActivity.SCHEDULE) {
			String prop = Utils.getProp(ConstantValue.HAVE_SCHEDULE_NOTICE);
			if ("true".equals(prop)) {
				flagholder.newDataSymble.setVisibility(View.VISIBLE);
			} else {
				flagholder.newDataSymble.setVisibility(View.GONE);
			}
		}
	}

	private class FlagHolder {
		public ImageView newDataSymble;
		public TextView nameView;
		public ImageView headView;
	}

}
