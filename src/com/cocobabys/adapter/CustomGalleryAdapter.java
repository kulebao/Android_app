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
import com.cocobabys.customview.CustomGallery;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class CustomGalleryAdapter extends BaseAdapter {
	private LayoutInflater infalter;
	private List<CustomGallery> data = new ArrayList<CustomGallery>();
	private ImageLoader imageLoader;

	private boolean isActionMultiplePick;
	protected boolean showDefaultPic = false;
	private DisplayImageOptions options;

	public void setShowDefaultPic(boolean showDefaultPic) {
		this.showDefaultPic = showDefaultPic;
	}

	public CustomGalleryAdapter(Context c, ImageLoader imageLoader) {
		infalter = (LayoutInflater) c
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.imageLoader = imageLoader;

		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.default_small_icon)
				.showImageForEmptyUri(R.drawable.default_small_icon)
				.showImageOnFail(R.drawable.default_small_icon)
				.cacheInMemory(true).imageScaleType(ImageScaleType.EXACTLY)
				.cacheOnDisk(false).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	public List<String> getAllSelectedPath() {
		List<String> list = new ArrayList<String>();
		for (CustomGallery gallery : data) {
			list.add(gallery.getSdcardPath());
		}
		return list;
	}

	public List<CustomGallery> getData() {
		return data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public CustomGallery getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setMultiplePick(boolean isMultiplePick) {
		this.isActionMultiplePick = isMultiplePick;
	}

	public void selectAll(boolean selection) {
		for (int i = 0; i < data.size(); i++) {
			data.get(i).setSeleted(selection);

		}
		notifyDataSetChanged();
	}

	public boolean isAllSelected() {
		boolean isAllSelected = true;

		for (int i = 0; i < data.size(); i++) {
			if (!data.get(i).isSeleted()) {
				isAllSelected = false;
				break;
			}
		}

		return isAllSelected;
	}

	public boolean isAnySelected() {
		boolean isAnySelected = false;

		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).isSeleted()) {
				isAnySelected = true;
				break;
			}
		}

		return isAnySelected;
	}

	public ArrayList<CustomGallery> getSelected() {
		ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).isSeleted()) {
				dataT.add(data.get(i));
			}
		}

		return dataT;
	}

	public void addAll(ArrayList<CustomGallery> files) {
		try {
			this.data.clear();
			this.data.addAll(files);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void changeSelection(View v, int position) {
		((ViewHolder) v.getTag()).imgQueueMultiSelected.setSelected(data.get(
				position).isSeleted());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {

			convertView = infalter.inflate(R.layout.gallery_item, null);
			holder = new ViewHolder();
			holder.imgQueue = (ImageView) convertView
					.findViewById(R.id.imgQueue);

			holder.imgQueueMultiSelected = (ImageView) convertView
					.findViewById(R.id.imgQueueMultiSelected);

			if (isActionMultiplePick) {
				holder.imgQueueMultiSelected.setVisibility(View.VISIBLE);
			} else {
				holder.imgQueueMultiSelected.setVisibility(View.GONE);
			}

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// holder.imgQueue.setTag(position);

		showImgByLoader(position, holder);

		return convertView;
	}

	private void showImgByLoader(int position, final ViewHolder holder) {
		try {
			String sdcardPath = data.get(position).getSdcardPath();
			ImageLoader.getInstance().displayImage("file://" + sdcardPath,
					holder.imgQueue, options);

			// ImageSize imagesize = new ImageSize(160, 160);
			// ImageLoader.getInstance().loadImage("file://" + sdcardPath,
			// imagesize, options, new SimpleImageLoadingListener() {
			// @Override
			// public void onLoadingComplete(String imageUri,
			// View view, Bitmap loadedImage) {
			// super.onLoadingComplete(imageUri, view, loadedImage);
			// holder.imgQueue.setImageBitmap(loadedImage);
			// }
			//
			// @Override
			// public void onLoadingStarted(String imageUri, View view) {
			// super.onLoadingStarted(imageUri, view);
			// holder.imgQueue
			// .setImageResource(R.drawable.default_small_icon);
			// }
			//
			// });

			if (isActionMultiplePick) {
				holder.imgQueueMultiSelected.setSelected(data.get(position)
						.isSeleted());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class ViewHolder {
		ImageView imgQueue;
		ImageView imgQueueMultiSelected;
	}

	public void clearCache() {
		// imageLoader.clearDiskCache();
		imageLoader.clearMemoryCache();
	}

	public void clear() {
		data.clear();
	}

	public void setSelected(String[] selected_path) {
		if (selected_path == null || selected_path.length == 0) {
			return;
		}
		for (CustomGallery gallery : data) {
			for (String path : selected_path) {
				if (gallery.getSdcardPath().equals(path)) {
					gallery.setSeleted(true);
				}
			}
		}
	}
}
