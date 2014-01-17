package com.djc.logintest.activities;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.djc.logintest.R;

public class ActivityHelper {
    public static void setBackKeyLitsenerOnTopbar(final Activity activity, int titleID) {
        TextView titleView = (TextView) activity.findViewById(R.id.topbarTitleView);
        titleView.setText(titleID);

        TextView backView = (TextView) activity.findViewById(R.id.topbarBackView);
        backView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }
}
