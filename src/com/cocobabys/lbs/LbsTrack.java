package com.cocobabys.lbs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatus.Builder;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.cocobabys.R;
import com.cocobabys.activities.UmengStatisticsActivity;
import com.cocobabys.bean.LocationInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.GetLocatorCoorHistoryJob;
import com.cocobabys.threadpool.MyThreadPoolMgr;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 此demo用来展示如何在地图上用GraphicsOverlay添加点、线、多边形、圆 同时展示如何在地图上用TextOverlay添加文字
 * 
 */
public class LbsTrack extends UmengStatisticsActivity {

	protected static final int GO_NEXT = 100;

	protected static final int STOP = 200;

	private static final int PAUSE = 300;

	private ProgressDialog dialog;

	// 地图相关
	MapView mMapView;
	BaiduMap mBaiduMap;
	// UI相关
	Button stopBtn;
	Button runBtn;
	private List<LatLng> points = new ArrayList<LatLng>();
	// 初始化全局 bitmap 信息，不用时及时 recycle
	private BitmapDescriptor bdA = BitmapDescriptorFactory
			.fromResource(R.drawable.lbs_icon_marka);
	private Marker currentMarker = null;
	private int currentMarkerIndex = 0;
	private ScheduledExecutorService service;
	private ScheduledFuture<?> future;

	private Handler handler;
	private boolean firstUse = true;
	private List<LocInfo> lList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lbs_activity_track);
		initViews();

		// 界面加载时添加绘制图层
		// addCustomElementsDemo();

		initPlayRecordTask();

		initHandler();

		runGetHistoryLocTask();
	}

	private void runGetHistoryLocTask() {
		dialog.show();
		GetLocatorCoorHistoryJob coorHistoryJob = new GetLocatorCoorHistoryJob(
				handler);
		coorHistoryJob.execute();
	}

	private void initViews() {
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

		initProgressDlg();
	}

	private void initProgressDlg() {
		dialog = new ProgressDialog(this);
		dialog.setMessage(getResources().getString(R.string.loading_data));
	}

	private void initHandler() {
		handler = new MyHandler(LbsTrack.this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				if (LbsTrack.this.isFinishing()) {
					Log.w("TrackDemoDJC isFinishing",
							"handleMessage donothing msg=" + msg.what);
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case GO_NEXT:
					doGoNext();
					break;
				case EventType.GET_HISTORY_LOCATION_SUCCESS:
					hanldeGetHistoryLocSuccess(msg);
					break;
				case EventType.GET_HISTORY_LOCATION_FAIL:
					Utils.makeToast(
							LbsTrack.this,
							Utils.getResString(R.string.lbs_get_history_location_fail));
					LbsTrack.this.finish();
					break;
				default:
					break;
				}
			}
		};
	}

	protected void hanldeGetHistoryLocSuccess(Message msg) {
		@SuppressWarnings("unchecked")
		List<LocationInfo> list = (List<LocationInfo>) msg.obj;
		if (list == null || list.isEmpty()) {
			Utils.makeToast(LbsTrack.this, Utils
					.getResString(R.string.lbs_get_history_location_is_empty));
			LbsTrack.this.finish();
			return;
		}

		list = removeSameLoc(list);

		lList = getLocInfoList(list);

		addCustomElements(lList);
		setCenter();
	}

	private List<LocInfo> getLocInfoList(List<LocationInfo> list) {
		List<LocInfo> locInfos = new ArrayList<LbsTrack.LocInfo>();

		Iterator<LocationInfo> iterator = list.iterator();

		LocationInfo pre = null;
		LocationInfo next = null;

		while (iterator.hasNext()) {
			next = iterator.next();

			if (pre != null) {
				LocInfo object = new LocInfo();
				object.info = pre;
				object.timeStay = next.getTimestamp() - pre.getTimestamp();
				locInfos.add(object);
			}

			pre = next;
		}

		// 加上最后一个节点
		LocInfo object = new LocInfo();
		object.info = next;
		object.timeStay = -1;
		locInfos.add(object);

		return locInfos;
	}

	// 如果相邻2个节点的坐标一致，就去掉，这表示定位器没有移动
	private List<LocationInfo> removeSameLoc(List<LocationInfo> list) {
		Iterator<LocationInfo> iterator = list.iterator();
		LocationInfo pre = null;

		while (iterator.hasNext()) {
			LocationInfo next = iterator.next();
			if (next.equals(pre)) {
				iterator.remove();
				continue;
			}
			pre = next;
		}
		return list;
	}

	protected synchronized void doPause() {
		runBtn.setText("播放");
		future.cancel(true);
	}

	private void initPlayRecordTask() {
		service = MyThreadPoolMgr.getGenericService();
	}

	public void showWindow(LatLng point, Button button) {
		// mBaiduMap.hideInfoWindow();
		InfoWindow mInfoWindow = new InfoWindow(
				BitmapDescriptorFactory.fromView(button), point, -52, null);
		mBaiduMap.showInfoWindow(mInfoWindow);
	}

	private Button getWindowBtn(LocInfo locInfo) {
		LocationInfo info = locInfo.info;
		StringBuffer buffer = new StringBuffer(Utils.convertTime(info
				.getTimestamp()));
		buffer.append("\n");
		buffer.append("速度:");
		buffer.append(DataUtils.convertSpeed(info.getSpeed()));
		buffer.append("km/h");

		if (locInfo.timeStay != -1) {
			buffer.append("\n");
			buffer.append("停留时长：" + getStayTime(locInfo.timeStay));
		}

		String content = buffer.toString();

		Button button = new Button(getApplicationContext());
		button.setBackgroundResource(R.drawable.lbs_popup);
		button.setTextColor(android.graphics.Color.BLACK);
		button.setText(content);

		button.setTextSize(14.0f);
		return button;
	}

	private String getStayTime(long timeStay) {
		StringBuffer buffer = new StringBuffer();

		long seconds = timeStay / 1000;

		long hour = seconds / 3600;

		long minute = (seconds % 3600) / 60;

		if (hour > 0) {
			buffer.append(hour + "小时");
		}

		if (hour == 0 && minute == 0) {
			minute = 1;
		}

		if (minute > 0) {
			buffer.append(minute + "分");
		}

		return buffer.toString();
	}

	/**
	 * 添加点、线、多边形、圆、文字
	 */
	public void addCustomElements(List<LocInfo> locInfos) {

		Log.d("", "BBB addCustomElements size=" + locInfos.size());

		for (LocInfo locInfo : locInfos) {
			LatLng coor = DataUtils.getCoor(locInfo.info.getLatitude(),locInfo.info.getLongitude());
			points.add(coor);
		}

		// 只有一个点，说明定位器从始至终就没有动过，无法画线,必须大于1才可以
		if (locInfos.size() > 1) {
			OverlayOptions ooPolyline = new PolylineOptions().width(5)
					.color(0xAAFF0000).points(points);
			mBaiduMap.addOverlay(ooPolyline);
		}
	}

	// private void someElse(LatLng p1, LatLng p2, LatLng p3) {
	// // 添加弧线
	// OverlayOptions ooArc = new
	// ArcOptions().color(0xAA00FF00).width(4).points(p1, p2, p3);
	// mBaiduMap.addOverlay(ooArc);
	// // 添加圆
	// LatLng llCircle = new LatLng(39.90923, 116.447428);
	// OverlayOptions ooCircle = new
	// CircleOptions().fillColor(0x000000FF).center(llCircle)
	// .stroke(new Stroke(5, 0xAA000000)).radius(1400);
	// mBaiduMap.addOverlay(ooCircle);
	//
	// LatLng llDot = new LatLng(39.98923, 116.397428);
	// OverlayOptions ooDot = new
	// DotOptions().center(llDot).radius(6).color(0xFF0000FF);
	// mBaiduMap.addOverlay(ooDot);
	// // 添加多边形
	// LatLng pt1 = new LatLng(39.93923, 116.357428);
	// LatLng pt2 = new LatLng(39.91923, 116.327428);
	// LatLng pt3 = new LatLng(39.89923, 116.347428);
	// LatLng pt4 = new LatLng(39.89923, 116.367428);
	// LatLng pt5 = new LatLng(39.91923, 116.387428);
	// List<LatLng> pts = new ArrayList<LatLng>();
	// pts.add(pt1);
	// pts.add(pt2);
	// pts.add(pt3);
	// pts.add(pt4);
	// pts.add(pt5);
	// OverlayOptions ooPolygon = new PolygonOptions().points(pts).stroke(new
	// Stroke(5, 0xAA00FF00))
	// .fillColor(0xAAFFFF00);
	// mBaiduMap.addOverlay(ooPolygon);
	// // 添加文字
	// LatLng llText = new LatLng(39.86923, 116.397428);
	// OverlayOptions ooText = new
	// TextOptions().bgColor(0xAAFFFF00).fontSize(24).fontColor(0xFFFF00FF)
	// .text("百度地图SDK").rotate(-30).position(llText);
	// mBaiduMap.addOverlay(ooText);
	// }

	public void stopClick() {
		if (currentMarkerIndex != 0) {
			doStop();
		}
	}

	public void runClick() {
		if (points.isEmpty()) {
			Utils.makeToast(LbsTrack.this, Utils
					.getResString(R.string.lbs_get_history_location_is_empty));
			return;
		}

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
		setCenter();
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
		LatLng latLng = points.get(currentMarkerIndex);
		LocInfo locInfo = lList.get(currentMarkerIndex);

		showMarker(latLng);
		showWindow(latLng, getWindowBtn(locInfo));
	}

	private void showMarker(LatLng point) {
		if (currentMarker != null) {
			currentMarker.remove();
		}
		OverlayOptions ooA = new MarkerOptions().position(point).icon(bdA);
		currentMarker = (Marker) (mBaiduMap.addOverlay(ooA));
		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(point));
	}

	private void setCenter() {
		if (currentMarker != null) {
			currentMarker.remove();
		}
		LatLng latLng = points.get(0);
		LocInfo locInfo = lList.get(0);

		OverlayOptions ooA = new MarkerOptions().position(latLng).icon(bdA);
		currentMarker = (Marker) (mBaiduMap.addOverlay(ooA));

		// 设置起点为地图中心
		Builder builder = new MapStatus.Builder().target(latLng);

		if (firstUse) {
			builder.zoom(15.0f);
			firstUse = false;
		}

		MapStatus status = builder.build();

		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));

		showWindow(latLng, getWindowBtn(locInfo));
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
		bdA.recycle();
		if (future != null) {
			future.cancel(true);
		}
	}

	private static class LocInfo {
		private LocationInfo info;
		private long timeStay;
	}

}
