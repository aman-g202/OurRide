package com.ourride.driver.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.ourride.driver.R;
import com.ourride.driver.apis.ApiConfig;
import com.ourride.driver.constant.Constant;
import com.ourride.driver.ui.activity.RiderequestActivity;
import com.ourride.driver.utils.BaseFragment;
import com.ourride.driver.utils.EmailChecker;
import com.ourride.driver.utils.SharedPrefrence;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;
import static com.ourride.driver.ui.activity.LoginActivity.loginfragmentManager;


public class LoginFragment extends BaseFragment implements View.OnClickListener, IApiResponse {
    private View rootview;
    private TextView tvSignUp,tvSignIn,forgetPasswordLogin,update,getOtp;
    private EditText et_login_email, et_login_password, phone,password,rePassword,otp;
    private TextInputLayout hidePass, hideRepass, hideOtp;
    private BottomSheetDialog dialog;
    private ApiRequest apiRequest;
    private int user_id;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_login_layout, container, false);
        activity = getActivity();
        mContext = getActivity();
        init();
        apiRequest = new ApiRequest(mContext, (IApiResponse) this);
        user_id = SharedPrefrence.getInt(mContext, "user_id");
        return rootview;
    }

    private void init() {
        tvSignIn = rootview.findViewById(R.id.tvSignIn);
        tvSignUp = rootview.findViewById(R.id.tvSignUp);
        et_login_email = rootview.findViewById(R.id.et_login_email);
        et_login_password = rootview.findViewById(R.id.et_login_password);
        forgetPasswordLogin = rootview.findViewById(R.id.forgetPasswordLogin);
        tvSignIn.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);

        et_login_email.addTextChangedListener(new MyTextWatcher(et_login_email));
        et_login_password.addTextChangedListener(new MyTextWatcher(et_login_password));

        forgetPasswordLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    private void startFragment(String tag, Fragment fragment) {
        loginfragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.right_enter, R.anim.left_out)
                .replace(R.id.login_frame, fragment, tag).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvSignIn:
                logInUser();
                break;
            case R.id.tvSignUp:
                startFragment(Constant.SignUpFragment, new SignUpFragment());
                break;
        }
    }

    public void logInUser(){
        if (!validateEmail() || !validatePassword()) {
            return;
        }

        Map<String, String> paramsReq = new HashMap<>();
        paramsReq.put("email", et_login_email.getText().toString().trim());
        paramsReq.put("password", et_login_password.getText().toString().trim());

        apiRequest.postRequest(ApiConfig.baseUrl+ApiConfig.loginDriver_passenger, "LoginUser", paramsReq, Request.Method.POST);


    }

    public void changePassword(){
        View view = getLayoutInflater().inflate(R.layout.forget_password_layout, null);

        phone = view.findViewById(R.id.phoneUpdatePass);
        password = view.findViewById(R.id.passUpdatePass);
        rePassword = view.findViewById(R.id.repassUpdatePass);
        otp = view.findViewById(R.id.otpUpdatePass);
        update = view.findViewById(R.id.changePasswordBtn);
        getOtp = view.findViewById(R.id.otpBtn);
        hidePass = view.findViewById(R.id.hideUpdatePass);
        hideRepass = view.findViewById(R.id.hideUpdateRepass);
        hideOtp = view.findViewById(R.id.hideOtp);

        dialog = new BottomSheetDialog(mContext);
        dialog.setContentView(view);
        dialog.show();

        phone.addTextChangedListener(new LoginFragment.MyTextWatcher(phone));
        password.addTextChangedListener(new LoginFragment.MyTextWatcher(password));
        rePassword.addTextChangedListener(new LoginFragment.MyTextWatcher(rePassword));
        otp.addTextChangedListener(new LoginFragment.MyTextWatcher(otp));

        getOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!validatePhone()){
                    return;
                }

                HashMap<String,String> paramReq = new HashMap<>();

                paramReq.put("phone", phone.getText().toString().trim());

                apiRequest.postRequest(ApiConfig.baseUrl+"forgerPassword.json", "ForgetPassword", paramReq, Request.Method.POST);


            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finallyUpdatePassword();

            }
        });

    }

    public void finallyUpdatePassword(){
        if (!validatePhone()){return;}

        if (hidePass.getVisibility() == View.VISIBLE && hideRepass.getVisibility() == View.VISIBLE && hideOtp.getVisibility() == View.VISIBLE){
            if (!validatePassword2() || !validateRePassword() || !validateOtp()){
                return;
            }
        }else{
            Toast.makeText(mContext, "Oops, some error occured", Toast.LENGTH_LONG).show();
            return;
        }

        HashMap<String,String> paramReq = new HashMap<>();
        paramReq.put("phone_or_email", phone.getText().toString().trim());
        paramReq.put("password", password.getText().toString().trim());
        paramReq.put("re-password", rePassword.getText().toString().trim());
        paramReq.put("otp", otp.getText().toString().trim());

        apiRequest.postRequest(ApiConfig.baseUrl+"updatePassword.json", "Updatepassword_WithOtp", paramReq, Request.Method.POST);

    }

    private boolean validatePassword() {
        if (et_login_password.getText().toString().trim().isEmpty()) {
            et_login_password.setError(getString(R.string.emptypassword));
            requestFocus(et_login_password);
            return false;
        } else {
            et_login_password.setError(null);
        }

        return true;
    }

    private boolean validatePassword2() {
        if (password.getText().toString().trim().isEmpty()) {
            password.setError(getString(R.string.emptypassword));
            requestFocus(password);
            return false;
        } else {
            password.setError(null);
        }

        return true;
    }

    private boolean validateRePassword(){
        if (rePassword.getText().toString().trim().isEmpty()) {
            rePassword.setError("Enter this field");
            requestFocus(rePassword);
            return false;
        } else if (!rePassword.getText().toString().trim().equals(password.getText().toString().trim())){
            rePassword.setError("Password does'nt matched");
            requestFocus(rePassword);
            return false;
        } else {
            rePassword.setError(null);
        }

        return true;

    }

    private boolean validateOtp() {
        if (otp.getText().toString().trim().isEmpty()) {
            otp.setError(getString(R.string.emptyotp));
            requestFocus(otp);
            return false;
        } else if (otp.getText().toString().trim().length() != 6){
            otp.setError(getString(R.string.errorotp));
            requestFocus(otp);
            return false;
        } else {
            otp.setError(null);
        }

        return true;
    }

    private boolean validateEmail() {
        if (et_login_email.getText().toString().trim().isEmpty()) {
            et_login_email.setError(getString(R.string.emptyemail));
            requestFocus(et_login_email);
            return false;
        } else if (!EmailChecker.isValid(et_login_email.getText().toString().trim())) {
            et_login_email.setError(getString(R.string.erroremail));
            requestFocus(et_login_email);
            return false;
        }else {
            et_login_email.setError(null);
        }

        return true;
    }

    private boolean validatePhone() {
        if (phone.getText().toString().trim().isEmpty()) {
            phone.setError(getString(R.string.emptyphone));
            requestFocus(phone);
            return false;
        } else if (phone.getText().toString().trim().length() != 10){
            phone.setError(getString(R.string.errorphone));
            requestFocus(phone);
            return false;
        } else {
            password.setError(null);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            ((Activity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {

        try {
            if (tag_json_obj.equals("LoginUser")) {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                    if (SharedPrefrence.getInt(mContext,"user_id") >= 0){
                        SharedPrefrence.saveInt(mContext, "user_id", responseObject.getInt("user_id"));
                    }
                    startActivity(new Intent(mContext, RiderequestActivity.class));
                }else{
                    Toast.makeText(mContext, responseObject.getString("message"), Toast.LENGTH_SHORT).show();
                }
            }
            else if (tag_json_obj.equals("ForgetPassword")){
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.getString("status").equals("success")){
                    Toast.makeText(mContext, "An otp has been sent to registered number", Toast.LENGTH_LONG).show();
                    hidePass.setVisibility(View.VISIBLE);
                    hideRepass.setVisibility(View.VISIBLE);
                    hideOtp.setVisibility(View.VISIBLE);
                    getOtp.setVisibility(View.GONE);
                    update.setVisibility(View.VISIBLE);
                    phone.setText(phone.getText().toString().trim());
                }else if (jsonObject.getString("status").equals("error")){
                    Toast.makeText(mContext, "Phone does'nt matched with database", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(mContext, "Oops, some error occured", Toast.LENGTH_LONG).show();
                }
            }
            else if (tag_json_obj.equals("Updatepassword_WithOtp")){
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.getString("status").equals("success")){
                    Toast.makeText(mContext, "Updated", Toast.LENGTH_LONG).show();
                    dialog.dismiss();

                }else if (jsonObject.getString("status").equals("error")){
                    Toast.makeText(mContext, jsonObject.getString("message").toString(), Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(mContext, "Oops, some error occured", Toast.LENGTH_LONG).show();
                }
            }
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, error.getMessage());

    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch (view.getId()) {
                case R.id.et_login_email:
                    validateEmail();
                    break;
                case R.id.repassUpdatePass:
                    validateRePassword();
                    break;
                case R.id.otpUpdatePass:
                    validateOtp();
                    break;
                case R.id.phoneUpdatePass:
                    validatePhone();
                    break;
            }
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.et_login_email:
                    validateEmail();
                    break;
                case R.id.et_login_password:
                    validatePassword();
                    break;
                case R.id.phoneUpdatePass:
                    validatePhone();
                    break;
                case R.id.passUpdatePass:
                    validatePassword2();
                    break;
                case R.id.repassUpdatePass:
                    validateRePassword();
                    break;
            }
        }
    }
}
