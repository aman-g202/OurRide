package com.ourride.driver.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ConnectionDirector {
    Context mcontext;
    public ConnectionDirector(Context mcontext){
        this.mcontext = mcontext;
    }
    public boolean isNetWorkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void show(Context context){
        Toast.makeText(context, "Internet Connection is not available",Toast.LENGTH_SHORT).show();
    }
}
