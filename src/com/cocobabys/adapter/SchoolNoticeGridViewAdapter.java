package com.cocobabys.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.activities.SchoolNoticeActivity;
import com.cocobabys.bean.MainGridInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.utils.DataUtils;

public class SchoolNoticeGridViewAdapter extends BaseAdapter {
	private Context context = null;
	private List<MainGridInfo> data;

	public SchoolNoticeGridViewAdapter(Context context, List<MainGridInfo> data) {
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public MainGridInfo getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			FlagHolder flagholder = this.new FlagHolder();
			convertView = LayoutInflater.from(this.context).inflate(R.layout.grid_item, null);
			flagholder.newDataSymble = (ImageView) convertView.findViewById(R.id.noticeImg);
			flagholder.nameView = (TextView) convertView.findViewById(R.id.ItemText);
			flagholder.headView = (ImageView) convertView.findViewById(R.id.ItemImage);
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
		setNoticeImg(position, flagholder);

		MainGridInfo info = getItem(position);
		int resid = info.getResID();
		flagholder.headView.setBackgroundResource(resid);

		int titleid = info.getTitleID();
		flagholder.nameView.setText(titleid);
	}

	private void setNoticeImg(final int position, FlagHolder flagholder) {
		MainGridInfo item = getItem(position);
		int resID = item.getResID();

		if (resID == R.drawable.pnotice) {
			String prop = DataUtils.getProp(ConstantValue.HAVE_NEWS_NOTICE);
			Log.d("", "setNoticeImg prop=" + prop);
			String hprop = DataUtils.getProp(ConstantValue.HAVE_HOMEWORK_NOTICE);
			if ("true".equals(prop)) {
				flagholder.newDataSymble.setVisibility(View.VISIBLE);
			} else {
				flagholder.newDataSymble.setVisibility(View.GONE);
			}
		} else if (resID == R.drawable.cook) {
			String prop = DataUtils.getProp(ConstantValue.HAVE_COOKBOOK_NOTICE);
			if ("true".equals(prop)) {
				flagholder.newDataSymble.setVisibility(View.VISIBLE);
			} else {
				flagholder.newDataSymble.setVisibility(View.GONE);
			}
		}
		// else if (position == SchoolNoticeActivity.HOMEWORK) {
		// String prop = DataUtils.getProp(ConstantValue.HAVE_HOMEWORK_NOTICE);
		// if ("true".equals(prop)) {
		// flagholder.newDataSymble.setVisibility(View.VISIBLE);
		// } else {
		// flagholder.newDataSymble.setVisibility(View.GONE);
		// }
		// }
		else if (resID == R.drawable.schedule) {
			String prop = DataUtils.getProp(ConstantValue.HAVE_SCHEDULE_NOTICE);
			if ("true".equals(prop)) {
				flagholder.newDataSymble.setVisibility(View.VISIBLE);
			} else {
				flagholder.newDataSymble.setVisibility(View.GONE);
			}
		} else if (resID == R.drawable.chat) {
			// String prop = DataUtils.getProp(ConstantValue.HAVE_CHAT_NOTICE);
			// if ("true".equals(prop)) {
			// flagholder.newDataSymble.setVisibility(View.VISIBLE);
			// } else {
			// flagholder.newDataSymble.setVisibility(View.GONE);
			// }
			flagholder.newDataSymble.setVisibility(View.GONE);
		} else if (resID == R.drawable.education) {
			String prop = DataUtils.getProp(ConstantValue.HAVE_EDUCATION_NOTICE);
			if ("true".equals(prop)) {
				flagholder.newDataSymble.setVisibility(View.VISIBLE);
			} else {
				flagholder.newDataSymble.setVisibility(View.GONE);
			}
		}
	}

	private class FlagHolder {
		public ImageView newDataSymble;
		public TextView nameView;
		public ImageView headView;
	}

}
