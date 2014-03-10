package com.djc.logintest.command;

import android.os.AsyncTask;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.utils.MethodUtils;
import com.djc.logintest.utils.Utils;

public class CommandCheckCookBook implements Command {

	@Override
	public void execute() {
		new CheckCookBookTask().execute();
	}

	class CheckCookBookTask extends AsyncTask<Void, Void, Void> {
		boolean has_new = false;

		@Override
		protected Void doInBackground(Void... params) {
			has_new = MethodUtils.checkCookBook();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (has_new) {
				Utils.saveProp(ConstantValue.HAVE_COOKBOOK_NOTICE, "true");
				MyApplication instance = MyApplication.getInstance();
				if (instance != null) {
					instance.updateNotify(0, 0);
				}
			}
		}
	}
}