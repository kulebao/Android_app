package com.cocobabys.activities;


import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.cocobabys.R;

public class ActivityHelper {
    public static void setBackKeyLitsenerOnTopbar(final Activity activity, int titleID) {
        TextView titleView = (TextView) activity.findViewById(R.id.topbarTitleView);
        titleView.setText(titleID);

        Button backView = (Button) activity.findViewById(R.id.rightBtn);
        backView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }
}
