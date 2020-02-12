package com.ourride.driver.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ourride.driver.R;
import com.ourride.driver.apis.ApiConfig;
import com.ourride.driver.ui.MainHomeActivity;
import com.ourride.driver.utils.BaseFragment;
import com.ourride.driver.utils.SharedPrefrence;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.android.volley.VolleyLog.TAG;
import static com.ourride.driver.ui.MainHomeActivity.tvEditProfile;

public class ProfileFragment extends BaseFragment implements View.OnClickListener, IApiResponse {
    private View rootView;
    private CircleImageView userImage;
    private TextView nameProfile,emailProfile,phoneProfile,changePasswordProfile,update,getOtp;
    private EditText  currentPassword,password,rePassword,otp;
    private TextInputLayout hidePass, hideRepass, hideOtp;
    private ApiRequest apiRequest;
    private int user_id;
    private int otpInput;
    private String phoneInput, passwordInput, rePasswordInput;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mContext = getActivity();
        if (SharedPrefrence.getInt(mContext,"user_id") != 0){
             user_id = SharedPrefrence.getInt(mContext,"user_id");
             apiRequest = new ApiRequest(mContext, (IApiResponse)this);
             apiRequest.postRequest(ApiConfig.baseUrl+"view/"+user_id+".json", "ShowProfile", Request.Method.GET);
        }else{
            Toast.makeText(mContext, "Oops, some error occured contact admin", Toast.LENGTH_LONG).show();
        }
        init();
        return rootView;
    }

    private void init() {
        tvEditProfile.setVisibility(View.VISIBLE);
        nameProfile = rootView.findViewById(R.id.nameProfile);
        emailProfile = rootView.findViewById(R.id.emailProfile);
        phoneProfile = rootView.findViewById(R.id.phoneProfile);
        userImage = rootView.findViewById(R.id.userImageProfile);
        changePasswordProfile = rootView.findViewById(R.id.changePassword);

     //   tvEditProfile.setOnClickListener(this);
        changePasswordProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }

    }

    public void changePassword(){
        View view = getLayoutInflater().inflate(R.layout.change_password_layout, null);

        currentPassword = view.findViewById(R.id.currentpassUpdatePass);
        password = view.findViewById(R.id.passUpdatePass);
        rePassword = view.findViewById(R.id.repassUpdatePass);
        update = view.findViewById(R.id.changePasswordBtn);

        BottomSheetDialog dialog = new BottomSheetDialog(mContext);
        dialog.setContentView(view);
        dialog.show();

//        getOtp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                phone.addTextChangedListener(new ProfileFragment.MyTextWatcher(phone));
////                password.addTextChangedListener(new ProfileFragment.MyTextWatcher(password));
////                rePassword.addTextChangedListener(new ProfileFragment.MyTextWatcher(rePassword));
////                otp.addTextChangedListener(new ProfileFragment.MyTextWatcher(otp));
//
//                if (!validatePhone()){
//                    return;
//                }
//
//                HashMap<String,String> paramReq = new HashMap<>();
//
//                paramReq.put("phone", phone.getText().toString().trim());
//
//                apiRequest.postRequest(ApiConfig.baseUrl+"forgerPassword.json", "ForgetPassword", paramReq, Request.Method.POST);
//
//
//            }
//        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPassword.addTextChangedListener(new ProfileFragment.MyTextWatcher(currentPassword));
                password.addTextChangedListener(new ProfileFragment.MyTextWatcher(password));
                rePassword.addTextChangedListener(new ProfileFragment.MyTextWatcher(rePassword));
//                otp.addTextChangedListener(new ProfileFragment.MyTextWatcher(otp));

                finallyUpdatePassword();

            }
        });



    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            ((Activity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean validateCurrentPassword() {
        if (currentPassword.getText().toString().trim().isEmpty()) {
            currentPassword.setError(getString(R.string.emptypassword));
            requestFocus(currentPassword);
            return false;
        } else {
            currentPassword.setError(null);
        }

        return true;
    }


    private boolean validatePassword() {
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


    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch (view.getId()) {
                case R.id.repassUpdatePass:
                    validateRePassword();
                    break;
            }
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.currentpassUpdatePass:
                        validateCurrentPassword();
                    break;
                case R.id.passUpdatePass:
                        validatePassword();
                    break;
                case R.id.repassUpdatePass:
                        validateRePassword();
                    break;
            }
        }
    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {
//        Toast.makeText(mContext, "hey", Toast.LENGTH_SHORT).show();
        try {
            if (tag_json_obj.equals("ShowProfile")) {
//                Toast.makeText(mContext, "hey", Toast.LENGTH_SHORT).show();
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("user")) {
                    JSONObject responseObject = jsonObject.getJSONObject("user");
                    if (responseObject.has("name")){
                        nameProfile.setText(responseObject.getString("name"));
                    }
                    emailProfile.setText(responseObject.getString("email"));
                    phoneProfile.setText(responseObject.getString("phone"));

                    RequestOptions options = new RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.ic_user_profile)
                            .error(R.drawable.ic_user_profile);

                    Glide.with(mContext).load(responseObject.getString("profile_image_url")).apply(options).into(userImage);

                }else{
                    Toast.makeText(mContext, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                }
            }
//            else if (tag_json_obj.equals("ForgetPassword")){
//                JSONObject jsonObject = new JSONObject(response);
//                if (jsonObject.getString("status").equals("success")){
//                    Toast.makeText(mContext, "An otp has been sent to registered number", Toast.LENGTH_LONG).show();
//                    hidePass.setVisibility(View.VISIBLE);
//                    hideRepass.setVisibility(View.VISIBLE);
//                    hideOtp.setVisibility(View.VISIBLE);
//                    getOtp.setVisibility(View.GONE);
//                    update.setVisibility(View.VISIBLE);
//                    phone.setText(phone.getText().toString().trim());
//                }else if (jsonObject.getString("status").equals("error")){
//                    Toast.makeText(mContext, "Phone does'nt matched with database", Toast.LENGTH_LONG).show();
//                }
//                else {
//                    Toast.makeText(mContext, "Oops, some error occured", Toast.LENGTH_LONG).show();
//                }
//            }
            else if (tag_json_obj.equals("Updatepassword_WithOtp")){
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.getString("status").equals("success")){
                    Toast.makeText(mContext, "Updated", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(mContext, MainHomeActivity.class);
                    startActivity(intent);

                }else if (jsonObject.getString("status").equals("error")){
                    Toast.makeText(mContext, jsonObject.getString("message").toString(), Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(mContext, "Oops, some error occured", Toast.LENGTH_LONG).show();
                }
            }
        }catch (Exception e){
            Log.d("aman", e.getMessage());
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, error.getMessage());
    }

    public void finallyUpdatePassword(){

        if (!validatePassword() || !validateRePassword() || !validateCurrentPassword()) {
            return;
        }

        HashMap<String,String> paramReq = new HashMap<>();
        paramReq.put("current-password", currentPassword.getText().toString().trim());
        paramReq.put("new-password", password.getText().toString().trim());
        paramReq.put("re-new-password", rePassword.getText().toString().trim());
//        paramReq.put("otp", otp.getText().toString().trim());

        apiRequest.postRequest(ApiConfig.baseUrl+"changePassword/"+user_id+".json", "Updatepassword_WithOtp", paramReq, Request.Method.POST);

    }
}
