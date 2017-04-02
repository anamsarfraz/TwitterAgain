package com.codepath.apps.twitter.fragments;

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
import android.widget.Toast;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitter.databinding.FragmentTweetsListBinding;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Connectivity;
import com.codepath.apps.twitter.util.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitter.util.OnTweetClickListener;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public abstract class TweetsListFragment extends Fragment {

    public static final String DEBUG = "DEBUG";
    public static final String ERROR = "ERROR";

    List<Tweet> tweets;
    long currMaxId;
    int retryCount;
    int currPosition;
    TweetsArrayAdapter tweetsArrayAdapter;
    LinearLayoutManager linearLayoutManager;
    private FragmentTweetsListBinding binding;
    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;
    private OnTweetClickListener tweetClickListener;
    TwitterClient client;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = TwitterApplication.getRestClient();
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
                currPosition = position;
                tweetClickListener.onItemClick(tweets.get(position));
            }

            @Override
            public void onImageClick(View imageView, int position) {
                currPosition = position;
                tweetClickListener.onViewClick(tweets.get(position).getUser());
            }

            @Override
            public void onTextClick(String text, boolean isSearch) {
                if (isSearch) {
                    tweetClickListener.onHashTagClick(text);
                } else {
                    client.getUserInfo(text, new JsonHttpResponseHandler() {
                        public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                             tweetClickListener.onViewClick(new User(jsonObject));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(ERROR, "Error getting user info: " + errorResponse.toString());

                        }
                    });
                }

            }

            @Override
            public void onReplyClick(View replyBtn, int position) {
                currPosition = position;
                tweetClickListener.onReplyClick(tweets.get(position));
            }

            @Override
            public void onRetweetClick(View retweetBtn, final int position) {
                currPosition = position;
                Tweet tweetChanged = tweets.get(position);
                if (tweetChanged.getRetweetedStatus() != null) {
                    tweetChanged = tweetChanged.getRetweetedStatus();
                }

                if (tweetChanged.isRetweeted()) {
                    tweetChanged.setRetweeted(false);
                    tweetChanged.setRetweetCount(tweetChanged.getRetweetCount()-1);

                    final Tweet modifiedTweet = tweetChanged;
                    tweetsArrayAdapter.notifyItemChanged(position);
                    client.unretweet(tweetChanged.getIdStr(), new JsonHttpResponseHandler() {
                        public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                            tweets.get(position).delete();
                            tweets.set(position, modifiedTweet);
                            Log.d(DEBUG, jsonObject.toString());
                            tweetsArrayAdapter.notifyItemChanged(position);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(ERROR, "Error UnRetweeting: " + errorResponse.toString());

                        }
                    });
                } else {
                    tweetChanged.setRetweeted(true);
                    tweetChanged.setRetweetCount(tweetChanged.getRetweetCount()+1);
                    tweetsArrayAdapter.notifyItemChanged(position);
                    client.retweet(tweetChanged.getIdStr(), new JsonHttpResponseHandler() {
                        public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                            Tweet newTweet = new Tweet(jsonObject);
                            newTweet.save();
                            tweets.set(position, newTweet);
                            Log.d(DEBUG, jsonObject.toString());
                            tweetsArrayAdapter.notifyItemChanged(position);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(ERROR, "Error Retweeting: " + errorResponse.toString());

                        }
                    });
                }
            }

            @Override
            public void onLikeClick(View likeBtn, final int position) {
                currPosition = position;
                Tweet tweetChanged = tweets.get(position);
                if (tweetChanged.getRetweetedStatus() != null) {
                    tweetChanged = tweetChanged.getRetweetedStatus();
                }

                if (tweetChanged.isFavorited()) {
                    tweetChanged.setFavorited(false);
                    tweetChanged.setFavoriteCount(tweetChanged.getFavoriteCount()-1);
                    tweetsArrayAdapter.notifyItemChanged(position);
                    final Tweet modifiedTweet = tweetChanged;
                    client.unfavorite(tweetChanged.getIdStr(), new JsonHttpResponseHandler() {
                        public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                            Tweet newTweet = new Tweet(jsonObject);
                            modifiedTweet.setFavoriteCount(newTweet.getFavoriteCount());
                            Log.d(DEBUG, jsonObject.toString());
                            tweetsArrayAdapter.notifyItemChanged(position);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(ERROR, "Error UnFavoriting: " + errorResponse.toString());

                        }
                    });
                } else {
                    tweetChanged.setFavorited(true);
                    tweetChanged.setFavoriteCount(tweetChanged.getFavoriteCount()+1);
                    tweetsArrayAdapter.notifyItemChanged(position);
                    final Tweet modifiedTweet = tweetChanged;
                    client.favorite(tweetChanged.getIdStr(), new JsonHttpResponseHandler() {
                        public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                            Tweet newTweet = new Tweet(jsonObject);
                            modifiedTweet.setFavoriteCount(newTweet.getFavoriteCount());
                            Log.d(DEBUG, jsonObject.toString());
                            tweetsArrayAdapter.notifyItemChanged(position);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(ERROR, "Error Favoriting: " + errorResponse.toString());

                        }
                    });
                }
            }

            @Override
            public void onMessageClick(View messageBtn, int position) {
                currPosition = position;
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

    public void changeItem(Tweet tweet) {
        tweets.set(currPosition, tweet);
        tweetsArrayAdapter.notifyItemChanged(currPosition);
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
    }


    public void beginNewSearch() {
        currMaxId = 0L;
        removeAll();

        loadMore();

    }

    public void loadMore() {
        binding.pbLoading.setVisibility(ProgressBar.VISIBLE);
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
}
