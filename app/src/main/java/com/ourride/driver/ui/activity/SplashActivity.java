package com.ourride.driver.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import com.ourride.driver.R;
import com.ourride.driver.ui.MainHomeActivity;
import com.ourride.driver.utils.BaseActivity;
import com.ourride.driver.utils.SharedPrefrence;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SharedPrefrence.getInt(mContext,"user_id") > 0) {
                    Intent intent = new Intent(SplashActivity.this, MainHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 4000);
    }
}
