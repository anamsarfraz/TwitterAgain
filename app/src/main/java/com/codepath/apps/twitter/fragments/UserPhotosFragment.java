package com.codepath.apps.twitter.fragments;


import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.adapters.PhotosArrayAdapter;
import com.codepath.apps.twitter.databinding.FragmentPhotosBinding;
import com.codepath.apps.twitter.models.Media;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.util.Connectivity;
import com.codepath.apps.twitter.util.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitter.util.OnTweetClickListener;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

import static com.codepath.apps.twitter.R.string.tweet;

public class UserPhotosFragment extends Fragment {
    public static final String DEBUG = "DEBUG";
    public static final String ERROR = "ERROR";
    private static final int RATE_LIMIT_ERR = 88;
    private static final int RETRY_LIMIT = 3;
    private static final long DELAY_MILLI = 3000;

    Handler handler;
    final Runnable fetchRunnable = new Runnable() {

        @Override
        public void run() {
            fetchTimeline();
        }
    };

    FragmentPhotosBinding binding;
    PhotosArrayAdapter photoAdapter;
    List<String> photos;
    long currMaxId;
    int retryCount;
    int currPosition;
    private EndlessRecyclerViewScrollListener scrollListener;
    private OnTweetClickListener tweetClickListener;
    LinearLayoutManager linearLayoutManager;
    TwitterClient client;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        photos = new ArrayList<>();
        photoAdapter = new PhotosArrayAdapter(getActivity(), photos);
        client = TwitterApplication.getRestClient();


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photos, container, false);


        tweetClickListener = (OnTweetClickListener) getActivity();
        setUpRecycleView();
        setUpRefreshControl();
        setUpScrollListeners();

        return binding.getRoot();
    }

    private void setUpRecycleView() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        tweetClickListener = (OnTweetClickListener) getActivity();
        binding.rvPhotos.setAdapter(photoAdapter);
        binding.rvPhotos.setLayoutManager(linearLayoutManager);



    }
    private void setUpRefreshControl() {
        binding.scPhotos.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                beginNewSearch();
            }
        });
        // Configure the refreshing colors
        binding.scPhotos.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void setUpScrollListeners() {
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMore();

            }
        };
        // Adds the scroll listener to RecyclerView
        binding.rvPhotos.addOnScrollListener(scrollListener);

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // fetch user timeline on first load
        beginNewSearch();
    }

    public static UserPhotosFragment newInstance(String screenName) {

        Bundle args = new Bundle();

        UserPhotosFragment userFragment = new UserPhotosFragment();
        args.putString("screen_name", screenName);

        userFragment.setArguments(args);
        return userFragment;
    }

    public void fetchTimeline() {
        String screenName = getArguments().getString("screen_name");
        client.getUserPhotos(screenName, currMaxId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                hideRefreshControl();
                Log.d("DEBUG", "user photos: " + jsonArray.toString());
                List<Tweet> newTweets = Tweet.fromJSONArray(jsonArray);
                processFetchedTweets(newTweets);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                hideRefreshControl();
                Log.e(ERROR, "Error fetching timeline: " + (errorResponse == null ? "Unknown error" : errorResponse.toString()));
                int errorCode = errorResponse.optJSONArray("errors").optJSONObject(0).optInt("code", 0);

                if (errorCode == RATE_LIMIT_ERR && retryCount < RETRY_LIMIT) {
                    retryCount++;
                    handler.postDelayed(fetchRunnable, DELAY_MILLI);
                } else {
                    fetchOffline();
                }
            }
        });
    }

    public void fetchOffline() {
        List<Tweet> newTweets = Tweet.recentItems(currMaxId);
        processFetchedTweets(newTweets);

    }

    private void processFetchedTweets(List<Tweet> newTweets) {
        retryCount = 0;

        List<String> newPhotos = getPhotoInfo(newTweets);
        int newSize = newTweets.size();
        if (newSize > 0) {
            currMaxId = newTweets.get(newSize-1).getUid()-1;
        }
        addAllPhotos(newPhotos);
        handler.removeCallbacks(fetchRunnable);
    }

    private List<String> getPhotoInfo(List<Tweet> newTweets) {
        List<String> newPhotos = new ArrayList<>();
        for (Tweet tweet : newTweets) {
            Media media = tweet.getMedia();
            if (media != null) {
                newPhotos.add(media.getImageUrl());
            }
        }
        return newPhotos;
    }

    public void addAllPhotos(List<String> newPhotos) {
        int curSize = photoAdapter.getItemCount();
        photos.addAll(newPhotos);
        int newSize = newPhotos.size();
        photoAdapter.notifyItemRangeInserted(curSize, newSize);
        binding.pbPhotos.setVisibility(ProgressBar.INVISIBLE);
    }

    public void removeAll() {
        photoAdapter.clearItems();
        scrollListener.resetState();
        hideRefreshControl();
    }

    public void beginNewSearch() {
        currMaxId = 0L;
        removeAll();

        loadMore();

    }

    public void loadMore() {
        binding.pbPhotos.setVisibility(ProgressBar.VISIBLE);
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

    public void hideRefreshControl() {
        if (binding.scPhotos.isRefreshing()) {
            binding.scPhotos.setRefreshing(false);
        }
    }
    /*public void postTweet() {
        client.postTweet(tweets.get(0).getBody(), null, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                Tweet.saveTweet(jsonObject);
                handler.removeCallbacks(postTweetRunnable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(ERROR, "Error creating tweet: " + errorResponse.toString());
                int errorCode = errorResponse.optJSONArray("errors").optJSONObject(0).optInt("code", 0);
                if (errorCode == RATE_LIMIT_ERR && retryCount < RETRY_LIMIT) {
                    retryCount++;
                    handler.postDelayed(postTweetRunnable, DELAY_MILLI);
                } else {
                    Toast.makeText(getContext(), "Error creating tweet. Please try again", Toast.LENGTH_SHORT).show();
                    retryCount = 0;
                    handler.removeCallbacks(postTweetRunnable);
                }
            }
        });
    }*/
}
