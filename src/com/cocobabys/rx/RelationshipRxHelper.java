package com.cocobabys.rx;

import com.cocobabys.net.CardMethod;
import com.cocobabys.net.MethodResult;

import android.util.Log;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

public class RelationshipRxHelper {

	public Observable<Integer> updateRelationship(final String cardnum, final String relationship) {
		return Observable.create(new OnSubscribe<Integer>() {
			@Override
			public void call(Subscriber<? super Integer> t) {
				try {
					Log.d("", "TTT updateRelationship");
					MethodResult methodResult = CardMethod.getMethod().changeRelationship(cardnum, relationship);
					t.onNext(methodResult.getResultType());
					t.onCompleted();
				} catch (Exception e) {
					Log.d("", "TTT updateRelationship error e=" + e.toString());
					t.onError(e);
				}
			}
		});
	}

}
