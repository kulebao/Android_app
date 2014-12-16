package com.cocobabys.lbs;

import android.widget.Button;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

public class LbsUtils {

	public static void drawPop(LatLng point, Button button, BaiduMap mBaiduMap, OverlayOptions option) {
		InfoWindow mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), point, -52, null);
		mBaiduMap.addOverlay(option);
		mBaiduMap.showInfoWindow(mInfoWindow);
	}
}
