package com.cocobabys.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cocobabys.R;
import com.cocobabys.bean.MerchantGridInfo;

public class MerchantGridViewAdapter extends BaseAdapter {
	private Context context;
	private List<MerchantGridInfo> list = new ArrayList<MerchantGridInfo>();

	public MerchantGridViewAdapter(Context context, List<MerchantGridInfo> list) {
		this.context = context;
		this.list = list;
	}

	private int clickTemp = -1;

	// 标识选择的Item
	public void setSeclection(int position) {
		clickTemp = position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(this.context).inflate(R.layout.merchant_grid_item, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.ItemImage);
			setDataToViews(position, holder);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			setDataToViews(position, holder);
		}

		return convertView;
	}

	private void setDataToViews(final int position, ViewHolder flagholder) {
		MerchantGridInfo item = getItem(position);
		// flagholder.imageView.setImageResource(item.getImageID());

		if (clickTemp == position) {
			flagholder.imageView.setImageResource(item.getSelectedImageID());
		} else {
			flagholder.imageView.setImageResource(item.getImageID());
		}
	}

	public final int getCount() {
		return list.size();
	}

	public final MerchantGridInfo getItem(int position) {
		return list.get(position);
	}

	public final long getItemId(int position) {
		return position;
	}

	public class ViewHolder {
		ImageView imageView;
	}
}
