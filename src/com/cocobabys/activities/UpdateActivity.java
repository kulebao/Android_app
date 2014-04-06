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
        String text = getInfo();
        Log.d("DJC UpdateActivity", "text =" + text);
        TextView textView = (TextView) findViewById(R.id.updateTextView);
        textView.setText(text);

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
                    Toast.makeText(UpdateActivity.this, "更新失败，非法地址:" + url, Toast.LENGTH_SHORT);
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
    public String getInfo() {
        StringBuffer buffer = new StringBuffer();
        url = Utils.getProp(JSONConstant.UPDATE_URL);

        String content = Utils.getProp(JSONConstant.UPDATE_CONTENT);
        Log.d("DJC UpdateActivity", "bef content =" + content);
        content = content.replace("\\n", "\n");
        Log.d("DJC UpdateActivity", "content =" + content);
        String versionName = Utils.getProp(JSONConstant.UPDATE_VERSION_NAME);
        long fileSize = Long.valueOf(Utils.getProp(JSONConstant.FILE_SIZE));

        buffer.append("版本号 " + versionName + "\n");
        buffer.append("版本大小 " + (fileSize / 1024) + "k" + "\n");
        buffer.append("更新说明:\n");
        buffer.append(content);
        return buffer.toString();
    }
}
