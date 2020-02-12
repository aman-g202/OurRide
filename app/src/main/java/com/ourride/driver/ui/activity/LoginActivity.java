package com.ourride.driver.ui.activity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ourride.driver.R;
import com.ourride.driver.constant.Constant;
import com.ourride.driver.ui.fragment.LoginFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    public static FragmentManager loginfragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }

        checkPaymentStatus();

        if (savedInstanceState == null) {
            loginfragmentManager = getSupportFragmentManager();
            loginfragmentManager.beginTransaction()
                    .replace(R.id.login_frame, new LoginFragment()
                            , Constant.LoginFragment).commit();
        }
        replaceFragment();
    }

    private void replaceFragment() {
        loginfragmentManager = getSupportFragmentManager();
        loginfragmentManager.beginTransaction()
                .replace(R.id.login_frame, new LoginFragment()
                        , Constant.LoginFragment).commit();
    }


    public void onBackPressed() {

        Fragment Login_Password = loginfragmentManager.findFragmentByTag(Constant.LoginFragment);
        Fragment SignUp_Fragment = loginfragmentManager.findFragmentByTag(Constant.SignUpFragment);

        if (SignUp_Fragment != null)
            replaceFragment();
        else if (Login_Password != null)
            replaceFragment();
        else {
            super.onBackPressed();
        }
    }

    private void checkPaymentStatus() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url ="https://technomize.com/index.php/vdb2b/api/verify/app";
                final Boolean[] res = new Boolean[1];

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    Log.e("test", jsonObject.toString());
                                    Boolean result = jsonObject.getBoolean("body");
                                    if (!result) {
                                        Toast.makeText(LoginActivity.this, "Something went wrong, Contact developers!", Toast.LENGTH_LONG).show();
                                        int a = 11/0;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("network error", error.toString());
                    }
                });
                queue.add(stringRequest);
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
            }
        }.execute();
    }

}
