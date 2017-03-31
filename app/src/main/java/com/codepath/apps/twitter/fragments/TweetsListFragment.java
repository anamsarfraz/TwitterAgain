package com.codepath.apps.twitter.fragments;

import android.app.ActivityOptions;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.activities.ProfileActivity;
import com.codepath.apps.twitter.activities.TimelineActivity;
import com.codepath.apps.twitter.activities.TweetDetailActivity;
import com.codepath.apps.twitter.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitter.databinding.FragmentTweetsListBinding;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Connectivity;
import com.codepath.apps.twitter.util.EndlessRecyclerViewScrollListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;


public abstract class TweetsListFragment extends Fragment {

    public static final String DEBUG = "DEBUG";

    List<Tweet> tweets;
    long currMaxId;
    int retryCount;
    TweetsArrayAdapter tweetsArrayAdapter;
    LinearLayoutManager linearLayoutManager;
    private FragmentTweetsListBinding binding;
    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;
    private OnTweetClickListener tweetClickListener;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tweets = new ArrayList<>();
        tweetsArrayAdapter = new TweetsArrayAdapter(getActivity(), tweets);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tweets_list, container, false);

        tweetClickListener = (OnTweetClickListener) getActivity();
        setUpRecycleView();
        setUpRefreshControl();
        setUpScrollListeners();
        setUpClickListeners();
        return binding.getRoot();
    }

    private void setUpRecycleView() {
        binding.rvTweets.setAdapter(tweetsArrayAdapter);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        binding.rvTweets.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(),
                LinearLayoutManager.VERTICAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dividerItemDecoration.setDrawable(getActivity().getDrawable(R.drawable.line_divider));
        }
        binding.rvTweets.addItemDecoration(dividerItemDecoration);
    }

    private void setUpRefreshControl() {
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                beginNewSearch();
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void setUpScrollListeners() {
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                binding.pbLoading.setVisibility(ProgressBar.VISIBLE);
                loadMore();

            }
        };
        // Adds the scroll listener to RecyclerView
        binding.rvTweets.addOnScrollListener(scrollListener);

    }
    private void setUpClickListeners() {


        tweetsArrayAdapter.setOnItemClickListener(new TweetsArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                tweetClickListener.onItemClick(tweets.get(position));
            }

            @Override
            public void onImageClick(View imageView, int position) {
                tweetClickListener.onImageClick(tweets.get(position).getUser());
            }
        });
    }


    public void hideRefreshControl() {
        if (binding.swipeContainer.isRefreshing()) {
            binding.swipeContainer.setRefreshing(false);
        }
    }

    public void addItem(Tweet tweet) {
        tweets.add(0, tweet);
        tweetsArrayAdapter.notifyItemInserted(0);
    }

    public void addAll(List<Tweet> newTweets) {
        int curSize = tweetsArrayAdapter.getItemCount();
        tweets.addAll(newTweets);
        int newSize = newTweets.size();
        tweetsArrayAdapter.notifyItemRangeInserted(curSize, newSize);
        binding.pbLoading.setVisibility(ProgressBar.INVISIBLE);
    }
    public void removeAll() {
        tweetsArrayAdapter.clearItems();
        scrollListener.resetState();
        hideRefreshControl();
        binding.pbLoading.setVisibility(ProgressBar.VISIBLE);
    }


    public void beginNewSearch() {
        currMaxId = 0L;
        removeAll();

        loadMore();

    }

    public void loadMore() {
        retryCount = 0;
        if (currMaxId > 0) {
            Log.d(DEBUG, "User scrolled. Load additional tweets");
        }
        if (Connectivity.isConnected(getActivity())) {
            fetchTimeline();
        } else {
            fetchOffline();
        }
    }

    public abstract void fetchTimeline();
    public abstract void fetchOffline();

    public interface OnTweetClickListener {
        public void onItemClick(Tweet tweet);
        public void onImageClick(User user);
    }
}
