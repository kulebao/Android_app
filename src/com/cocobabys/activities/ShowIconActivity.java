package com.cocobabys.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.utils.ImageUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ShowIconActivity extends UmengStatisticsActivity {

	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_icon);
		showIcon();
	}

	private void showIcon() {
		ImageView image = (ImageView) findViewById(R.id.imageview);
		String path = getIntent().getStringExtra(ConstantValue.LOCAL_URL);

		// int maxPixel = ImageDownloader.getMaxPix();
		//
		// bitmap = ImageDownloader.getResizedBmp(maxPixel, path);
		// image.setImageBitmap(bitmap);
		ImageUtils.getImageLoader().displayImage("file://" + path, image);
	}

	@Override
	protected void onDestroy() {
		if (bitmap != null) {
			bitmap.recycle();
		}
		super.onDestroy();
	}

}
