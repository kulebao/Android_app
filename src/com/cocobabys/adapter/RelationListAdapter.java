package com.cocobabys.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.utils.ImageUtils;

public class RelationListAdapter extends BaseAdapter{
    private final Context    context;
    private List<ParentInfo> mList;

    public void setLocationInfoList(List<ParentInfo> list){
        this.mList = list;
    }

    public RelationListAdapter(Context activityContext, List<ParentInfo> list){
        this.context = activityContext;
        mList = list;
    }

    public void clear(){
        mList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount(){
        return mList.size();
    }

    @Override
    public Object getItem(int position){
        return mList.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            FlagHolder flagholder = this.new FlagHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.relation_item, null);
            flagholder.phone = (TextView)convertView.findViewById(R.id.phone);
            flagholder.relation = (TextView)convertView.findViewById(R.id.relation);
            flagholder.headView = (ImageView)convertView.findViewById(R.id.headView);
            setDataToViews(position, flagholder);
            convertView.setTag(flagholder);
        } else{
            FlagHolder flagholder = (FlagHolder)convertView.getTag();
            if(flagholder != null){
                setDataToViews(position, flagholder);
            }
        }

        return convertView;
    }

    private void setDataToViews(final int position, FlagHolder flagholder){
        final ParentInfo parentInfo = mList.get(position);
        flagholder.relation.setText(parentInfo.getRelationship());
        flagholder.phone.setText(parentInfo.getPhone());

        if(!TextUtils.isEmpty(parentInfo.getPortrait())){
            ImageUtils.displayEx(parentInfo.getPortrait(), flagholder.headView, 100, 100);
        } else{
            flagholder.headView.setImageResource(R.drawable.chat_head_icon);
        }

    }

    private class FlagHolder{
        public TextView  phone;
        public TextView  relation;
        public ImageView headView;
    }
}