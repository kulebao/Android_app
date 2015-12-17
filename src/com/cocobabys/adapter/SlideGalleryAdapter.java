package com.cocobabys.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.cocobabys.R;
import com.cocobabys.constant.EventType;
import com.cocobabys.customview.ProgressWheel;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.taskmgr.DownloadImgeJob;
import com.cocobabys.utils.ImageUtils;
import com.cocobabys.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

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

public class SlideGalleryAdapter extends BaseAdapter {
	private LayoutInflater infalter;
	private List<String> localUrlList = new ArrayList<String>();
	private ExpInfo expInfo = new ExpInfo();
	private DownloadImgeJob downloadImgeJob;
	private Handler handler;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;

	public SlideGalleryAdapter(Context c, ExpInfo expInfo, DownloadImgeJob downloadImgeJob) {
		infalter = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.expInfo = expInfo;
		this.localUrlList = expInfo.getLocalUrls(false);
		this.downloadImgeJob = downloadImgeJob;

		initHandler();

		this.downloadImgeJob.setHanlder(handler);
		addToDownloadTask(expInfo);

		imageLoader = ImageUtils.getImageLoader();

		options = new DisplayImageOptions.Builder().showImageOnFail(R.drawable.default_icon) // 设置图片加载/解码过程中错误时候显示的图片
				.cacheInMemory(true)// 设置下载的图片是否缓存在内存中
				// .considerExifParams(true) // 是否考虑JPEG图像EXIF参数（旋转，翻转）
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
				.bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型//
				.build();// 构建完成
	}

	public SlideGalleryAdapter(Context c, List<String> localUrlList) {
		infalter = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.localUrlList = localUrlList;
		imageLoader = ImageUtils.getImageLoader();
		// do not use cache,use Universal-Image-Load
		// initCache();
	}

	private void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.DOWNLOAD_FILE_SUCCESS:
					notifyDataSetChanged();
					break;
				default:
					break;
				}
			}
		};
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
			// showAnimate(holder, path);
			showIcon(holder, path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showIcon(final ViewHolder holder, String path) {
		String url = path;

		// 如果原图不存在，则找缩略图
		if (!new File(path).exists()) {
			String name = Utils.getName(path);
			// 原图显示失败则暂时显示缩略图
			url = expInfo.getThumbnailDir() + name;
		}

		url = "file://" + url;

		Log.d("", "showIcon url=" + url);
		imageLoader.displayImage(url, holder.imageView, options);
	}

	//暂时去掉动画，会影响显示效果
	private void showAnimate(final ViewHolder holder, String path) {
		if (new File(path).exists()) {
			holder.progressWheel.setVisibility(View.GONE);
		} else {
			if (!holder.progressWheel.isSpinning()) {
				holder.progressWheel.spin();
			}
			holder.progressWheel.setVisibility(View.VISIBLE);
		}
	}

	private void addToDownloadTask(ExpInfo info) {
		List<String> serverUrls = info.getServerUrls();
		for (String serverUrl : serverUrls) {
			String localUrl = info.serverUrlToLocalUrl(serverUrl, false);
			if (!new File(localUrl).exists()) {
				downloadImgeJob.addTask(serverUrl, localUrl, 800f, 600f);
			}
		}
	}

	public class ViewHolder {
		ImageView imageView;
		ProgressWheel progressWheel;
	}

}
