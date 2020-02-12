package com.ourride.driver.utils;

import android.content.Context;
import android.widget.Toast;

public class Alerts {

    public static void show(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
