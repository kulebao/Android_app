package com.cocobabys.video;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.cocobabys.R;
import com.cocobabys.activities.ActivityHelper;
import com.cocobabys.activities.UmengStatisticsActivity;
import com.cocobabys.adapter.VideoDeviceListAdapter;
import com.cocobabys.utils.Utils;
import com.huamaitel.api.HMDefines.NodeTypeInfo;
import com.huamaitel.api.HMJniInterface;

public class DeviceActivity extends UmengStatisticsActivity {
	private static final String TAG = "DeviceActivity";
	private ListView mListView;
	private List<VideoDeviceInfo> mListData = new ArrayList<VideoDeviceInfo>();
	private VideoDeviceListAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_activity);

		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.watch_baby);

		mListView = (ListView) findViewById(R.id.id_device_list);

		// Get the root of the tree.
		int treeId = VideoApp.treeId;
		int rootId = VideoApp.getJni().getRoot(treeId);

		VideoApp.rootList.clear();
		VideoApp.rootList.add(rootId);

		getChildrenByNodeId(rootId);

		adapter = new VideoDeviceListAdapter(this, mListData);

		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				VideoDeviceInfo info = adapter.getItem(position);
				int nodeType = info.getNodeType();
				Log.i("DeviceActivity:", "nodeType:" + nodeType);
				int nodeId = info.getNodeid();
				Log.i("DeviceActivity:", "nodeId:" + nodeId);

				VideoApp.curNodeHandle = nodeId;
				if (nodeType == NodeTypeInfo.NODE_TYPE_DVS
						|| nodeType == NodeTypeInfo.NODE_TYPE_GROUP) {
					// VideoApp.rootList.add(nodeId);
					// getChildrenByNodeId(nodeId);
					//
					// ((SimpleAdapter) mListView.getAdapter())
					// .notifyDataSetChanged();
				} else if (nodeType == NodeTypeInfo.NODE_TYPE_DEVICE) {
					if (!info.isOnline()) {
						Utils.makeToast(DeviceActivity.this,
								"设备不在线，无法观看，请联系幼儿园处理,谢谢！");
						return;
					}
					Intent intent = new Intent();
					intent.setClass(DeviceActivity.this, PlayActivity.class);
					intent.putExtra("nodeId", nodeId);
					VideoApp.mIsUserLogin = true;
					startActivity(intent);
				}
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (VideoApp.treeId != 0) {
			VideoApp.getJni().releaseTree(VideoApp.treeId);
		}

		if (VideoApp.serverId != 0) {
			VideoApp.getJni().disconnectServer(VideoApp.serverId);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (VideoApp.rootList.size() != 1) {
				int nodeId = VideoApp.rootList
						.get(VideoApp.rootList.size() - 2);
				VideoApp.rootList.remove(VideoApp.rootList.size() - 1);

				getChildrenByNodeId(nodeId);

				((SimpleAdapter) mListView.getAdapter()).notifyDataSetChanged();
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	// Get the children list by this parent node.
	private void getChildrenByNodeId(int nodeId) {
		Log.d(TAG, "getDeviceListByNodeId nodeId: " + nodeId);
		if (nodeId != 0) {
			HMJniInterface sdk = VideoApp.getJni();
			mListData.clear();

			int count = sdk.getChildrenCount(nodeId);
			Log.d(TAG, "getChildrenCount: " + count);
			for (int i = 0; i < count; ++i) {
				int childrenNode = sdk.getChildAt(nodeId, i);
				int nodeType = sdk.getNodeType(childrenNode);

				// if (nodeType == NodeTypeInfo.NODE_TYPE_GROUP) {
				// obj.put("img", R.drawable.folder);
				// } else if (nodeType == NodeTypeInfo.NODE_TYPE_DEVICE) {
				// obj.put("img", R.drawable.device);
				// } else if (nodeType == NodeTypeInfo.NODE_TYPE_DVS) {
				// obj.put("img", R.drawable.dvs);
				// } else if (nodeType == NodeTypeInfo.NODE_TYPE_CHANNEL) {
				// obj.put("img", R.drawable.device);
				// }

				Log.d(TAG, " childNode: " + childrenNode);
				Log.d(TAG, "childNode Url: " + sdk.getNodeUrl(childrenNode));
				Log.d(TAG, "childNode sn: " + sdk.getDeviceSn(childrenNode));

				VideoDeviceInfo deviceInfo = new VideoDeviceInfo();
				deviceInfo.setDeviceName(sdk.getNodeName(childrenNode));
				deviceInfo.setNodeid(childrenNode);
				deviceInfo.setNodeType(nodeType);
				deviceInfo.setOnline(sdk.isOnline(childrenNode));

				mListData.add(deviceInfo);
			}
		}
	}
}
