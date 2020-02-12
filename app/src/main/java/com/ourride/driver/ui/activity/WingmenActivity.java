package com.ourride.driver.ui.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.ourride.driver.R;
import com.ourride.driver.services.DriverLocationService;
import com.ourride.driver.ui.MainHomeActivity;
import com.ourride.driver.utils.Helper;
import com.ourride.driver.utils.PathRoute.JsonUtil;
import com.ourride.driver.utils.SharedPrefrence;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class WingmenActivity extends AppCompatActivity implements OnMapReadyCallback, ServiceConnection {

    private Context mContext;
    private SupportMapFragment mMapFragment;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest.Builder builder;
    private DriverLocationService s;
    private LatLng mCurrentLocation;
    private GoogleMap mMap;
    private Marker driverMarker;
    private Toolbar toolbar;
    private float bearing;
    private MarkerOptions yourLocationMarker;
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 100;
    private static final int REQUEST_CHECK_SETTINGS = 102;
    private int connected_driver_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wingmen);
        mContext = WingmenActivity.this;
        toolbar= findViewById(R.id.toolbar);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }

        if (!Helper.isNetworkAvailable(mContext)) {
            Toast.makeText(mContext, "Please turn on the internet", Toast.LENGTH_LONG).show();
        }

        toolbar.setTitle("Live Location");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorToolBarText));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        connected_driver_id = SharedPrefrence.getInt(mContext, "connected_driver_id");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        showPermissionAlert();

        mLocationRequest = createLocationRequest();
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        checkLocationSetting(builder);

        try {
            // Loading map
            mMap.clear();
            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initilizeMap() {
        if (mMap == null) {
            mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mMapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mMap = googleMap;
//            mMap.setMyLocationEnabled(true);
            mMap.setMaxZoomPreference(16.0f);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        if (mMap == null) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        setMapStyle();

        fetchLastLocation();
//        Intent driverService  = new Intent(mContext, DriverLocationService.class);
//        stopService(driverService);
//        Intent service = new Intent(getApplicationContext(), DriverLocationService.class);
//        getApplicationContext().startService(service);

        // This code adds the listener and subscribes passenger to channel with driver's location.
        if (connected_driver_id != 0) {

            View parentLayout = findViewById(android.R.id.content);
            Snackbar.make(parentLayout, "Wait for driver to start Journey!", Snackbar.LENGTH_LONG)
                    .setAction("CLOSE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    })
                    .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                    .show();

            MainHomeActivity.pubNub.addListener(new SubscribeCallback() {
                @Override
                public void status(PubNub pub, PNStatus status) {

                }

                @Override
                public void message(PubNub pub, final PNMessageResult message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Map<String, String> newLocation = JsonUtil.fromJson(message.getMessage().toString(), LinkedHashMap.class);
                                updateUI(newLocation);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void presence(PubNub pub, PNPresenceEventResult presence) {

                }
            });
            MainHomeActivity.pubNub.subscribe()
                    .channels(Arrays.asList(connected_driver_id+"-driverchannel")) // subscribed to channel
                    .execute();
        }
        else {
            View parentLayout = findViewById(android.R.id.content);
            Snackbar.make(parentLayout, "You are not working with any existing driver right now!", Snackbar.LENGTH_LONG)
                    .setAction("CLOSE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    })
                    .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                    .show();
        }
    }

    private void setMapStyle() {
        try {
            mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            mContext, R.raw.map_style_retro));

        } catch (Resources.NotFoundException e) {
            Log.e("WingmanActivity--", "Cannot find style.", e);
        }
    }

    private void fetchLastLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
//                    Toast.makeText(MainActivity.this, "Permission not granted, Kindly allow permission", Toast.LENGTH_LONG).show();
                showPermissionAlert();
                return;
            }
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            Log.d("LAST LOCATION: ", location.toString());
                            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            yourLocationMarker = new MarkerOptions().position(mCurrentLocation);
                            mMap.addMarker(yourLocationMarker.title("Your Location"));
                            addCameraToMap(mCurrentLocation);
                        }
                    }
                });

    }

    private void checkLocationSetting(LocationSettingsRequest.Builder builder) {

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                return;
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull final Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                    builder1.setTitle(R.string.location_update_request);
                    builder1.setMessage(R.string.send_location_continious);
                    builder1.create();
                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            try {
                                resolvable.startResolutionForResult(WingmenActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                    builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(mContext, "Location update permission not granted", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder1.show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
            }
            else {
                checkLocationSetting(builder);
            }
        }
    }

    private void addCameraToMap(LatLng latLng){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(17)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void showPermissionAlert(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WingmenActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // permission was denied, show alert to explain permission
                    showPermissionAlert();
                }else{
                    //permission is granted now start a background service
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fetchLastLocation();
                    }
                }
            }
        }
    }

    private void updateUI(Map<String, String> newLoc) {
        LatLng newLocation = new LatLng(Double.valueOf(newLoc.get("lat")), Double.valueOf(newLoc.get("lng")));
        bearing = Float.valueOf(newLoc.get("bearing"));
        if (driverMarker != null) {
            animateCar(newLocation);
            boolean contains = mMap.getProjection()
                    .getVisibleRegion()
                    .latLngBounds
                    .contains(newLocation);
            if (!contains) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
            }
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    newLocation, 16.5f));
            driverMarker = mMap.addMarker(new MarkerOptions()
                    .position(newLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                    .anchor(0.5f, 0.5f)
                    .rotation(bearing)
                    .flat(true));
        }
    }

    private void animateCar(final LatLng destination) {
        final LatLng startPosition = driverMarker.getPosition();
        final LatLng endPosition = new LatLng(destination.latitude, destination.longitude);
        final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(2000); // duration 2 seconds
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                try {
                    float v = animation.getAnimatedFraction();
                    LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                    driverMarker.setPosition(newPosition);
                    driverMarker.setAnchor(0.5f, 0.5f);
                    driverMarker.setRotation(bearing);
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition
                            (new CameraPosition.Builder().target(newPosition)
                                    .bearing(bearing)
                                    .zoom(16.5f).build()));
                } catch (Exception ex) {
                }
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        valueAnimator.start();
    }

    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
//        bindService(new Intent(this, DriverLocationService.class), this,
//                Context.BIND_AUTO_CREATE);
        mMapFragment.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapFragment.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindService(this);
//        Intent service = new Intent(getApplicationContext(), DriverLocationService.class);
//        getApplicationContext().stopService(service);
        mMapFragment.onDestroy();
        mMap.clear();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapFragment.onLowMemory();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
//        DriverLocationService.MyBinder b = (DriverLocationService.MyBinder) service;
//        s = b.getService();

        //Toast.makeText(WingmenActivity.this, "Connected to service", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        s = null;
    }
}


