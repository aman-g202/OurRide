package com.ourride.driver.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;

import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ourride.driver.R;
import com.ourride.driver.apis.ApiConfig;
import com.ourride.driver.constant.Constant;
import com.ourride.driver.receiver.MyBroadCastReceiver;
import com.ourride.driver.services.DriverLocationService;
import com.ourride.driver.ui.activity.EditProfileActivity;
import com.ourride.driver.ui.activity.LoginActivity;
import com.ourride.driver.ui.activity.ShareAppActivity;
import com.ourride.driver.ui.fragment.BookingFragment;
import com.ourride.driver.ui.fragment.DashboardFragment;
import com.ourride.driver.ui.fragment.HistoryFragment;
import com.ourride.driver.ui.fragment.ProfileFragment;
import com.ourride.driver.ui.fragment.RideRequestFragment;
import com.ourride.driver.ui.fragment.RoleFragment;
import com.ourride.driver.utils.BaseActivity;
import com.ourride.driver.utils.FragmentUtils;
import com.ourride.driver.utils.NetworkManager;
import com.ourride.driver.utils.SharedPrefrence;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;

import org.json.JSONException;
import org.json.JSONObject;

public class MainHomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, IApiResponse, ServiceConnection {

    public static FragmentManager fragmentManager;
    public static FragmentUtils fragmentUtils;
    public static TextView tvEditProfile;
    public static Toolbar toolbar;
    public String nameUser;
    public String emailUser;
    public String phoneUser;
    public String userImageUrl;
    private ApiRequest apiRequest;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest.Builder builder;
    private FusedLocationProviderClient fusedLocationClient;
    private MyBroadCastReceiver myBroadcastReceiver1,myBroadcastReceiver;
    private int user_id;
    private static final String TAG = "MainHomeActivity";
    public static PubNub pubNub;
    private static final int REQUEST_CHECK_SETTINGS = 102;
    private DriverLocationService s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tvEditProfile = findViewById(R.id.tvEditProfile);
        tvEditProfile.setVisibility(View.GONE);
        tvEditProfile.setOnClickListener(this);
        mContext = MainHomeActivity.this;
        myBroadcastReceiver1 = null;
        myBroadcastReceiver = null;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            checkPermission();
        }

        user_id = SharedPrefrence.getInt(mContext,"user_id");
        apiRequest = new ApiRequest(mContext, (IApiResponse)this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        mLocationRequest = createLocationRequest();
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        checkLocationSetting(builder);


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
//To do//
                            return;
                        }

                        // Get the Instance ID token//
                        String token = task.getResult().getToken();
                        Log.d(TAG, "FCM TOKEN: "+token);

                        sendRegistrationToServer(token);

//                        HashMap<String,String> paramReq = new HashMap<>();
//
//                        paramReq.put("token", token);
//
//                        apiRequest.postRequest(ApiConfig.baseUrl+"edit/"+user_id+".json", "NotificationRequest", paramReq, Request.Method.POST);

                    }
                });

        initPubnub();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fragmentManager = getSupportFragmentManager();
        fragmentUtils = new FragmentUtils(fragmentManager);
        toolbar.setTitle("Home");
        fragmentUtils.replaceFragment(new DashboardFragment(), Constant.DashboardFragment, R.id.main_frame);

        Intent service = new Intent(getApplicationContext(), DriverLocationService.class);
        getApplicationContext().startService(service);

        NetworkManager networkManager = new NetworkManager(mContext);
        if (!networkManager.isNetworkAvailable()) {
            myBroadcastReceiver1 = new MyBroadCastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(myBroadcastReceiver1, filter);
        }
    }

    private void sendRegistrationToServer(final String token) {
        Log.e("MainHomeActivity", "sendRegistrationToServer: " + token);
        if (SharedPrefrence.getInt(mContext,"user_id") > 0) {
//            Registering receiver using Local Broadcast---------
            myBroadcastReceiver=new MyBroadCastReceiver();
            IntentFilter filter1=new IntentFilter();
            filter1.addAction("com.ourride.android.action.broadcast");
            LocalBroadcastManager.getInstance(mContext).registerReceiver(myBroadcastReceiver,filter1);

//            sending broadcast to MyBroadCastReceiver

            Intent intent = new Intent("com.ourride.android.action.broadcast");
            intent.putExtra("token", token);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.e(TAG, "sendRegistrationToServer: " + token);
        }
    }

    private void initPubnub() {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-9a8fc866-b76d-11e9-98d6-a6da8a14d776");
        pnConfiguration.setPublishKey("pub-c-22eaf815-cdd5-4775-8195-be76a1ca0ffa");
        pnConfiguration.setSecure(true);
        pubNub = new PubNub(pnConfiguration);
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 123: {
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

    private void showPermissionAlert(){
        if (ActivityCompat.checkSelfPermission(MainHomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainHomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainHomeActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }
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
                                resolvable.startResolutionForResult(MainHomeActivity.this,
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
                        }
                    }
                });

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Fragment profile = fragmentManager.findFragmentByTag(Constant.ProfileFragment);
        Fragment myBooking = fragmentManager.findFragmentByTag(Constant.BookingFragment);
        Fragment history = fragmentManager.findFragmentByTag(Constant.HistoryFragment);
        Fragment role = fragmentManager.findFragmentByTag(Constant.RoleFragment);
        Fragment riderequest = fragmentManager.findFragmentByTag(Constant.RideRequestFragment);


        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        if (profile != null) {
            toolbar.setTitle(Constant.DashboardFragment);
            fragmentUtils.replaceFragment(new DashboardFragment(), Constant.DashboardFragment, R.id.main_frame);
        }
        else if (myBooking != null) {
            toolbar.setTitle(Constant.DashboardFragment);
            fragmentUtils.replaceFragment(new DashboardFragment(), Constant.DashboardFragment, R.id.main_frame);
        }
        else if (history != null) {
            toolbar.setTitle(Constant.DashboardFragment);
            fragmentUtils.replaceFragment(new DashboardFragment(), Constant.DashboardFragment, R.id.main_frame);
        }
        else if (riderequest != null) {
            toolbar.setTitle(Constant.DashboardFragment);
            fragmentUtils.replaceFragment(new DashboardFragment(), Constant.DashboardFragment, R.id.main_frame);
        }
        else if (role != null) {
            toolbar.setTitle(Constant.DashboardFragment);
            fragmentUtils.replaceFragment(new DashboardFragment(), Constant.DashboardFragment, R.id.main_frame);
        }
        else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Closing Activity")
                    .setMessage("Are you sure you want to close this app?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//  ****************************     make all these values to 0 when ride completed    ******************************************
//                            clearEverySavedData();
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }

                    })
                    .setNegativeButton("NO", null)
                    .show();
        }
    }

    private void clearEverySavedData() {
        SharedPrefrence.saveInt(mContext, "connected_driver_id", 0);
        SharedPrefrence.saveInt(mContext, "selected_wingman_id", 0);
        SharedPrefrence.save(mContext, "selected_wingman_lat", "0.0");
        SharedPrefrence.save(mContext, "selected_wingman_lng", "0.0");
        SharedPrefrence.saveInt(mContext, "connected_passenger_id", 0);
        SharedPrefrence.saveInt(mContext, "connected_passenger_booking_id", 0);
        SharedPrefrence.save(mContext, "connected_passenger_user_to_lat", "0.0");
        SharedPrefrence.save(mContext, "connected_passenger_user_to_lng", "0.0");
        SharedPrefrence.save(mContext, "connected_passenger_user_from_lat", "0.0");
        SharedPrefrence.save(mContext, "connected_passenger_user_from_lng", "0.0");
        SharedPrefrence.save(mContext, "WingmanAcceptedRequest", "");
        SharedPrefrence.save(mContext, "RideStartedWithPassenger", "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_home, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.icDashBoard) {
            toolbar.setTitle(Constant.DashboardFragment);
            fragmentUtils.replaceFragment(new DashboardFragment(), Constant.DashboardFragment, R.id.main_frame);
        } else if (id == R.id.icProfile) {
            toolbar.setTitle(Constant.ProfileFragment);
            fragmentUtils.replaceFragment(new ProfileFragment(), Constant.ProfileFragment, R.id.main_frame);
        } else if (id == R.id.icMyBookings) {
            toolbar.setTitle(Constant.BookingFragment);
            fragmentUtils.replaceFragment(new BookingFragment(), Constant.BookingFragment, R.id.main_frame);
        } else if (id == R.id.icHistory) {
            toolbar.setTitle(Constant.HistoryFragment);
            fragmentUtils.replaceFragment(new HistoryFragment(), Constant.HistoryFragment, R.id.main_frame);
        } else if (id == R.id.icShare) {
            startActivity(new Intent(mContext, ShareAppActivity.class));
        } else if (id == R.id.icRideRequest) {
            toolbar.setTitle("Ride Request");
            fragmentUtils.replaceFragment(new RideRequestFragment(), Constant.RideRequestFragment, R.id.main_frame);
        } else if (id == R.id.icRole) {
            toolbar.setTitle("Role");
            fragmentUtils.replaceFragment(new RoleFragment(), Constant.RoleFragment, R.id.main_frame);
        } else if (id == R.id.icLogout) {
            SharedPrefrence.saveInt(mContext, "user_id",0);
            clearEverySavedData();
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvEditProfile:
                if (SharedPrefrence.getInt(mContext,"user_id") != 0){
                    user_id = SharedPrefrence.getInt(mContext,"user_id");
                    apiRequest.postRequest(ApiConfig.baseUrl+"view/"+user_id+".json", "ShowProfile", Request.Method.GET);
                }else{
                    Toast.makeText(mContext, "Oops, some error occured contact admin", Toast.LENGTH_LONG).show();
                }
                break;

        }
    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {
        try {
            if (tag_json_obj.equals("ShowProfile")) {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("user")) {
                    JSONObject responseObject = jsonObject.getJSONObject("user");
                    if (responseObject.has("name")){
                        nameUser = responseObject.getString("name");
                    }
                    emailUser = responseObject.getString("email");
                    phoneUser =responseObject.getString("phone");
                    userImageUrl = responseObject.getString("profile_image_url");
                    Intent intent = new Intent(mContext, EditProfileActivity.class);
                    intent.putExtra("name",nameUser);
                    intent.putExtra("email", emailUser);
                    intent.putExtra("phone", phoneUser);
                    intent.putExtra("user_image_url", userImageUrl);
//                    Toast.makeText(mContext, responseObject.getString("profile_image_url"), Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }else{
                    Toast.makeText(mContext, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                }
            }
            else if (tag_json_obj.equals("NotificationRequest")) {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                    return;
                }else{
                    Toast.makeText(mContext, responseObject.getString("message"), Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(mContext, "Oops some error occured!", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Log.d("aman", e.getMessage());
        }

    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setSmallestDisplacement(30);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, error.getMessage());
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, DriverLocationService.class), this,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
        Intent service = new Intent(getApplicationContext(), DriverLocationService.class);
        getApplicationContext().stopService(service);
        if (myBroadcastReceiver1 != null) {
            unregisterReceiver(myBroadcastReceiver1);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        DriverLocationService.MyBinder b = (DriverLocationService.MyBinder) service;
        s = b.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
