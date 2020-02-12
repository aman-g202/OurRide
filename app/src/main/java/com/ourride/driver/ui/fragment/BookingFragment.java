package com.ourride.driver.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.ourride.driver.R;
import com.ourride.driver.apis.ApiConfig;
import com.ourride.driver.ui.activity.WingmenListActivity;
import com.ourride.driver.utils.BaseFragment;
import com.ourride.driver.utils.SharedPrefrence;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ourride.driver.ui.MainHomeActivity.tvEditProfile;

public class BookingFragment extends BaseFragment implements IApiResponse {
    private View rootView;
    private Context context;
    private ApiRequest apiRequest;
    private TextView bookingDate, bookingTime,pickFrom,dropTo,vehicleNumber,assignedUser,selectWingmanTv,currentBooking;
    private LinearLayout mainCOntent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_booking, container, false);
        tvEditProfile.setVisibility(View.GONE);

        context = getActivity();
        apiRequest = new ApiRequest(context, (IApiResponse)this);

        init();

        if (SharedPrefrence.getInt(context, "connected_passenger_booking_id") > 0) {
            int booking_id = SharedPrefrence.getInt(context, "connected_passenger_booking_id");
            apiRequest.postRequest(ApiConfig.baseUrlCurrentBooking + booking_id + ".json", "GetBookingById", Request.Method.GET);
        } else {
            mainCOntent.setVisibility(View.GONE);
            selectWingmanTv.setVisibility(View.GONE);
            currentBooking.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    private void init() {
        bookingDate = rootView.findViewById(R.id.bookingDate);
        bookingTime = rootView.findViewById(R.id.bookingTime);
        pickFrom = rootView.findViewById(R.id.pickFrom);
        dropTo = rootView.findViewById(R.id.dropTo);
        vehicleNumber = rootView.findViewById(R.id.vehicleNumber);
        assignedUser = rootView.findViewById(R.id.assignedUser);
        selectWingmanTv = rootView.findViewById(R.id.selectWingman);
        currentBooking = rootView.findViewById(R.id.currentBooking);
        mainCOntent = rootView.findViewById(R.id.mainContent);

        selectWingmanTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, WingmenListActivity.class));
            }
        });

    }


    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {
        if (tag_json_obj.equals("GetBookingById")){
            JSONObject responseObject = new JSONObject(response);
            if (responseObject.getString("status").equalsIgnoreCase("success")) {
                JSONObject booking = responseObject.getJSONObject("booking");
                    pickFrom.setText(booking.getString("from_address"));
                    dropTo.setText(booking.getString("to_address"));
                    JSONObject userObject = booking.getJSONObject("user");
                    assignedUser.setText(userObject.getString("name"));
                    vehicleNumber.setText(userObject.getString("licence_plate"));
                    String datetime[] = new String[2];
                    datetime = booking.getString("created").split("T");
                    bookingDate.setText(datetime[0]);
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
                    bookingTime.setText(originalTime);
            } else {
                Toast.makeText(context, responseObject.getString("message"), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(context, "Some error occured please try after sometime", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        try{
            Log.e("MyBookingError--", error.getMessage());
        }catch(Exception e) {
            Log.e("MyBookingException--", e.getMessage());
        }
    }
}
