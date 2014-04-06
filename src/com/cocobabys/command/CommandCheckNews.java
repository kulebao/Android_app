package com.cocobabys.command;

import android.os.AsyncTask;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.utils.MethodUtils;
import com.cocobabys.utils.Utils;

public class CommandCheckNews implements Command {

	@Override
	public void execute() {
		new CheckNewsTask().execute();
	}

	class CheckNewsTask extends AsyncTask<Void, Void, Void> {
		boolean has_new = false;

		@Override
		protected Void doInBackground(Void... params) {
			has_new = MethodUtils.checkNews();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (has_new) {
				Utils.saveProp(ConstantValue.HAVE_NEWS_NOTICE, "true");
				MyApplication instance = MyApplication.getInstance();
				if (instance != null) {
					instance.updateNotify(0, 0);
				}
				MethodUtils.setNewsNotification();
			}
		}
	}
}
