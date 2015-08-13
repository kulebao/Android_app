package com.cocobabys.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cocobabys.R;
import com.cocobabys.customview.ProgressWheel;
import com.cocobabys.utils.ImageUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class MyGalleryAdapter extends BaseAdapter {
	private LayoutInflater infalter;
	private List<String> urlList = new ArrayList<String>();
	private ImageLoader imageLoader;

	public MyGalleryAdapter(Context c, List<String> list) {
		infalter = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.urlList = list;
		imageLoader = ImageUtils.getImageLoader();
	}

	@Override
	public int getCount() {
		return urlList.size();
	}

	@Override
	public String getItem(int position) {
		return urlList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public List<String> getList() {
		return urlList;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = infalter.inflate(R.layout.slide_item, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.image);
			holder.progressWheel = (ProgressWheel) convertView.findViewById(R.id.circleProgressBar);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		setDataToView(position, holder);
		return convertView;
	}

	private void setDataToView(final int position, final ViewHolder holder) {
		try {
			String path = getItem(position);
			holder.progressWheel.setVisibility(View.VISIBLE);
			showIcon(holder, path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showIcon(final ViewHolder holder, String path) {
		imageLoader.displayImage(path, holder.imageView, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				super.onLoadingComplete(imageUri, view, loadedImage);
				holder.progressWheel.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				super.onLoadingFailed(imageUri, view, failReason);
				holder.progressWheel.setVisibility(View.GONE);
			}

		});
	}

	public class ViewHolder {
		ImageView imageView;
		ProgressWheel progressWheel;
	}

}
