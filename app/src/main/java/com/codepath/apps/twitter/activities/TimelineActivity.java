package com.codepath.apps.twitter.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.adapters.TweetsPagerAdapter;
import com.codepath.apps.twitter.fragments.ComposeFragment;
import com.codepath.apps.twitter.fragments.HomeTimelineFragment;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.TwitterApplication;
import jp.wasabeef.glide.transformations.CropCircleTransformation;


import com.codepath.apps.twitter.databinding.ActivityTimelineBinding;


public class TimelineActivity extends AppCompatActivity implements ComposeFragment.OnComposeListener {

    public static final String DEBUG = "DEBUG";
    public static final String ERROR = "ERROR";
    TweetsPagerAdapter adapterViewPager;
    ActivityTimelineBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);

        loadUserProfileImage();
        setTabViewPager();
        setUpPageChangeListener();
        setToolbarScroll();
        setUpClickListeners();
        processSendIntent();
    }

    private void setUpPageChangeListener() {
        // Attach the page change listener inside the activity
        binding.vpTimeline.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                // Hide FAB on Mentions View and visible on Home View
                binding.fabCompose.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Noop
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Noop
            }
        });
    }

    private void setTabViewPager() {
        // Set the view pager adapter for the pager
        adapterViewPager = new TweetsPagerAdapter(getSupportFragmentManager());
        binding.vpTimeline.setAdapter(adapterViewPager);
        // Find the sliding tabstrip
        // Attach the tab strip to the view pager
        binding.tbViews.pstsToolbar.setViewPager(binding.vpTimeline);

    }

    private void setToolbarScroll() {
        binding.abTimeline.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;


            @Override
            public void onOffsetChanged(final AppBarLayout appBarLayout, int verticalOffset) {
                //Initialize the size of the scroll
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                //Check if the view is collapsed
                if (scrollRange + verticalOffset == 0) {
                    binding.tbTimeline.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.twitter_blue));

                }else{
                    binding.tbTimeline.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
                }
            }
        });
    }

    private void loadUserProfileImage() {

        ImageView imgView = binding.tbViews.ivUserImageTimeline;
        Glide.with(this)
                .load(User.getCurrentUser().getProfileImageUrl())
                .bitmapTransform(new CropCircleTransformation(this))
                .placeholder(R.drawable.tweet_social)
                .crossFade()
                .into(imgView);
    }

    private void processSendIntent() {
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Log.d(DEBUG, "Share Intent received");
            if ("text/plain".equals(type)) {

                // Make sure to check whether returned data will be null.
                String titleOfPage = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                String urlOfPage = intent.getStringExtra(Intent.EXTRA_TEXT);

                String sharedContent = String.format("%s\n%s", titleOfPage, urlOfPage);
                showComposeDialog(sharedContent);
            }
        }
    }



    @Override
    public void onStop() {
        super.onStop();
    }

    private void showComposeDialog(String shareContent) {
        FragmentManager fm = getSupportFragmentManager();
        ComposeFragment composeFragment = ComposeFragment.newInstance(shareContent);
        composeFragment.show(fm, "fragment_compose");
    }

    private void setUpClickListeners() {
        binding.fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showComposeDialog(null);
            }
        });

        binding.tbViews.ivUserImageTimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimelineActivity.this, ProfileActivity.class);
                startActivity(intent);

            }
        });

        binding.tbViews.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

    }
    public void logout() {

        TwitterApplication.getRestClient().clearAccessToken();
        Intent intent = new Intent(TimelineActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    public void createTweet(Tweet tweet) {
        HomeTimelineFragment homeFrag = (HomeTimelineFragment) adapterViewPager.getRegisteredFragment(0);
        homeFrag.addItem(tweet);
        homeFrag.postTweet();

    }
}
