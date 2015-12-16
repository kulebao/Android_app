package com.cocobabys.rx;

import com.cocobabys.bean.FullParentInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.ParentMethod;
import com.cocobabys.utils.DataUtils;

import android.util.Log;
import rx.Observable;
import rx.Subscriber;
import rx.Observable.OnSubscribe;
import rx.functions.Func1;

public class ParentRxHelper {

	public Observable<FullParentInfo> getOnlineParentInfo() {
		return Observable.create(new OnSubscribe<FullParentInfo>() {
			@Override
			public void call(Subscriber<? super FullParentInfo> t) {
				try {
					Log.d("", "CCC getOnlineParentInfo");
					final FullParentInfo parent = ParentMethod.getMethod().getParent(DataUtils.getAccount());
					t.onNext(parent);
					t.onCompleted();
				} catch (Exception e) {
					Log.d("", "CCC getOnlineParentInfo error e=" + e.toString());
					t.onError(e);
				}
			}
		});
	}

	public Func1<FullParentInfo, FullParentInfo> updateParentFunc(final String name) {
		return new Func1<FullParentInfo, FullParentInfo>() {

			@Override
			public FullParentInfo call(FullParentInfo fullParentInfo) {
				Log.d("", "CCC call fullParentInfo=" + fullParentInfo);
				try {
					if (fullParentInfo != null) {
						fullParentInfo.setName(name);
						MethodResult updateParent = ParentMethod.getMethod().updateParent(fullParentInfo);
						if (EventType.UPDATE_PARENT_SUCCESS == updateParent.getResultType()) {
							return fullParentInfo;
						}
					}
				} catch (Exception e) {
					Log.d("", "CCC updateParentFunc error e=" + e.toString());
				}

				return null;
			}
		};
	}
}
