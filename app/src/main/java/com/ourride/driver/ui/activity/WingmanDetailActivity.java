package com.ourride.driver.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ourride.driver.R;
import com.ourride.driver.apis.ApiConfig;
import com.ourride.driver.ui.MainHomeActivity;
import com.ourride.driver.utils.SharedPrefrence;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.android.volley.VolleyLog.TAG;

public class WingmanDetailActivity extends AppCompatActivity implements IApiResponse {

    private Context mContext;
    private ApiRequest apiRequest;
    private CircleImageView user_image;
    private ImageView backArrow;
    private TextView user_name, user_address,phone1,call1,phone2,call2,user_distance,user_duration;
    private Button select_wingman;
    private int user_id;
    private int driver_id;
    private String lat, lng, distance, duration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wingman_detail);
        mContext = WingmanDetailActivity.this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }

        final Intent intent = getIntent();
        user_id = intent.getIntExtra("user_id", 0);
        lat = intent.getStringExtra("lat");
        lng = intent.getStringExtra("lng");
        distance = intent.getStringExtra("distance");
        duration = intent.getStringExtra("duration");

        driver_id = SharedPrefrence.getInt(mContext, "user_id");

        user_name = findViewById(R.id.user_name);
        user_address = findViewById(R.id.user_address);
        phone1 = findViewById(R.id.phone1);
        call1 = findViewById(R.id.call1);
        phone2 = findViewById(R.id.phone2);
        call2 = findViewById(R.id.call2);
        user_distance = findViewById(R.id.user_distance);
        user_duration = findViewById(R.id.user_duration);
        select_wingman = findViewById(R.id.select_wingman);
        user_image = findViewById(R.id.user_photo);
        backArrow = findViewById(R.id.back_arrow);

        call1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phone1.getText().equals("")) {
                    String uri = "tel:" + phone1.getText().toString().trim() ;
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
            }
        });

        call2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phone2.getText().equals("")) {
                    String uri = "tel:" + phone2.getText().toString().trim() ;
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
            }
        });

        select_wingman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SharedPrefrence.getInt(mContext, "selected_wingman_id") > 0) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                    builder1.setIcon(android.R.drawable.ic_dialog_alert);
                    builder1.setTitle(R.string.requestwingman);
                    builder1.setMessage(R.string.messagerequestwingman);
                    builder1.create();
                    builder1.setPositiveButton("Request Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Map<String, String> paramsReq = new HashMap<>();
                            paramsReq.put("to_user_id", String.valueOf(user_id));
                            paramsReq.put("status", "init");
                            paramsReq.put("request_type", "wingman");
                            apiRequest.postRequest(ApiConfig.baseUrlNotification + "send/" + driver_id + ".json", "SendWingmanRequest", paramsReq, Request.Method.POST);
                        }
                    });
                    builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder1.show();
                }
                else if (SharedPrefrence.getInt(mContext, "connected_passenger_id") == 0) {
                    Toast.makeText(mContext, "You do not have any passenger, kindly wait for passenger ride request", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, String> paramsReq = new HashMap<>();
                    paramsReq.put("to_user_id", String.valueOf(user_id));
                    paramsReq.put("status", "init");
                    paramsReq.put("request_type", "wingman");
                    apiRequest.postRequest(ApiConfig.baseUrlNotification + "send/" + driver_id + ".json", "SendWingmanRequest", paramsReq, Request.Method.POST);
                }
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        apiRequest = new ApiRequest(mContext, (IApiResponse) this);

        apiRequest.postRequest(ApiConfig.baseUrl+"view/"+user_id+".json", "GetWingmandetail", Request.Method.GET);

    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {

        try {
            if (tag_json_obj.equals("GetWingmandetail")) {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("user")) {
                    JSONObject data = jsonObject.getJSONObject("user");
                    user_name.setText(data.getString("name"));
                    user_address.setText(data.getString("work"));
                    phone1.setText(data.getString("phone"));
                    phone2.setText(data.getString("phone_work"));
                    user_distance.setText(distance);
                    user_duration.setText(duration);

                    RequestOptions options = new RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.ic_icon)
                            .error(R.drawable.ic_icon);

                    Glide.with(WingmanDetailActivity.this).load(data.getString("profile_image_url")).apply(options).into(user_image);
                }else{
                    Toast.makeText(mContext, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                }
            }
            else if (tag_json_obj.equals("SendWingmanRequest")) {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equals("success")) {
                    SharedPrefrence.save(mContext, "selected_wingman_lat", lat);
                    SharedPrefrence.save(mContext, "selected_wingman_lng", lng);
                    SharedPrefrence.saveInt(mContext,"selected_wingman_id", user_id);
                    Toast.makeText(mContext, responseObject.getString("message")+", wait for wingman response!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(mContext, MainHomeActivity.class));
                    finish();
                }
                else {
                    Toast.makeText(mContext, "user_id does not exist", Toast.LENGTH_SHORT).show();
                }
            }
            else {
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
