package com.ourride.driver.ui.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.ourride.driver.R;
import com.ourride.driver.apis.ApiConfig;
import com.ourride.driver.ui.MainHomeActivity;
import com.ourride.driver.utils.SharedPrefrence;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

public class SelectionActivity extends AppCompatActivity implements IApiResponse, ServiceConnection {

    private Button driverButton, passengerButton;
    private Context mContext;
    private Toolbar toolbar;
    private ApiRequest apiRequest;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest.Builder builder;
    private FusedLocationProviderClient fusedLocationClient;
    private int user_id;
    public  static PubNub pubNub = null;
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 100;
    private static final int REQUEST_CHECK_SETTINGS = 102;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        mContext = SelectionActivity.this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }

        driverButton = findViewById(R.id.driverButton);
        passengerButton = findViewById(R.id.passengerButton);
        toolbar= findViewById(R.id.toolbar);
        apiRequest = new ApiRequest(mContext, (IApiResponse) this);

        user_id = SharedPrefrence.getInt(mContext, "user_id");
        toolbar.setTitle("Role");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorToolBarText));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initPubnub();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        mLocationRequest = createLocationRequest();
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        checkLocationSetting(builder);


        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, String> paramReqs = new HashMap<>();
                paramReqs.put("today_iam", "Driver");

                apiRequest.postRequest(ApiConfig.baseUrl+"edit/"+user_id+".json", "DriverRole", paramReqs, Request.Method.POST);
            }
        });

        passengerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, String> paramReqs = new HashMap<>();
                paramReqs.put("today_iam", "Wingman");

                apiRequest.postRequest(ApiConfig.baseUrl+"edit/"+user_id+".json", "WingmanRole", paramReqs, Request.Method.POST);
            }
        });

    }

    private void initPubnub() {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-9a8fc866-b76d-11e9-98d6-a6da8a14d776");
        pnConfiguration.setPublishKey("pub-c-22eaf815-cdd5-4775-8195-be76a1ca0ffa");
        pnConfiguration.setSecure(true);
        pubNub = new PubNub(pnConfiguration);
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
                                resolvable.startResolutionForResult(SelectionActivity.this,
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

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setSmallestDisplacement(30);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void showPermissionAlert(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SelectionActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {

        try {

            if (tag_json_obj.equals("DriverRole")) {

                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                     SharedPrefrence.save(mContext, "role", "Driver");
                     startActivity(new Intent(SelectionActivity.this, MainHomeActivity.class));
                }else{
                    Toast.makeText(mContext, responseObject.getString("message"), Toast.LENGTH_SHORT).show();
                }

            } else if (tag_json_obj.equals("WingmanRole")) {

                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                    SharedPrefrence.save(mContext, "role", "Wingman");
                    startActivity(new Intent(SelectionActivity.this, MainHomeActivity.class));
                }else{
                    Toast.makeText(mContext, responseObject.getString("message"), Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(mContext, "Oops some error occured!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, error.getMessage());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
