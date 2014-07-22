package com.sla.codurs.chas.utils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.sla.codurs.chas.fragments.FragmentChasPager;
import com.sla.codurs.chas.fragments.FragmentOneMapPager;
import com.sla.codurs.chas.fragments.FragmentSLAPager;

/**
 * Created by Moistyburger on 11/7/14.
 */
public class PageAdapter extends FragmentPagerAdapter {
    public PageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0)
            return FragmentChasPager.newInstance("Chas", "Chas");
        if (position == 1)
            return FragmentOneMapPager.newInstance("OneMap", "OneMap");
        if (position == 2)
            return FragmentSLAPager.newInstance("SLA", "SLA");

        else
            return null;
    }


    @Override
    public int getCount() {
        return 3;
    }
}
