package com.cocobabys.adapter;

import java.util.List;

import com.cocobabys.R;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TeacherListAdapter extends BaseAdapter {
	private final Context context;
	private List<Teacher> mList;

	public TeacherListAdapter(Context activityContext, List<Teacher> list) {
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
	public Teacher getItem(int position) {
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
			convertView = LayoutInflater.from(this.context).inflate(R.layout.teacher_item, null);
			flagholder.nameView = (TextView) convertView.findViewById(R.id.nameView);
			flagholder.headView = (ImageView) convertView.findViewById(R.id.headView);
			flagholder.imsmsView = (ImageView) convertView.findViewById(R.id.imsms);
			flagholder.phoneView = (ImageView) convertView.findViewById(R.id.phone);
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
		final Teacher teacher = mList.get(position);
		flagholder.nameView.setText(teacher.getName());
		Bitmap loacalBitmap = Utils.getLoacalBitmap(teacher.getLocalIconPath());
		if (loacalBitmap != null) {
			Utils.setImg(flagholder.headView, loacalBitmap);
		} else {
			flagholder.headView.setImageResource(R.drawable.chat_head_icon);
		}

		// flagholder.imsmsView.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Log.d("", "start im id=" + teacher.getIMUserid() + " name =" +
		// teacher.getName());
		// //通知ContactListActivity这里发起了私聊，等会直接退出到主界面
		// EventBus.getDefault().post(new EmptyEvent());
		// RongIM.getInstance().startPrivateChat(context, teacher.getIMUserid(),
		// teacher.getName());
		// }
		// });

		flagholder.phoneView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(teacher.getPhone())) {
					Utils.startToCall(context, teacher.getPhone());
				}
			}
		});
	}

	private class FlagHolder {
		public TextView nameView;
		public ImageView headView;
		public ImageView imsmsView;
		public ImageView phoneView;
	}

	public void refresh(List<Teacher> list) {
		mList.clear();
		mList.addAll(list);
		notifyDataSetChanged();
	}
}