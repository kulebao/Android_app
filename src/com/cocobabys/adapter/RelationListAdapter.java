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
import com.cocobabys.utils.Utils;

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
    public ParentInfo getItem(int position){
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
            flagholder.name = (TextView)convertView.findViewById(R.id.name);
            flagholder.headView = (ImageView)convertView.findViewById(R.id.headView);
            flagholder.rightarrow = (ImageView)convertView.findViewById(R.id.rightarrow);
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
        flagholder.name.setText(parentInfo.getName());

        if(TextUtils.isEmpty(parentInfo.getPhone())){
            flagholder.relation.setTextColor(Utils.getResColor(R.color.gray));
            flagholder.phone.setVisibility(View.GONE);
            flagholder.rightarrow.setVisibility(View.VISIBLE);
        } else{
            flagholder.relation.setTextColor(Utils.getResColor(R.color.blue));
            flagholder.rightarrow.setVisibility(View.GONE);
            flagholder.phone.setVisibility(View.VISIBLE);
        }

        if(!TextUtils.isEmpty(parentInfo.getPortrait())){
            ImageUtils.displayEx(parentInfo.getPortrait(), flagholder.headView, 100, 100);
        } else{
            flagholder.headView.setImageResource(R.drawable.relation_logo);
        }

    }

    private class FlagHolder{
        public TextView  phone;
        public TextView  relation;
        public TextView  name;
        public ImageView headView;
        public ImageView rightarrow;
    }
}