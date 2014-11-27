package com.cocobabys.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cocobabys.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class SimpleGridViewAdapter extends BaseAdapter {
	private Context context;
	private List<String> localUrlList = new ArrayList<String>();
	private ImageLoader imageLoader;
	private DisplayImageOptions options;

	public SimpleGridViewAdapter(Context context, ImageLoader imageLoader, List<String> list) {
		this.context = context;
		this.localUrlList = list;
		this.imageLoader = imageLoader;
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.default_small_icon)
				.showImageForEmptyUri(R.drawable.default_small_icon).showImageOnFail(R.drawable.default_small_icon)
				.cacheInMemory(true)
				// .cacheOnDisk(true)
				.considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(this.context).inflate(R.layout.simple_grid_item, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.ItemImage);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		String path = "file://" + getItem(position);
		Log.d("III", "SimpleGridViewAdapter path =" + path);

		imageLoader.displayImage(path, holder.imageView, options, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				holder.imageView.setImageResource(R.drawable.default_small_icon);
				super.onLoadingStarted(imageUri, view);
			}
		});

		return convertView;
	}

	public final int getCount() {
		return localUrlList.size();
	}

	public final String getItem(int position) {
		return localUrlList.get(position);
	}

	public final long getItemId(int position) {
		return position;
	}

	public class ViewHolder {
		ImageView imageView;
	}
}
