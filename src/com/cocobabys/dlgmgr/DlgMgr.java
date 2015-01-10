package com.cocobabys.dlgmgr;

import java.lang.reflect.Field;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;

import com.cocobabys.R;
import com.cocobabys.customview.CustomDialog;

public class DlgMgr {
	public static void showSingleBtnResDlg(int resID, Context context) {
		CustomDialog.Builder builder = DlgMgr.getSingleBtnDlg(context);
		builder.setMessage(context.getResources().getString(resID));
		builder.create().show();
	}

	public static CustomDialog.Builder getSingleBtnDlg(Context context, OnClickListener confirmListener) {
		CustomDialog.Builder builder = new com.cocobabys.customview.CustomDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.notice));
		builder.setPositiveButton(context.getResources().getString(R.string.confirm), confirmListener);
		return builder;
	}

	public static CustomDialog.Builder getSingleBtnDlg(Context context) {
		return getSingleBtnDlg(context, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
	}

	public static CustomDialog.Builder getTwoBtnDlg(Context context, OnClickListener confirmListener) {
		CustomDialog.Builder builder = new com.cocobabys.customview.CustomDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.notice));
		builder.setPositiveButton(context.getResources().getString(R.string.confirm), confirmListener)
				.setNegativeButton(context.getResources().getString(R.string.cancel), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		return builder;
	}

	public static CustomDialog.Builder getTwoBtnDlg(Context context, OnClickListener confirmListener,
			OnClickListener cancelListener) {
		CustomDialog.Builder builder = new com.cocobabys.customview.CustomDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.notice));
		builder.setPositiveButton(context.getResources().getString(R.string.confirm), confirmListener)
				.setNegativeButton(context.getResources().getString(R.string.cancel), cancelListener);
		return builder;
	}

	public static AlertDialog.Builder getTextEntryDialog(Context context, int dialogViewID, int titleID,
			int posBtnText, OnClickListener confirmListener) {

		LayoutInflater factory = LayoutInflater.from(context);
		final View textEntryView = factory.inflate(dialogViewID, null);
		return new AlertDialog.Builder(context).setTitle(titleID).setView(textEntryView)
				.setPositiveButton(posBtnText, confirmListener);
	}

	public static AlertDialog.Builder getListDialog(Context context, int itemsID, OnClickListener confirmListener) {

		return new AlertDialog.Builder(context).setItems(itemsID, confirmListener);
	}

	public static AlertDialog.Builder getListDialog(Context context, CharSequence[] items,
			OnClickListener confirmListener) {
		return new AlertDialog.Builder(context).setItems(items, confirmListener);
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
