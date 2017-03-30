package com.codepath.apps.twitter.activities;

import android.databinding.DataBindingUtil;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.databinding.ActivityProfileBinding;
import com.codepath.apps.twitter.fragments.UserHeaderFragment;
import com.codepath.apps.twitter.fragments.UserTimelineFragment;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;

import org.parceler.Parcels;

import static com.codepath.apps.twitter.models.User.getCurrentUser;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        User user= Parcels.unwrap(getIntent().getParcelableExtra("user"));
        setSupportActionBar(binding.tbProfile);
        getSupportActionBar().setTitle(user.getName());
        binding.tbProfile.setTitle("Profile");
        
        // Get screen name

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            UserHeaderFragment userHeaderFragment = UserHeaderFragment.newInstance(user);
            ft.replace(R.id.flUserHeader, userHeaderFragment);
            UserTimelineFragment userTimelineFragment = UserTimelineFragment.newInstance(user.getScreenName());
            // Display user fragment into the activity dynamically. For that we need to put in a
            // container in the layout and load the fragment there
            ft.replace(R.id.flContainer, userTimelineFragment);
            ft.commit();
        }
    }


}
