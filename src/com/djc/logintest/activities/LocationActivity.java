package com.djc.logintest.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.djc.logintest.R;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.BindedNumInfo;

public class LocationActivity extends Activity {
    private static final int MIN_ZOOM = 12;
    private static final int MAX_ZOOM = 19;
    private WebView myWebView;
    private ProgressDialog progressDialog;
    // 默认zoom
    private int zoom = 16;
    // center后面第一个参数是经度，第二个参数是纬度,表示地图以此坐标为中心
    // markers后面第一个参数是经度，第二个参数是纬度，表示地图上对该坐标进行标注
    // 该地址固定
    private String constantUrl = "http://api.map.baidu.com/staticimage?"
            + "center=%s,%s&markers=%s,%s&markerStyles=l,0";

    private String url = "";
    private String address;
    private String lbs_num;

    // private String url = "http://api.map.baidu.com/staticimage?"
    // + "center=%s,%s&zoom=%d&markers=%s,%s&markerStyles=l,0";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);
        genUrl();
        initProgressDlg();
        initWebView();
        initView();
        myWebView.loadUrl(String.format(url, zoom));
    }

    private void genUrl() {
        Intent intent = getIntent();
        String latitude = intent.getStringExtra(JSONConstant.LATITUDE);
        String longitude = intent.getStringExtra(JSONConstant.LONGITUDE);
        address = intent.getStringExtra(JSONConstant.ADDRESS);
        String timestamp = intent.getStringExtra(JSONConstant.TIME_STAMP);
        lbs_num = intent.getStringExtra(JSONConstant.LBS_NUM);
        constantUrl = String.format(constantUrl, longitude, latitude, longitude, latitude);
        url = constantUrl + "&zoom=%d";
    }

    public void initWebView() {
        myWebView = (WebView) findViewById(R.id.webView);

        myWebView.setWebViewClient(new WebViewClient() {
            // 打开链接前的事件
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("WebViewClient", "shouldOverrideUrlLoading");
                return super.shouldOverrideUrlLoading(view, url);
            }

            // 载入页面开始的事件
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d("WebViewClient", "onPageStarted");
                progressDialog.show();
            }

            // 载入页面完成的事件
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("WebViewClient", "onPageFinished");
                progressDialog.cancel();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description,
                    String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.d("WebViewClient", "onReceivedError");
            }

        });
    }

    public void initProgressDlg() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
    }

    private void initView() {
        initTitle();

        Button bigBtn = (Button) findViewById(R.id.big);
        Button smallBtn = (Button) findViewById(R.id.small);

        bigBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBigBtn();
            }
        });

        smallBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSmallBtn();
            }
        });
    }

    public void initTitle() {
        TextView locationInfoView = (TextView) findViewById(R.id.locationInfo);
        BindedNumInfo bindedNumInfoByNum = DataMgr.getInstance().getBindedNumInfoByNum(lbs_num);
        String title = lbs_num + " " + bindedNumInfoByNum.getNickname() + " \n" + address;
        locationInfoView.setText(title);
    }

    protected void handleSmallBtn() {
        if (zoom >= MAX_ZOOM) {
            Toast.makeText(this, "已经到最小", Toast.LENGTH_SHORT).show();
            return;
        }
        zoom++;
        myWebView.loadUrl(String.format(url, zoom));
    }

    protected void handleBigBtn() {
        if (zoom <= MIN_ZOOM) {
            Toast.makeText(this, "已经到最大", Toast.LENGTH_SHORT).show();
            return;
        }
        zoom--;
        myWebView.loadUrl(String.format(url, zoom));
    }

    @Override
    protected void onDestroy() {
        myWebView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("", "onNewIntent");
        super.onNewIntent(intent);
        zoom = 16;
        genUrl();
        initTitle();
        myWebView.loadUrl(String.format(url, zoom));
    }

    @Override
    protected void onResume() {
        Log.d("", "onResume");
        super.onResume();
    }

}