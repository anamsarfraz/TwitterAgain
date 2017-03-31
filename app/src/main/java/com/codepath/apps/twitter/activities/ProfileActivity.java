package com.codepath.apps.twitter.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.adapters.ProfilePagerAdapter;
import com.codepath.apps.twitter.adapters.TweetsPagerAdapter;
import com.codepath.apps.twitter.databinding.ActivityProfileBinding;
import com.codepath.apps.twitter.fragments.TweetsListFragment;
import com.codepath.apps.twitter.fragments.UserHeaderFragment;
import com.codepath.apps.twitter.fragments.UserTimelineFragment;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.OnTweetClickListener;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;

import org.parceler.Parcels;

import static com.codepath.apps.twitter.R.id.pstsToolbar;
import static com.codepath.apps.twitter.models.User.getCurrentUser;
import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class ProfileActivity extends AppCompatActivity implements OnTweetClickListener {

    ActivityProfileBinding binding;
    ProfilePagerAdapter adapterViewPager;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));

        setSupportActionBar(binding.tbProfile);
        getSupportActionBar().setTitle(user.getName());
        binding.tbProfile.setTitle("Profile");

        setTabViewPager();

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            UserHeaderFragment userHeaderFragment = UserHeaderFragment.newInstance(user);
            ft.replace(R.id.flUserHeader, userHeaderFragment);
            ft.commit();
        }
    }

    private void setTabViewPager() {
        // Set the view pager adapter for the pager
        adapterViewPager = new ProfilePagerAdapter(getSupportFragmentManager(), user);
        binding.rlUserProfile.vpUserProfile.setAdapter(adapterViewPager);
        // Find the sliding tabstrip
        // Attach the tab strip to the view pager
        binding.rlUserProfile.pstsProfile.setViewPager(binding.rlUserProfile.vpUserProfile);

    }


    @Override
    public void onItemClick(Tweet tweet) {
        Intent intent = new Intent(this, TweetDetailActivity.class);
        intent.putExtra("tweet", Parcels.wrap(tweet));

        Bundle animationBundle =
                ActivityOptions.makeCustomAnimation(getContext(), R.anim.slide_from_left,R.anim.slide_to_left).toBundle();
        startActivity(intent, animationBundle);
    }

    @Override
    public void onViewClick(User user) {
        if (user.getScreenName().equals(this.user.getScreenName())) {
            // if the click is on the image of the same user whos profile activity is being currently
            // viewed right now, then ignore the click
            return;
        }
        // Go to user profile
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("user", Parcels.wrap(user));
        Bundle animationBundle =
                ActivityOptions.makeCustomAnimation(getContext(), R.anim.slide_from_left,R.anim.slide_to_left).toBundle();
        startActivity(intent, animationBundle);

    }
}
