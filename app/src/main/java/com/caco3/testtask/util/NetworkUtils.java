package com.caco3.testtask.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {
    private NetworkUtils(){
        throw new AssertionError(NetworkUtils.class.getName() + " not intended to be instantiated");
    }

    /**
     * Tests whether network is available
     * @param context to get {@link ConnectivityManager}
     * @return true if network available, false otherwise
     */
    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}