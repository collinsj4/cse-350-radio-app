package com.app.vaporwave.utils;

import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.app.vaporwave.Config;
import com.app.vaporwave.R;
import com.app.vaporwave.activities.MainActivity;
import com.app.vaporwave.adapters.AdapterNavigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ViewPagerHelper {

    AppCompatActivity activity;
    MenuItem prevMenuItem;

    public ViewPagerHelper(AppCompatActivity activity) {
        this.activity = activity;
    }

    public int pageNumber(BottomNavigationView bottomNavigationView) {
        if (Config.DISPLAY_SOCIAL_IN_NAVIGATION_MENU) {
            return AdapterNavigation.PAGER_NUMBER;
        } else {
            bottomNavigationView.getMenu().findItem(R.id.navigation_social).setVisible(false);
            return AdapterNavigation.PAGER_NUMBER - 1;
        }
    }

    public int pageNumberSimple(BottomNavigationView bottomNavigationView) {
        if (Config.DISPLAY_SOCIAL_IN_NAVIGATION_MENU) {
            return AdapterNavigation.PAGER_NUMBER_SIMPLE;
        } else {
            bottomNavigationView.getMenu().findItem(R.id.navigation_social).setVisible(false);
            return AdapterNavigation.PAGER_NUMBER_SIMPLE - 1;
        }
    }

    public void setupViewPager(FragmentManager fragmentManager, ViewPager viewPager, BottomNavigationView bottomNavigationView, Toolbar toolbar) {

        viewPager.setAdapter(new AdapterNavigation.BottomNavigationAdapter(activity.getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(pageNumber(bottomNavigationView));
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                viewPager.setCurrentItem(0);
            } else if (itemId == R.id.navigation_explore) {
                viewPager.setCurrentItem(1);
            } else if (itemId == R.id.navigation_category) {
                viewPager.setCurrentItem(2);
            } else if (itemId == R.id.navigation_favorite) {
                viewPager.setCurrentItem(3);
            } else if (itemId == R.id.navigation_social) {
                viewPager.setCurrentItem(4);
            } else {
                viewPager.setCurrentItem(0);
            }
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ((MainActivity) activity).hideKeyboard();
            return false;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);

                int currentItem = viewPager.getCurrentItem();
                if (currentItem == 0) {
                    toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                } else if (currentItem == 1) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_explore));
                } else if (currentItem == 2) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_category));
                } else if (currentItem == 3) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_favorite));
                } else if (currentItem == 4) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_social));
                } else {
                    toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setupViewPagerRTL(FragmentManager fragmentManager, RtlViewPager viewPager, BottomNavigationView bottomNavigationView, Toolbar toolbar) {

        viewPager.setAdapter(new AdapterNavigation.BottomNavigationAdapter(activity.getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(pageNumber(bottomNavigationView));
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                viewPager.setCurrentItem(0);
            } else if (itemId == R.id.navigation_explore) {
                viewPager.setCurrentItem(1);
            } else if (itemId == R.id.navigation_category) {
                viewPager.setCurrentItem(2);
            } else if (itemId == R.id.navigation_favorite) {
                viewPager.setCurrentItem(3);
            } else if (itemId == R.id.navigation_social) {
                viewPager.setCurrentItem(4);
            } else {
                viewPager.setCurrentItem(0);
            }
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ((MainActivity) activity).hideKeyboard();
            return false;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);

                int currentItem = viewPager.getCurrentItem();
                if (currentItem == 0) {
                    toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                } else if (currentItem == 1) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_explore));
                } else if (currentItem == 2) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_category));
                } else if (currentItem == 3) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_favorite));
                } else if (currentItem == 4) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_social));
                } else {
                    toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setupViewPagerSimple(FragmentManager fragmentManager, ViewPager viewPager, BottomNavigationView bottomNavigationView, Toolbar toolbar) {

        viewPager.setAdapter(new AdapterNavigation.BottomNavigationAdapterSimple(activity.getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(pageNumberSimple(bottomNavigationView));
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_explore) {
                viewPager.setCurrentItem(0);
            } else if (itemId == R.id.navigation_category) {
                viewPager.setCurrentItem(1);
            } else if (itemId == R.id.navigation_favorite) {
                viewPager.setCurrentItem(2);
            } else if (itemId == R.id.navigation_social) {
                viewPager.setCurrentItem(3);
            } else {
                viewPager.setCurrentItem(0);
            }
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ((MainActivity) activity).hideKeyboard();
            return false;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);

                int currentItem = viewPager.getCurrentItem();
                if (currentItem == 0) {
                    toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                } else if (currentItem == 1) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_category));
                } else if (currentItem == 2) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_favorite));
                } else if (currentItem == 3) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_social));
                } else {
                    toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setupViewPagerSimpleRTL(FragmentManager fragmentManager, RtlViewPager viewPager, BottomNavigationView bottomNavigationView, Toolbar toolbar) {

        viewPager.setAdapter(new AdapterNavigation.BottomNavigationAdapterSimple(activity.getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(pageNumberSimple(bottomNavigationView));
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_explore) {
                viewPager.setCurrentItem(0);
            } else if (itemId == R.id.navigation_category) {
                viewPager.setCurrentItem(1);
            } else if (itemId == R.id.navigation_favorite) {
                viewPager.setCurrentItem(2);
            } else if (itemId == R.id.navigation_social) {
                viewPager.setCurrentItem(3);
            } else {
                viewPager.setCurrentItem(0);
            }
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ((MainActivity) activity).hideKeyboard();
            return false;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);

                int currentItem = viewPager.getCurrentItem();
                if (currentItem == 0) {
                    toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                } else if (currentItem == 1) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_category));
                } else if (currentItem == 2) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_favorite));
                } else if (currentItem == 3) {
                    toolbar.setTitle(activity.getResources().getString(R.string.title_nav_social));
                } else {
                    toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

}
