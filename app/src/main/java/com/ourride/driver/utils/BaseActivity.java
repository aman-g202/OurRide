package com.ourride.driver.utils;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.ourride.driver.R;
//import com.our.ride.retrofit_provider.RetrofitApiClient;


public class BaseActivity extends AppCompatActivity {
    public Context mContext;
    public ConnectionDirector cd;
//    public RetrofitApiClient retrofitApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        mContext = this;
 /*       cd = new ConnectionDirector(mContext);
        retrofitApiClient = RetrofitService.getRetrofit();*/
    }
}
