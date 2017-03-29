package com.codepath.apps.twitter.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.codepath.apps.twitter.fragments.HomeTimelineFragment;
import com.codepath.apps.twitter.fragments.MentionsTimelineFragment;


// Order and creation of fragments with the page
public class TweetsPagerAdapter extends SmartFragmentStatePagerAdapter {
    final String HOME = "Home";
    final String MENTIONS = "Mentions";
    private String tabTitles [] = {HOME, MENTIONS};

    // Adapter gets the manager insert or remove from the activity
    public TweetsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    // The order and creation of fragments with the page
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new HomeTimelineFragment();
        } else if (position == 1) {
            return new MentionsTimelineFragment();
        } else {
            return null;
        }
    }

    // Return the tab title at the top
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    // Number of fragments to swipe in between
    @Override
    public int getCount() {
        return tabTitles.length;
    }
}
