package com.cocobabys.activities;

import com.cocobabys.R;
import com.cocobabys.utils.ShareUtils;

import android.os.Bundle;
import android.view.View;

public class ShowIconExActivity extends UmengStatisticsActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_icon_ex);
    }

    public void share(View view){
        ShareUtils.shareMsg(this, "请选择分享方式", "", "请用浏览器打开 http://dwz.cn/28iWj2，免费下载【幼乐宝】吧", "");
        // ShareUtils.shareMsgEx(this, "请选择分享方式", "",
        // "请用浏览器打开 http://dwz.cn/28iWj2，免费下载【幼乐宝】吧", "");
    }

}
