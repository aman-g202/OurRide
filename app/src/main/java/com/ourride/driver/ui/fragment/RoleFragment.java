package com.ourride.driver.ui.fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.api.ResolvableApiException;
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
import com.ourride.driver.constant.Constant;
import com.ourride.driver.ui.MainHomeActivity;
import com.ourride.driver.utils.BaseFragment;
import com.ourride.driver.utils.SharedPrefrence;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.android.volley.VolleyLog.TAG;
import static com.ourride.driver.ui.MainHomeActivity.tvEditProfile;

public class RoleFragment extends BaseFragment implements IApiResponse, ServiceConnection {
    private View rootView;
    private Button driverButton, wingmanButton;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest.Builder builder;
    private TextView roleTextView;
    private ApiRequest apiRequest;
    private int user_id;
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 100;
    private static final int REQUEST_CHECK_SETTINGS = 102;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_role, container, false);
        tvEditProfile.setVisibility(View.GONE);
        mContext = getActivity();
        activity = getActivity();

        apiRequest = new ApiRequest(mContext, (IApiResponse) this);
        user_id = SharedPrefrence.getInt(mContext, "user_id");

        showPermissionAlert();

        mLocationRequest = createLocationRequest();
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        checkLocationSetting(builder);

        init();
        return rootView;
    }

    private void init() {
        driverButton = rootView.findViewById(R.id.driverButton);
        wingmanButton = rootView.findViewById(R.id.passengerButton);
        roleTextView = rootView.findViewById(R.id.roleTextView);

        roleTextView.setText("Your Current Role is " + SharedPrefrence.get(mContext, "role"));

        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> paramReqs = new HashMap<>();
                paramReqs.put("today_iam", "Driver");

                apiRequest.postRequest(ApiConfig.baseUrl+"edit/"+user_id+".json", "DriverRole", paramReqs, Request.Method.POST);
            }
        });
        wingmanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> paramReqs = new HashMap<>();
                paramReqs.put("today_iam", "Wingman");

                apiRequest.postRequest(ApiConfig.baseUrl+"edit/"+user_id+".json", "WingmanRole", paramReqs, Request.Method.POST);
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

    private void checkLocationSetting(LocationSettingsRequest.Builder builder) {

        SettingsClient client = LocationServices.getSettingsClient(mContext);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                return;
            }
        });

        task.addOnFailureListener(getActivity(), new OnFailureListener() {
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
                                resolvable.startResolutionForResult(getActivity(),
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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

    private void showPermissionAlert(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);
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
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {

        try {

            if (tag_json_obj.equals("DriverRole")) {

                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                    SharedPrefrence.save(mContext, "role", "Driver");
                    Toast.makeText(mContext, "Your Role Updated!", Toast.LENGTH_LONG).show();
                    MainHomeActivity.fragmentUtils.replaceFragment(new DashboardFragment(), Constant.DashboardFragment, R.id.main_frame);
                }else{
                    Toast.makeText(mContext, responseObject.getString("message"), Toast.LENGTH_SHORT).show();
                }

            } else if (tag_json_obj.equals("WingmanRole")) {

                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                    SharedPrefrence.save(mContext, "role", "Wingman");
                    Toast.makeText(mContext, "Your Role Updated!", Toast.LENGTH_LONG).show();
                    MainHomeActivity.fragmentUtils.replaceFragment(new DashboardFragment(), Constant.DashboardFragment, R.id.main_frame);
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
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
