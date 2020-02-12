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
import com.ourride.driver.ui.MainHomeActivity;
import com.ourride.driver.ui.activity.LoginActivity;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import static com.android.volley.VolleyLog.TAG;

public class Button_Listener_Accept_User extends BroadcastReceiver implements IApiResponse {

    private ApiRequest apiRequest;
    private Context context;
    private int bookingId,passengerId,bookingIdPk;
    private String user_to_lat,user_to_lng,user_from_lat,user_from_lng;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(intent.getExtras().getInt("id"));
        apiRequest = new ApiRequest(context, (IApiResponse) this);
        this.context = context;
        bookingId = Integer.parseInt(intent.getStringExtra("booking_id"));
        bookingIdPk = Integer.parseInt(intent.getStringExtra("booking_pk_id"));
        passengerId = Integer.parseInt(intent.getStringExtra("passenger_id"));
        user_to_lat = intent.getStringExtra("user_to_lat");
        user_to_lng = intent.getStringExtra("user_to_lng");
        user_from_lat = intent.getStringExtra("user_from_lat");
        user_from_lng = intent.getStringExtra("user_from_lng");
        Log.e("Bookingid", String.valueOf(bookingId));

        apiRequest.postRequest(ApiConfig.baseUrlNotificationUser+bookingId+"/accepted.json", "NotificationRequest", Request.Method.GET);
    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {

        if (tag_json_obj.equals("NotificationRequest")) {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject statusObject = jsonObject.getJSONObject("status");
            if (statusObject.getString("status").equals("success")) {
                SharedPrefrence.save(context, "connected_passenger_user_to_lat", user_to_lat);
                SharedPrefrence.save(context, "connected_passenger_user_to_lng", user_to_lng);
                SharedPrefrence.save(context, "connected_passenger_user_from_lat", user_from_lat);
                SharedPrefrence.save(context, "connected_passenger_user_from_lng", user_from_lng);
                SharedPrefrence.saveInt(context, "connected_passenger_id", passengerId);
                SharedPrefrence.saveInt(context, "connected_passenger_booking_id", bookingIdPk);
                Log.e("Ride request--", "accepted successfully");
                Intent intent;
                if (SharedPrefrence.getInt(context,"user_id") > 0) {
                    intent = new Intent(context, MainHomeActivity.class);
                } else {
                    intent = new Intent(context, LoginActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, statusObject.getString("message"), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Oops some error occured!", Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    public void onErrorResponse(VolleyError error) {
        try {
            Log.d(TAG, error.getMessage());
        }
        catch (Exception e){
            Log.d(TAG, e.getMessage());
        }

    }
}
