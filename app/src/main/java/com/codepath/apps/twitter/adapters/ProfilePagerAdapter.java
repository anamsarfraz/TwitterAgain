package com.codepath.apps.twitter.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.codepath.apps.twitter.fragments.MentionsTimelineFragment;
import com.codepath.apps.twitter.fragments.UserTimelineFragment;
import com.codepath.apps.twitter.models.User;


// Order and creation of fragments with the page
public class ProfilePagerAdapter extends SmartFragmentStatePagerAdapter {
    final String TWEETS = "Tweets";
    final String FAVORITES = "Favorites";
    final String MEDIA = "Media";
    private String tabTitles [] = {TWEETS, FAVORITES, MEDIA};
    User user;

    // Adapter gets the manager insert or remove from the activity
    public ProfilePagerAdapter(FragmentManager fm, User user) {
        super(fm);
        this.user = user;
    }

    // The order and creation of fragments with the page
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return UserTimelineFragment.newInstance(user.getScreenName());
        } else if (position < 3) {
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
