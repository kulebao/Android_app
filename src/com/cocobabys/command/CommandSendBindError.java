package com.cocobabys.command;

import com.cocobabys.constant.ConstantValue;
import com.cocobabys.utils.Utils;

import android.os.AsyncTask;

public class CommandSendBindError implements Command {

	@Override
	public void execute() {
		new sendBindErrorTask().execute();
	}

	private class sendBindErrorTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			Utils.getProp(ConstantValue.BIND_ERROR);
			// send to server
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}
}
