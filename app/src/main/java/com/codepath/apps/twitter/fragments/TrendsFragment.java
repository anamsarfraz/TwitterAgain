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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.adapters.TrendsArrayAdapter;
import com.codepath.apps.twitter.databinding.FragmentTrendsBinding;
import com.codepath.apps.twitter.models.Trend;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.util.Connectivity;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static java.util.Collections.addAll;

public class TrendsFragment extends Fragment {
    public static final String DEBUG = "DEBUG";
    public static final String ERROR = "ERROR";
    private static final int RATE_LIMIT_ERR = 88;
    private static final int RETRY_LIMIT = 3;
    private static final long DELAY_MILLI = 3000;

    List<Trend> trends;
    int retryCount;
    Handler handler;
    TwitterClient client;
    TrendsArrayAdapter trendsArrayAdapter;
    LinearLayoutManager linearLayoutManager;
    private OnTrendClickListener trendClickListener;
    private FragmentTrendsBinding binding;
    final Runnable fetchRunnable = new Runnable() {

        @Override
        public void run() {
            fetchTrends();
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
        client = TwitterApplication.getRestClient();
        trends = new ArrayList<>();
        trendsArrayAdapter = new TrendsArrayAdapter(getActivity(), trends);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trends, container, false);

        trendClickListener = (OnTrendClickListener) getActivity();
        setUpRecycleView();
        setUpRefreshControl();
        setUpClickListeners();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       // fetch trends
        fetchTrends();
    }

    private void setUpRecycleView() {
        binding.rvTrends.setAdapter(trendsArrayAdapter);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        binding.rvTrends.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(),
                LinearLayoutManager.VERTICAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dividerItemDecoration.setDrawable(getActivity().getDrawable(R.drawable.line_divider));
        }
        binding.rvTrends.addItemDecoration(dividerItemDecoration);
    }

    private void setUpRefreshControl() {
        binding.scTrends.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTrends();
            }
        });
        // Configure the refreshing colors
        binding.scTrends.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void setUpClickListeners() {


        trendsArrayAdapter.setOnItemClickListener(new TrendsArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                trendClickListener.onSearchQuery(trends.get(position).getName());

            }
        });
    }


    public void hideRefreshControl() {
        if (binding.scTrends.isRefreshing()) {
            binding.scTrends.setRefreshing(false);
        }
    }

    public void fetchTrends() {
        client.getTrends(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                hideRefreshControl();
                Log.d("DEBUG", "trends: " + jsonArray.toString());
                List<Trend> newTrends = Trend.fromJSONArray(jsonArray.optJSONObject(0).optJSONArray("trends"));
                processFetchedTrends(newTrends);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                hideRefreshControl();
                Log.e(ERROR, "Error fetching trends: " + (errorResponse == null ? "Uknown error" : errorResponse.toString()));
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
        List<Trend> newTrends = Trend.getTrends();
        processFetchedTrends(newTrends);

    }

    private void processFetchedTrends(List<Trend> newTrends) {
        retryCount = 0;
        trendsArrayAdapter.clearItems();
        trends.addAll(newTrends);
        trendsArrayAdapter.notifyItemRangeChanged(0, newTrends.size());
        handler.removeCallbacks(fetchRunnable);
    }

    public void postTweet() {
        client.postTweet(/*tweets.get(0).getBody()*/"Random text. TO DO FIX", null, new JsonHttpResponseHandler() {
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

    public interface OnTrendClickListener {
        public void onSearchQuery(String trendName);
    }
}
