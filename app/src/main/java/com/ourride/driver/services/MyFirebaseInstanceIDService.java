package com.ourride.driver.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.ourride.driver.utils.Config;
import com.ourride.driver.utils.SharedPrefrence;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService{
    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();
    private Context mContext;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        mContext = this;

        sendRegistrationToServer(refreshedToken);

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void sendRegistrationToServer(final String token) {
        Log.e(TAG, "sendRegistrationToServer: " + token);
        if (SharedPrefrence.getInt(mContext,"user_id") > 0) {
        }
    }

}
