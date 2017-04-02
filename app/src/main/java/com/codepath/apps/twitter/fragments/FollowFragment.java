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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.adapters.FollowArrayAdapter;
import com.codepath.apps.twitter.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitter.databinding.FragmentFollowBinding;
import com.codepath.apps.twitter.databinding.FragmentTweetsListBinding;
import com.codepath.apps.twitter.models.Follow;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Connectivity;
import com.codepath.apps.twitter.util.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitter.util.OnTweetClickListener;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.media.CamcorderProfile.get;
import static com.codepath.apps.twitter.R.string.tweet;


public class FollowFragment extends Fragment {

    public static final String DEBUG = "DEBUG";
    public static final String ERROR = "ERROR";
    private static final int RATE_LIMIT_ERR = 88;
    private static final int RETRY_LIMIT = 3;
    private static final long DELAY_MILLI = 3000;

    List<Follow> follows;
    long nextCursor;
    int retryCount;
    int currPosition;
    FollowArrayAdapter followArrayAdapter;
    LinearLayoutManager linearLayoutManager;
    private FragmentFollowBinding binding;
    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;
    TwitterClient client;
    Handler handler;
    boolean isFollowers;
    String screenName;
    final Runnable fetchRunnable = new Runnable() {

        @Override
        public void run() {
            fetchFollows();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = TwitterApplication.getRestClient();
        follows = new ArrayList<>();
        followArrayAdapter = new FollowArrayAdapter(getActivity(), follows);
        handler = new Handler();
        isFollowers = getArguments().getBoolean("get_followers");
        screenName = getArguments().getString("screen_name");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_follow, container, false);

        setUpRecycleView();
        setUpRefreshControl();
        setUpScrollListeners();
        setUpClickListeners();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // fetch followers/following on first load
        beginNewSearch();
    }

    public static FollowFragment newInstance(boolean isFollowers, String screeName) {

        Bundle args = new Bundle();

        FollowFragment followFragment = new FollowFragment();
        args.putBoolean("get_followers", isFollowers);
        args.putString("screen_name", screeName);

        followFragment.setArguments(args);
        return followFragment;
    }

    private void setUpRecycleView() {
        binding.rvFollow.setAdapter(followArrayAdapter);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        binding.rvFollow.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(),
                LinearLayoutManager.VERTICAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dividerItemDecoration.setDrawable(getActivity().getDrawable(R.drawable.line_divider));
        }
        binding.rvFollow.addItemDecoration(dividerItemDecoration);
    }

    private void setUpRefreshControl() {
        binding.scFollow.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                beginNewSearch();
            }
        });
        // Configure the refreshing colors
        binding.scFollow.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void setUpScrollListeners() {
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (nextCursor != 0) {
                    loadMore();
                }


            }
        };
        // Adds the scroll listener to RecyclerView
        binding.rvFollow.addOnScrollListener(scrollListener);
    }

    private void setUpClickListeners() {


        followArrayAdapter.setOnItemClickListener(new FollowArrayAdapter.OnItemClickListener() {
            @Override
            public void onImageClick(View imageView, int position) {
                currPosition = position;
                Follow follow = follows.get(position);
                follow.setFollowing(!follow.isFollowing());
                changeItem(follow);
                client.startFollow(follow.isFollowing(), follow.getScreenName(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                       Log.d(DEBUG, "Successfully started following");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e(ERROR, "Error following user: "+errorResponse.toString());
                    }
                });
            }
        });
    }


    public void hideRefreshControl() {
        if (binding.scFollow.isRefreshing()) {
            binding.scFollow.setRefreshing(false);
        }
    }

    public void addItem(Follow follow) {
        follows.add(0, follow);
        followArrayAdapter.notifyItemInserted(0);
    }

    public void changeItem(Follow follow) {
        follows.set(currPosition, follow);
        followArrayAdapter.notifyItemChanged(currPosition);
    }

    public void addAll(List<Follow> newFollows) {
        int curSize = followArrayAdapter.getItemCount();
        follows.addAll(newFollows);
        int newSize = newFollows.size();
        followArrayAdapter.notifyItemRangeInserted(curSize, newSize);
        binding.pbFollow.setVisibility(ProgressBar.INVISIBLE);
    }
    public void removeAll() {
        followArrayAdapter.clearItems();
        scrollListener.resetState();
        hideRefreshControl();
    }


    public void beginNewSearch() {
        nextCursor = -1L;
        removeAll();

        loadMore();

    }

    public void loadMore() {
        binding.pbFollow.setVisibility(ProgressBar.VISIBLE);
        retryCount = 0;
        if (nextCursor > 0) {
            Log.d(DEBUG, "User scrolled. Load additional tweets");
        }
        if (Connectivity.isConnected(getActivity())) {
            fetchFollows();
        } else {
            fetchOffline();
        }
    }

    private void processFetchedFollows(List<Follow> newFollows) {
        retryCount = 0;
        int newSize = newFollows.size();

        addAll(newFollows);
        handler.removeCallbacks(fetchRunnable);
    }

    public void fetchFollows() {

        client.getFollow(isFollowers, nextCursor, screenName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                hideRefreshControl();
                Log.d("DEBUG", "Follow response: " + jsonObject.toString());
                List<Follow> newFollows = Follow.fromJSONArray(jsonObject.optJSONArray("users"));
                nextCursor = jsonObject.optLong("next_cursor");
                processFetchedFollows(newFollows);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                hideRefreshControl();
                Log.e(ERROR, "Error fetching follows: " + (errorResponse == null ? "Uknown error" : errorResponse.toString()));
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

    }
}
