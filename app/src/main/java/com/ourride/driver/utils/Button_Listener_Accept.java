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
import com.ourride.driver.ui.activity.LoginActivity;
import com.ourride.driver.ui.activity.WingmenActivity;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import static com.android.volley.VolleyLog.TAG;

public class Button_Listener_Accept extends BroadcastReceiver implements IApiResponse {

    private int notification_id;
    private int driver_id;
    private ApiRequest apiRequest;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(intent.getExtras().getInt("id"));
        apiRequest = new ApiRequest(context, (IApiResponse) this);
        notification_id = intent.getIntExtra("id", 0);
        driver_id = Integer.parseInt(intent.getStringExtra("driver_id"));
        //Toast.makeText(context, String.valueOf(driver_id)+"hfghfgfgvgmfgvghfhgvhgfhv", Toast.LENGTH_LONG).show();
        this.context = context;

        apiRequest.postRequest(ApiConfig.baseUrlNotification+"action/"+notification_id+"/accepted.json", "NotificationRequest", Request.Method.GET);

    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {
        if (tag_json_obj.equals("NotificationRequest")) {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject responseObject = jsonObject.getJSONObject("response");
            if (responseObject.getString("status").equals("success")) {
                SharedPrefrence.saveInt(context, "connected_driver_id", driver_id);
                Intent intent;
                if (SharedPrefrence.getInt(context,"user_id") > 0) {
                    intent = new Intent(context, WingmenActivity.class);
                } else {
                    intent  = new Intent(context, LoginActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
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
