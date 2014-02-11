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

public class MyGridViewAdapter extends BaseAdapter {
	private Context context = null;
	private List<? extends Map<String, ?>> data;
	private boolean showNews = false;

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
			flagholder.numberView = (TextView) convertView
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
	
	public void setNewsNotice(boolean show){
		this.showNews = show;
		notifyDataSetChanged();
	}

	private void setDataToViews(final int position, FlagHolder flagholder) {
		if (position == 0) {
			if(showNews){
				flagholder.numberView.setVisibility(View.VISIBLE);
			}else{
				flagholder.numberView.setVisibility(View.GONE);
			}
		}
		
		HashMap<String, Object> map = (HashMap<String, Object>) getItem(position);
		int object = (Integer) map.get("ItemImage");
		flagholder.headView.setBackgroundResource(object);
		
		String content = (String) map.get("ItemText");
		flagholder.nameView.setText(content);
	}

	private class FlagHolder {
		public TextView numberView;
		public TextView nameView;
		public ImageView headView;
	}

}
