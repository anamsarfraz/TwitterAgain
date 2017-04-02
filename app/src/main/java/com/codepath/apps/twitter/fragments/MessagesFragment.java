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
import com.codepath.apps.twitter.adapters.MessagesArrayAdapter;
import com.codepath.apps.twitter.databinding.FragmentMessagesBinding;
import com.codepath.apps.twitter.models.Message;

import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MessagesFragment extends Fragment {
    public static final String DEBUG = "DEBUG";
    public static final String ERROR = "ERROR";
    private static final int RATE_LIMIT_ERR = 88;
    private static final int RETRY_LIMIT = 3;
    private static final long DELAY_MILLI = 3000;

    List<Message> messages;
    int retryCount;
    int currMaxId;
    Handler handler;
    TwitterClient client;
    MessagesArrayAdapter messagesArrayAdapter;
    LinearLayoutManager linearLayoutManager;
    private FragmentMessagesBinding binding;
    final Runnable fetchRunnable = new Runnable() {

        @Override
        public void run() {
            fetchMessages();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        client = TwitterApplication.getRestClient();
        messages = new ArrayList<>();
        messagesArrayAdapter = new MessagesArrayAdapter(getActivity(), messages);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_messages, container, false);
        setUpRecycleView();
        setUpRefreshControl();
        //setUpClickListeners();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       // fetch messages
        currMaxId = 0;
        fetchMessages();
    }

    private void setUpRecycleView() {
        binding.rvMessages.setAdapter(messagesArrayAdapter);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        binding.rvMessages.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(),
                LinearLayoutManager.VERTICAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dividerItemDecoration.setDrawable(getActivity().getDrawable(R.drawable.line_divider));
        }
        binding.rvMessages.addItemDecoration(dividerItemDecoration);
    }

    private void setUpRefreshControl() {
        binding.scMessages.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchMessages();
            }
        });
        // Configure the refreshing colors
        binding.scMessages.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void setUpClickListeners() {


        messagesArrayAdapter.setOnItemClickListener(new MessagesArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                //messageClickListener.onClick();
            }
        });
    }


    public void hideRefreshControl() {
        if (binding.scMessages.isRefreshing()) {
            binding.scMessages.setRefreshing(false);
        }
    }

    public void fetchMessages() {
        client.getMessages(currMaxId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                hideRefreshControl();
                Log.d("DEBUG", "messages: " + jsonArray.toString());
                List<Message> newMessages = Message.fromJSONArray(jsonArray);
                processFetchedTrends(newMessages);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                hideRefreshControl();
                Log.e(ERROR, "Error fetching messages: " + (errorResponse == null ? "Uknown error" : errorResponse.toString()));
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
        List<Message> newMessages = Message.recentItems(0);
        processFetchedTrends(newMessages);

    }

    private void processFetchedTrends(List<Message> newMessages) {
        retryCount = 0;
        messagesArrayAdapter.clearItems();
        messages.addAll(newMessages);
        messagesArrayAdapter.notifyItemRangeChanged(0, newMessages.size());
        handler.removeCallbacks(fetchRunnable);
    }

    public void sendMessage(User user, String message) {
        client.sendMessage(user.getScreenName(), message, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                Toast.makeText(
                        getActivity(),
                        "Your message has been sucessfully sent!",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(
                        getActivity(),
                        "Error sending message. Please try again later.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
