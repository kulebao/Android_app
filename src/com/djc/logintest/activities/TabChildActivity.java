package com.djc.logintest.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.djc.logintest.R;

public class TabChildActivity extends Activity {

    @Override
    public void onBackPressed() {
        showConfirmDlg();
    }

    private void showConfirmDlg() {
        Dialog dialog = new AlertDialog.Builder(this).setTitle(R.string.exit_app)
                .setPositiveButton(R.string.confirm, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNeutralButton(R.string.back, null).create();
        dialog.show();
    }
}
