package com.djc.logintest.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.djc.logintest.R;
import com.djc.logintest.bean.CookbookItem;

public class CookBookListAdapter extends BaseAdapter {
    private final Context context;
    private List<CookbookItem> cookInfos;

    public void setCookInfoList(List<CookbookItem> list) {
        this.cookInfos = list;
    }

    public CookBookListAdapter(Context activityContext, List<CookbookItem> list) {
        this.context = activityContext;
        cookInfos = list;
    }

    public void clear() {
        cookInfos.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cookInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return cookInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            FlagHolder flagholder = this.new FlagHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.cook_item, null);
            flagholder.dateView = (TextView) convertView.findViewById(R.id.dateView);
            flagholder.weekView = (TextView) convertView.findViewById(R.id.weekView);
            flagholder.firstcontentView = (TextView) convertView.findViewById(R.id.firstcontent);
            flagholder.thirdcontentView = (TextView) convertView.findViewById(R.id.thirdcontent);
            flagholder.secondcontentView = (TextView) convertView.findViewById(R.id.secondcontent);
            flagholder.fouthcontentView = (TextView) convertView.findViewById(R.id.fouthcontent);

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
        CookbookItem cookInfo = cookInfos.get(position);
        flagholder.dateView.setText(cookInfo.getCookDate());
        flagholder.weekView.setText(cookInfo.getCookWeek());
        flagholder.firstcontentView.setText(cookInfo.getFirstContent());
        flagholder.secondcontentView.setText(cookInfo.getSecContent());
        flagholder.thirdcontentView.setText(cookInfo.getThirdContent());
        flagholder.fouthcontentView.setText(cookInfo.getFouthContent());
    }

    private class FlagHolder {
        public TextView fouthcontentView;
        public TextView thirdcontentView;
        public TextView secondcontentView;
        public TextView dateView;
        public TextView weekView;
        public TextView firstcontentView;
    }
}