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

import static com.android.volley.VolleyLog.TAG;

public class Button_Listener_Reject extends BroadcastReceiver implements IApiResponse {

    private int notification_id;
    private ApiRequest apiRequest;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(intent.getExtras().getInt("id"));
        apiRequest = new ApiRequest(context, (IApiResponse) this);
        notification_id = intent.getIntExtra("id", 0);
        this.context = context;

        apiRequest.postRequest(ApiConfig.baseUrlNotification+"action/"+notification_id+"/rejected.json", "NotificationRequest", Request.Method.GET);

    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {
        if (tag_json_obj.equals("NotificationRequest")) {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject responseObject = jsonObject.getJSONObject("response");
            if (responseObject.getString("status").equals("success")) {
                Toast.makeText(context, "Driver request to ride with them as a wingman is rejected", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, responseObject.getString("message"), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Oops some error occured!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, error.getMessage());
    }
}
