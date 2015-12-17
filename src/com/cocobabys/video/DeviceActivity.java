package com.cocobabys.video;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cocobabys.R;
import com.cocobabys.activities.ActivityHelper;
import com.cocobabys.activities.UmengStatisticsActivity;
import com.cocobabys.adapter.VideoDeviceListAdapter;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.utils.Utils;
import com.huamaitel.api.HMDefines.NodeTypeInfo;
import com.huamaitel.api.HMJniInterface;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public class DeviceActivity extends UmengStatisticsActivity{
    private static final String    TAG       = "DeviceActivity";
    private ListView               mListView;
    private List<VideoDeviceInfo>  mListData = new ArrayList<VideoDeviceInfo>();
    private VideoDeviceListAdapter adapter;
    private boolean                isPublicVideoAccount;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_activity);
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.watch_baby);
        mListView = (ListView)findViewById(R.id.id_device_list);

        // Get the root of the tree.
        int treeId = VideoApp.treeId;
        int rootId = VideoApp.getJni().getRoot(treeId);

        VideoApp.rootList.clear();
        VideoApp.rootList.add(rootId);

        getChildrenByNodeId(rootId);

        if(!mListData.isEmpty()){
            Collections.sort(mListData, new ChineseComparator());
        }

        initList();

        showNotice();
    }

    private void initList(){
        adapter = new VideoDeviceListAdapter(this, mListData);

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                VideoDeviceInfo info = adapter.getItem(position);
                int nodeType = info.getNodeType();
                Log.i("DeviceActivity:", "nodeType:" + nodeType);
                int nodeId = info.getNodeid();
                Log.i("DeviceActivity:", "nodeId:" + nodeId);

                VideoApp.curNodeHandle = nodeId;
                if(nodeType == NodeTypeInfo.NODE_TYPE_DVS || nodeType == NodeTypeInfo.NODE_TYPE_GROUP){
                    // VideoApp.rootList.add(nodeId);
                    // getChildrenByNodeId(nodeId);
                    //
                    // ((SimpleAdapter) mListView.getAdapter())
                    // .notifyDataSetChanged();
                } else if(nodeType == NodeTypeInfo.NODE_TYPE_DEVICE){
                    if(!info.isOnline()){
                        Utils.makeToast(DeviceActivity.this, "设备不在线，无法观看，请联系幼儿园处理,谢谢！");
                        return;
                    }
                    Intent intent = new Intent();
                    intent.setClass(DeviceActivity.this, PlayActivity.class);
                    intent.putExtra("nodeId", nodeId);
                    intent.putExtra(ConstantValue.IS_PUBLIC_VIDEO, isPublicVideoAccount);
                    VideoApp.mIsUserLogin = true;
                    startActivity(intent);
                }
            }
        });
    }

    private void showNotice(){
        isPublicVideoAccount = getIntent().getBooleanExtra(ConstantValue.IS_PUBLIC_VIDEO, false);
        if(isPublicVideoAccount){
            LinearLayout video_notice = (LinearLayout)findViewById(R.id.video_notice);
            video_notice.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if(VideoApp.treeId != 0){
            VideoApp.getJni().releaseTree(VideoApp.treeId);
        }

        if(VideoApp.serverId != 0){
            VideoApp.getJni().disconnectServer(VideoApp.serverId);
        }
    }

    // 如果是分组展开的情况，这里是收起分组，而不是退出。本应用不使用分组列表的形式
    // @Override
    // public boolean onKeyDown(int keyCode, KeyEvent event){
    // if(keyCode == KeyEvent.KEYCODE_BACK){
    // if(VideoApp.rootList.size() != 1){
    // int nodeId = VideoApp.rootList.get(VideoApp.rootList.size() - 2);
    // VideoApp.rootList.remove(VideoApp.rootList.size() - 1);
    //
    // getChildrenByNodeId(nodeId);
    //
    // ((SimpleAdapter)mListView.getAdapter()).notifyDataSetChanged();
    // return true;
    // }
    // }
    //
    // return super.onKeyDown(keyCode, event);
    // }

    // Get the children list by this parent node.
    private void getChildrenByNodeId(int nodeId){
        Log.d(TAG, "getDeviceListByNodeId nodeId: " + nodeId);
        if(nodeId != 0){
            HMJniInterface sdk = VideoApp.getJni();
            mListData.clear();

            int count = sdk.getChildrenCount(nodeId);
            Log.d(TAG, "getChildrenCount: " + count);
            for(int i = 0; i < count; ++i){
                int childrenNode = sdk.getChildAt(nodeId, i);
                int nodeType = sdk.getNodeType(childrenNode);

                if(nodeType == NodeTypeInfo.NODE_TYPE_DVS || nodeType == NodeTypeInfo.NODE_TYPE_GROUP){
                    getChildrenByNodeId(childrenNode);
                } else if(nodeType == NodeTypeInfo.NODE_TYPE_DEVICE){
                    addData(sdk, childrenNode, nodeType);
                }
            }
        }
    }

    private void addData(HMJniInterface sdk, int childrenNode, int nodeType){
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
