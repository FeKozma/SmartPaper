package com.fekozma.wallpaperchanger.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor {
	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request()
			.newBuilder()
			.header("User-Agent", "SmartPaper/1.0 (felix.kozma@gmail.com)")
			.build();
		return chain.proceed(request);
	}
}
