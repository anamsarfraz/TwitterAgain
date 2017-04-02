package com.codepath.apps.twitter.activities;

import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.codepath.apps.twitter.fragments.ComposeFragment;
import com.codepath.apps.twitter.fragments.ConfirmationFragment;
import com.codepath.apps.twitter.models.Draft;
import com.codepath.apps.twitter.models.Tweet;

import static com.codepath.apps.twitter.R.id.etCompose;

public abstract class ComposeActivity extends AppCompatActivity implements ComposeFragment.OnComposeListener, ConfirmationFragment.UpdateDraftDialogListener {

    ComposeFragment composeFragment;
    FragmentManager fm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fm = getSupportFragmentManager();
    }

    public void showComposeDialog(String shareContent, boolean isMessage) {
        composeFragment = ComposeFragment.newInstance(shareContent, isMessage);
        composeFragment.show(fm, "fragment_compose");
    }

    @Override
    public void onConfirmUpdateDialog(int position) {
        composeFragment.handleConfirmation(position);

    }

    @Override
    public abstract void createTweet(Tweet tweet);

    @Override
    public void cancelTweet() {
        Log.d("DEBUG", "show confirmation dialog");
        fm = getSupportFragmentManager();
        ConfirmationFragment confirmationFragment = ConfirmationFragment.newInstance("Save draft?");
        confirmationFragment.show(fm, "fragment_confirmation");
    }
}
