package com.codepath.apps.twitter.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.databinding.ActivityFollowBinding;
import com.codepath.apps.twitter.databinding.ActivitySearchBinding;
import com.codepath.apps.twitter.fragments.FollowFragment;
import com.codepath.apps.twitter.fragments.TrendsFragment;
import com.codepath.apps.twitter.fragments.TweetSearchFragment;
import com.codepath.apps.twitter.models.Follow;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;

import org.parceler.Parcels;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class FollowActivity extends AppCompatActivity {

    ActivityFollowBinding binding;
    FollowFragment followFragment;
    TweetSearchFragment tweetSearchFragment;
    String title;
    boolean isFollowers;
    String screeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_follow);

        processIntent();
        setUpToolbar();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        followFragment = FollowFragment.newInstance(isFollowers, screeName);
        ft.replace(R.id.flFollow, followFragment);
        ft.commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void processIntent() {
        Intent intent = getIntent();
        isFollowers = intent.getBooleanExtra("get_followers", false);
        title = isFollowers ? "Followers" : "Following";
        screeName = intent.getStringExtra("screen_name");
    }

    private void setUpToolbar() {
        setSupportActionBar(binding.tbFollow);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(title);
    }
}
