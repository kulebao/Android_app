package com.djc.logintest.adapter;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.djc.logintest.R;
import com.djc.logintest.activities.SchoolNoticeActivity;
import com.djc.logintest.dbmgr.info.ChatInfo;
import com.djc.logintest.dlgmgr.DlgMgr;
import com.djc.logintest.utils.Utils;

public class ChatListAdapter extends BaseAdapter {
	// 最小显示时间间隔为2分钟
	private static final long MIN_TIME_LIMIT = 2 * 60 * 1000L;
	private final Context context;
	private List<ChatInfo> dataList;

	public void setLocationInfoList(List<ChatInfo> list) {
		this.dataList = list;
	}

	public ChatListAdapter(Context activityContext, List<ChatInfo> list) {
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
					R.layout.chat_item, null);
			flagholder.sendView = (TextView) convertView
					.findViewById(R.id.sender);
			flagholder.bodyView = (TextView) convertView
					.findViewById(R.id.content);
			flagholder.timestampView = (TextView) convertView
					.findViewById(R.id.timestamp);
			flagholder.resendView = (ImageView) convertView
					.findViewById(R.id.resend);
			flagholder.headiconView = (ImageView) convertView
					.findViewById(R.id.headicon);
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
		final ChatInfo info = dataList.get(position);
		flagholder.sendView.setText(info.getSender());
		flagholder.bodyView.setText(info.getContent());
		setTimeView(position, flagholder);
		setResendView(position, flagholder);
	}

	private void setTimeView(final int position, FlagHolder flagholder) {
		final ChatInfo info = dataList.get(position);
		final ChatInfo preinfo = getPreChatinfo(position);
		if (info.getSend_result() == ChatInfo.SEND_FAIL) {
			flagholder.timestampView.setVisibility(View.GONE);
		} else if (preinfo == null
				|| preinfo.getSend_result() == ChatInfo.SEND_FAIL
				|| (info.getTimestamp() - preinfo.getTimestamp()) > MIN_TIME_LIMIT) {
			flagholder.timestampView.setVisibility(View.VISIBLE);
			flagholder.timestampView.setText(info.getFormattedTime());

		} else {
			flagholder.timestampView.setVisibility(View.GONE);
		}
	}

	private ChatInfo getPreChatinfo(int position) {
		int prePosition = position - 1;
		if (prePosition >= 0) {
			return dataList.get(prePosition);
		}
		return null;
	}

	private void setResendView(final int position, FlagHolder flagholder) {
		final ChatInfo info = dataList.get(position);
		flagholder.resendView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				DlgMgr.getListDialog(context, R.array.resend_items,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								Log.d("resend", "which =" + position);
							}
						}).create().show();

				return true;
			}
		});

		if (info.getSend_result() == ChatInfo.SEND_FAIL) {
			flagholder.resendView.setVisibility(View.VISIBLE);
		} else {
			flagholder.resendView.setVisibility(View.GONE);
		}
	}

	private class FlagHolder {
		public TextView sendView;
		public TextView bodyView;
		public TextView timestampView;
		public ImageView resendView;
		public ImageView headiconView;
	}
}