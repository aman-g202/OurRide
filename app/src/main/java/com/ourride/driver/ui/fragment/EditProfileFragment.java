package com.ourride.driver.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ourride.driver.R;
import com.ourride.driver.utils.BaseFragment;

import static com.ourride.driver.ui.MainHomeActivity.tvEditProfile;

public class EditProfileFragment extends BaseFragment {
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        init();
        return rootView;
    }

    private void init() {
        tvEditProfile.setVisibility(View.VISIBLE);
    }
}
