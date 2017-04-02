package com.codepath.apps.twitter.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.databinding.ActivitySearchBinding;
import com.codepath.apps.twitter.fragments.TrendsFragment;
import com.codepath.apps.twitter.fragments.TweetSearchFragment;
import com.codepath.apps.twitter.fragments.UserHeaderFragment;
import com.codepath.apps.twitter.fragments.UserTimelineFragment;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Constants;
import com.codepath.apps.twitter.util.OnTweetClickListener;

import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.EditText;

import org.parceler.Parcels;

import static com.codepath.apps.twitter.R.string.tweet;
import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class SearchActivity extends AppCompatActivity implements TrendsFragment.OnTrendClickListener, OnTweetClickListener {

    ActivitySearchBinding binding;
    TrendsFragment trendsFragment;
    TweetSearchFragment tweetSearchFragment;
    SearchView searchView;
    EditText et;
    private final int REQUEST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);

        setUpToolbar();

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            trendsFragment = new TrendsFragment();
            ft.replace(R.id.flSearch, trendsFragment);
            ft.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.search_hint));
        int searchEditId = android.support.v7.appcompat.R.id.search_src_text;
        et = (EditText) searchView.findViewById(searchEditId);
        et.setTextColor(ContextCompat.getColor(this, R.color.twitter_blue));
        et.setHintTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        searchItem.expandActionView();
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);

                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

        searchView.setOnQueryTextFocusChangeListener(new SearchView.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("Search Activity", "in focus change: "+hasFocus);
            }
        });
        processIntent();
        return super.onCreateOptionsMenu(menu);
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
        String query = intent.getStringExtra("query");
        if (query != null) {
            et.setText(query);
            performSearch(query);
        }
    }

    public void performSearch(String query) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        tweetSearchFragment = TweetSearchFragment.newInstance(query);
        ft.replace(R.id.flSearch, tweetSearchFragment);
        ft.commit();

    }
    private void setUpToolbar() {
        setSupportActionBar(binding.tbSearch);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");
    }

    @Override
    public void onSearchQuery(String trendName) {
        et.setText(trendName);
        performSearch(trendName);
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
            tweetSearchFragment.changeItem(tweet);
        }
    }
}
