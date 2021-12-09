package com.ag18.record;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new RecordFragment();
            case 1:
                return new RecordFragment();
            case 2:
                return new SettingsFragment();
            default:
                return new RecordFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}