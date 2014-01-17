package com.djc.logintest.adapter;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.djc.logintest.R;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.LocationInfo;
import com.djc.logintest.utils.Utils;

public class LocationInfoListAdapter extends BaseAdapter {
    private final Context context;
    private List<LocationInfo> locationInfoList;

    public void setLocationInfoList(List<LocationInfo> list) {
        this.locationInfoList = list;
    }

    public LocationInfoListAdapter(Context activityContext, List<LocationInfo> list) {
        this.context = activityContext;
        locationInfoList = list;
    }

    public void clear() {
        locationInfoList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return locationInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return locationInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            FlagHolder flagholder = this.new FlagHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.location_item, null);
            flagholder.lbsNumView = (TextView) convertView.findViewById(R.id.lbsNumView);
            flagholder.addressView = (TextView) convertView.findViewById(R.id.locationAddressView);
            flagholder.timestampView = (TextView) convertView.findViewById(R.id.timeStampView);
            flagholder.deleteView = (ImageView) convertView.findViewById(R.id.deleteView);

            setDataToViews(position, flagholder);
            convertView.setTag(flagholder);
        } else {
            FlagHolder flagholder = (FlagHolder) convertView.getTag();
            if (flagholder != null) {
                setDataToViews(position, flagholder);
            }
        }

        return convertView;
    }

    private void setDataToViews(final int position, FlagHolder flagholder) {
        final LocationInfo locationInfo = locationInfoList.get(position);

        flagholder.lbsNumView.setText(locationInfo.getLbs_num());
        flagholder.addressView.setText(locationInfo.getAddress());
        flagholder.timestampView.setText(locationInfo.getTimestamp());

        flagholder.deleteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DDD pos", "position =" + position);
                Utils.showTwoBtnResDlg(R.string.delete_location_confirm, context,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataMgr.getInstance().deleteLocationInfo(locationInfo.getId());
                                locationInfoList.remove(position);
                                notifyDataSetChanged();
                            }
                        });
            }
        });
    }

    private class FlagHolder {
        public TextView lbsNumView;
        public TextView addressView;
        public TextView timestampView;
        public ImageView deleteView;
    }
}