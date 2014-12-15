package com.cocobabys.lbs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.cocobabys.R;

/**
 * 此demo用来展示如何在地图上用GraphicsOverlay添加点、线、多边形、圆 同时展示如何在地图上用TextOverlay添加文字
 * 
 */
public class LbsTrack extends Activity {

	protected static final int GO_NEXT = 100;

	protected static final int STOP = 200;

	private static final int PAUSE = 300;

	// 地图相关
	MapView mMapView;
	BaiduMap mBaiduMap;
	// UI相关
	Button stopBtn;
	Button runBtn;
	private List<LatLng> points = new ArrayList<LatLng>();
	// 初始化全局 bitmap 信息，不用时及时 recycle
	private BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.drawable.lbs_icon_marka);
	private Marker currentMarker = null;
	private int currentMarkerIndex = 0;
	private ScheduledExecutorService service;
	private ScheduledFuture<?> future;

	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lbs_activity_track);
		// 初始化地图
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// UI初始化
		runBtn = (Button) findViewById(R.id.run);
		stopBtn = (Button) findViewById(R.id.stop);

		OnClickListener runListener = new OnClickListener() {
			public void onClick(View v) {
				runClick();
			}
		};
		OnClickListener stopListener = new OnClickListener() {
			public void onClick(View v) {
				stopClick();
			}
		};

		runBtn.setOnClickListener(runListener);
		stopBtn.setOnClickListener(stopListener);

		// 界面加载时添加绘制图层
		addCustomElementsDemo();

		initMarker();

		initTask();

		initHandler();
	}

	private void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (LbsTrack.this.isFinishing()) {
					Log.w("TrackDemoDJC isFinishing", "handleMessage donothing msg=" + msg.what);
					return;
				}

				switch (msg.what) {
				case GO_NEXT:
					doGoNext();
					break;
				default:
					break;
				}
			}
		};
	}

	protected synchronized void doPause() {
		runBtn.setText("播放");
		future.cancel(true);
	}

	private void initTask() {
		service = Executors.newScheduledThreadPool(10);
	}

	private void initMarker() {
		if (currentMarker != null) {
			currentMarker.remove();
		}
		OverlayOptions ooA = new MarkerOptions().position(points.get(0)).icon(bdA);
		currentMarker = (Marker) (mBaiduMap.addOverlay(ooA));

		setCenter();
	}

	private void setCenter() {
		// 设置起点为地图中心
		MapStatus status = new MapStatus.Builder().target(points.get(0)).zoom(13.0f).build();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
	}

	/**
	 * 添加点、线、多边形、圆、文字
	 */
	public void addCustomElementsDemo() {
		// 添加折线
		double original_lat = 30.539591;
		double original_lon = 104.079256;

		LatLng p1 = new LatLng(original_lat, original_lon);
		LatLng p2 = new LatLng(original_lat + 0.005, original_lon + 0.012);
		LatLng p3 = new LatLng(original_lat + 0.011, original_lon + 0.008);
		LatLng p4 = new LatLng(original_lat - 0.012, original_lon - 0.07);
		LatLng p5 = new LatLng(original_lat + 0.004, original_lon + 0.05);
		LatLng p6 = new LatLng(original_lat + 0.003, original_lon + 0.06);

		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);
		points.add(p5);
		points.add(p6);
		OverlayOptions ooPolyline = new PolylineOptions().width(5).color(0xAAFF0000).points(points);
		mBaiduMap.addOverlay(ooPolyline);
		// someElse(p1, p2, p3);
	}

	private void someElse(LatLng p1, LatLng p2, LatLng p3) {
		// 添加弧线
		OverlayOptions ooArc = new ArcOptions().color(0xAA00FF00).width(4).points(p1, p2, p3);
		mBaiduMap.addOverlay(ooArc);
		// 添加圆
		LatLng llCircle = new LatLng(39.90923, 116.447428);
		OverlayOptions ooCircle = new CircleOptions().fillColor(0x000000FF).center(llCircle)
				.stroke(new Stroke(5, 0xAA000000)).radius(1400);
		mBaiduMap.addOverlay(ooCircle);

		LatLng llDot = new LatLng(39.98923, 116.397428);
		OverlayOptions ooDot = new DotOptions().center(llDot).radius(6).color(0xFF0000FF);
		mBaiduMap.addOverlay(ooDot);
		// 添加多边形
		LatLng pt1 = new LatLng(39.93923, 116.357428);
		LatLng pt2 = new LatLng(39.91923, 116.327428);
		LatLng pt3 = new LatLng(39.89923, 116.347428);
		LatLng pt4 = new LatLng(39.89923, 116.367428);
		LatLng pt5 = new LatLng(39.91923, 116.387428);
		List<LatLng> pts = new ArrayList<LatLng>();
		pts.add(pt1);
		pts.add(pt2);
		pts.add(pt3);
		pts.add(pt4);
		pts.add(pt5);
		OverlayOptions ooPolygon = new PolygonOptions().points(pts).stroke(new Stroke(5, 0xAA00FF00))
				.fillColor(0xAAFFFF00);
		mBaiduMap.addOverlay(ooPolygon);
		// 添加文字
		LatLng llText = new LatLng(39.86923, 116.397428);
		OverlayOptions ooText = new TextOptions().bgColor(0xAAFFFF00).fontSize(24).fontColor(0xFFFF00FF)
				.text("百度地图SDK").rotate(-30).position(llText);
		mBaiduMap.addOverlay(ooText);
	}

	public void stopClick() {
		if (currentMarkerIndex != 0) {
			doStop();
		}
	}

	public void runClick() {
		if ("播放".equals(runBtn.getText().toString())) {
			runBtn.setText("暂停");
			runTask();
		} else {
			runBtn.setText("播放");
			doPause();
		}
	}

	private void runTask() {
		future = service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				handler.sendEmptyMessage(GO_NEXT);
			}

		}, 0, 3, TimeUnit.SECONDS);
	}

	private synchronized void doStop() {
		currentMarkerIndex = 0;
		initMarker();
		runBtn.setText("播放");
		boolean cancel = future.cancel(true);
		Log.w("doStop", "doStop cancel=" + cancel);
	}

	private synchronized void doGoNext() {
		if (currentMarkerIndex >= points.size() - 1) {
			doStop();
			return;
		}

		currentMarkerIndex++;
		OverlayOptions ooA = new MarkerOptions().position(points.get(currentMarkerIndex)).icon(bdA);
		currentMarker.remove();
		currentMarker = (Marker) (mBaiduMap.addOverlay(ooA));
		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(points.get(currentMarkerIndex)));
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mMapView.onDestroy();
		super.onDestroy();
		service.shutdownNow();
		bdA.recycle();
	}

}
