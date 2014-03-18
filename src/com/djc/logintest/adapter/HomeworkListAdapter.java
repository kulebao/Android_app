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
import com.djc.logintest.dbmgr.info.Homework;
import com.djc.logintest.utils.Utils;

public class HomeworkListAdapter extends BaseAdapter {
	private final Context context;
	private List<Homework> dataList;

	public void setLocationInfoList(List<Homework> list) {
		this.dataList = list;
	}

	public HomeworkListAdapter(Context activityContext, List<Homework> list) {
		this.context = activityContext;
		dataList = list;
	}

	public void clear() {
		dataList.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			FlagHolder flagholder = this.new FlagHolder();
			convertView = LayoutInflater.from(this.context).inflate(
					R.layout.notice_item, null);
			flagholder.titleView = (TextView) convertView
					.findViewById(R.id.titleView);
			flagholder.bodyView = (TextView) convertView
					.findViewById(R.id.bodyView);
			flagholder.timestampView = (TextView) convertView
					.findViewById(R.id.timeStampView);
			flagholder.fromView = (TextView) convertView
					.findViewById(R.id.fromview);
			flagholder.deleteView = (ImageView) convertView
					.findViewById(R.id.deleteView);
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
		final Homework info = dataList.get(position);
		flagholder.titleView.setText(info.getTitle());
		flagholder.bodyView.setText(info.getContent());
		flagholder.timestampView.setText(info.getFormattedTime());

		flagholder.fromView.setText(DataMgr.getInstance()
				.getClassNameByClassID(info.getClass_id()));

		flagholder.deleteView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("DDD pos", "position =" + position);
				Utils.showTwoBtnResDlg(R.string.delete_notice_confirm, context,
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// DataMgr.getInstance().deleteNotice(info.getId());
								dataList.remove(position);
								notifyDataSetChanged();
							}
						});
			}
		});
	}

	private class FlagHolder {
		public TextView titleView;
		public TextView bodyView;
		public TextView timestampView;
		public TextView fromView;
		public ImageView deleteView;
	}
}