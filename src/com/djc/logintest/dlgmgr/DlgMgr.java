package com.djc.logintest.dlgmgr;

import java.lang.reflect.Field;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;

import com.djc.logintest.R;
import com.djc.logintest.customview.CustomDialog;

public class DlgMgr {

    public static AlertDialog.Builder getSingleBtnDlg(Context context) {
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle(context.getResources().getString(R.string.notice));
        builder.setPositiveButton(context.getResources().getString(R.string.confirm),
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder;
    }

    public static CustomDialog.Builder getTwoBtnDlg(Context context, OnClickListener confirmListener) {
        CustomDialog.Builder builder = new com.djc.logintest.customview.CustomDialog.Builder(
                context);
        builder.setTitle(context.getResources().getString(R.string.notice));
        builder.setPositiveButton(context.getResources().getString(R.string.confirm),
                confirmListener).setNegativeButton(
                context.getResources().getString(R.string.cancel), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder;
    }

    public static AlertDialog.Builder getSingleBtnDlg(Context context,
            OnClickListener confirmListener) {
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle(context.getResources().getString(R.string.notice));
        builder.setPositiveButton(context.getResources().getString(R.string.confirm),
                confirmListener);
        return builder;
    }

    public static AlertDialog.Builder getTextEntryDialog(Context context, int dialogViewID,
            int titleID, int posBtnText, OnClickListener confirmListener) {

        LayoutInflater factory = LayoutInflater.from(context);
        final View textEntryView = factory.inflate(dialogViewID, null);
        return new AlertDialog.Builder(context).setTitle(titleID).setView(textEntryView)
                .setPositiveButton(posBtnText, confirmListener);
    }

    public static AlertDialog.Builder getListDialog(Context context, int itemsID,
            OnClickListener confirmListener) {

        return new AlertDialog.Builder(context).setItems(itemsID, confirmListener);
    }

    public static void disableDlgDismiss(AlertDialog dlg) {
        try {
            Field field = dlg.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dlg, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cancelDialog(AlertDialog dlg) {
        DlgMgr.enableDlgDismiss(dlg);
        dlg.cancel();
    }

    private static void enableDlgDismiss(AlertDialog dlg) {
        try {
            Field field = dlg.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dlg, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
