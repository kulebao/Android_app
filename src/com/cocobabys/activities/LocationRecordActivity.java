package com.cocobabys.activities;
import java.util.List;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.cocobabys.R;
import com.cocobabys.adapter.LocationInfoListAdapter;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.LocationInfo;
import com.cocobabys.utils.Utils;

public class LocationRecordActivity extends UmengStatisticsActivity {

	private ListView list;
	private LocationInfoListAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_list);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this,
				R.string.location_record);
		initListAdapter();
	}

	private void initListAdapter() {
		List<LocationInfo> listinfo = DataMgr.getInstance().getLocationInfos();
		adapter = new LocationInfoListAdapter(this, listinfo);
		list = (ListView) findViewById(R.id.location_list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				LocationInfo info = (LocationInfo) adapter.getItem(position);
				startToLocationActivity(info);
			}
		});
	}

	private void startToLocationActivity(LocationInfo info) {
		Intent intent = new Intent(this, LocationActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		intent.putExtra(JSONConstant.LATITUDE, info.getLatitude());
		intent.putExtra(JSONConstant.LONGITUDE, info.getLongitude());
		intent.putExtra(JSONConstant.ADDRESS, info.getAddress());
		intent.putExtra(JSONConstant.TIME_STAMP, info.getTimestamp());
		intent.putExtra(JSONConstant.LBS_NUM, info.getLbs_num());
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, Menu.FIRST, 1, R.string.remove_all);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == Menu.FIRST) {
			Utils.showTwoBtnResDlg(R.string.delete_all_location_confirm, this,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// DataMgr.getInstance().clearLocationTable();
							adapter.clear();
						}
					});
		}
		return true;
	}
}