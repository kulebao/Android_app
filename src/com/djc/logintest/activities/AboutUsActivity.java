package com.djc.logintest.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

import com.djc.logintest.R;

public class AboutUsActivity extends UmengStatisticsActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.about_us);
        setVersion();
    }

    private void setVersion() {
        TextView versionView = (TextView) findViewById(R.id.version);
        versionView.setText(getVersionName());
    }

    private String getVersionName() {
        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        String version = "";
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            version = packInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }
}
