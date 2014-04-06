package com.cocobabys.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.bean.ScheduleListItem;

public class ScheduleListAdapter extends BaseAdapter {
    private final Context context;
    private List<ScheduleListItem> items;

    public void setCookInfoList(List<ScheduleListItem> list) {
        this.items = list;
    }

    public ScheduleListAdapter(Context activityContext, List<ScheduleListItem> list) {
        this.context = activityContext;
        items = list;
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            FlagHolder flagholder = this.new FlagHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.schedule_item, null);
            flagholder.dateView = (TextView) convertView.findViewById(R.id.dateView);
            flagholder.weekView = (TextView) convertView.findViewById(R.id.weekView);
            flagholder.amContentView = (TextView) convertView.findViewById(R.id.amcontent);
            flagholder.pmContentView = (TextView) convertView.findViewById(R.id.pmcontent);

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
        ScheduleListItem item = items.get(position);
        flagholder.dateView.setText(item.getDate());
        flagholder.weekView.setText(item.getDayofweek());
        flagholder.amContentView.setText(item.getMorningContent());
        flagholder.pmContentView.setText(item.getAfternoonContent());
    }

    private class FlagHolder {
        public TextView dateView;
        public TextView weekView;
        public TextView amContentView;
        public TextView pmContentView;
    }
}