package com.ourride.driver.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.VolleyError;
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

import static com.android.volley.VolleyLog.TAG;
import static com.ourride.driver.ui.MainHomeActivity.tvEditProfile;

public class RideRequestFragment extends BaseFragment implements IApiResponse {

    private View rootView;
    private Button acceptButton, notacceptButton;
    private Context mContext;
    private ApiRequest apiRequest;
    private int user_id;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_riderequest, container, false);
        tvEditProfile.setVisibility(View.GONE);
        mContext = getActivity();
        activity = getActivity();

        apiRequest = new ApiRequest(mContext, (IApiResponse) this);
        user_id = SharedPrefrence.getInt(mContext, "user_id");

        init();
        return rootView;
    }

    private void init() {
        acceptButton = rootView.findViewById(R.id.acceptButton);
        notacceptButton = rootView.findViewById(R.id.notacceptButton);


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
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {

        try {

            if (tag_json_obj.equals("OnlineRequest")) {

                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                    Toast.makeText(mContext, "Your Ride Request is Started!", Toast.LENGTH_LONG).show();
                    MainHomeActivity.fragmentUtils.replaceFragment(new DashboardFragment(), Constant.DashboardFragment, R.id.main_frame);
                }else{
                    Toast.makeText(mContext, responseObject.getString("message"), Toast.LENGTH_SHORT).show();
                }

            } else if (tag_json_obj.equals("OfflineRequest")) {

                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                    Toast.makeText(mContext, "Your Ride Request is Stopped!", Toast.LENGTH_LONG).show();
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
}
