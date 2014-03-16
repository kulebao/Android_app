package com.djc.logintest.constant;

import java.util.HashMap;
import java.util.Map;

import com.djc.logintest.R;

public class EventMap {
    private static Map<Integer, Integer> map = new HashMap<Integer, Integer>() {
        private static final long serialVersionUID = 1L;

        {
            put(EventType.PHONE_NUM_IS_INVALID, R.string.phone_invalid);
            put(EventType.PHONE_NUM_INPUT_ERROR, R.string.phone_input_error);

            put(EventType.AUTH_CODE_IS_INVALID, R.string.auth_code_invalid);
            put(EventType.AUTH_CODE_INPUT_ERROR, R.string.auth_code_input_error);
            put(EventType.GET_AUTH_CODE_FAIL, R.string.get_auth_code_fail);

            put(EventType.NET_WORK_INVALID, R.string.net_error);

            put(EventType.PWD_FORMAT_ERROR, R.string.pwd_format_error);
            put(EventType.PWD_INCORRECT, R.string.pwd_incorrect);
            put(EventType.OLD_PWD_FORMAT_ERROR, R.string.old_pwd_format_error);
            put(EventType.NEW_PWD_FORMAT_ERROR, R.string.new_pwd_format_error);
        }
    };

    public static int getErrorResID(int errorType) {
        Integer integer = map.get(errorType);
        if (integer == null) {
            integer = R.string.unknow_error;
        }
        return integer;
    }
}
