package com.cocobabys.fragment;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.Subject;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.baidu.mapapi.model.LatLng;
import com.cocobabys.R;
import com.cocobabys.activities.BusinessActivity;
import com.cocobabys.constant.EventType;
import com.cocobabys.handler.MyHandler;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public abstract class MyListFragment extends Fragment{
    PullToRefreshListView    msgListView;
    ProgressDialog           dialog;
    Handler                  myhandler;
    private FragmentActivity holderActivity;

    private void initDialog(){
        dialog = new ProgressDialog(holderActivity);
        dialog.setMessage(getResources().getString(R.string.loading_data));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        holderActivity = getActivity();
        initListView();
        initDialog();
        initHander();
        runLoadDataTask();
        registeLocationSubsriber();
    }

    private void registeLocationSubsriber(){
        try{
            BusinessActivity activity = (BusinessActivity)getActivity();
            Subject<LatLng, LatLng> locationSubsciber = activity.getLocationSubsciber();

            locationSubsciber.observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<LatLng>(){
                @Override
                public void call(LatLng start){
                    Message msg = Message.obtain();
                    msg.what = EventType.GET_SELF_LOCATION_SUCCESS;
                    msg.obj = start;
                    myhandler.sendMessage(msg);
                    Log.d("", "call start=" + start.toString());
                    // Utils.makeToast(getActivity(), "receive lat = " +
                    // start.latitude + " long=" + start.longitude);
                }
            }, new Action1<Throwable>(){
                @Override
                public void call(Throwable e){
                    Log.e("", "onError e=" + e.toString());
                }
            });

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initListView(){
        msgListView.setMode(Mode.BOTH);
        setRefreshListener();
    }

    private void setRefreshListener(){
        // Set a listener to be invoked when the list should be refreshed.
        msgListView.setOnRefreshListener(new OnRefreshListener2<ListView>(){
            /**
             * onPullDownToRefresh will be called only when the user has Pulled
             * from the start, and released.
             */
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView){
                // Do work to refresh the list here.
                refreshHead();
            }

            /**
             * onPullUpToRefresh will be called only when the user has Pulled
             * from the end, and released.
             */
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView){
                refreshTail();
            }
        });

    }

    public abstract void refreshTail();

    public abstract void refreshHead();

    public abstract void runLoadDataTask();

    public abstract void handleMsg(Message msg);

    private void initHander(){

        myhandler = new MyHandler(holderActivity, dialog){
            @Override
            public void handleMessage(Message msg){
                if(holderActivity == null || holderActivity.isFinishing()){
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                msgListView.onRefreshComplete();
                super.handleMessage(msg);
                handleMsg(msg);
            }
        };
    }
}
