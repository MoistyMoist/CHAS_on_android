package com.sla.codurs.chas.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.sla.codurs.chas.R;
import com.sla.codurs.chas.fragments.FragmentChasPager;
import com.sla.codurs.chas.fragments.FragmentOneMapPager;
import com.sla.codurs.chas.fragments.FragmentSLAPager;
import com.sla.codurs.chas.utils.PageAdapter;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenActivity extends Activity implements FragmentChasPager.OnFragmentInteractionListener,FragmentSLAPager.OnFragmentInteractionListener,FragmentOneMapPager.OnFragmentInteractionListener {

    private static final int NUM_PAGES = 4;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    PageAdapter mSectionsPagerAdapter;
    Timer timer;
    int page = 0;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private int currentPage;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);



        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new PageAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        handler = new Handler();

        final Runnable update = new Runnable() {
            public void run() {
                if (currentPage == NUM_PAGES - 1) {
                    timer.cancel();
                    timer.purge();
                    startActivity(new Intent(SplashScreenActivity.this, BaseActivity.class));
                    finish();

                }
                else {
                    mViewPager.setCurrentItem(currentPage++, true);
                }
            }
        };

        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                handler.post(update);
            }
        }, 0, 1000);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


}
