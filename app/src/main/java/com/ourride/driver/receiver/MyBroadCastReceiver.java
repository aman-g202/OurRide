package com.ourride.driver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.ourride.driver.R;
import com.ourride.driver.apis.ApiConfig;
import com.ourride.driver.utils.NetworkManager;
import com.ourride.driver.utils.SharedPrefrence;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyBroadCastReceiver extends BroadcastReceiver{
    private static final String TAG = "MyBroadcastReceiver";
    private String type;
    @Override
    public void onReceive(Context context, Intent intent) {
        type = intent.getAction();
        if (("com.ourride.android.action.broadcast").equals(type)) {
            Log.e(TAG, "Sending Token");
            final PendingResult pendingResult = goAsync();
            Task asyncTask = new Task(pendingResult, intent, context);
            asyncTask.execute();
        }
        if (("android.net.conn.CONNECTIVITY_CHANGE").equals(type)) {
                NetworkManager networkManager = new NetworkManager(context);
                if (networkManager.isNetworkAvailable()) {
                    AppCompatActivity yourActivity = (AppCompatActivity) context;
                    View parentLayout = yourActivity.findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "You are online", Snackbar.LENGTH_LONG)
                            .setAction("CLOSE", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            })
                            .setActionTextColor(context.getResources().getColor(R.color.colorPrimary))
                            .show();
                } else {
                    AppCompatActivity yourActivity = (AppCompatActivity) context;
                    View parentLayout = yourActivity.findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "You are offline, kindly turn on the internet for proper working of app", Snackbar.LENGTH_LONG)
                            .setAction("CLOSE", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            })
                            .setActionTextColor(context.getResources().getColor(R.color.colorPrimary))
                            .show();
                }
        }

    }

    private static class Task extends AsyncTask<String, Integer, String> {

        private final PendingResult pendingResult;
        private final Intent intent;
        private final Context context;

        private Task(PendingResult pendingResult, Intent intent, Context context) {
            Log.e("My BroadCastReceiver", "sending");
            this.pendingResult = pendingResult;
            this.intent = intent;
            this.context = context;

        }

        @Override
        protected String doInBackground(String... strings) {
            String token = intent.getStringExtra("token");
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = ApiConfig.baseUrl+"edit/"+ SharedPrefrence.getInt(context,"user_id")+".json";
            Log.e("url", url);
            final HashMap<String,String> paramReq = new HashMap<>();
            paramReq.put("token", token);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONObject responseObject = jsonObject.getJSONObject("response");
                                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                                    Log.e(TAG, "token send successfully");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("network error", error.toString());
                }

            }) {
                @Override
                protected Map<String, String> getParams() {
                    return paramReq;
                }
            };
            queue.add(stringRequest);
            return "done";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // finish() so the BroadcastReceiver can be recycled.
            if (pendingResult != null) {
                pendingResult.finish();
            }
        }
    }
}
