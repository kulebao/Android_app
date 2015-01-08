package com.cocobabys.utils;

import java.io.File;

import android.graphics.Bitmap;
import android.os.Environment;

import com.cocobabys.activities.MyApplication;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class ImageUtils {

	private static void initImageLoader() {
		try {
			String CACHE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.temp_tmp";
			new File(CACHE_DIR).mkdirs();

			MyApplication context = MyApplication.getInstance();
			File cacheDir = StorageUtils.getOwnCacheDirectory(context, CACHE_DIR);

			DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
					.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Bitmap.Config.RGB_565)
					.cacheInMemory(true).cacheOnDisk(true).build();

			// ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
			// .defaultDisplayImageOptions(defaultOptions).discCache(new UnlimitedDiscCache(cacheDir))
			// .diskCacheSize(50 * 1024 * 1024).diskCacheFileCount(100).memoryCacheExtraOptions(480, 800)
			// .diskCacheExtraOptions(480, 800, null)
			// .memoryCache(new UsingFreqLimitedMemoryCache(4 * 1024 * 1024)).memoryCacheSizePercentage(12).build();

			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
					.defaultDisplayImageOptions(defaultOptions).threadPriority(Thread.NORM_PRIORITY - 2)
					.denyCacheImageMultipleSizesInMemory().diskCacheFileNameGenerator(new Md5FileNameGenerator())
					.diskCacheSize(50 * 1024 * 1024) // 50 Mb
					.tasksProcessingOrder(QueueProcessingType.LIFO)
					.memoryCacheSize(4*1024*1024)
					// .writeDebugLogs() // Remove for release app
					.build();

			ImageLoader imageLoader = ImageLoader.getInstance();
			imageLoader.init(config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized static ImageLoader getImageLoader() {
		ImageLoader instance = ImageLoader.getInstance();

		if (!instance.isInited()) {
			initImageLoader();
		}

		return instance;
	}
}
