package com.ourride.driver.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.core.app.ActivityCompat;

import com.ourride.driver.ui.MainHomeActivity;
import com.ourride.driver.ui.activity.LoginActivity;


public class SessionManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context ctx;

    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "sessionmanager";
    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_DIAPLAY_NAME = "diaplayname";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ID = "id";
    public static final String KEY_USERNAME = "username";

    public static final String KEY_PAYMENT_TYPE = "Case on delivery";
    public static final String KEY_ORDER_NAME = "name";
    public static final String KEY_ORDER_MOBILE = "mobile";
    public static final String KEY_ORDER_ADDRESS = "address";
    public static final String KEY_ORDER_CITY = "city";
    public static final String KEY_ORDER_STATE = "state";
    public static final String KEY_ORDER_COUNTRY = "country";
    public static final String KEY_ORDER_ZIPCODE = "zipcode";
    public static final String KEY_ORDER_PRICE = "price";

    public static final String KEY_ORDER_NAME1 = "name";
    public static final String KEY_ORDER_MOBILE1 = "mobile";
    public static final String KEY_ORDER_ADDRESS1 = "address";
    public static final String KEY_ORDER_CITY1 = "city";
    public static final String KEY_ORDER_STATE1 = "state";
    public static final String KEY_ORDER_COUNTRY1 = "country";
    public static final String KEY_ORDER_ZIPCODE1 = "zipcode";

    public SessionManager(Context context) {
        this.ctx = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String userid, String username, String displayname, String email) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_DIAPLAY_NAME, displayname);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ID, userid);
        editor.commit();
    }

    public void setData(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void checkLogin() {
        if (!this.isLoggedIn()) {
            Intent i = new Intent(ctx, MainHomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
        }
    }

    public void logoutUser(MainHomeActivity mainActivity) {
        editor.clear();
        editor.commit();
        ActivityCompat.finishAffinity(mainActivity);
        Intent i = new Intent(ctx.getApplicationContext(), LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    public String getData(String key) {
        return pref.getString(key, null);
    }
}
