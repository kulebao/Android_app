package com.djc.logintest.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.util.Log;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.customexception.BindFailException;
import com.djc.logintest.customexception.InvalidTokenException;

public class MyProxy implements InvocationHandler {

	private Object target;

	/**
	 * 绑定委托对象并返回一个代理类
	 * 
	 * @param target
	 * @return
	 */
	public Object bind(Object target) {
		this.target = target;
		// 取得代理对象
		return Proxy.newProxyInstance(target.getClass().getClassLoader(),
				target.getClass().getInterfaces(), this); // 要绑定接口(这是一个缺陷，cglib弥补了这一缺陷)
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object result = null;
		Log.d("DDD", "MyProxy");
		// 执行方法
		try {
			result = method.invoke(target, args);
		} catch (Exception e) {
			if (e.getCause() instanceof BindFailException) {
				result = EventType.NET_WORK_INVALID;
			} else if (e.getCause() instanceof InvalidTokenException) {
				result = EventType.TOKEN_INVALID;
			} else {
				throw e;
			}
		}
		return result;
	}

}
