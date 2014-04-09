package com.cocobabys.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.utils.Utils;

public class UpdateActivity extends UmengStatisticsActivity {
	private String url;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update);
		initView();
	}

	private void initView() {
		initTextview();

		initBtn();
	}

	private void initBtn() {
		Button updateBtn = (Button) findViewById(R.id.updateBtn);
		updateBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Log.d("DJC GET ", "url = " + url);
					Uri uri = Uri.parse(url);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
					UpdateActivity.this.finish();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(UpdateActivity.this, "更新失败，非法地址:" + url, Toast.LENGTH_SHORT).show();
				}
			}
		});

		Button cancelUpdateBtn = (Button) findViewById(R.id.cancelUpdateBtn);
		cancelUpdateBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UpdateActivity.this.finish();
			}
		});
	}

	// 在ChechUpdateMethod中保存到文件，这里直接从文件获取
	public void initTextview() {
		url = Utils.getProp(JSONConstant.UPDATE_URL);

		String content = Utils.getProp(JSONConstant.UPDATE_CONTENT);
		content = content.replace("\\n", "\n");
		String versionName = Utils.getProp(JSONConstant.UPDATE_VERSION_NAME);
		long fileSize = Long.valueOf(Utils.getProp(JSONConstant.FILE_SIZE));

		TextView version = (TextView) findViewById(R.id.versionNameContent);
		version.setText(versionName);

		TextView size = (TextView) findViewById(R.id.sizecontent);
		size.setText((fileSize / 1024) + "k");

		TextView summarycontent = (TextView) findViewById(R.id.summarycontent);
		summarycontent.setText(content);

		// buffer.append("版本号 " + versionName + "\n");
		// buffer.append("版本大小 " + (fileSize / 1024) + "k" + "\n");
		// buffer.append("更新说明:\n");
		// buffer.append(content);
	}
}
