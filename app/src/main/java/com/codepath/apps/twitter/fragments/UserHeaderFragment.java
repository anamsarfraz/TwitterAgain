package com.codepath.apps.twitter.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.databinding.FragmentUserHeaderBinding;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.FormatUtil;
import com.codepath.apps.twitter.util.OnTweetClickListener;
import com.codepath.apps.twitter.util.PatternEditableBuilder;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.CropCircleTransformation;


public class UserHeaderFragment extends Fragment {

    public static final String ERROR = "ERROR";
    private FragmentUserHeaderBinding binding;
    private String FOLLOWERS = "FOLLOWERS";
    private String FOLLOWING = "FOLLOWING";
    private OnTweetClickListener tweetClickListener;
    TwitterClient client;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = TwitterApplication.getRestClient();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_header, container, false);

        tweetClickListener = (OnTweetClickListener) getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateUserHeader();
    }

    public static UserHeaderFragment newInstance(User user) {

        Bundle args = new Bundle();

        UserHeaderFragment userHeaderFragment = new UserHeaderFragment();
        args.putParcelable("user", Parcels.wrap(user));

        userHeaderFragment.setArguments(args);
        return userHeaderFragment;
    }

    private void populateUserHeader() {
        User user = Parcels.unwrap(getArguments().getParcelable("user"));
        Glide.with(this)
                .load(user.getProfileImageUrl())
                .bitmapTransform(new CropCircleTransformation(getActivity()))
                .placeholder(R.drawable.tweet_social)
                .crossFade()
                .into(binding.ivUserProfile);
        binding.tvUserNameProfile.setText(user.getName());
        binding.tvScreenNameProfile.setText(user.getScreenName());
        binding.tvUserDescription.setText(user.getTagLine());
        new PatternEditableBuilder().
                addPattern(Pattern.compile("\\@(\\w+)"),
                        ContextCompat.getColor(getContext(), R.color.twitter_blue),
                        new PatternEditableBuilder.SpannableClickedListener() {
                            @Override
                            public void onSpanClicked(String text) {
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
                        }).
                addPattern(Pattern.compile("\\#(\\w+)"),
                        ContextCompat.getColor(getContext(), R.color.twitter_blue),
                        new PatternEditableBuilder.SpannableClickedListener() {
                            @Override
                            public void onSpanClicked(String text) {
                                tweetClickListener.onHashTagClick(text);
                            }
                        }).into(binding.tvUserDescription);
        binding.tvFollowers.setText(FormatUtil.buildSpan(FormatUtil.format(user.getFollowersCount()), FOLLOWERS));
        binding.tvFollowing.setText(FormatUtil.buildSpan(FormatUtil.format(user.getFollowingCount()), FOLLOWING));
    }
}

