// used for sending location to server in every 30sec
package com.ourride.driver.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
import com.ourride.driver.ui.MainHomeActivity;
import com.ourride.driver.ui.activity.SelectionActivity;
import com.ourride.driver.utils.SharedPrefrence;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class DriverLocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mlocationCallback;
    public static LocationSettingsRequest.Builder builder;
    public Context context;
    public LatLng mOrigin;
    public LatLng mCurrentLocation = new LatLng(0.0, 0.0);
    private Map<String, String> message;
    private final IBinder mBinder = new MyBinder();
    private int user_id;
    private Handler h;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        context = this;
        user_id = SharedPrefrence.getInt(context, "user_id");
        mLocationRequest = createLocationRequest();

        mlocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                message = new LinkedHashMap<String, String>();
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    mOrigin = new LatLng(location.getLatitude(), location.getLongitude());
                    message.put("lat", String.valueOf(mOrigin.latitude));
                    message.put("lng", String.valueOf(mOrigin.longitude));
                    message.put("user_id", String.valueOf(user_id));
                    sendToPubNubChannel(message);
                }
            };
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        h = new Handler();
        runnable = new Runnable() {
            private long time = 0;
            @Override
            public void run() {
                startLocationUpdates();
                time += 30000;
                h.postDelayed(this, 30000);
            }
        };
        h.postDelayed(runnable, 30000); // 30 second delay (takes millis)
        return Service.START_NOT_STICKY;
    }

    private void sendToPubNubChannel(Map<String, String> message) {
        if (SelectionActivity.pubNub != null) {
            SelectionActivity.pubNub.publish()
                    .message(message)
                    .channel("update_lat_lng")
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            // handle publish result, status always present, result if successful
                            // status.isError() to see if error happened
                            if (!status.isError()) {
                                System.out.println("pub timetoken: " + result.getTimetoken());
//                                Toast.makeText(context, result.getTimetoken().toString(), Toast.LENGTH_SHORT).show();
                            }
                            System.out.println("pub status code: " + status.getStatusCode());
//                            Toast.makeText(context, String.valueOf(status.getStatusCode()), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            MainHomeActivity.pubNub.publish()
                    .message(message)
                    .channel("update_lat_lng")
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            // handle publish result, status always present, result if successful
                            // status.isError() to see if error happened
                            if (!status.isError()) {
                                System.out.println("pub timetoken: " + result.getTimetoken());
                              //  Toast.makeText(context, result.getTimetoken().toString(), Toast.LENGTH_SHORT).show();
                            }
                            System.out.println("pub status code: " + status.getStatusCode());
                         //   Toast.makeText(context, String.valueOf(status.getStatusCode()), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    public class MyBinder extends Binder {
        public DriverLocationService getService() {
            return DriverLocationService.this;
        }
    }

    public void startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        fusedLocationClient.requestLocationUpdates(mLocationRequest,
                mlocationCallback,
                null /* Looper */);
    }


    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(mlocationCallback);
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(15000);
        mLocationRequest.setSmallestDisplacement(30);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        h.removeCallbacks(runnable);
        System.out.println("service stopped");
        stopLocationUpdates();
        super.onDestroy();
    }
}

