package com.cocobabys.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.utils.Utils;

public class LogWriter {

	private static LogWriter mLogWriter;

	private static Writer mWriter;

	private static SimpleDateFormat df;

	private boolean debug = true;

	private LogWriter() {
		debug = MyApplication.getInstance().isForTest();
	}

	public static synchronized LogWriter getInstance() {
		try {
			if (mLogWriter == null) {
				String path = Utils.getSDCardFileDir(Utils.APP_COMMON_LOGS).getAbsolutePath();

				path = path + File.separator + new SimpleDateFormat("[yy-MM-dd]").format(new Date()) + ".txt";

				Log.d("", "LogWriter path=" + path);
				mLogWriter = new LogWriter();
				mWriter = new BufferedWriter(new FileWriter(path, true), 2048);
				df = new SimpleDateFormat("[yy-MM-dd hh:mm:ss]: ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mLogWriter;
	}

	public synchronized void close() {
		Utils.close(mWriter);
		mWriter = null;
	}

	public void print(String log) {
		if (!debug) {
			return;
		}

		try {
			mWriter.write(df.format(new Date()));
			mWriter.write(log);
			mWriter.write("\r\n");
			mWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.w("", log);
	}

	public void print(Class<?> cls, String log) { // 如果还想看是在哪个类里可以用这个方法
		if (!debug) {
			return;
		}

		Log.w("", log);
		try {
			mWriter.write(df.format(new Date()));
			mWriter.write(cls.getSimpleName() + " ");
			mWriter.write(log);
			mWriter.write("\r\n");
			mWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
