package com.djc.logintest.command;

import android.util.Log;

import com.djc.logintest.constant.ConstantValue;

public class CommandFactory {
	private CommandFactory() {
	}

	public static CommandFactory getCommandFactory() {
		return new CommandFactory();
	}

	public Command createCommand(int type) {
		Command command = null;
		switch (type) {
		case ConstantValue.COMMAND_TYPE_CHECK_NOTICE:
			Log.d("ddd", "onStartCommand COMMAND_TYPE_CHECK_NOTICE");
			command = new CommandCheckNews();
			break;
		case ConstantValue.COMMAND_TYPE_CHECK_COOKBOOK:
			Log.d("ddd", "onStartCommand COMMAND_TYPE_CHECK_COOKBOOK");
			command = new CommandCheckCookBook();
			break;

		default:
			command = new CommandEmpty();
			break;
		}

		return command;
	}
}
