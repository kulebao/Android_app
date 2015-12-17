package com.cocobabys.adapter;

import java.util.List;

import com.cocobabys.R;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChildListAdapter extends BaseAdapter {
    private final Context context;
    private List<ChildInfo> mList;

    public void setLocationInfoList(List<ChildInfo> list) {
        this.mList = list;
    }

    public ChildListAdapter(Context activityContext, List<ChildInfo> list) {
        this.context = activityContext;
        mList = list;
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            FlagHolder flagholder = this.new FlagHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.child_item, null);
            flagholder.nameView = (TextView) convertView.findViewById(R.id.nameView);
            flagholder.headView = (ImageView) convertView.findViewById(R.id.headView);
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
        final ChildInfo childInfo = mList.get(position);
        flagholder.nameView.setText(childInfo.getChild_nick_name());
        Bitmap loacalBitmap = Utils.getLoacalBitmap(childInfo.getLocal_url());
        if(loacalBitmap !=null){
            Utils.setImg(flagholder.headView, loacalBitmap);
        }else{
        	flagholder.headView.setImageResource(R.drawable.default_child_head_icon);
        }
    }

    private class FlagHolder {
        public TextView nameView;
        public ImageView headView;
    }
}