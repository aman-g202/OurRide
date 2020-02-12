package com.ourride.driver.utils;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.ourride.driver.apis.ApiConfig;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class Button_Listener_Reject_User extends BroadcastReceiver implements IApiResponse {

    private int notification_id;
    private ApiRequest apiRequest;
    private Context context;
    private int bookingId;
    @Override

    public void onReceive(Context context, Intent intent) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(intent.getExtras().getInt("id"));
        apiRequest = new ApiRequest(context, (IApiResponse) this);
        notification_id = intent.getIntExtra("id", 0);
        bookingId = Integer.parseInt(intent.getStringExtra("booking_id"));
        Log.e("Bookingid", String.valueOf(bookingId));
        this.context = context;

        apiRequest.postRequest(ApiConfig.baseUrlNotificationUser+bookingId+"/rejected.json","NotificationRequest", Request.Method.GET);
    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {
        if (tag_json_obj.equals("NotificationRequest")) {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("success")) {
                Log.e("ride Rejected --", "rejected successfully");
                Toast.makeText(context, "User request to BookRide with you is rejected", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Oops, some error occured, try after some time", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Oops some error occured, try after some time", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d("Notification Rejected", error.getMessage());
    }
}
