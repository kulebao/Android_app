package com.djc.logintest.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.djc.logintest.R;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.utils.ImageDownloader;

public class ShowIconActivity extends Activity {

	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_icon);
		showIcon();
		initBtn();
	}

	private void initBtn() {
		TextView send = (TextView) findViewById(R.id.send);
		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});

		TextView cancel = (TextView) findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ShowIconActivity.this.finish();
			}
		});
	}

	private void showIcon() {
		ImageView image = (ImageView) findViewById(R.id.imageview);
		String path = getIntent().getStringExtra(ConstantValue.TMP_CHAT_PATH);

		int maxPixel = ImageDownloader.getMaxPix();

		bitmap = ImageDownloader.getResizedBmp(maxPixel, path);
		image.setImageBitmap(bitmap);
	}

	@Override
	protected void onDestroy() {
		bitmap.recycle();
		super.onDestroy();
	}

}
