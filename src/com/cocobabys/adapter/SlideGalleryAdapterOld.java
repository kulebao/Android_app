package com.cocobabys.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cocobabys.R;
import com.cocobabys.constant.EventType;
import com.cocobabys.customview.ProgressWheel;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.taskmgr.DownloadImgeJob;
import com.cocobabys.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class SlideGalleryAdapterOld extends BaseAdapter {
	private LayoutInflater infalter;
	private ImageLoader imageLoader;
	private List<String> localUrlList = new ArrayList<String>();
	private ExpInfo expInfo = new ExpInfo();
	private DownloadImgeJob downloadImgeJob;
	private Handler handler;

	public SlideGalleryAdapterOld(Context c, ImageLoader imageLoader,
			ExpInfo expInfo, DownloadImgeJob downloadImgeJob) {
		infalter = (LayoutInflater) c
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.imageLoader = imageLoader;
		this.expInfo = expInfo;
		this.localUrlList = expInfo.getLocalUrls(false);
		this.downloadImgeJob = downloadImgeJob;

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.DOWNLOAD_IMG_SUCCESS:
					notifyDataSetChanged();
					break;
				default:
					break;
				}
			}

		};
		this.downloadImgeJob.setHanlder(handler);
		addToDownloadTask(expInfo);
	}

	@Override
	public int getCount() {
		return localUrlList.size();
	}

	@Override
	public String getItem(int position) {
		return localUrlList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public List<String> getList() {
		return localUrlList;
	}

	public void remove(int position) {
		try {
			localUrlList.remove(position);
			notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = infalter.inflate(R.layout.slide_item, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.image);
			holder.progressWheel = (ProgressWheel) convertView
					.findViewById(R.id.circleProgressBar);
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
			if (new File(path).exists()) {
				holder.progressWheel.setVisibility(View.GONE);
			} else {
				if (!holder.progressWheel.isSpinning()) {
					holder.progressWheel.spin();
				}
				holder.progressWheel.setVisibility(View.VISIBLE);
			}

			imageLoader.displayImage("file://" + path, holder.imageView,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							Log.e("III", "onLoadingStarted imageUri="
									+ imageUri);
							super.onLoadingStarted(imageUri, view);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							Log.e("III", "onLoadingFailed  imageUri="
									+ imageUri);
							super.onLoadingComplete(imageUri, view, loadedImage);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							Log.e("III", "onLoadingFailed  imageUri="
									+ imageUri);
							String name = Utils.getName(imageUri);
							// 原图显示失败则暂时显示缩略图
							String path = "file://" + expInfo.getThumbnailDir()
									+ name;
							imageLoader.displayImage(path, holder.imageView);
							super.onLoadingFailed(imageUri, view, failReason);
						}

					});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addToDownloadTask(ExpInfo info) {
		List<String> serverUrls = info.getServerUrls();
		for (String serverUrl : serverUrls) {
			String localUrl = info.serverUrlToLocalUrl(serverUrl, false);
			if (!new File(localUrl).exists()) {
				downloadImgeJob.addTask(serverUrl, localUrl, 1080f, 1080f);
			}
		}
	}

	public class ViewHolder {
		ImageView imageView;
		ProgressWheel progressWheel;
	}

	public void clearCache() {
		imageLoader.clearDiscCache();
		imageLoader.clearMemoryCache();
	}
}
