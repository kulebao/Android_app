package com.cocobabys.command;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.MethodUtils;

import android.os.AsyncTask;

public class CommandCheckChat implements Command {

	@Override
	public void execute() {
		new CheckTask().execute();
	}

	class CheckTask extends AsyncTask<Void, Void, Void> {
		boolean has_new = false;

		@Override
		protected Void doInBackground(Void... params) {
			has_new = MethodUtils.checkNewChat();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (has_new) {
				DataUtils.saveProp(ConstantValue.HAVE_CHAT_NOTICE, "true");
				MyApplication instance = MyApplication.getInstance();
				if (instance != null) {
					instance.updateNotify(0, 0);
				}
			}
		}
	}
}
