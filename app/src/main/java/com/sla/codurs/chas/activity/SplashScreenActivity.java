package com.sla.codurs.chas.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.sla.codurs.chas.R;
import com.sla.codurs.chas.fragments.FragmentChasPager;
import com.sla.codurs.chas.fragments.FragmentCodursPager;
import com.sla.codurs.chas.fragments.FragmentOneMapPager;
import com.sla.codurs.chas.fragments.FragmentSLAPager;
import com.sla.codurs.chas.HTTP.GetChasRequest;
import com.sla.codurs.chas.utils.PageAdapter;
import com.sla.codurs.chas.utils.StaticObjects;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenActivity extends Activity implements FragmentChasPager.OnFragmentInteractionListener,FragmentSLAPager.OnFragmentInteractionListener,FragmentOneMapPager.OnFragmentInteractionListener,FragmentCodursPager.OnFragmentInteractionListener {

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

        //AUTO PAGE
        timer = new Timer(); // At this line a new Thread will be created
        timer.scheduleAtFixedRate(new AutoPage(), 0,  1500);


        GetChasRequest searchRequest= new GetChasRequest();
        new BackgroundTask().execute(searchRequest,searchRequest);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class BackgroundTask extends AsyncTask<Runnable, Integer, Long> {

        @Override
        protected void onPostExecute(Long result) {


        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(getBaseContext(), "Loading...", Toast.LENGTH_LONG).show();
            super.onPreExecute();
        }

        @Override
        protected Long doInBackground(Runnable... task) {

                task[0].run();
            return null;
        }
    }

    class AutoPage extends TimerTask {

        @Override
        public void run() {

            // As the TimerTask run on a seprate thread from UI thread we have
            // to call runOnUiThread to do work on UI thread.
            runOnUiThread(new Runnable() {
                public void run() {

                    if (page > 3) { // In my case the number of pages are 5
                        if(StaticObjects.chases==null){
                            page=0;
                            Log.i("DATA","still null");
                        }
                        else
                        {
                            timer.cancel();
                            Intent i = new Intent(SplashScreenActivity.this, BaseActivity.class);
                            startActivity(i);
                        }

                        Toast.makeText(getApplicationContext(), "Timer stoped",
                                Toast.LENGTH_LONG).show();
                    } else {
                        mViewPager.setCurrentItem(page++);
                    }
                }
            });

        }
    }


}
