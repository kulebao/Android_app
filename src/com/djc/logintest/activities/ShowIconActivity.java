package com.djc.logintest.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.djc.logintest.R;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.utils.ImageDownloader;

public class ShowIconActivity extends Activity {

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

		int maxPixel = ImageDownloader.getMaxPix();

		bitmap = ImageDownloader.getResizedBmp(maxPixel, path);
		image.setImageBitmap(bitmap);
	}

	@Override
	protected void onDestroy() {
		if (bitmap != null) {
			bitmap.recycle();
		}
		super.onDestroy();
	}

}
