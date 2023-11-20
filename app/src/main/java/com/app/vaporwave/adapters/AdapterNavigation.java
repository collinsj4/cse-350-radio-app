package com.app.vaporwave.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.app.vaporwave.Config;
import com.app.vaporwave.fragments.FragmentCategory;
import com.app.vaporwave.fragments.FragmentFavorite;
import com.app.vaporwave.fragments.FragmentHome;
import com.app.vaporwave.fragments.FragmentRadio;
import com.app.vaporwave.fragments.FragmentSocial;

@SuppressWarnings("ALL")
public class AdapterNavigation {

    public static final int PAGER_NUMBER = 5;
    public static final int PAGER_NUMBER_SIMPLE = 4;

    public static class BottomNavigationAdapter extends FragmentPagerAdapter {

        public BottomNavigationAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FragmentHome();
                case 1:
                    return new FragmentRadio();
                case 2:
                    return new FragmentCategory();
                case 3:
                    return new FragmentFavorite();
                case 4:
                    return new FragmentSocial();
            }
            return null;
        }

        @Override
        public int getCount() {
            if (Config.DISPLAY_SOCIAL_IN_NAVIGATION_MENU) {
                return PAGER_NUMBER;
            } else {
                return PAGER_NUMBER - 1;
            }
        }

    }

    public static class BottomNavigationAdapterSimple extends FragmentPagerAdapter {

        public BottomNavigationAdapterSimple(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FragmentRadio();
                case 1:
                    return new FragmentCategory();
                case 2:
                    return new FragmentFavorite();
                case 3:
                    return new FragmentSocial();
            }
            return null;
        }

        @Override
        public int getCount() {
            if (Config.DISPLAY_SOCIAL_IN_NAVIGATION_MENU) {
                return PAGER_NUMBER_SIMPLE;
            } else {
                return PAGER_NUMBER_SIMPLE - 1;
            }
        }

    }

}
