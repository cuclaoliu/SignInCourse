package edu.cuc.stephen.signincourse;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import edu.cuc.stephen.signincourse.utils.AssetsDatabaseManager;

public class MainActivity extends FragmentActivity {

    private ViewPager viewPager;
    private List<Fragment> tabs = new ArrayList<>();
    private FragmentPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AssetsDatabaseManager.initManager(getApplication());
        SignInFragment signInFragment = new SignInFragment();
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        BrowseStudents browseStudentsFragment = new BrowseStudents();
        SettingFragment settingFragment = new SettingFragment();
        tabs.add(browseStudentsFragment);
        tabs.add(signInFragment);
        tabs.add(settingFragment);
        adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public android.support.v4.app.Fragment getItem(int position) {
                return tabs.get(position);
            }

            @Override
            public int getCount() {
                return tabs.size();
            }
        };
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setCurrentItem(0);
    }

    public void clearDatabase(View view) {
        if(R.id.button_delete_database == view.getId()) {
            AssetsDatabaseManager.deleteAllDatabase();
        }
    }

    public void openCamera(View view) {
        viewPager.setCurrentItem(1);
    }
}
