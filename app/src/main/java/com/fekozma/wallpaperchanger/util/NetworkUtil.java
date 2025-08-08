package com.fekozma.wallpaperchanger.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkUtil {

	public static boolean isInternetAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm == null) return false;

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			Network network = cm.getActiveNetwork();
			if (network == null) return false;

			NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
			return capabilities != null &&
				(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
					capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
					capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
		} else {
			// For older devices (below API 23)
			android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			return activeNetwork != null && activeNetwork.isConnected();
		}
	}
}
