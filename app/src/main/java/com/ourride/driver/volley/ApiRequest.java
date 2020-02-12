package com.ourride.driver.volley;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ourride.driver.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class ApiRequest {

    Context oContext;
    IApiResponse apiResponse;


    public ApiRequest(Context oContext, IApiResponse apiResponse) {
        this.oContext = oContext;
        this.apiResponse = apiResponse;
    }


    public void postRequest(final String url, final String tag_json_obj, int method) {
        if (CheckConnection.isNetworkAvailable(oContext)) {
            System.out.println("URL::::" + url);
            final ProgressDialog pDialog = new ProgressDialog(oContext);
            if (!tag_json_obj.equals("NotificationRequest")) {
                pDialog.setCancelable(false);
                pDialog.setMessage(oContext.getResources().getString(R.string.loading));
                pDialog.show();
            }
            final StringRequest strReq = new StringRequest(method, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d("Push Response POST", response.toString());
                    try {
                        apiResponse.onResultReceived(response, tag_json_obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (!tag_json_obj.equals("NotificationRequest")) {
                        if (pDialog != null && pDialog.isShowing())
                            pDialog.dismiss();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // imgSendNotification.setVisibility(ImageView.);
                    apiResponse.onErrorResponse(error);
                    error.printStackTrace();
                    VolleyLog.e("Push Response Error:", "Error: " + error.getMessage());
                    if (pDialog != null && pDialog.isShowing())
                        pDialog.dismiss();
                }
            }) {
                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                        String json = null;

                        try {
                            String responseBody = new String(volleyError.networkResponse.data);
                            JSONObject jsonObject = new JSONObject(responseBody);

                            Log.d("parse jsonObject", jsonObject.toString());
                            json = trimMessage(jsonObject, "message");
                            if (json != null)
                                displayMessage(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return volleyError;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    params.put("BCOV-Policy", "BCpkADawqM1SpooUKBOplYm5a7ii7WhWNOiEA7R9TWhjHJ3FUY05lOlP0yYica33dswQiGiY0HBPCFfs4Ac0_2cT8l3XnfF66I4q8hb5nPC0SZc3ve-lVjNqoEwzhpv-qtVYIf8OmBaxriAs");
                    return params;
                }
            };

            strReq.setRetryPolicy(new DefaultRetryPolicy(240 * 1000, 1, 1.0f));
            AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
        } else {
            AlertManager.showToast(oContext, oContext.getString(R.string.InternetNotAvailabel));
        }
    }


    public String trimMessage(JSONObject obj, String key) {
        String trimmedString = null;

        try {
            // JSONObject obj = new JSONObject(json);
            JSONObject errorObj = obj.getJSONObject("error");
            trimmedString = errorObj.getString(key);
            int statusCode = errorObj.getInt("code");
            if (trimmedString.equalsIgnoreCase("Token has expired") || trimmedString.equalsIgnoreCase("Unauthorized.")) {
                //ShardPreferences.logOut(oContext);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return trimmedString;
    }

    public void displayMessage(final String toastString) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                AlertManager.showToast(oContext, toastString);
            }
        });
    }


    public void postRequest(final String url, final String tag_json_obj, final Map<String, String> paramsReq, int method) {
        if (CheckConnection.isNetworkAvailable(oContext)) {
            System.out.println("URL::::" + url);
            System.out.println("paramsReq::::" + paramsReq);
            Random r = new Random();
            final ProgressDialog pDialog = new ProgressDialog(oContext);
            if (!tag_json_obj.equals("Update_Token")) {
                pDialog.setCancelable(false);
                pDialog.setMessage(oContext.getResources().getString(R.string.loading));
                pDialog.show();
            }
            final StringRequest strReq = new StringRequest(method, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d("Push Response POST", response.toString());
                    try {
                        apiResponse.onResultReceived(response, tag_json_obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (pDialog != null && pDialog.isShowing())
                            pDialog.dismiss();
                    } catch (Exception e) {

                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // imgSendNotification.setVisibility(ImageView.);
                    apiResponse.onErrorResponse(error);
                    VolleyLog.e("Push Response Error", "Error: " + error.getMessage());
                    if (pDialog != null && pDialog.isShowing())
                        pDialog.hide();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {

                    return paramsReq;
                }


                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                        String json = null;

                        try {
                            String responseBody = new String(volleyError.networkResponse.data);
                            JSONObject jsonObject = new JSONObject(responseBody);

                            Log.d("parse jsonObject", jsonObject.toString());
                            json = trimMessage(jsonObject, "message");
                            if (json != null)
                                displayMessage(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return volleyError;
                }
            };
            strReq.setRetryPolicy(new DefaultRetryPolicy(240 * 1000, 1, 1.0f));
            AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
        } else {
            VolleyError error = new VolleyError();
            apiResponse.onErrorResponse(error);
            AlertManager.showToast(oContext, oContext.getString(R.string.InternetNotAvailabel));
        }
    }


    public void postRequestPayment(final String url, final String tag_json_obj, final Map<String, String> paramsReq, int method) {
        if (CheckConnection.isNetworkAvailable(oContext)) {
            System.out.println("URL::::" + url);
            System.out.println("paramsReq::::" + paramsReq);
            Random r = new Random();
            final ProgressDialog pDialog = new ProgressDialog(oContext);
            pDialog.setCancelable(false);
            pDialog.setMessage("Payment Verified..");
            pDialog.show();
            final StringRequest strReq = new StringRequest(method, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d("Push Response POST", response.toString());
                    try {
                        apiResponse.onResultReceived(response, tag_json_obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (pDialog != null && pDialog.isShowing())
                            pDialog.hide();
                    } catch (Exception e) {

                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // imgSendNotification.setVisibility(ImageView.);
                    apiResponse.onErrorResponse(error);
                    VolleyLog.e("Push Response Error", "Error: " + error.getMessage());
                    if (pDialog != null && pDialog.isShowing())
                        pDialog.hide();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {

                    return paramsReq;
                }


                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                        String json = null;

                        try {
                            String responseBody = new String(volleyError.networkResponse.data);
                            JSONObject jsonObject = new JSONObject(responseBody);

                            Log.d("parse jsonObject", jsonObject.toString());
                            json = trimMessage(jsonObject, "message");
                            if (json != null)
                                displayMessage(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return volleyError;
                }
            };
            strReq.setRetryPolicy(new DefaultRetryPolicy(240 * 1000, 1, 1.0f));
            AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
        } else {
            VolleyError error = new VolleyError();
            apiResponse.onErrorResponse(error);
            AlertManager.showToast(oContext, oContext.getString(R.string.InternetNotAvailabel));
        }
    }


    public void postRequest(final int page, final String url, final String tag_json_obj, final Map<String, String> paramsReq, int method) {
        if (CheckConnection.isNetworkAvailable(oContext)) {
            System.out.println("URL::::" + url);
            System.out.println("paramsReq::::" + paramsReq);


            final ProgressDialog pDialog = new ProgressDialog(oContext);
            pDialog.setCancelable(false);
            pDialog.setMessage(oContext.getResources().getString(R.string.loading));

            if (page == 0) {
                if (pDialog != null) {
                    //pDialog.setMessage(array[i1]);
                    pDialog.show();
                }
            }
            final StringRequest strReq = new StringRequest(method, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d("Push Response POST", response.toString());
                    try {
                        apiResponse.onResultReceived(response, tag_json_obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    if (pDialog != null && pDialog.isShowing())
                        pDialog.dismiss();
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // imgSendNotification.setVisibility(ImageView.);
                    apiResponse.onErrorResponse(error);
                    VolleyLog.e("Push Response Error", "Error: " + error.getMessage());
                    if (pDialog != null && pDialog.isShowing())
                        pDialog.hide();
                    displayMessage(error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    return paramsReq;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }

                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                        String json = null;
                        try {
                            String responseBody = new String(volleyError.networkResponse.data);
                            JSONObject jsonObject = new JSONObject(responseBody);
                            Log.d("parse jsonObject", jsonObject.toString());
                            json = trimMessage(jsonObject, "message");
                            if (json != null)
                                displayMessage(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return volleyError;
                }
            };
            strReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 100, 1, 1.0f));
            AppController.getInstance().addToRequestQueue(strReq);
        } else {
            VolleyError error = new VolleyError();
            apiResponse.onErrorResponse(error);
            AlertManager.showToast(oContext, oContext.getString(R.string.InternetNotAvailabel));
        }

    }


    public void postRequestBackground(final String url, final String tag_json_obj, final Map<String, String> paramsReq, int method) {
        if (CheckConnection.isNetworkAvailable(oContext)) {
            System.out.println("Url:" + url);
            System.out.println("paramsReq:: "+paramsReq);

            final StringRequest strReq = new StringRequest(method, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d("Push Response POST", response.toString());
                    try {
                        apiResponse.onResultReceived(response, tag_json_obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // imgSendNotification.setVisibility(ImageView.);
                    apiResponse.onErrorResponse(error);
                    VolleyLog.e("Push Response Error", "Error: " + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    return paramsReq;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }

                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    return volleyError;
                }
            };
            strReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 100, 1, 1.0f));
            AppController.getInstance().addToRequestQueue(strReq);
        }
    }

    public void postRequestLocation(final String url, final String tag_json_obj, final Map<String, String> paramsReq,
                                    int method, String mSearchNew, String mSearchLast) {
        if (CheckConnection.isNetworkAvailable(oContext)) {
            System.out.println("Url:" + url);
            System.out.println("paramsReq:: "+paramsReq);

            if(AppController.getInstance().getRequestQueue() != null){
                AppController.getInstance().getRequestQueue().cancelAll(mSearchLast);
            }

            final StringRequest strReq = new StringRequest(method, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d("Push Response POST", response.toString());
                    try {
                        apiResponse.onResultReceived(response, tag_json_obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // imgSendNotification.setVisibility(ImageView.);
                    apiResponse.onErrorResponse(error);
                    VolleyLog.e("Push Response Error", "Error: " + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    return paramsReq;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }

                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    return volleyError;
                }
            };
           // strReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 100, 1, 1.0f));
            strReq.setTag(mSearchNew);
            AppController.getInstance().addToRequestQueue(strReq);
        }
    }


    public void postRequestWithImage(final String url, final String tag_json_obj, final Map<String, String> paramsReq,
                                     final Map<String, MultipartRequest.DataPart> params, int method) {
        final ProgressDialog pDialog = new ProgressDialog(oContext);

        System.out.println("URL :: "+url);
        System.out.println("paramsReq:: "+paramsReq);

        pDialog.setMessage(oContext.getString(R.string.loading));
        pDialog.show();
        MultipartRequest mr = new MultipartRequest(method, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);

                System.out.println("response:: "+resultResponse);

                try {
                    apiResponse.onResultReceived(resultResponse, tag_json_obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                pDialog.dismiss();

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                apiResponse.onErrorResponse(error);
                pDialog.hide();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                return paramsReq;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                /*Map<String, MultipartRequest.DataPart> params = new HashMap<>();
                //  Bitmap bitmap = BitmapFactory.decodeFile(ivImage);
                if (bm != null)
                    params.put("outlet_image", new DataPart("OutletImage.jpg", getFileDataFromDrawable(bm), "image/jpeg"));
*/
                return params;
            }
        };

        Volley.newRequestQueue(oContext).add(mr);
        mr.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 100, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


}
