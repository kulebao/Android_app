package com.cocobabys.adapter;

import io.rong.imkit.RongIM;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.greenrobot.event.EventBus;

import com.cocobabys.R;
import com.cocobabys.bean.IMExpandInfo;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.dbmgr.info.GroupParentInfo;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.event.EmptyEvent;
import com.cocobabys.utils.ImageUtils;
import com.cocobabys.utils.Utils;

public class ExpandableAdapter extends BaseExpandableListAdapter {
	private Context mContext;
	private List<IMExpandInfo> expandInfos;
	private LayoutInflater inflater;
	private List<ChildInfo> selfChildrenInfo;
	private ParentInfo selfParentInfo;

	public ExpandableAdapter(Context context, List<IMExpandInfo> list) {
		mContext = context;
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		expandInfos = list;

		selfChildrenInfo = DataMgr.getInstance().getAllChildrenInfo();
		selfParentInfo = DataMgr.getInstance().getSelfInfoByPhone();
	}

	public GroupParentInfo getChild(int groupPosition, int childPosition) {
		return expandInfos.get(groupPosition).getGroupParentInfoList().get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public int getChildrenCount(int groupPosition) {
		if (!expandInfos.isEmpty()) {
			return expandInfos.get(groupPosition).getGroupParentInfoList().size();
		}
		return 0;
	}

	// group method stub
	public IMExpandInfo getGroup(int groupPosition) {
		return expandInfos.get(groupPosition);
	}

	public int getGroupCount() {
		return expandInfos.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.group_item, null);
			GroupViewHolder holder = new GroupViewHolder();

			holder.mGroupName = (TextView) convertView.findViewById(R.id.group_name);
			holder.mGroupHead = (ImageView) convertView.findViewById(R.id.groupHead);

			setGroupData(groupPosition, holder);
			convertView.setTag(holder);
		} else {
			GroupViewHolder holder = (GroupViewHolder) convertView.getTag();
			setGroupData(groupPosition, holder);
		}
		return convertView;
	}

	private void setGroupData(int groupPosition, GroupViewHolder viewHolder) {
		IMExpandInfo group = getGroup(groupPosition);
		viewHolder.mGroupName.setText(group.getChildInfo().getName());
		ImageUtils.displayEx(group.getChildInfo().getPortrait(), viewHolder.mGroupHead, 40, 40);
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		if (convertView == null) {
			ChildViewHolder viewHolder = new ChildViewHolder();
			convertView = inflater.inflate(R.layout.group_child_item, null);

			viewHolder.nameView = (TextView) convertView.findViewById(R.id.nameView);
			viewHolder.headView = (ImageView) convertView.findViewById(R.id.groupChildHead);
			viewHolder.imsmsView = (ImageView) convertView.findViewById(R.id.imsms);
			viewHolder.phoneView = (ImageView) convertView.findViewById(R.id.phone);
			setChildData(groupPosition, childPosition, viewHolder);
			convertView.setTag(viewHolder);
		} else {
			ChildViewHolder viewHolder = (ChildViewHolder) convertView.getTag();
			setChildData(groupPosition, childPosition, viewHolder);
		}
		return convertView;
	}

	private void setChildData(int groupPosition, int childPosition, ChildViewHolder viewHolder) {
		final GroupParentInfo child = getChild(groupPosition, childPosition);
		viewHolder.nameView.setText(child.getNick_name());

		ImageUtils.displayEx(child.getPortrait(), viewHolder.headView, 40, 40);

		if (isSelfChildParent(groupPosition, childPosition)) {
			viewHolder.imsmsView.setVisibility(View.VISIBLE);
			viewHolder.phoneView.setVisibility(View.VISIBLE);

			viewHolder.imsmsView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d("", "start im id=" + child.getIMUserid() + " name =" + child.getName());
					// 通知ContactListActivity这里发起了私聊，等会直接退出到主界面
					EventBus.getDefault().post(new EmptyEvent());
					RongIM.getInstance().startPrivateChat(mContext, child.getIMUserid(), child.getName());
				}
			});

			viewHolder.phoneView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!TextUtils.isEmpty(child.getPhone())) {
						Utils.startToCall(mContext, child.getPhone());
					}
				}
			});
		} else {
			viewHolder.imsmsView.setVisibility(View.GONE);
			viewHolder.phoneView.setVisibility(View.GONE);
		}
	}

	private boolean isSelfChildParent(int groupPosition, int childPosition) {
		IMExpandInfo group = getGroup(groupPosition);
		GroupParentInfo child = getChild(groupPosition, childPosition);

		for (ChildInfo childInfo : selfChildrenInfo) {
			if (childInfo.getServer_id().equals(group.getChildInfo().getChild_id())
					// 自己不要给自己发消息打电话。。。
					&& !child.getParent_id().equals(selfParentInfo.getParent_id())) {
				return true;
			}
		}

		return false;
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private class ChildViewHolder {
		TextView nameView;
		ImageView headView;
		ImageView imsmsView;
		ImageView phoneView;
	}

	private class GroupViewHolder {
		TextView mGroupName;
		ImageView mGroupHead;
	}

	public void refresh(List<IMExpandInfo> list) {
		expandInfos.clear();
		expandInfos.addAll(list);
	}
}
