package com.djc.logintest.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.djc.logintest.R;
import com.djc.logintest.customview.CustomDialog;

public class MyActivity extends UmengStatisticsActivity {
    @Override
    public void onBackPressed() {
        showConfirmDlg();
    }

    private void showConfirmDlg() {
        
        Dialog dialog = new CustomDialog.Builder(this).setTitle(R.string.exit_app)
                .setPositiveButton(R.string.confirm, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton(R.string.back, null).createTwoBtn();
        dialog.show();
    }
}
