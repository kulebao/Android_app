package com.cocobabys.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
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

public class SlideGalleryAdapter extends BaseAdapter {
	private static final int MAXPIX = 1080 * 1080;
	private LayoutInflater infalter;
	private List<String> localUrlList = new ArrayList<String>();
	private ExpInfo expInfo = new ExpInfo();
	private DownloadImgeJob downloadImgeJob;
	private Handler handler;
	private LruCache<String, Bitmap> lruCache;

	public SlideGalleryAdapter(Context c, ExpInfo expInfo, DownloadImgeJob downloadImgeJob) {
		infalter = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.expInfo = expInfo;
		this.localUrlList = expInfo.getLocalUrls(false);
		this.downloadImgeJob = downloadImgeJob;

		initCache();
		initHandler();
		
		this.downloadImgeJob.setHanlder(handler);
		addToDownloadTask(expInfo);
	}

	public SlideGalleryAdapter(Context c, List<String> localUrlList) {
		infalter = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.localUrlList = localUrlList;
		initCache();
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

	private void initCache() {
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		lruCache = new LruCache<String, Bitmap>(maxMemory) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight() / 1024;
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
			showIcon(holder, path);
			showAnimate(holder, path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showIcon(final ViewHolder holder, String path) {
		Bitmap bitmap = lruCache.get(path);
		if (bitmap == null) {
			Bitmap loacalBitmap = Utils.getLoacalBitmap(path, MAXPIX);
			if (loacalBitmap != null) {
				lruCache.put(path, loacalBitmap);
				Utils.setImg(holder.imageView, loacalBitmap);
			} else {
				String name = Utils.getName(path);
				// 原图显示失败则暂时显示缩略图
				String nailPath = expInfo.getThumbnailDir() + name;
				Bitmap nailBitmap = Utils.getLoacalBitmap(nailPath, MAXPIX);
				if (nailBitmap != null) {
					Utils.setImg(holder.imageView, nailBitmap);
				} else {
					holder.imageView.setImageResource(R.drawable.default_small_icon);
				}
			}
		} else {
			Utils.setImg(holder.imageView, bitmap);
		}
	}

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
				downloadImgeJob.addTask(serverUrl, localUrl, 1080f, 1080f);
			}
		}
	}

	public class ViewHolder {
		ImageView imageView;
		ProgressWheel progressWheel;
	}

}
