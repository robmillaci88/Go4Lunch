package com.example.robmillaci.go4lunch.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Checks if we have a network connection available
 */
public class NetworkInfoChecker {

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
