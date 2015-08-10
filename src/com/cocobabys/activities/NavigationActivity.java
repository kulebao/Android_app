package com.cocobabys.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.OpenClientUtil;
import com.baidu.mapapi.utils.route.BaiduMapRoutePlan;
import com.baidu.mapapi.utils.route.RouteParaOption;
import com.cocobabys.R;
import com.cocobabys.bean.BusinessInfo;
import com.cocobabys.utils.Utils;

public class NavigationActivity extends UmengStatisticsActivity {
	// 单位毫秒
	private static final int GET_LOC_TIME_SPAN = 2000;

	private static final String COOR_TYPE = "bd09ll";

	private LatLng myLoc = null;
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LatLng end;
	private boolean isFirstLoc = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	public void setEndPoint(BusinessInfo info) {
		// end = new LatLng(30.541362, 104.075397);
		end = null;
		if (info.getLocation().isValid()) {
			end = new LatLng(info.getLocation().getLatitude(), info.getLocation().getLongitude());
		}

	}

	private void init() {
		initLocationClient();
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
		Log.d("", "start mLocClient");
		mLocClient.start();
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				Log.d("", "location == null return");
				return;
			}

			Log.d("", "onReceiveLocation location=" + location.getAltitude() + "," + location.getLongitude());

			if (isFirstLoc) {
				isFirstLoc = false;
				myLoc = new LatLng(location.getLatitude(), location.getLongitude());

				Log.d("", "mLocClient stop");
				// 获取到位置后，就无须再定位
				mLocClient.stop();
				mLocClient.unRegisterLocationListener(myListener);
				// startRoutePlanDriving();
				// NavigationActivity.this.finish();
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	public void startRoutePlanDriving() {
		if (myLoc == null) {
			Utils.makeToast(this, R.string.invalid_myloc);
			Log.d("", "startRoutePlanDriving do nothing myLoc= is null");
			return;
		}

		if (end == null) {
			Utils.makeToast(this, R.string.invalid_targetloc);
			Log.d("", "startRoutePlanDriving do nothing endLoc is null");
			return;
		}

		Log.d("", "startRoutePlanDriving myLoc=" + myLoc.latitude + "," + myLoc.longitude);
		Log.d("", "startRoutePlanDriving end=" + end.latitude + "," + end.longitude);

		// LatLng pt_start = new LatLng(34.264642646862, 108.95108518068);

		// 构建 route搜索参数
		RouteParaOption para = new RouteParaOption().startPoint(myLoc)
		// .startName("天安门")
				.endPoint(end);
		// .endName("大雁塔").cityName("西安");

		// RouteParaOption para = new RouteParaOption()
		// .startName("天安门").endName("百度大厦");

		// RouteParaOption para = new RouteParaOption()
		// .startPoint(pt_start).endPoint(pt_end);

		try {
			BaiduMapRoutePlan.openBaiduMapDrivingRoute(para, this);
		} catch (Exception e) {
			Log.d("", "e =" + e.toString());
			e.printStackTrace();
			showDialog();
		}

	}

	/**
	 * 提示未安装百度地图app或app版本过低
	 * 
	 */
	public void showDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				OpenClientUtil.getLatestBaiduMapApp(NavigationActivity.this);
			}
		});

		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mLocClient.unRegisterLocationListener(myListener);
		// 退出时销毁定位
		mLocClient.stop();
	}

}
