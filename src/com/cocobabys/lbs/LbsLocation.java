package com.cocobabys.lbs;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatus.Builder;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult.AddressComponent;
import com.baidu.mapapi.utils.DistanceUtil;
import com.cocobabys.R;
import com.cocobabys.activities.UmengStatisticsActivity;
import com.cocobabys.bean.LocationInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.GetLocatorCoorJob;
import com.cocobabys.jobs.RefreshLocationJob;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

/**
 * 此demo用来展示如何结合定位SDK实现定位，并使用MyLocationOverlay绘制定位位置 同时展示如何使用自定义图标绘制并点击时弹出泡泡
 * 
 */
public class LbsLocation extends UmengStatisticsActivity implements
		OnGetGeoCoderResultListener {
	private static final String COOR_TYPE = "bd09ll";
	private static final int GET_LOC_TIME_SPAN = 14000;
	// 固定刷新间隔时间，设置为20s
	private static final int MAX_COUNT_DOWN = 15;
	private LatLng start;
	private LatLng end = new LatLng(30.541591, 104.082256);;
	// 定位相关
	private LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	private BitmapDescriptor mCurrentMarker;
	private GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用

	private MapView mMapView;
	private BaiduMap mBaiduMap;

	// UI相关
	private OnCheckedChangeListener radioButtonListener;
	private Button requestLocButton;
	boolean isFirstLoc = true;// 是否首次定位
	private TextView distanceView;
	private Button changeCircleBtn;
	private TextView locationInfoView;
	private BitmapDescriptor bdA = BitmapDescriptorFactory
			.fromResource(R.drawable.lbs_icon_marka);
	private Handler handler;
	private RefreshLocationJob refreshJob;
	private TextView showtime;
	private GetLocatorCoorJob getCoorJob;
	private MyLocationData locData;
	private Button refreshBtn;
	private boolean firstUse = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lbs_activity_location);
		initHandler();
		initViews();
		initGeoCoder();
		initMap();
		runRefreshTask();
	}

	private void initHandler() {
		handler = new MyHandler(LbsLocation.this) {
			@Override
			public void handleMessage(Message msg) {
				if (LbsLocation.this.isFinishing()) {
					Log.w("TrackDemoDJC isFinishing",
							"handleMessage donothing msg=" + msg.what);
					return;
				}

				switch (msg.what) {
				case EventType.COUNTDOWN_EVENT:
					Log.d("", "AAA showtime.setText msg.arg1=" + msg.arg1);

					showtime.setText(String.format(
							Utils.getResString(R.string.lbs_refresh_time),
							msg.arg1));
					break;
				case EventType.GET_LAST_LOCATION_FAIL:
					Utils.makeToast(LbsLocation.this,
							Utils.getResString(R.string.lbs_get_location_fail));
					handleGetLocFail();
					break;
				case EventType.GET_LAST_LOCATION_SUCCESS:
					handleGetLocSuccess(msg);
					break;
				default:
					break;
				}
			}

		};
	}

	protected void handleGetLocFail() {

	}

	protected void handleGetLocSuccess(Message msg) {
		Log.d("", "AAA handleGetLocSuccess");

		LocationInfo info = (LocationInfo) msg.obj;
		end = DataUtils.getCoor(info);

		mBaiduMap.clear();

		mBaiduMap.setMyLocationData(locData);
		drawLine(start, end);
		drawPop(end, info);

		// MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		// MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(start,
		// 15.0f);
		// mBaiduMap.animateMapStatus(u);

		setDistance();
		getLocationInfo();

		if (isLocatorCenter()) {
			setCenter(end);
		} else {
			setCenter(start);
		}
	}

	private void runRefreshTask() {
		Log.d("DDD ", "AAA runRefreshTask ");
		refreshJob = new RefreshLocationJob(handler, MAX_COUNT_DOWN);
		refreshJob.execute();
	}

	private void initMap() {
		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		// void setScaleControlPosition(Point p)
		// 设置比例尺控件的位置，在 onMapLoadFinish 后生效
		// void setZoomControlsPosition(Point p)
		// 设置缩放控件的位置，在 onMapLoadFinish 后生效
		// void showScaleControl(boolean show)
		// 设置是否显示比例尺控件
		// void showZoomControls(boolean show)
		// 设置是否显示缩放控件
		mBaiduMap = mMapView.getMap();

		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);

		// 是否允许拖动地图，默认允许,false不允许拖动
		// mBaiduMap.getUiSettings().setScrollGesturesEnabled(false);

		// 定位初始化
		initLocationClient();
	}

	private void initViews() {
		showtime = (TextView) findViewById(R.id.showtime);
		distanceView = (TextView) findViewById(R.id.distance);
		locationInfoView = (TextView) findViewById(R.id.locationInfo);

		initRefreshBtn();
		initRequestBtn();
		initGroupBtn();
		initChangeCircleBtn();
	}

	private void initRefreshBtn() {
		refreshBtn = (Button) findViewById(R.id.refresh);
		refreshBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (refreshJob != null) {
					refreshJob.cancel(true);
					runRefreshTask();
				}

				if (getCoorJob != null) {
					getCoorJob.cancel(true);
					getCoorJob = new GetLocatorCoorJob(handler);
					getCoorJob.execute();
				}
			}
		});
	}

	private void initGroupBtn() {
		RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
		radioButtonListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.normalType) {
					mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
				}
				if (checkedId == R.id.satelliteType) {
					mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
				}
			}
		};
		group.setOnCheckedChangeListener(radioButtonListener);
	}

	private void initRequestBtn() {
		requestLocButton = (Button) findViewById(R.id.button1);

		mCurrentMode = LocationMode.NORMAL;
		requestLocButton.setText("普通");

		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					requestLocButton.setText("跟随");
					mCurrentMode = LocationMode.FOLLOWING;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case COMPASS:
					requestLocButton.setText("普通");
					mCurrentMode = LocationMode.NORMAL;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case FOLLOWING:
					requestLocButton.setText("罗盘");
					mCurrentMode = LocationMode.COMPASS;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				}
			}
		};
		requestLocButton.setOnClickListener(btnClickListener);
	}

	private void initLocationClient() {
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		// 百度标准坐标系
		option.setCoorType(COOR_TYPE); // 设置坐标类型
		// 每隔GET_LOC_TIME_SPAN秒上报一次我的位置
		option.setScanSpan(GET_LOC_TIME_SPAN);
		mLocClient.setLocOption(option);
		mLocClient.start();
	}

	private void setCenter(LatLng center) {
		// 设置定位器为地图中心
		// 设置起点为地图中心
		Builder builder = new MapStatus.Builder().target(center);

		if (firstUse) {
			builder.zoom(15.0f);
			firstUse = false;
		}

		MapStatus status = builder.build();

		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
	}

	private void initGeoCoder() {
		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
	}

	private void initChangeCircleBtn() {
		changeCircleBtn = (Button) findViewById(R.id.changeCircle);
		changeCircleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ("以我为中心".equals(changeCircleBtn.getText().toString())) {
					changeCircleBtn.setText("以定位器为中心");
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(start);
					mBaiduMap.animateMapStatus(u);
				} else {
					changeCircleBtn.setText("以我为中心");
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(end);
					mBaiduMap.animateMapStatus(u);

				}
			}
		});
	}

	// 是否是以定位器为中心
	private boolean isLocatorCenter() {
		return "以我为中心".equals(changeCircleBtn.getText().toString());
	}

	private void drawLine(LatLng start, LatLng end) {
		// LatLng pt1 = new LatLng(30.539591, 104.079256);
		// LatLng pt2 = new LatLng(31.539691, 105.079356);
		// LatLng pt3 = new LatLng(39.89923, 116.347428);
		// LatLng pt4 = new LatLng(39.89923, 116.367428);
		// LatLng pt5 = new LatLng(39.91923, 116.387428);
		List<LatLng> pts = new ArrayList<LatLng>();
		pts.add(start);
		pts.add(end);
		// pts.add(pt3);
		// pts.add(pt4);
		// pts.add(pt5);
		OverlayOptions polygonOption = new PolylineOptions().points(pts);
		mBaiduMap.addOverlay(polygonOption);
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;

			locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			start = new LatLng(location.getLatitude(), location.getLongitude());

			Log.d("TTT", "lat =" + location.getLatitude() + "  lon ="
					+ location.getLongitude());
			changeCircleBtn.setVisibility(View.VISIBLE);
			runGetLocatorCoorTask();
			// handleReceiveLocation();
		}
	}

	private void setDistance() {
		double distance = DistanceUtil.getDistance(start, end);
		distanceView.setText("相距 " + (int) distance + "米");
	}

	public void runGetLocatorCoorTask() {
		if (getCoorJob == null || getCoorJob.isDone()) {
			getCoorJob = new GetLocatorCoorJob(handler);
			getCoorJob.execute();
			return;
		}

		if (getCoorJob != null && !getCoorJob.isDone()) {
			// 上次获取定位器坐标的任务未执行完,新任务又来了，这里任务执行失败
			getCoorJob.cancel(true);
			handler.sendEmptyMessage(EventType.GET_LAST_LOCATION_FAIL);
		}
	}

	public void getLocationInfo() {
		mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(end));
	}

	@Override
	public void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	public void drawPop(LatLng point, LocationInfo info) {
		Button button = new Button(getApplicationContext());
		button.setBackgroundResource(R.drawable.lbs_popup);
		button.setTextColor(android.graphics.Color.BLACK);
		String content = getPopContent(info);
		button.setText(content);

		button.setTextSize(14.0f);
		OverlayOptions option = new MarkerOptions().position(point).icon(bdA);
		InfoWindow mInfoWindow = new InfoWindow(
				BitmapDescriptorFactory.fromView(button), point, -52, null);
		mBaiduMap.addOverlay(option);
		mBaiduMap.showInfoWindow(mInfoWindow);
	}

	private String getPopContent(LocationInfo info) {
		StringBuffer buffer = new StringBuffer(Utils.convertTime(info
				.getTimestamp()));
		buffer.append("\n");
		buffer.append("速度:");
		buffer.append(DataUtils.convertSpeed(info.getSpeed()));
		buffer.append("千米/小时");
		String content = buffer.toString();
		return content;
	}

	@Override
	public void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mLocClient.stop();
		super.onDestroy();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		bdA.recycle();
		stopTasks();
	}

	private void stopTasks() {
		if (refreshJob != null) {
			refreshJob.cancel(true);
		}
		if (getCoorJob != null) {
			getCoorJob.cancel(true);
		}
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		Log.d("GeoCodeResult", "result =" + result);
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		StringBuffer content = new StringBuffer("定位器位置:");
		Log.d("onGetReverseGeoCodeResult", "result =" + result);
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			locationInfoView.setText(content.append("未知区域").toString());
			return;
		}

		AddressComponent addressDetail = result.getAddressDetail();
		Log.d("onGetReverseGeoCodeResult", "" + addressDetail.province);
		Log.d("onGetReverseGeoCodeResult", "" + addressDetail.city);
		Log.d("onGetReverseGeoCodeResult", "" + addressDetail.district);
		Log.d("onGetReverseGeoCodeResult", "" + addressDetail.street);
		Log.d("onGetReverseGeoCodeResult", "" + addressDetail.streetNumber);

		Log.d("onGetReverseGeoCodeResult", "" + result.getBusinessCircle());
		content.append(result.getAddress());

		List<PoiInfo> poiList = result.getPoiList();

		if (poiList != null) {
			double minDis = Double.MAX_VALUE;
			PoiInfo shouldShow = null;
			for (PoiInfo info : poiList) {
				double currentDis = DistanceUtil
						.getDistance(end, info.location);

				if (currentDis < minDis) {
					shouldShow = info;
					minDis = currentDis;
				}

				Log.d("onGetReverseGeoCodeResult", "info " + info.name);
				Log.d("onGetReverseGeoCodeResult", "info " + info.address);
			}

			if (shouldShow != null) {
				content.append(".离").append(shouldShow.name).append("约")
						.append((int) minDis).append("米");
			}
		}

		locationInfoView.setText(content.toString());
	}

}
