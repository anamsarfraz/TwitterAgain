package com.codepath.apps.twitter.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.databinding.FragmentUserHeaderBinding;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.FormatUtil;
import org.parceler.Parcels;
import jp.wasabeef.glide.transformations.CropCircleTransformation;


public class UserHeaderFragment extends Fragment {


    private FragmentUserHeaderBinding binding;
    private String FOLLOWERS = "FOLLOWERS";
    private String FOLLOWING = "FOLLOWING";

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
                .load(user.getProfileImageUrl())
                .bitmapTransform(new CropCircleTransformation(getActivity()))
                .placeholder(R.drawable.tweet_social)
                .crossFade()
                .into(binding.ivUserProfile);
        binding.tvUserNameProfile.setText(user.getName());
        binding.tvScreenNameProfile.setText(user.getScreenName());
        binding.tvUserDescription.setText(user.getTagLine());
        binding.tvFollowers.setText(FormatUtil.buildSpan(FormatUtil.format(user.getFollowersCount()), FOLLOWERS));
        binding.tvFollowing.setText(FormatUtil.buildSpan(FormatUtil.format(user.getFollowingCount()), FOLLOWING));
    }
}

