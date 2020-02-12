package com.ourride.driver.volley;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class AppController extends Application {
 
    public static final String TAG = AppController.class
            .getSimpleName();
 
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static AppController mInstance;
    private static Context mContext;
 
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mContext=this;


    }

    /*@Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
        MultiDex.install(this);
    }*/
 
    public static synchronized AppController getInstance() {
        return mInstance;
    }
 
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
         return mRequestQueue;
    }
 
    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }
 
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }
 
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
 
    public void cancelPendingRequests(String tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public static Context getContext(){
        return mContext;
    }


    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;






}