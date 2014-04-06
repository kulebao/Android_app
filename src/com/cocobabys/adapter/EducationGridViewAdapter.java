package com.cocobabys.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cocobabys.R;

public class EducationGridViewAdapter extends BaseAdapter {
	private Context context = null;
	private List<? extends Map<String, ?>> data;

	public EducationGridViewAdapter(Context context,
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
					R.layout.education_item, null);
			flagholder.itemImgView = (ImageView) convertView
					.findViewById(R.id.itemImg);
			flagholder.rankImgView = (ImageView) convertView
					.findViewById(R.id.rankImg);
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
		HashMap<String, Object> map = (HashMap<String, Object>) getItem(position);
		int object = (Integer) map.get("ItemImage");
		flagholder.itemImgView.setBackgroundResource(object);

		int rank = (Integer) map.get("rank");
		if (rank == 1) {
			flagholder.rankImgView.setImageResource(R.drawable.badrank);
		} else if (rank == 2) {
			flagholder.rankImgView.setImageResource(R.drawable.normalrank);
		} else {
			flagholder.rankImgView.setImageResource(R.drawable.goodrank);
		}
	}

	private class FlagHolder {
		public ImageView rankImgView;
		public ImageView itemImgView;
	}

}
