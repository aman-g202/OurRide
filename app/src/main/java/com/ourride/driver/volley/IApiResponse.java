package com.ourride.driver.volley;

import com.android.volley.VolleyError;

import org.json.JSONException;

public interface IApiResponse {

    //void onResultReceived(JSONObject response, String tag_json_obj);
    void onResultReceived(String response, String tag_json_obj) throws JSONException;
    void onErrorResponse(VolleyError error);
}
