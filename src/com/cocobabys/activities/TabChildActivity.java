package com.cocobabys.activities;

import com.cocobabys.R;
import com.cocobabys.customview.CustomDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class TabChildActivity extends UmengStatisticsActivity {

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
