package com.cocobabys.lbs;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.cocobabys.R;
import com.cocobabys.activities.UmengStatisticsActivity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 此demo用来展示如何在地图上用GraphicsOverlay添加点、线、多边形、圆 同时展示如何在地图上用TextOverlay添加文字
 * 
 */
public class LbsFence extends UmengStatisticsActivity {

	// 地图相关
	MapView mMapView;
	BaiduMap mBaiduMap;
	// UI相关
	Button resetBtn;
	Button clearBtn;
	private LatLng initCenter = new LatLng(30.539591, 104.079256);
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lbs_activity_fence);
		// 初始化地图
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// UI初始化
		clearBtn = (Button) findViewById(R.id.button1);
		resetBtn = (Button) findViewById(R.id.button2);

		OnClickListener clearListener = new OnClickListener() {
			public void onClick(View v) {
				clearClick();
			}
		};
		OnClickListener restListener = new OnClickListener() {
			public void onClick(View v) {
				resetClick();
			}
		};

		clearBtn.setOnClickListener(clearListener);
		resetBtn.setOnClickListener(restListener);

		// 界面加载时添加绘制图层
		addCustomElementsDemo(initCenter);

		mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {

			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {

			}

			@Override
			public void onMapStatusChangeFinish(MapStatus arg0) {
				mMapView.getMap().clear();
				LatLng la = mBaiduMap.getMapStatus().target;// 屏幕中心点经纬度
				addCustomElementsDemo(la);
			}

			@Override
			public void onMapStatusChange(MapStatus arg0) {

			}
		});
		
		// 设置起点为地图中心
		MapStatus status = new MapStatus.Builder().target(initCenter).zoom(13.0f).build();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
	}

	/**
	 * 添加点、线、多边形、圆、文字
	 * @param llCircle 
	 */
	public void addCustomElementsDemo(LatLng llCircle) {
		// 添加圆
		OverlayOptions ooCircle = new CircleOptions().fillColor(0x000000FF).center(llCircle)
				.stroke(new Stroke(5, 0xAA000000)).radius(500);
		mBaiduMap.addOverlay(ooCircle);

		LatLng llDot = new LatLng(39.98923, 116.397428);
		OverlayOptions ooDot = new DotOptions().center(llDot).radius(6).color(0xFF0000FF);
		mBaiduMap.addOverlay(ooDot);
	}

	public void resetClick() {
		// 添加绘制元素
		addCustomElementsDemo(initCenter);
	}

	public void clearClick() {
		// 清除所有图层
		mMapView.getMap().clear();
	}

	@Override
	public void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mMapView.onDestroy();
		super.onDestroy();
	}

}
