package com.djc.logintest.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

import com.djc.logintest.R;
import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.customexception.DecodeBitmapException;

public class ImageDownloader {
	private static final int LIMIT_WITH = 320;
	private static final int LIMIT_HEIGHT = 480;
	private static final int HTTP_CONN_TIMEOUT = 30000;
	// 读超时5分钟
	private static final int HTTP_READ_TIMEOUT = 300000;
	private String imageUrl;
	private float limitWith = 320;
	private float limitHeight = 480;

	public ImageDownloader(String imageUrl) {
		this.imageUrl = imageUrl;
		float xfactor = Float.valueOf(MyApplication.getInstance()
				.getResources().getString(R.string.xfactor));
		this.limitHeight = xfactor * LIMIT_HEIGHT;
		this.limitWith = xfactor * LIMIT_WITH;

	}

	public ImageDownloader(String imageUrl, float limitWith, float limitHeight) {
		this.imageUrl = imageUrl;
		this.limitHeight = limitHeight;
		this.limitWith = limitWith;
	}

	Bitmap download() {
		HttpURLConnection connection = null;
		try {
			URL url = new URL(imageUrl);

			// SocketAddress addr = new InetSocketAddress("proxynj.zte.com.cn",
			// 80);
			// Proxy typeProxy = new Proxy(Proxy.Type.HTTP, addr);
			// connection = (HttpURLConnection) url.openConnection(typeProxy);

			connection = (HttpURLConnection) url.openConnection();

			connection.setConnectTimeout(HTTP_CONN_TIMEOUT);
			connection.setReadTimeout(HTTP_READ_TIMEOUT);
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.setDoOutput(false);

			// connection.setRequestProperty("Range", "bytes=0-");
			Bitmap bitmap = getBitmapformInputStream(url.openStream(),
					url.toString());
			connection.disconnect();

			if (bitmap == null) {
				Log.e("LIYI", "download failed !bitmap == null");
			}

			return bitmap;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("LIYI", "download IOException e:" + e.toString());
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}

	private void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	Bitmap getBitmapformInputStream(InputStream ins, String url)
			throws IOException {
		try {

			ImageBufferedInputStream bis = new ImageBufferedInputStream(ins,
					url);
			// 标记其实位置，供reset参考
			bis.mark(0);

			BitmapFactory.Options opts = new BitmapFactory.Options();
			// true,只是读图片大小，不申请bitmap内存
			opts.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeStream(bis, null, opts);
			if (tmp == null) {
				throw new DecodeBitmapException("url :" + url
						+ " can not be decoded ");
			}
			Log.e("DIMG", "width=" + opts.outWidth + "; height="
					+ opts.outHeight);

			opts.inSampleSize = computeSampleSize(opts.outWidth, opts.outHeight);
			// opts.inSampleSize = computeSampleSize(opts,LIMIT_WITH,
			// LIMIT_WITH*LIMIT_HEIGHT);

			// 设为false，这次不是预读取图片大小，而是返回申请内存，bitmap数据
			opts.inJustDecodeBounds = false;
			// 缓冲输入流定位至头部，mark()
			bis.reset();
			Bitmap bm = tmp;

			close(ins);
			close(bis);
			return bm;

		} catch (MalformedURLException e1) {
			Log.e("", "MalformedURLException");
			e1.printStackTrace();
		} catch (IOException e) {
			Log.e("", "IOException");
			e.printStackTrace();
			throw new DecodeBitmapException(e.toString());
		}
		return null;
	}

	int computeSampleSize(int outWidth, int outHeight) {
		int w = outWidth;
		int h = outHeight;
		int candidateW = (int) (w / limitWith);
		int candidateH = (int) (h / limitHeight);
		int candidate = Math.max(candidateW, candidateH);
		if (candidate == 0)
			return 1;
		if (candidate > 1) {
			if ((w > limitWith) && (w / candidate) < limitWith)
				candidate -= 1;
		}
		if (candidate > 1) {
			if ((h > limitHeight) && (h / candidate) < limitHeight)
				candidate -= 1;
		}

		Log.e("", "for w/h " + w + "/" + h + " returning " + candidate + "("
				+ (w / candidate) + " / " + (h / candidate));
		return candidate;
	}

	public static Bitmap getResizedBmp(int maxPixel, String path) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = computeSampleSize(options, -1, maxPixel);
		options.inPreferredConfig = Config.RGB_565;
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	public static int getMaxPix() {
		DisplayMetrics dm = new DisplayMetrics();
		dm = MyApplication.getInstance().getResources().getDisplayMetrics();

		Log.d("DDD", "w = " + dm.widthPixels + " h=" + dm.heightPixels
				+ " density=" + dm.density);
		int maxPixel = (int) (dm.widthPixels * dm.heightPixels * dm.density);
		return maxPixel;
	}

	public static int getMaxPixWithDensity(int width, int height) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = MyApplication.getInstance().getResources().getDisplayMetrics();
		int maxPixel = (int) (width * height);// * dm.density);
		return maxPixel;
	}

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

}
