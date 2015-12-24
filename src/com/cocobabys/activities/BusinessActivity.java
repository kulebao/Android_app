package com.cocobabys.activities;

import rx.subjects.ReplaySubject;
import rx.subjects.Subject;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.cocobabys.R;
import com.cocobabys.fragment.ActionFragment;
import com.cocobabys.fragment.ShopFragment;
import com.umeng.analytics.MobclickAgent;

public class BusinessActivity extends FragmentActivity{
    private static final int        ACTION              = 0;
    private static final int        MERCHANT            = 1;

    private int                     currentIndex        = ACTION;
    private ImageView               actionView;
    private ImageView               shopView;

    // 每15秒上报一次我的位置
    private static final int        LOCATION_SCAN_SPAN  = 1 * 1000;
    private LocationClient          mLocClient;
    private BDLocationListener      myListener          = new MyLocationListenner();
    // 设置 百度标准坐标系
    private static final String     BAIDU_STANDARD_COOR = "bd09ll";
    private LatLng                  start               = null;
    private Subject<LatLng, LatLng> locationSubsciber;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business);

        locationSubsciber = ReplaySubject.create();

        initView();
        Log.d("", "MainActivity action");
        moveToAction();
        initLocationClient();
    }

    public Subject<LatLng, LatLng> getLocationSubsciber(){
        return locationSubsciber;
    }

    private void initLocationClient(){
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType(BAIDU_STANDARD_COOR);
        // 返回具体位置信息
        option.setIsNeedAddress(true);
        option.setScanSpan(LOCATION_SCAN_SPAN);
        mLocClient.setLocOption(option);

        mLocClient.start();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            if(mLocClient != null){
                mLocClient.stop();
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        MobclickAgent.onResume(this); // 统计时长
    }

    @Override
    public void onPause(){
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void initView(){
        actionView = (ImageView)findViewById(R.id.actionView);
        shopView = (ImageView)findViewById(R.id.shopView);
    }

    private void moveToAction(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        ActionFragment fragment = new ActionFragment();
        transaction.replace(R.id.djc_fragment, fragment);
        transaction.commit();
    }

    private void moveToShop(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        ShopFragment fragment = new ShopFragment();
        transaction.replace(R.id.djc_fragment, fragment);
        transaction.commit();
    }

    public void action(View view){
        Log.d("", "action currentIndex=" + currentIndex);
        if(currentIndex == ACTION){
            return;
        }

        actionView.setImageResource(R.drawable.action1);
        shopView.setImageResource(R.drawable.shop0);

        moveToAction();
        currentIndex = ACTION;
    }

    public void shop(View view){
        Log.d("", "shop currentIndex=" + currentIndex);

        if(currentIndex == MERCHANT){
            return;
        }

        actionView.setImageResource(R.drawable.action0);
        shopView.setImageResource(R.drawable.shop1);

        moveToShop();
        currentIndex = MERCHANT;
    }

    private class MyLocationListenner implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation location){
            if(location == null){
                // 考虑失败是否需要重新获取
                Log.w("", "未获取到本机位置");
                return;
            }

            if(start != null){
                Log.w("", "已经获取到位置！");
                return;
            }

            start = new LatLng(location.getLatitude(), location.getLongitude());
            Log.w("", "AAA send start");
            locationSubsciber.onNext(start);
            mLocClient.stop();
        }
    }
}
