package com.ourride.driver.ui.fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ourride.driver.R;
import com.ourride.driver.constant.Constant;
import com.ourride.driver.utils.BaseFragment;
import com.ourride.driver.utils.ConnectionDirector;
import com.ourride.driver.utils.pinview.Pinview;

import static com.ourride.driver.ui.activity.LoginActivity.loginfragmentManager;

public class ForgotPasswordFragment extends BaseFragment implements View.OnClickListener {
    private View rootview;
    private Button btn_fplogin;
    private TextView otpTime;
    private LinearLayout resendLayout;
    private Pinview pinview1;
    private String strMobile, strOtp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        activity = getActivity();
        mContext = getActivity();
        cd = new ConnectionDirector(mContext);
        init();
        return rootview;
    }

    private void init() {
        ((Button) rootview.findViewById(R.id.btn_fplogin)).setOnClickListener(this);
        btn_fplogin = rootview.findViewById(R.id.btn_fplogin);
        pinview1 = rootview.findViewById(R.id.pinview1);
        otpTime = (TextView) rootview.findViewById(R.id.otpTime);
        resendLayout = (LinearLayout) rootview.findViewById(R.id.resendLayout);
        btn_fplogin.setOnClickListener(this);

        otptime();
    }

    private void startFragment(String tag, Fragment fragment) {
        loginfragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.left_enter, R.anim.right_out)
                .replace(R.id.login_frame, fragment, tag).commit();
    }

    private void otptime() {
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                otpTime.setVisibility(View.VISIBLE);
                otpTime.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                otpTime.setVisibility(View.GONE);
                resendLayout.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_fplogin:
                // otpApi();
                startFragment(Constant.SignUpFragment, new SignUpFragment());
                break;
        }
    }

























   /* private void otpApi() {
        if (cd.isNetWorkAvailable()) {
            //strMobile = ((EditText) rootview.findViewById(R.id.et_login_email)).getText().toString();
            strOtp = pinview1.getValue();
            if (strOtp.isEmpty()) {
                ((EditText) rootview.findViewById(R.id.et_login_password)).setError("Please enter otp");
            } else {
                RetrofitService.getLoginData(new Dialog(mContext), retrofitApiClient.otpApi(strMobile, strOtp), new WebResponse() {
                    @Override
                    public void onResponseSuccess(Response<?> result) {
                        LoginModel loginModel = (LoginModel) result.body();

                        if (!loginModel.getError())
                        {
                            Alerts.show(mContext, loginModel.getMessage());

                            AppPreference.setBooleanPreference(mContext, Constant.LOGIN_API , true);
                            AppPreference.setStringPreference(mContext, Constant.User_Id , loginModel.getUser().getId());

                            Gson gson = new GsonBuilder().setLenient().create();
                            String data = gson.toJson(loginModel);
                            AppPreference.setStringPreference(mContext, Constant.User_Data, data);
                            User.setUser(loginModel);
                            Intent intent = new Intent(mContext , HomeActivity.class);
                            mContext.startActivity(intent);
                        }
                    }

                    @Override
                    public void onResponseFailed(String error) {
                        Alerts.show(mContext, error);
                    }
                });
            }
        }else {
            cd.show(mContext);
        }
    }*/
}
