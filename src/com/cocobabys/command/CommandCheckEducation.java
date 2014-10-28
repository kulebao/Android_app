package com.cocobabys.command;

import android.os.AsyncTask;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.MethodUtils;

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
				DataUtils.saveProp(ConstantValue.HAVE_EDUCATION_NOTICE, "true");
				MyApplication instance = MyApplication.getInstance();
				if (instance != null) {
					instance.updateNotify(0, 0);
				}
			}
		}
	}
}
