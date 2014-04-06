package com.cocobabys.command;

import android.util.Log;

import com.cocobabys.constant.ConstantValue;

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
		case ConstantValue.COMMAND_TYPE_CHECK_HOMEWORK:
			Log.d("ddd", "onStartCommand COMMAND_TYPE_CHECK_HOMEWORK");
			command = new CommandCheckHomework();
			break;
		case ConstantValue.COMMAND_TYPE_CHECK_SCHEDULE:
			Log.d("ddd", "onStartCommand COMMAND_TYPE_CHECK_SCHEDULE");
			command = new CommandCheckSchedule();
			break;
		case ConstantValue.COMMAND_TYPE_CHECK_CHAT:
			Log.d("ddd", "onStartCommand COMMAND_TYPE_CHECK_CHAT");
			command = new CommandCheckChat();
			break;
		case ConstantValue.COMMAND_TYPE_CHECK_EDU:
			Log.d("ddd", "onStartCommand COMMAND_TYPE_CHECK_EDU");
			command = new CommandCheckEducation();
			break;

		default:
			command = new CommandEmpty();
			break;
		}

		return command;
	}
}
