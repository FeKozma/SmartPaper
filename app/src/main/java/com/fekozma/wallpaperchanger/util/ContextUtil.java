package com.fekozma.wallpaperchanger.util;

import android.content.Context;

public class ContextUtil {
	private static Context applicationContext;

	public static Context getContext() {
		return applicationContext;
	}
	public static void setContext(Context context) {
		applicationContext = context;
	}
}
