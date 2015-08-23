package com.cocobabys.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.bean.BusinessInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.utils.ImageUtils;

public class MerchantListAdapter extends BaseAdapter {
	private Context context;
	private List<? extends BusinessInfo> list;

	public MerchantListAdapter(Context context, List<? extends BusinessInfo> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(this.context).inflate(R.layout.merchant_item, null);

			holder = createHolder(convertView);

			setDataToViews(position, holder);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			setDataToViews(position, holder);
		}

		return convertView;
	}

	private ViewHolder createHolder(View convertView) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.imageView = (ImageView) convertView.findViewById(R.id.image);
		holder.titleView = (TextView) convertView.findViewById(R.id.title);
		holder.contactView = (TextView) convertView.findViewById(R.id.contact);
		holder.addressView = (TextView) convertView.findViewById(R.id.address);
		return holder;
	}

	private void setDataToViews(final int position, ViewHolder flagholder) {
		BusinessInfo item = getItem(position);

		if (!item.getLogos().isEmpty()) {
			// imageLoader.displayImage(item.getLogo(), flagholder.imageView);
			ImageUtils.displayEx(item.getLogos().get(0).getUrl(), flagholder.imageView,
					ConstantValue.ACTION_PIC_MAX_WIDTH, ConstantValue.ACTION_PIC_MAX_HEIGHT);
		} else {
			flagholder.imageView.setImageResource(R.drawable.logo2);
		}

		flagholder.titleView.setText(item.getTitle());
		flagholder.contactView.setText(item.getContact());
		flagholder.addressView.setText(item.getAddress());
	}

	@Override
	public final int getCount() {
		return list.size();
	}

	@Override
	public final BusinessInfo getItem(int position) {
		return list.get(position);
	}

	@Override
	public final long getItemId(int position) {
		return position;
	}

	public void clearData() {
		list.clear();
		notifyDataSetChanged();
	}

	public class ViewHolder {
		ImageView imageView;
		TextView titleView;
		TextView contactView;
		TextView addressView;
	}
}
