package com.ourride.driver.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.ourride.driver.R;
import com.ourride.driver.adapters.WingmenListAdapter;
import com.ourride.driver.apis.ApiConfig;
import com.ourride.driver.constant.MyDividerItemDecoration;
import com.ourride.driver.pojo.WingmenListResult;
import com.ourride.driver.ui.RecyclerTouchListeners.RecyclerWingmenListTouchListener;
import com.ourride.driver.utils.SharedPrefrence;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.android.volley.VolleyLog.TAG;

public class WingmenListActivity extends AppCompatActivity implements IApiResponse {

    private RecyclerView recyclerView;
    private Context mContext;
    private Toolbar toolbar;
    private ApiRequest apiRequest;
    private List<WingmenListResult> wingmenListResults;
    private WingmenListAdapter wingmenListAdapter;
    private int user_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wingmen_list);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }

        mContext = WingmenListActivity.this;

        toolbar= findViewById(R.id.toolbar);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_wingmen_list);
        apiRequest = new ApiRequest(mContext, (IApiResponse) this);
        user_id = SharedPrefrence.getInt(mContext, "user_id");

        toolbar.setTitle("Online Wingmen");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorToolBarText));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this,LinearLayoutManager.VERTICAL,16));

        recyclerView.addOnItemTouchListener(new RecyclerWingmenListTouchListener(getApplicationContext(), recyclerView, new WingmenListActivity.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                WingmenListResult wingman = wingmenListResults.get(position);
                Intent intent = new Intent(mContext, WingmanDetailActivity.class);
                intent.putExtra("user_id", wingman.getUser_id());
                intent.putExtra("lat", wingman.getLat());
                intent.putExtra("lng", wingman.getLng());
                intent.putExtra("distance", wingman.getDistance());
                intent.putExtra("duration", wingman.getDuration());
                mContext.startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        apiRequest.postRequest(ApiConfig.baseUrl+"wings/"+user_id+".json", "GetWingMen", Request.Method.GET);

    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {

        try {
            if (tag_json_obj.equals("GetWingMen")) {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                    JSONArray data = responseObject.getJSONArray("data");
                    wingmenListResults = new ArrayList<WingmenListResult>();
                    WingmenListResult wingmenListResult;
                    for (int i=0; i<data.length(); i++) {
                        JSONObject item = data.getJSONObject(i);
                        wingmenListResult = new WingmenListResult();
                        wingmenListResult.setUser_id(item.getInt("user_id"));
                        wingmenListResult.setName(item.getString("name"));
                        wingmenListResult.setDistance(item.getString("distance"));
                        wingmenListResult.setDuration(item.getString("duration"));
                        wingmenListResult.setLat(item.getString("lat"));
                        wingmenListResult.setLng(item.getString("lng"));

                        wingmenListResults.add(wingmenListResult);
                    }
                    wingmenListAdapter = new WingmenListAdapter(mContext, wingmenListResults);
                    recyclerView.setAdapter(wingmenListAdapter);
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

    public interface ClickListener {
        void onClick(View child, int childPosition);

        void onLongClick(View child, int childPosition);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
