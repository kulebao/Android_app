/*
 * Copyright 2014 The Android Open Source Project Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.cocobabys.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.alibaba.fastjson.JSON;
import com.cocobabys.R;
import com.cocobabys.activities.MerchantActivity;
import com.cocobabys.adapter.MerchantGridViewAdapter;
import com.cocobabys.adapter.MerchantListAdapter;
import com.cocobabys.bean.MerchantGridInfo;
import com.cocobabys.bean.MerchantInfo;
import com.cocobabys.bean.PullToRefreshListInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.jobs.GetMechantJob;
import com.cocobabys.listener.MyPullToRefreshOnItemClickListener;
import com.cocobabys.utils.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class ShopFragment extends MyListFragment {

	private int currentCategory = ConstantValue.MERCHANT_CATEGORY_CAMERA;

	private List<MerchantInfo> merchantInfos = new ArrayList<MerchantInfo>();

	private List<MerchantGridInfo> gridInfos = new ArrayList<MerchantGridInfo>() {
		private static final long serialVersionUID = 1L;

		{
			add(new MerchantGridInfo(R.drawable.photography1, R.drawable.photography0,
					ConstantValue.MERCHANT_CATEGORY_CAMERA));
			add(new MerchantGridInfo(R.drawable.tour1, R.drawable.tour0, ConstantValue.MERCHANT_CATEGORY_GAME));
			add(new MerchantGridInfo(R.drawable.train1, R.drawable.train0, ConstantValue.MERCHANT_CATEGORY_EDUCATION));
			add(new MerchantGridInfo(R.drawable.shopping1, R.drawable.shopping0,
					ConstantValue.MERCHANT_CATEGORY_SHOPPING));
			add(new MerchantGridInfo(R.drawable.other1, R.drawable.other0, ConstantValue.MERCHANT_CATEGORY_OTHER));
		}
	};

	private MerchantGridViewAdapter gridViewAdapter;
	private MerchantListAdapter listAdapter;

	private GridView gridview;

	private GetMechantJob actionJob;

	public ShopFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.shop, container, false);
		// 注意pulltorefreshlist的高度必须设置为fill_parent,否则无法显示
		msgListView = (PullToRefreshListView) view.findViewById(R.id.pulltorefreshlist);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initGridView(view);
		initListView(view);
	}

	private void initListView(View view) {
		listAdapter = new MerchantListAdapter(getActivity(), merchantInfos);

		msgListView.setAdapter(listAdapter);

		msgListView.setOnItemClickListener(new MyPullToRefreshOnItemClickListener() {
			@Override
			public void handleClick(int realPosition) {
				startToActionDetailActivity(realPosition);
			}
		});

	}

	private void startToActionDetailActivity(int position) {
		MerchantInfo item = (MerchantInfo) listAdapter.getItem(position);

		Intent intent = new Intent();
		FragmentActivity activity = getActivity();
		intent.setClass(activity, MerchantActivity.class);
		intent.putExtra(ConstantValue.MERCHANT_DETAIL, JSON.toJSONString(item));
		activity.startActivity(intent);
	}

	private void initGridView(View view) {
		gridview = (GridView) view.findViewById(R.id.gridview);
		gridViewAdapter = new MerchantGridViewAdapter(getActivity(), gridInfos);
		gridview.setAdapter(gridViewAdapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				handleGridItemClick(position);
			}
		});

		gridViewAdapter.setSeclection(0);
	}

	@Override
	public void runLoadDataTask() {
		dialog.show();
		PullToRefreshListInfo info = new PullToRefreshListInfo();
		doGet(info, ConstantValue.MERCHANT_CATEGORY_CAMERA);
	}

	private synchronized void doGet(PullToRefreshListInfo info, int category) {
		if (actionJob != null && !actionJob.isDone()) {
			actionJob.cancel(true);
		}

		actionJob = new GetMechantJob(myhandler, info, category);
		actionJob.execute();
	}

	@Override
	public void handleMsg(Message msg) {
		Log.d("DDD handleMsg getResult", "handleMsg what : " + msg.what + " ar1=" + msg.arg1);
		switch (msg.what) {
		case EventType.MECHANT_GET_FAIL:
			Utils.makeToast(getActivity(), Utils.getResString(R.string.get_merchant_fail));
			break;
		case EventType.MECHANT_GET_SUCCESS:
			handleGetMerchantSuccess(msg);
			break;
		default:
			break;
		}
	}

	private void handleGetMerchantSuccess(Message msg) {
		@SuppressWarnings("unchecked")
		List<MerchantInfo> list = (List<MerchantInfo>) msg.obj;
		if (list.isEmpty()) {
			Utils.makeToast(getActivity(), Utils.getResString(R.string.get_merchant_empty));
		} else {
			if (msg.arg1 == ConstantValue.TYPE_GET_HEAD) {
				merchantInfos.addAll(0, list);
			} else {
				merchantInfos.addAll(list);
			}
			listAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void refreshTail() {
		Log.d("", "refreshTail currentCategory=" + currentCategory);
		PullToRefreshListInfo info = new PullToRefreshListInfo();
		if (!merchantInfos.isEmpty()) {
			info.setTo(merchantInfos.get(merchantInfos.size() - 1).getId());
		}
		info.setType(ConstantValue.TYPE_GET_TAIL);
		doGet(info, currentCategory);
	}

	@Override
	public void refreshHead() {
		Log.d("", "refreshHead currentCategory=" + currentCategory);
		PullToRefreshListInfo info = new PullToRefreshListInfo();
		if (!merchantInfos.isEmpty()) {
			info.setFrom(merchantInfos.get(0).getId());
		}
		info.setType(ConstantValue.TYPE_GET_HEAD);
		doGet(info, currentCategory);
	}

	private void handleGridItemClick(int position) {
		MerchantGridInfo item = gridViewAdapter.getItem(position);

		if (currentCategory == item.getCategory()) {
			Log.d("", "same currentCategory :" + currentCategory);
			return;
		}

		dialog.setCancelable(false);
		dialog.show();

		gridViewAdapter.setSeclection(position);
		gridViewAdapter.notifyDataSetChanged();

		currentCategory = item.getCategory();

		listAdapter.clearData();

		PullToRefreshListInfo info = new PullToRefreshListInfo();
		doGet(info, currentCategory);
	}

}
