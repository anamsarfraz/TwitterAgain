package com.codepath.apps.twitter.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TweetSearchFragment extends TweetsListFragment {
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
    final Runnable postTweetRunnable = new Runnable() {

        @Override
        public void run() {
            postTweet();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // fetch user timeline on first load
        beginNewSearch();
    }

    public static TweetSearchFragment newInstance(String query) {

        Bundle args = new Bundle();

        TweetSearchFragment tweetSearchFragment = new TweetSearchFragment();
        args.putString("query", query);

        tweetSearchFragment.setArguments(args);
        return tweetSearchFragment;
    }

    @Override
    public void fetchTimeline() {
        String query = getArguments().getString("query");
        client.searchTweets(query, currMaxId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                hideRefreshControl();
                JSONArray jsonArray= jsonObject.optJSONArray("statuses");
                Log.d("DEBUG", "search: " + jsonArray.toString());
                List<Tweet> newTweets = Tweet.fromJSONArray(jsonArray);
                processFetchedTweets(newTweets);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                hideRefreshControl();
                Log.e(ERROR, "Error fetching tweets from search: " + (errorResponse == null ? "Uknown error" : errorResponse.toString()));
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

    @Override
    public void fetchOffline() {
        List<Tweet> newTweets = Tweet.recentItems(currMaxId);
        processFetchedTweets(newTweets);

    }

    private void processFetchedTweets(List<Tweet> newTweets) {
        retryCount = 0;
        int newSize = newTweets.size();
        if (newSize > 0) {
            currMaxId = newTweets.get(newSize-1).getUid()-1;
        }
        addAll(newTweets);
        handler.removeCallbacks(fetchRunnable);
    }



    public void postTweet() {
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
    }
}
