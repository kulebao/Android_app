/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cocobabys.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.bean.AblumInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.threadpool.MyThreadPoolMgr;
import com.cocobabys.utils.DataUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import de.greenrobot.event.EventBus;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class GalleryDirListFragment extends BaseEventFragment {

	public static final int INDEX = 0;

	List<AblumInfo> infos = new ArrayList<AblumInfo>();
	// String[] imageUrls = Constants.IMAGES;
	private Handler handler;
	private DisplayImageOptions options;
	private static final String TAG = "GalleryDirListFragment";

	private AbsListView listView;

	private ImageAdapter adapter;

	private AblumInfo firstInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.default_small_icon)
				.showImageForEmptyUri(R.drawable.default_small_icon).showImageOnFail(R.drawable.default_small_icon)
				.cacheInMemory(true).considerExifParams(true)
				.displayer(new RoundedBitmapDisplayer(20)).build();
		handler = new Handler();
		loadData();
	}

	public void onEventMainThread(AblumInfo firstInfo) {
		this.firstInfo = firstInfo;
	}

	private void loadData() {
		MyThreadPoolMgr.getGenericService().execute(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Log.d("DDD", TAG + "loadData 222 id=" + Thread.currentThread().getId());
						initData();
						infos.addAll(DataUtils.getGalleryPhotosDirs());
						adapter.notifyDataSetChanged();
					}

				}, 500);
				Looper.loop();
				Log.d("DDD", TAG + "loadData exit");
			}
		});
	}

	private void initData() {
		if (this.firstInfo != null) {
			infos.add(this.firstInfo);
		} else {
			Log.e("DDD", "DJC initData but firstInfo is null!");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fr_image_list, container, false);
		listView = (ListView) rootView.findViewById(android.R.id.list);
		adapter = new ImageAdapter();
		((ListView) listView).setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position != 0) {
					AblumInfo item = adapter.getItem(position);
					EventBus.getDefault().post(item.getDirName());
				} else {
					EventBus.getDefault().post(ConstantValue.RECENTLY_PIC_DIR);
				}
				GalleryDirListFragment.this.getActivity().onBackPressed();
			}
		});
		return rootView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		AnimateFirstDisplayListener.displayedImages.clear();
	}

	private static class ViewHolder {
		TextView text;
		ImageView image;
	}

	class ImageAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

		ImageAdapter() {
			inflater = LayoutInflater.from(getActivity());
		}

		@Override
		public int getCount() {
			return infos.size();
		}

		@Override
		public AblumInfo getItem(int position) {
			if (infos.isEmpty()) {
				return null;
			}
			return infos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			if (convertView == null) {
				view = inflater.inflate(R.layout.item_list_image, parent, false);
				holder = new ViewHolder();
				holder.text = (TextView) view.findViewById(R.id.text);
				holder.image = (ImageView) view.findViewById(R.id.image);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.text.setText(getItem(position).getDirName() + "(" + getItem(position).getDirCount() + ")");

			ImageLoader.getInstance().displayImage(getItem(position).getLastestPicPath(), holder.image, options,
					animateFirstListener);

			return view;
		}
	}

	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}
}