package com.ourride.driver.ui.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ourride.driver.EntityClasses.DirectionObject;
import com.ourride.driver.EntityClasses.LegsObject;
import com.ourride.driver.EntityClasses.PolylineObject;
import com.ourride.driver.EntityClasses.RouteObject;
import com.ourride.driver.EntityClasses.StepsObject;
import com.ourride.driver.R;
import com.ourride.driver.services.LocationService;
import com.ourride.driver.ui.MainHomeActivity;
import com.ourride.driver.utils.Helper;
import com.ourride.driver.utils.PathRoute.GsonRequest;
import com.ourride.driver.utils.PathRoute.VolleySingleton;
import com.ourride.driver.utils.SharedPrefrence;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverMapActivity extends AppCompatActivity implements OnMapReadyCallback, ServiceConnection, IApiResponse {

    private Context mContext;
    private SupportMapFragment mMapFragment;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mlocationCallback;
    private LocationSettingsRequest.Builder builder;
    private LocationService s;
    private LatLng mCurrentLocation;
    private GoogleMap mMap;
    private LatLng wingmanLocation;
    private MarkerOptions yourLocationMarker;
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 100;
    private static final int REQUEST_CHECK_SETTINGS = 102;
    private static final String TAG = DriverMapActivity.class.getSimpleName();
    public static PubNub pubNub;
    private Handler h;
    private Runnable runnable;
    private List<LatLng> driverMovingLocations;
    private Polyline polyline1;
    private Double selected_wingman_lat, selected_wingman_lng;
    private int selected_wingman_id;
    private Toolbar toolbar;
    private TextView enterOtp, startRide, completeRide;
    private BottomSheetDialog dialog;
    private ApiRequest apiRequest;
    private int checkWhichPathToDraw = 0;
    private int userId, bookingId;
    AlertDialog.Builder dialogBuilder;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }

        mContext = DriverMapActivity.this;
        toolbar= findViewById(R.id.toolbar);
        enterOtp = findViewById(R.id.enterOtp);
        startRide = findViewById(R.id.startRide);
        completeRide = findViewById(R.id.completeRide);
        driverMovingLocations = new ArrayList<LatLng>();

        apiRequest = new ApiRequest(mContext, (IApiResponse)this);

        userId = SharedPrefrence.getInt(mContext, "user_id");
        bookingId = SharedPrefrence.getInt(mContext, "connected_passenger_booking_id");

        if (!Helper.isNetworkAvailable(mContext)) {
            Toast.makeText(mContext, "Please turn on the internet", Toast.LENGTH_LONG).show();
        }

        toolbar.setTitle("Navigation");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorToolBarText));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initPubnub();

        enterOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOtpTakingMethod();
            }
        });
        startRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawRouteFromPassengerSourceToDestination();
            }
        });

        completeRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                builder1.setIcon(android.R.drawable.ic_dialog_alert);
                builder1.setTitle("Ride Complete");
                builder1.setMessage(" Do you want to complete the ride ? ");
                builder1.create();
                builder1.setPositiveButton("COMPLETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String, String> params = new HashMap<>();
                        params.put("driver_id", String.valueOf(userId));
                        params.put("booking_id", String.valueOf(bookingId));

                        apiRequest.postRequest("http://3.18.88.39/api/bookings/complete.json", "CompleteBooking", params, Request.Method.POST);
                    }
                });
                builder1.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder1.show();
            }
        });

        selected_wingman_id = SharedPrefrence.getInt(mContext, "selected_wingman_id");
        if (SharedPrefrence.get(mContext, "selected_wingman_lat") != "" && SharedPrefrence.get(mContext, "selected_wingman_lng") != "") {
            selected_wingman_lat = Double.valueOf(SharedPrefrence.get(mContext, "selected_wingman_lat"));
            selected_wingman_lng = Double.valueOf(SharedPrefrence.get(mContext, "selected_wingman_lng"));
        }


        if ((selected_wingman_id != 0 && SharedPrefrence.get(mContext, "WingmanAcceptedRequest").equalsIgnoreCase("Request Accepted By Wingman")) || SharedPrefrence.get(mContext, "RideStartedWithPassenger").equals("RideStartedWithPassenger")) {

            Intent intent = getIntent();
            wingmanLocation = new LatLng(selected_wingman_lat, selected_wingman_lng);

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            showPermissionAlert();

            mLocationRequest = createLocationRequest();
            builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            checkLocationSetting(builder);

            mlocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        // Update UI with location data
                        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                        float bearing = location.getBearing();
                        driverMovingLocations.add(loc);
                        drawCustomMovementLine(driverMovingLocations, bearing);
                    }
                }

                ;
            };

            try {
                // Loading map
                mMap.clear();
                initilizeMap();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(mContext, "You don't have any wingman yet!", Toast.LENGTH_LONG).show();
            finish();
            startActivity(new Intent(DriverMapActivity.this, MainHomeActivity.class));
        }
    }

    private void initPubnub() {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-9a8fc866-b76d-11e9-98d6-a6da8a14d776");
        pnConfiguration.setPublishKey("pub-c-22eaf815-cdd5-4775-8195-be76a1ca0ffa");
        pnConfiguration.setSecure(true);
        pubNub = new PubNub(pnConfiguration);
    }

    private void initilizeMap() {
        if (mMap == null) {
            mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.driverMap);
            mMapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMaxZoomPreference(16.0f);
        if (mMap == null) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        setMapStyle();

        checkWhichPathToDraw = 0;
        if (SharedPrefrence.get(mContext, "RideStartedWithPassenger").equals("")) {
            Log.e("DriverMap--", "fetchLastLOcationRun");
            fetchLastLocation();
        }
        startLocationUpdates();
        h = new Handler();
        runnable = new Runnable() {
            private long time = 0;
            @Override
            public void run() {
                startLocationUpdates();
                time += 10000;
                h.postDelayed(this, 10000);
            }
        };
        h.postDelayed(runnable, 10000); // 10 second delay (takes millis)

        Intent service = new Intent(getApplicationContext(), LocationService.class);
        getApplicationContext().startService(service);

        if (SharedPrefrence.get(mContext, "RideStartedWithPassenger").equals("RideStartedWithPassenger")) {
            drawRouteFromPassengerSourceToDestination();
        }

    }

    private void setMapStyle() {
        try {
            mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            mContext, R.raw.map_style_retro));

        } catch (Resources.NotFoundException e) {
            Log.e("DriverMapActivity---", "Cannot find style.", e);
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
        if (fusedLocationClient != null) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                Log.d("LAST LOCATION: ", location.toString());
                                mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                driverMovingLocations.add(mCurrentLocation);
                                yourLocationMarker = new MarkerOptions().position(mCurrentLocation);
                                mMap.addMarker(yourLocationMarker.title("Your Location"));
                                addCameraToMap(mCurrentLocation);
                                drawRoute();
                            }
                        }
                    });
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
        if (fusedLocationClient != null) {
            fusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mlocationCallback,
                    null /* Looper */);
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(mlocationCallback);
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
                startLocationUpdates();
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
                                resolvable.startResolutionForResult(DriverMapActivity.this,
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
                startLocationUpdates();
            }
            else {
                checkLocationSetting(builder);
            }
        }
    }

    private void addCameraToMap(LatLng latLng){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(16.5f)
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
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);
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
                        if (SharedPrefrence.get(mContext, "RideStartedWithPassenger").equals("")) {
                            fetchLastLocation();
                        }
                    }
                }
            }
        }
    }

    private void drawRoute() {

        LatLng defaultLocation = yourLocationMarker.getPosition();

        //use Google Direction API to get the route between these Locations
        String directionApiPath = Helper.getUrl(String.valueOf(defaultLocation.latitude), String.valueOf(defaultLocation.longitude),
                String.valueOf(wingmanLocation.latitude), String.valueOf(wingmanLocation.longitude));
        Log.d(TAG, "PATH-driver to wingman *********--------- " + directionApiPath);
        getDirectionFromDirectionApiServer(directionApiPath);

    }

    private void drawRouteFromWingmanToPassenger() {
        if (SharedPrefrence.getInt(mContext, "connected_passenger_id") > 0) {
            String passengerSourceLat = SharedPrefrence.get(mContext, "connected_passenger_user_from_lat");
            String passengerSourceLng = SharedPrefrence.get(mContext, "connected_passenger_user_from_lng");
            LatLng passengerSourceLocation = new LatLng(Double.valueOf(passengerSourceLat), Double.valueOf(passengerSourceLng));
            MarkerOptions passengerSourceMarker = new MarkerOptions().position(passengerSourceLocation);

            mMap.addMarker(passengerSourceMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("Passenger Location"));

            //use Google Direction API to get the route between these Locations
            String directionApiPath = Helper.getUrl(String.valueOf(wingmanLocation.latitude), String.valueOf(wingmanLocation.longitude),
                    passengerSourceLat, passengerSourceLng);
            Log.d(TAG, "PATH-wingman to passenger-----*********--------- " + directionApiPath);
            getDirectionFromDirectionApiServer(directionApiPath);
        } else {
            Toast.makeText(mContext, "You do not have any passenger, kindly wait for passenger ride request", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawRouteFromPassengerSourceToDestination() {
        if (SharedPrefrence.getInt(mContext, "connected_passenger_id") > 0) {
            refreshMap(mMap);
            checkWhichPathToDraw = 2;
            enterOtp.setVisibility(View.GONE);
            startRide.setVisibility(View.GONE);
            completeRide.setVisibility(View.VISIBLE);
            String passengerDestinationLat = SharedPrefrence.get(mContext, "connected_passenger_user_to_lat");
            String passengerDestinationLng = SharedPrefrence.get(mContext, "connected_passenger_user_to_lng");
            String passengerSourceLat = SharedPrefrence.get(mContext, "connected_passenger_user_from_lat");
            String passengerSourceLng = SharedPrefrence.get(mContext, "connected_passenger_user_from_lng");
            LatLng passengerSourceLocation = new LatLng(Double.valueOf(passengerSourceLat), Double.valueOf(passengerSourceLng));
            LatLng passengerDestinationLocation = new LatLng(Double.valueOf(passengerDestinationLat), Double.valueOf(passengerDestinationLng));
            MarkerOptions passengerSourceMarker = new MarkerOptions().position(passengerSourceLocation);
            MarkerOptions passengerDestinationMarker = new MarkerOptions().position(passengerDestinationLocation);

            mMap.addMarker(passengerSourceMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("Passenger Source Location"));
            mMap.addMarker(passengerDestinationMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("Passenger Destination Location"));

            //use Google Direction API to get the route between these Locations
            String directionApiPath = Helper.getUrl(passengerSourceLat, passengerSourceLng,
                    passengerDestinationLat, passengerDestinationLng);
            Log.d(TAG, "PATH-Passenger Source to Destination-----*********--------- " + directionApiPath);
            getDirectionFromDirectionApiServer(directionApiPath);
        } else {
            Toast.makeText(mContext, "You do not have any passenger, kindly wait for passenger ride request", Toast.LENGTH_SHORT).show();
        }
    }

    private void markStartingLocationOnMap(GoogleMap mapObject, LatLng location){
        mapObject.addMarker(new MarkerOptions().position(location).title("Current location"));
        mapObject.moveCamera(CameraUpdateFactory.newLatLng(location));

    }

    private void refreshMap(GoogleMap mapInstance){
        mapInstance.clear();
    }

    private void getDirectionFromDirectionApiServer(String url){
        GsonRequest<DirectionObject> serverRequest = new GsonRequest<DirectionObject>(
                Request.Method.GET,
                url,
                DirectionObject.class,
                createRequestSuccessListener(),
                createRequestErrorListener());
        serverRequest.setRetryPolicy(new DefaultRetryPolicy(
                Helper.MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(serverRequest);
    }

    private Response.Listener<DirectionObject> createRequestSuccessListener() {
        return new Response.Listener<DirectionObject>() {
            @Override
            public void onResponse(DirectionObject response) {
                try {
                    Log.d("JSON Response", response.toString());
                    if(response.getStatus().equals("OK")){
                        List<LatLng> mDirections = getDirectionPolylines(response.getRoutes());
                        if (checkWhichPathToDraw == 0) {
                            drawRouteOnMap(mMap, mDirections);
                        } else if (checkWhichPathToDraw == 1) {
                            drawSecondRouteOnMap(mMap, mDirections);
                        } else {
                            drawFinalRouteOnMap(mMap, mDirections);
                        }
                    }else{
                        Toast.makeText(DriverMapActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        };
    }

    private List<LatLng> getDirectionPolylines(List<RouteObject> routes){
        List<LatLng> directionList = new ArrayList<LatLng>();
        for(RouteObject route : routes){
            List<LegsObject> legs = route.getLegs();
            for(LegsObject leg : legs){
                List<StepsObject> steps = leg.getSteps();
                for(StepsObject step : steps){
                    PolylineObject polyline = step.getPolyline();
                    String points = polyline.getPoints();
                    List<LatLng> singlePolyline = decodePoly(points);
                    for (LatLng direction : singlePolyline){
                        directionList.add(direction);
                    }
                }
            }
        }
        return directionList;
    }

    private Response.ErrorListener createRequestErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
    }

    private void drawRouteOnMap(GoogleMap map, List<LatLng> positions){
        MarkerOptions wingmanLocMarker = new MarkerOptions().position(wingmanLocation);
        map.addMarker(wingmanLocMarker.title("Wingman Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        PolylineOptions options = new PolylineOptions().width(12).color(Color.RED).geodesic(true);
        options.addAll(positions);
        Polyline polyline = map.addPolyline(options);
        polyline.setJointType(JointType.ROUND);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(positions.get(1).latitude, positions.get(1).longitude))
                .zoom(17)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        checkWhichPathToDraw = 1;
        drawRouteFromWingmanToPassenger();
    }

    private void drawSecondRouteOnMap(GoogleMap map, List<LatLng> positions){
        PolylineOptions options = new PolylineOptions().width(12).color(Color.rgb(245,124,0)).geodesic(true);
        options.addAll(positions);
        Polyline polyline = map.addPolyline(options);
        polyline.setJointType(JointType.ROUND);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(positions.get(1).latitude, positions.get(1).longitude))
                .zoom(17)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void drawFinalRouteOnMap(GoogleMap map, List<LatLng> positions) {
        PolylineOptions options = new PolylineOptions().width(12).color(Color.rgb(245,124,0)).geodesic(true);
        options.addAll(positions);
        Polyline polyline = map.addPolyline(options);
        polyline.setJointType(JointType.ROUND);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(positions.get(1).latitude, positions.get(1).longitude))
                .zoom(17)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void drawCustomMovementLine(List<LatLng> positions, float bearing) {
        if (polyline1 != null) {
            polyline1.remove();
        }
        polyline1 = mMap.addPolyline(new PolylineOptions()
                .addAll(positions));
//        polyline1.setStartCap(new CustomCap(
//                BitmapDescriptorFactory.fromResource(R.drawable.my_location), 5));
        polyline1.setWidth(12);
        polyline1.setColor(0xff000000);
        polyline1.setJointType(JointType.ROUND);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(positions.get(positions.size()-1).latitude, positions.get(positions.size()-1).longitude))
                .bearing(bearing)
                .zoom(16.5f)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private void startOtpTakingMethod() {
        View view = getLayoutInflater().inflate(R.layout.passenger_otp_layout, null);
        final EditText enterOtpPassenger;
        TextView otpPassengerButton;
        enterOtpPassenger = view.findViewById(R.id.enterOtpPassenger);
        otpPassengerButton = view.findViewById(R.id.otpPassengerButton);

        dialog = new BottomSheetDialog(mContext);
        dialog.setContentView(view);
        dialog.show();

        otpPassengerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enterOtpPassenger.getText().toString().equals("")) {
                    Toast.makeText(mContext, "Enter Otp to start the ride", Toast.LENGTH_LONG).show();
                } else {
                    int otp = Integer.parseInt(enterOtpPassenger.getText().toString());
                    // send to the server for the verification
                    if (SharedPrefrence.getInt(mContext, "connected_passenger_id") > 0) {
                        int booking_id = SharedPrefrence.getInt(mContext, "connected_passenger_booking_id");
                        Map<String, String> param = new HashMap<>();
                        param.put("booking_id", String.valueOf(booking_id));
                        param.put("otp", String.valueOf(otp));

                        apiRequest.postRequest("http://3.18.88.39/api/bookings/verify_otp.json", "VefifyingOtp", param, Request.Method.POST);

                    } else {
                        Toast.makeText(mContext, "You do not have any passenger, kindly wait for passenger ride request", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
        if (selected_wingman_id != 0 && SharedPrefrence.get(mContext, "WingmanAcceptedRequest").equalsIgnoreCase("Request Accepted By Wingman")) {
            bindService(new Intent(this, LocationService.class), this,
                    Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (selected_wingman_id != 0 && SharedPrefrence.get(mContext, "WingmanAcceptedRequest").equals("Request Accepted By Wingman")) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (selected_wingman_id != 0 && SharedPrefrence.get(mContext, "WingmanAcceptedRequest").equals("Request Accepted By Wingman")) {
            h.removeCallbacks(runnable);
            unbindService(this);
            Intent service = new Intent(getApplicationContext(), LocationService.class);
            getApplicationContext().stopService(service);
            mMap.clear();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        LocationService.MyBinder b = (LocationService.MyBinder) service;
        s = b.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {
        if (tag_json_obj.equals("VefifyingOtp")) {
            JSONObject resultObject = new JSONObject(response);
            JSONObject responseObject = resultObject.getJSONObject("response");
            if (responseObject.getString("status").equalsIgnoreCase("success")) {
                dialog.dismiss();
                enterOtp.setVisibility(View.GONE);
                startRide.setVisibility(View.VISIBLE);
                SharedPrefrence.save(mContext, "RideStartedWithPassenger", "RideStartedWithPassenger");
            } else {
                Toast.makeText(mContext, "Incorrect Otp, Kindly re-enter again", Toast.LENGTH_SHORT).show();
            }

        } else if (tag_json_obj.equals("CompleteBooking")) {
            JSONObject resultObject = new JSONObject(response);
            JSONObject responseObject = resultObject.getJSONObject("response");
            if (responseObject.getString("status").equals("success")) {
                dialogBuilder = new AlertDialog.Builder(mContext);
                View layoutView = getLayoutInflater().inflate(R.layout.dialog_ridecomplete_layout, null);
                Button buttonDialog = layoutView.findViewById(R.id.btnDialog);
                dialogBuilder.setView(layoutView);
                dialogBuilder.setCancelable(false);
                alertDialog = dialogBuilder.create();
                alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
                buttonDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        clearEverySavedData();
                        Intent intent = new Intent(mContext, MainHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });

            } else {
                Toast.makeText(mContext, "Some error occured, try again!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, "Some error occured on the server side!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        try {
            Log.e("DriverMap_Error", error.getMessage());
        } catch (Exception e) {
            Log.e("DriverMap_Exception", e.getMessage());
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
}
