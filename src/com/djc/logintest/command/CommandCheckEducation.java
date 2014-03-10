package com.djc.logintest.command;

import android.os.AsyncTask;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.utils.MethodUtils;
import com.djc.logintest.utils.Utils;

public class CommandCheckEducation implements Command {

	@Override
	public void execute() {
		new CheckTask().execute();
	}

	class CheckTask extends AsyncTask<Void, Void, Void> {
		boolean has_new = false;

		@Override
		protected Void doInBackground(Void... params) {
			has_new = MethodUtils.checkEdu();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (has_new) {
				Utils.saveProp(ConstantValue.HAVE_EDUCATION_NOTICE, "true");
				MyApplication instance = MyApplication.getInstance();
				if (instance != null) {
					instance.updateNotify(0, 0);
				}
			}
		}
	}
}
