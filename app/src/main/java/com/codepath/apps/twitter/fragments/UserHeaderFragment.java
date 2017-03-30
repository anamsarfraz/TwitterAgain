package com.codepath.apps.twitter.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.activities.TweetDetailActivity;
import com.codepath.apps.twitter.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitter.databinding.FragmentTweetsListBinding;
import com.codepath.apps.twitter.databinding.FragmentUserHeaderBinding;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Connectivity;
import com.codepath.apps.twitter.util.EndlessRecyclerViewScrollListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.codepath.apps.twitter.models.User_Table.screenName;

public class UserHeaderFragment extends Fragment {
    public static final String DEBUG = "DEBUG";


    private FragmentUserHeaderBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_header, container, false);

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
                .load(User.getCurrentUser().getProfileImageUrl())
                .bitmapTransform(new CropCircleTransformation(getActivity()))
                .placeholder(R.drawable.tweet_social)
                .crossFade()
                .into(binding.ivUserProfile);
        binding.tvUserNameProfile.setText(user.getName());
        binding.tvScreenNameProfile.setText(user.getScreenName());
        binding.tvUserDescription.setText(user.getTagLine());
        //binding.tvFollowers.setText(user.getFollowersCount());
        //binding.tvFollowing.setText(user.getFollowingCount());
    }
}

