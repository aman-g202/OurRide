package com.ourride.driver.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.ourride.driver.R;
import com.ourride.driver.adapters.HistoryAdapter;
import com.ourride.driver.apis.ApiConfig;
import com.ourride.driver.pojo.HistoryListResult;
import com.ourride.driver.utils.BaseFragment;
import com.ourride.driver.utils.SharedPrefrence;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ourride.driver.ui.MainHomeActivity.tvEditProfile;

public class HistoryFragment extends BaseFragment implements IApiResponse {
    private View rootView;
    private RecyclerView recyclerView;
    private Context mContext;
    private Toolbar toolbar;
    private ApiRequest apiRequest;
    private HistoryAdapter historyAdapter;
    private int user_id;
    private List<HistoryListResult> historyListResults;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_history_list, container, false);
        tvEditProfile.setVisibility(View.GONE);
        mContext = getActivity();
        init();
        apiRequest = new ApiRequest(mContext, (IApiResponse) this);
        user_id = SharedPrefrence.getInt(mContext, "user_id");

        if (SharedPrefrence.get(mContext,"role").equals("Wingman")) {
            apiRequest.postRequest(ApiConfig.baseUrlBookings+user_id+"/instant/wingman.json","driverAllBooking", Request.Method.GET);
        } else {
            apiRequest.postRequest(ApiConfig.baseUrlBookings+user_id+"/instant/driver.json","driverAllBooking", Request.Method.GET);
        }

        return rootView;
    }

    private void init() {
        recyclerView = rootView.findViewById(R.id.fragment_history_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.addItemDecoration(new MyDividerItemDecoration(mContext,LinearLayoutManager.VERTICAL,16));
    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {
        if (tag_json_obj.equals("driverAllBooking")){
            JSONObject responseObject = new JSONObject(response);
            JSONArray bookings = responseObject.getJSONArray("bookings");
            historyListResults = new ArrayList<HistoryListResult>();
            HistoryListResult historyBooking;
            if (bookings.length() != 0) {
               for (int i = 0; i<bookings.length(); i++) {
                   JSONObject booking = (JSONObject) bookings.get(i);
                   historyBooking = new HistoryListResult();
                   historyBooking.setPickFrom(booking.getString("from_address"));
                   historyBooking.setDropTo(booking.getString("to_address"));
                   JSONObject user = booking.getJSONObject("user");
                   historyBooking.setAssignedUser(user.getString("name"));
                   historyBooking.setVehicleNumber(user.getString("licence_plate"));
                   String datetime[] = new String[2];
                   datetime = booking.getString("created").split("T");
                   historyBooking.setBookingDate(datetime[0]);
                   List<String> time = new ArrayList<>();
                   time = Arrays.asList(datetime[1].split(":"));
                   int hour = Integer.parseInt(time.get(0));
                   int min = Integer.parseInt(time.get(1));
                   String originalTime = "";
                   if (hour >= 12) {
                       if (hour % 12 == 0) {
                           if (hour == 12) {
                               originalTime = hour + ":" + min + " PM";
                           } else {
                               originalTime = 12 + ":" + min + " AM";
                           }
                       }
                       else {
                           hour%=12;
                           originalTime = hour + ":" + min + " PM";
                       }
                   } else {
                       originalTime = hour + ":" + min + " AM";
                   }
                   historyBooking.setBookingTime(originalTime);
                   historyListResults.add(historyBooking);
               }
               historyAdapter = new HistoryAdapter(mContext,historyListResults);
               recyclerView.setAdapter(historyAdapter);
            } else {
                Toast.makeText(mContext, "No Bookings Found!", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(mContext, "Some error occured please try after sometime", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        try{
            Log.e("HistoryBookingError--", error.getMessage());
        }catch(Exception e) {
            Log.e("HistoryBookException--", e.getMessage());
        }
    }
}
