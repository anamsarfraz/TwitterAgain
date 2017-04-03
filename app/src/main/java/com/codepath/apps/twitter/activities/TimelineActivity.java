package com.codepath.apps.twitter.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.adapters.TweetsPagerAdapter;
import com.codepath.apps.twitter.fragments.HomeTimelineFragment;
import com.codepath.apps.twitter.fragments.MessagesFragment;
import com.codepath.apps.twitter.fragments.TweetsListFragment;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Constants;
import com.codepath.apps.twitter.util.OnTweetClickListener;
import com.codepath.apps.twitter.util.TwitterApplication;
import jp.wasabeef.glide.transformations.CropCircleTransformation;


import com.codepath.apps.twitter.databinding.ActivityTimelineBinding;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.parceler.Parcels;

import static com.codepath.apps.twitter.R.string.tweet;
import static com.raizlabs.android.dbflow.config.FlowManager.getContext;


public class TimelineActivity extends ComposeActivity implements OnTweetClickListener {

    public static final String DEBUG = "DEBUG";
    public static final String ERROR = "ERROR";
    private final int REQUEST_CODE = 20;
    private final int SEARCH_CODE = 30;
    TweetsPagerAdapter adapterViewPager;
    ActivityTimelineBinding binding;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);

        loadUserProfileImage(binding.tbViews.ivUserImageTimeline);
        setTabViewPager();
        setToolbarScroll();
        setUpClickListeners();
        processSendIntent();

        // Setup drawer view
        setupDrawerContent();


    }

    private void setTabViewPager() {
        // Set the view pager adapter for the pager
        adapterViewPager = new TweetsPagerAdapter(getSupportFragmentManager());
        binding.vpTimeline.setAdapter(adapterViewPager);
        // Find the sliding tabstrip
        // Attach the tab strip to the view pager
        binding.tbViews.pstsToolbar.setViewPager(binding.vpTimeline);
        binding.tbViews.pstsToolbar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 2) {
                    binding.fabCompose.setImageResource(R.drawable.message_compose);
                } else {
                    binding.fabCompose.setImageResource(R.drawable.tweet_compose);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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

    private void loadUserProfileImage(ImageView profileImgView) {
        Glide.with(this)
                .load(User.getCurrentUser().getProfileImageUrl())
                .bitmapTransform(new CropCircleTransformation(this))
                .placeholder(R.drawable.tweet_social)
                .crossFade()
                .into(profileImgView);
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
                showComposeDialog(sharedContent, false, null);
            }
        }
    }



    @Override
    public void onStop() {
        super.onStop();
    }

    private void setUpClickListeners() {
        binding.fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.vpTimeline.getCurrentItem() == 2) {
                    Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                    intent.putExtra("user_search", true);
                    Bundle animationBundle =
                            ActivityOptions.makeCustomAnimation(getContext(), R.anim.slide_from_left,R.anim.slide_to_left).toBundle();
                    startActivityForResult(intent, SEARCH_CODE, animationBundle);
                } else {
                    showComposeDialog(null, false, null);
                }

            }
        });

        binding.tbViews.ivUserImageTimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(GravityCompat.START);
                ImageView profileBannerView = (ImageView) binding.nvView.findViewById(R.id.ivNavHeader);
                if (User.getCurrentUser().getProfileBannerUrl() != null) {
                    Glide.with(getApplicationContext())
                            .load(User.getCurrentUser().getProfileBannerUrl())
                            .placeholder(R.drawable.tweet_social)
                            .crossFade()
                            .into(profileBannerView);
                }
                loadUserProfileImage((ImageView) binding.nvView.findViewById(R.id.ivNavProfile));
                TextView tvNavUserName = (TextView) binding.nvView.findViewById(R.id.tvNavUsername);
                TextView tvNavScreenName = (TextView) binding.nvView.findViewById(R.id.tvNavScreenName);
                tvNavUserName.setText(User.getCurrentUser().getName());
                tvNavScreenName.setText(
                        String.format("%s%s", Constants.ATRATE, User.getCurrentUser().getScreenName()));

            }
        });

        binding.tbViews.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);

                Bundle animationBundle =
                        ActivityOptions.makeCustomAnimation(getContext(), R.anim.slide_from_left,R.anim.slide_to_left).toBundle();
                startActivity(intent, animationBundle);
            }
        });

    }

    private void setupDrawerContent() {
        binding.nvView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // select drawer item and perform action
        int postion = 0;
        switch(menuItem.getItemId()) {
            case R.id.profile:
                postion = 1;
                break;
            case R.id.logout:
                postion = 2;
                break;
            default:
                break;
        }


        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        if (postion == 1) {
            Intent intent = new Intent(TimelineActivity.this, ProfileActivity.class);
            intent.putExtra("user", Parcels.wrap(User.getCurrentUser()));
            Bundle animationBundle =
                    ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slide_from_left,R.anim.slide_to_left).toBundle();
            startActivity(intent, animationBundle);
        } else if (postion == 2) {
            logout();
        }
        menuItem.setChecked(false);

        // Close the navigation drawer
        binding.drawerLayout.closeDrawers();
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

    @Override
    public void createMessage(String message) {
        MessagesFragment messagesFragment = (MessagesFragment) adapterViewPager.getRegisteredFragment(2);
        messagesFragment.sendMessage(user, message);
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
            HomeTimelineFragment homeFrag = (HomeTimelineFragment) adapterViewPager.getRegisteredFragment(0);
            homeFrag.changeItem(tweet);
        } else if (resultCode == RESULT_OK && requestCode == SEARCH_CODE) {
            user = Parcels.unwrap(data.getParcelableExtra("user"));
            showComposeDialog(null, true, user.getName());
        }
    }
}
