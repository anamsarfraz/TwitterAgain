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
import android.widget.Toast;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.adapters.FollowArrayAdapter;
import com.codepath.apps.twitter.adapters.UserArrayAdapter;
import com.codepath.apps.twitter.databinding.FragmentFollowBinding;
import com.codepath.apps.twitter.databinding.FragmentUserSearchBinding;
import com.codepath.apps.twitter.models.Follow;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Connectivity;
import com.codepath.apps.twitter.util.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class UserSearchFragment extends Fragment {

    public static final String DEBUG = "DEBUG";
    public static final String ERROR = "ERROR";
    private static final int RATE_LIMIT_ERR = 88;
    private static final int RETRY_LIMIT = 3;
    private static final long DELAY_MILLI = 3000;

    List<User> users;
    int page;
    int retryCount;
    int currPosition;
    UserArrayAdapter userArrayAdapter;
    LinearLayoutManager linearLayoutManager;
    private FragmentUserSearchBinding binding;
    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;
    TwitterClient client;
    Handler handler;
    String query;
    final Runnable fetchRunnable = new Runnable() {

        @Override
        public void run() {
            fetchUsers();
        }
    };
    private OnUserClickListener onUserClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = TwitterApplication.getRestClient();
        users = new ArrayList<>();
        userArrayAdapter = new UserArrayAdapter(getActivity(), users);
        handler = new Handler();
        query = getArguments().getString("query");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_search, container, false);

        onUserClickListener = (OnUserClickListener) getActivity();
        setUpRecycleView();
        setUpRefreshControl();
        setUpScrollListeners();
        setUpClickListeners();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // fetch following users on first load
        fetchFollowers();
    }

    public void fetchFollowers() {
        client.getFollow(true, -1, User.getCurrentUser().getScreenName(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                hideRefreshControl();
                Log.d("DEBUG", "Follow response: " + jsonObject.toString());
                List<User> newUsers = User.fromJSONArray(jsonObject.optJSONArray("users"));
                processFetchedUsers(newUsers);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                hideRefreshControl();
                Log.e(ERROR, "Error fetching followers: " + (errorResponse == null ? "Uknown error" : errorResponse.toString()));
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

    public static UserSearchFragment newInstance(String query) {

        Bundle args = new Bundle();

        UserSearchFragment userSearchFragment = new UserSearchFragment();
        args.putString("query", query);

        userSearchFragment.setArguments(args);
        return userSearchFragment;
    }

    private void setUpRecycleView() {
        binding.rvUser.setAdapter(userArrayAdapter);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        binding.rvUser.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(),
                LinearLayoutManager.VERTICAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dividerItemDecoration.setDrawable(getActivity().getDrawable(R.drawable.line_divider));
        }
        binding.rvUser.addItemDecoration(dividerItemDecoration);
    }

    private void setUpRefreshControl() {
        binding.scUser.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                beginNewSearch();
            }
        });
        // Configure the refreshing colors
        binding.scUser.setColorSchemeResources(android.R.color.holo_blue_bright,
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
        binding.rvUser.addOnScrollListener(scrollListener);
    }

    private void setUpClickListeners() {
        userArrayAdapter.setOnItemClickListener(new UserArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View imageView, int position) {
                currPosition = position;
                final User user = users.get(position);
                client.getRelationship(User.getCurrentUser().getScreenName(), user.getScreenName(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                        Log.d("DEBUG", "User relationship response: " + jsonObject.toString());
                        boolean canDM = jsonObject.optJSONObject("relationship").optJSONObject("source").optBoolean("can_dm");
                        if (canDM) {
                            onUserClickListener.processUser(user);
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    "Sorry you cannot message this account.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e(ERROR, "Error fetching relationship: " + (errorResponse == null ? "Uknown error" : errorResponse.toString()));
                    }
                });
            }
        });
    }


    public void hideRefreshControl() {
        if (binding.scUser.isRefreshing()) {
            binding.scUser.setRefreshing(false);
        }
    }

    public void addItem(User user) {
        users.add(0, user);
        userArrayAdapter.notifyItemInserted(0);
    }

    public void changeItem(User user) {
        users.set(currPosition, user);
        userArrayAdapter.notifyItemChanged(currPosition);
    }

    public void addAll(List<User> newUsers) {
        int curSize = userArrayAdapter.getItemCount();
        users.addAll(newUsers);
        int newSize = newUsers.size();
        userArrayAdapter.notifyItemRangeInserted(curSize, newSize);
        binding.pbUser.setVisibility(ProgressBar.INVISIBLE);
    }
    public void removeAll() {
        userArrayAdapter.clearItems();
        scrollListener.resetState();
        hideRefreshControl();
    }


    public void beginNewSearch(String query) {
        this.query = query;
        beginNewSearch();
    }

    public void beginNewSearch() {
        page = 0;
        removeAll();

        loadMore();

    }

    public void loadMore() {
        binding.pbUser.setVisibility(ProgressBar.VISIBLE);
        retryCount = 0;
        if (page > 0) {
            Log.d(DEBUG, "User scrolled. Load additional tweets");
        }
        if (Connectivity.isConnected(getActivity())) {
            fetchUsers();
        } else {
            fetchOffline();
        }
    }

    private void processFetchedUsers(List<User> newUsers) {
        retryCount = 0;
        int newSize = newUsers.size();

        addAll(newUsers);
        handler.removeCallbacks(fetchRunnable);
    }

    public void fetchUsers() {

        client.searchUsers(page, query, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                hideRefreshControl();
                Log.d("DEBUG", "User search response: " + jsonArray.toString());
                List<User> newUsers = User.fromJSONArray(jsonArray);
                page ++;
                processFetchedUsers(newUsers);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                hideRefreshControl();
                Log.e(ERROR, "Error fetching users: " + (errorResponse == null ? "Uknown error" : errorResponse.toString()));
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

    public interface OnUserClickListener {
        public void processUser(User user);
    }
}
