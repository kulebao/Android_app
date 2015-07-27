package com.cocobabys.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.bean.BusinessInfo;
import com.cocobabys.utils.ImageUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MerchantListAdapter extends BaseAdapter {
	private Context context;
	private List<? extends BusinessInfo> list;
	private ImageLoader imageLoader;

	public MerchantListAdapter(Context context, List<? extends BusinessInfo> list) {
		this.context = context;
		this.list = list;

		imageLoader = ImageUtils.getImageLoader();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(this.context).inflate(
					R.layout.merchant_item, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.image);
			holder.titleView = (TextView) convertView.findViewById(R.id.title);
			setDataToViews(position, holder);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			setDataToViews(position, holder);
		}

		return convertView;
	}

	private void setDataToViews(final int position, ViewHolder flagholder) {
		BusinessInfo item = getItem(position);

		if (!TextUtils.isEmpty(item.getLogo())) {
			imageLoader.displayImage(item.getLogo(), flagholder.imageView);
		}

		flagholder.titleView.setText(item.getTitle());
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
	}
}
