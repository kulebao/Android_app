/*
 * Copyright (C) 2012 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.greenrobot.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

class SubscriberMethodFinder {
	private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC;
	private static final Map<String, List<SubscriberMethod>> methodCache = new HashMap<String, List<SubscriberMethod>>();
	private static final Map<Class<?>, Class<?>> skipMethodVerificationForClasses = new ConcurrentHashMap<Class<?>, Class<?>>();

	List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass, String eventMethodName) {
		//类名+"."+onEvent作为key
		String key = subscriberClass.getName() + '.' + eventMethodName;
		List<SubscriberMethod> subscriberMethods;
		// 对methodCache同步
		synchronized (methodCache) {
			subscriberMethods = methodCache.get(key);
		}

		if (subscriberMethods != null) {
			// 如果是注册过的，通过缓存直接返回
			return subscriberMethods;
		}
		subscriberMethods = new ArrayList<SubscriberMethod>();

		// 订阅者类型,实际类型
		Class<?> clazz = subscriberClass;
		HashSet<String> eventTypesFound = new HashSet<String>();
		StringBuilder methodKeyBuilder = new StringBuilder();
		while (clazz != null) {
			String name = clazz.getName();
			// 如果订阅类继承了系统类，就不再判断系统类了
			if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
				// Skip system classes, this just degrades performance
				break;
			}

			// Starting with EventBus 2.2 we enforced methods to be public (might change with annotations again)
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				String methodName = method.getName();
				// 必须以onEvent开头
				if (methodName.startsWith(eventMethodName)) {
					int modifiers = method.getModifiers();
					// 必须是public方法，且不能是抽象或静态方法
					if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
						//获取所有参数列表，注意是参数的个数，不是参数类型的个数，方法名称有点误导
						Class<?>[] parameterTypes = method.getParameterTypes();

						// 必须有且仅有一个参数
						if (parameterTypes.length == 1) {
							String modifierString = methodName.substring(eventMethodName.length());
							ThreadMode threadMode;
							// 分为4类，onEvent，onEventPostThread，onEventMainThread，onEventAsync对应不同的处理方式
							if (modifierString.length() == 0) {
								threadMode = ThreadMode.PostThread;
							} else if (modifierString.equals("MainThread")) {
								threadMode = ThreadMode.MainThread;
							} else if (modifierString.equals("BackgroundThread")) {
								threadMode = ThreadMode.BackgroundThread;
							} else if (modifierString.equals("Async")) {
								threadMode = ThreadMode.Async;
							} else {
								//如果类本身就有onEvent开头的方法，添加到skipMethodVerificationForClasses中忽略，否则会抛出异常
								//如果使用自定义注解可以避免下面的问题
								if (skipMethodVerificationForClasses.containsKey(clazz)) {
									continue;
								} else {
									throw new EventBusException("Illegal onEvent method, check for typos: " + method);
								}
							}
							//接受的消息类型
							Class<?> eventType = parameterTypes[0];
							methodKeyBuilder.setLength(0);
							methodKeyBuilder.append(methodName);
							methodKeyBuilder.append('>').append(eventType.getName());
							String methodKey = methodKeyBuilder.toString();
							// 在有继承关系的订阅类中，如果方法名和参数类型都一致，只能存在一个
							if (eventTypesFound.add(methodKey)) {
								// Only add if not already found in a sub class
								subscriberMethods.add(new SubscriberMethod(method, threadMode, eventType));
							}
						}
					} else if (!skipMethodVerificationForClasses.containsKey(clazz)) {
						Log.d(EventBus.TAG, "Skipping method (not public, static or abstract): " + clazz + "."
								+ methodName);
					}
				}
			}
			// 注意在反射获取类名时，获取的是真正实例化的类名
			clazz = clazz.getSuperclass();
		}

		// 如果注册了自己，却没有定义消息处理函数，则会抛出异常
		if (subscriberMethods.isEmpty()) {
			throw new EventBusException("Subscriber " + subscriberClass + " has no public methods called "
					+ eventMethodName);
		} else {
			synchronized (methodCache) {
				methodCache.put(key, subscriberMethods);
			}
			return subscriberMethods;
		}
	}

	static void clearCaches() {
		synchronized (methodCache) {
			methodCache.clear();
		}
	}

	// 如果订阅类自身存在onEvent开头的方法，满足EventBus的一切要求，但又不是EventBus需要的，那么在这里加入，以免EventBus报错
	static void skipMethodVerificationFor(Class<?> clazz) {
		if (!methodCache.isEmpty()) {
			throw new IllegalStateException("This method must be called before registering anything");
		}
		skipMethodVerificationForClasses.put(clazz, clazz);
	}

	public static void clearSkipMethodVerifications() {
		skipMethodVerificationForClasses.clear();
	}
}
