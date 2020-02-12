package com.ourride.driver.utils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class FragmentUtils {

    private FragmentManager fragmentManager;

    public FragmentUtils(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public void replaceFragment(Fragment fragment, String tag, int frameId) {
        fragmentManager
                .beginTransaction()
                .replace(frameId, fragment,
                        tag).commit();
    }
}
