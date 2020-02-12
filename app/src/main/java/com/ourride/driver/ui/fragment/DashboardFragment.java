package com.ourride.driver.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ourride.driver.R;
import com.ourride.driver.constant.Constant;
import com.ourride.driver.ui.MainHomeActivity;
import com.ourride.driver.ui.activity.DriverMapActivity;
import com.ourride.driver.ui.activity.WingmenListActivity;
import com.ourride.driver.ui.activity.WingmenActivity;
import com.ourride.driver.utils.BaseFragment;
import com.ourride.driver.utils.SharedPrefrence;

import static com.ourride.driver.ui.MainHomeActivity.tvEditProfile;

public class DashboardFragment extends BaseFragment implements View.OnClickListener{
    private View rootView;
    private ImageView mapImageView, profileImageView, bookingsImageView, historyImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mContext = getActivity();
        activity = getActivity();
        tvEditProfile.setVisibility(View.GONE);
        init();
        return rootView;
    }

    private void init () {
        mapImageView = rootView.findViewById(R.id.mapView);
        profileImageView = rootView.findViewById(R.id.profileView);
        bookingsImageView = rootView.findViewById(R.id.bookingsView);
        historyImageView = rootView.findViewById(R.id.historyView);

        mapImageView.setOnClickListener(this);
        profileImageView.setOnClickListener(this);
        bookingsImageView.setOnClickListener(this);
        historyImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mapView:
                if (SharedPrefrence.get(mContext, "role").equals("Driver")) {
                    new AlertDialog.Builder(mContext)
                            .setIcon(android.R.drawable.ic_menu_info_details)
                            .setTitle("Action to Navigate")
                            .setMessage("Are you already connected with wingman and wants to travel to his location ?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(mContext, DriverMapActivity.class));
                                }

                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(mContext, WingmenListActivity.class));
                                }
                            })
                            .show();
                }
                else if (SharedPrefrence.get(mContext, "role").equals("Wingman")) {
                    startActivity(new Intent(mContext, WingmenActivity.class));
                }
                break;
            case R.id.profileView:
                MainHomeActivity.fragmentUtils.replaceFragment(new ProfileFragment(), Constant.ProfileFragment, R.id.main_frame);
                break;
            case R.id.bookingsView:
                MainHomeActivity.fragmentUtils.replaceFragment(new BookingFragment(), Constant.BookingFragment, R.id.main_frame);
                break;
            case R.id.historyView:
                MainHomeActivity.fragmentUtils.replaceFragment(new HistoryFragment(), Constant.HistoryFragment, R.id.main_frame);
                break;
        }
    }

}
