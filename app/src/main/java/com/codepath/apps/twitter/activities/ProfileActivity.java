package com.codepath.apps.twitter.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.adapters.ProfilePagerAdapter;
import com.codepath.apps.twitter.adapters.TweetsPagerAdapter;
import com.codepath.apps.twitter.databinding.ActivityProfileBinding;
import com.codepath.apps.twitter.fragments.FollowFragment;
import com.codepath.apps.twitter.fragments.HomeTimelineFragment;
import com.codepath.apps.twitter.fragments.TweetsListFragment;
import com.codepath.apps.twitter.fragments.UserHeaderFragment;
import com.codepath.apps.twitter.fragments.UserTimelineFragment;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Constants;
import com.codepath.apps.twitter.util.OnTweetClickListener;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;

import org.parceler.Parcels;

import static com.codepath.apps.twitter.R.id.pstsToolbar;
import static com.codepath.apps.twitter.R.string.tweet;
import static com.codepath.apps.twitter.models.User.getCurrentUser;
import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class ProfileActivity extends ComposeActivity implements OnTweetClickListener, UserHeaderFragment.OnFollowListener {

    ActivityProfileBinding binding;
    ProfilePagerAdapter adapterViewPager;
    User user;
    private final int REQUEST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));

        setSupportActionBar(binding.tbProfile);
        getSupportActionBar().setTitle(user.getName());
        binding.tbProfile.setTitle("Profile");

        setTabViewPager();
        setUpClickListeners();

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            UserHeaderFragment userHeaderFragment = UserHeaderFragment.newInstance(user);
            ft.replace(R.id.flUserHeader, userHeaderFragment);
            ft.commit();
        }
    }

    private void setUpClickListeners() {
        binding.fabComposeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getScreenName().equals(User.getCurrentUser().getScreenName())) {
                    showComposeDialog(null, false);
                } else {
                    showComposeDialog(String.format("%s%s ", Constants.ATRATE, user.getScreenName()), false);
                }
            }
        });
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
        startActivityForResult(intent, REQUEST_CODE, animationBundle);
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

    @Override
    public void onHashTagClick(String hashTag) {
        Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
        intent.putExtra("query", hashTag);
        Bundle animationBundle =
                ActivityOptions.makeCustomAnimation(getContext(), R.anim.slide_from_left,R.anim.slide_to_left).toBundle();
        startActivity(intent, animationBundle);
    }

    @Override
    public void onReplyClick(Tweet tweet) {
        Intent intent = new Intent(this, TweetDetailActivity.class);
        intent.putExtra("tweet", Parcels.wrap(tweet));
        intent.putExtra("is_reply", true);

        Bundle animationBundle =
                ActivityOptions.makeCustomAnimation(getContext(), R.anim.slide_from_left,R.anim.slide_to_left).toBundle();
        startActivityForResult(intent, REQUEST_CODE, animationBundle);
    }

    @Override
    public void onMessageClick(Tweet tweet) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            UserTimelineFragment userFrag = (UserTimelineFragment) adapterViewPager.getRegisteredFragment(0);
            userFrag.changeItem(tweet);
        }
    }

    @Override
    public void createTweet(Tweet tweet) {
        UserTimelineFragment userFrag = (UserTimelineFragment) adapterViewPager.getRegisteredFragment(0);
        userFrag.addItem(tweet);
        userFrag.postTweet();
    }

    @Override
    public void getFollow(boolean isFollowers) {
        Intent intent = new Intent(getApplicationContext(), FollowActivity.class);
        intent.putExtra("get_followers", isFollowers);
        intent.putExtra("screen_name", user.getScreenName());
        Bundle animationBundle =
                ActivityOptions.makeCustomAnimation(getContext(), R.anim.slide_from_left,R.anim.slide_to_left).toBundle();
        startActivity(intent, animationBundle);

    }
}
