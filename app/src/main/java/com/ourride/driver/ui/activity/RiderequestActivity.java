package com.ourride.driver.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ourride.driver.R;
import com.ourride.driver.apis.ApiConfig;
import com.ourride.driver.utils.SharedPrefrence;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

public class RiderequestActivity extends AppCompatActivity implements IApiResponse {

    private Button acceptButton, notacceptButton;
    private Context mContext;
    private Toolbar toolbar;
    private ApiRequest apiRequest;
    private int user_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riderequest);
        mContext = RiderequestActivity.this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }

        acceptButton = findViewById(R.id.acceptButton);
        notacceptButton = findViewById(R.id.notacceptButton);
        toolbar= findViewById(R.id.toolbar);
        apiRequest = new ApiRequest(mContext, (IApiResponse) this);

        user_id = SharedPrefrence.getInt(mContext, "user_id");
        toolbar.setTitle("Ride Request");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorToolBarText));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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
                        Log.e(TAG, "FCM TOKEN RIDE REQUEST: "+token);

                        HashMap<String,String> paramReq = new HashMap<>();

                        paramReq.put("token", token);

                        apiRequest.postRequest(ApiConfig.baseUrl+"edit/"+SharedPrefrence.getInt(mContext,"user_id")+".json", "Update_Token", paramReq, Request.Method.POST);

                    }
                });


        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, String> paramReqs = new HashMap<>();
                paramReqs.put("is_online", "Yes");

                apiRequest.postRequest(ApiConfig.baseUrl+"edit/"+user_id+".json", "OnlineRequest", paramReqs, Request.Method.POST);
            }
        });

        notacceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, String> paramReqs = new HashMap<>();
                paramReqs.put("is_online", "No");

                apiRequest.postRequest(ApiConfig.baseUrl+"edit/"+user_id+".json", "OfflineRequest", paramReqs, Request.Method.POST);
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {

        try {

            if (tag_json_obj.equals("OnlineRequest")) {

                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                    startActivity(new Intent(RiderequestActivity.this, SelectionActivity.class));
                }else{
                    Toast.makeText(mContext, responseObject.getString("message"), Toast.LENGTH_SHORT).show();
                }

            } else if (tag_json_obj.equals("OfflineRequest")) {

                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                    startActivity(new Intent(RiderequestActivity.this, SelectionActivity.class));
                }else{
                    Toast.makeText(mContext, responseObject.getString("message"), Toast.LENGTH_SHORT).show();
                }

            } else if (tag_json_obj.equals("Update_Token")) {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                    return;
                }else{
                    return;
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
}
