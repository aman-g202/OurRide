package com.ourride.driver.volley;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CheckConnection {
    static NetworkInfo WifiInformation, mobileInformation;
    static ConnectivityManager connection_manager;
    private static boolean isNetworkAvaliable = false;

    public static boolean isNetworkAvailable(Context cxt) {

        connection_manager = (ConnectivityManager) cxt.getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiInformation = connection_manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        mobileInformation = connection_manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (WifiInformation.isConnected() || mobileInformation.isConnected()) {
            isNetworkAvaliable = true;
        } else {
            isNetworkAvaliable = false;
        }
        return isNetworkAvaliable;
    }
}

