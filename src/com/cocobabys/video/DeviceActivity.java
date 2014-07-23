package com.cocobabys.video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
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
import com.huamaitel.api.HMDefines.NodeTypeInfo;
import com.huamaitel.api.HMJniInterface;

public class DeviceActivity extends Activity {
	private static final String TAG = "DeviceActivity";
	private ListView mListView;
	private List<Map<String, Object>> mListData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_activity);
		mListView = (ListView) findViewById(R.id.id_device_list);
		mListData = new ArrayList<Map<String, Object>>();

		// Get the root of the tree.
		int treeId = VideoApp.treeId;
		int rootId = VideoApp.getJni().getRoot(treeId);

		VideoApp.rootList.clear();
		VideoApp.rootList.add(rootId);

		getChildrenByNodeId(rootId);

		SimpleAdapter adapter = new SimpleAdapter(this, mListData, 
				R.layout.item_device,
				new String[] { "img", "name" }, 
				new int[] { R.id.id_img_deviceIcon, R.id.id_device_name });
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) arg0.getItemAtPosition(position);
				int nodeType = (Integer) map.get("type");
				Log.i("DeviceActivity:", "nodeType:" + nodeType);
				int nodeId = (Integer) map.get("id");
				Log.i("DeviceActivity:", "nodeId:" + nodeId);

				VideoApp.curNodeHandle = nodeId;
				if (nodeType == NodeTypeInfo.NODE_TYPE_DVS || nodeType == NodeTypeInfo.NODE_TYPE_GROUP) {
					VideoApp.rootList.add(nodeId);
					getChildrenByNodeId(nodeId);

					((SimpleAdapter) mListView.getAdapter()).notifyDataSetChanged();
				} else if (nodeType == NodeTypeInfo.NODE_TYPE_DEVICE) {
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
				int nodeId = VideoApp.rootList.get(VideoApp.rootList.size() - 2);
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
				Map<String, Object> obj = new HashMap<String, Object>();
				int childrenNode = sdk.getChildAt(nodeId, i);
				int nodeType = sdk.getNodeType(childrenNode);

				obj.put("type", nodeType);

				if (nodeType == NodeTypeInfo.NODE_TYPE_GROUP) {
					obj.put("img", R.drawable.folder);
				} else if (nodeType == NodeTypeInfo.NODE_TYPE_DEVICE) {
					obj.put("img", R.drawable.device);
				} else if (nodeType == NodeTypeInfo.NODE_TYPE_DVS) {
					obj.put("img", R.drawable.dvs);
				} else if (nodeType == NodeTypeInfo.NODE_TYPE_CHANNEL) {
					obj.put("img", R.drawable.device);
				}

				Log.d(TAG, " childNode: " + childrenNode);
				Log.d(TAG, "childNode Url: " + sdk.getNodeUrl(childrenNode));
				Log.d(TAG, "childNode sn: " + sdk.getDeviceSn(childrenNode));

				obj.put("id", childrenNode);
				obj.put("name", sdk.getNodeName(childrenNode));

				mListData.add(obj);
			}
		}
	}
}
