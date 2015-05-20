package com.cocobabys.command;

import android.os.AsyncTask;

import com.cocobabys.constant.ConstantValue;
import com.cocobabys.utils.DataUtils;

public class CommandSendBindError implements Command {

	@Override
	public void execute() {
		new sendBindErrorTask().execute();
	}

	private class sendBindErrorTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			DataUtils.getProp(ConstantValue.BIND_ERROR);
			// send to server
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}
}
