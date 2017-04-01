package com.codepath.apps.twitter.activities;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.databinding.ActivityTweetDetailBinding;
import com.codepath.apps.twitter.fragments.ComposeFragment;
import com.codepath.apps.twitter.models.Media;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Constants;
import com.codepath.apps.twitter.util.DateUtil;
import com.codepath.apps.twitter.util.FormatUtil;
import com.codepath.apps.twitter.util.PatternEditableBuilder;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.codepath.apps.twitter.R.color.twitter_blue;
import static com.codepath.apps.twitter.util.FormatUtil.buildSpan;
import static com.raizlabs.android.dbflow.config.FlowManager.getContext;


public class TweetDetailActivity extends ComposeActivity {

    private static final int PROFILE_IMG_ROUND = 6;
    private static final int MEDIA_IMG_ROUND = 10;
    private static final String RETWEEETS = "RETWEETS";
    private static final String LIKES = "LIKES";
    private static final String REPLY_TO = "Reply to ";
    private static final int MAX_COUNT = 140;

    public static final String ERROR = "ERROR";


    private ActivityTweetDetailBinding binding;
    Tweet tweet;
    Tweet retweetedStatus;
    String screenName;
    boolean isReply;
    TwitterClient client;
    private String replyStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tweet_detail);
        client = TwitterApplication.getRestClient();

        setSupportActionBar(binding.tbDetail);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation((float)10.0);

        processIntent();
        populateViews();
        setUpClickListeners();

    }

    private void setUpClickListeners() {
        binding.btnExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 int remainingCount = Integer.parseInt(binding.tvCharCountDetail.getText().toString());
                if (remainingCount == MAX_COUNT) {
                    showComposeDialog(screenName);
                } else {
                    showComposeDialog(binding.etReply.getText().toString());
                }

            }
        });

        binding.btnReplyDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int remainingCount = Integer.parseInt(binding.tvCharCountDetail.getText().toString());
                if (remainingCount == MAX_COUNT) {
                    showComposeDialog(screenName);
                } else {
                    showComposeDialog(binding.etReply.getText().toString());
                }

            }
        });

        binding.etReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isReply) {
                    setUpReplyText();
                }

                binding.etReply.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        int currLength = binding.etReply.getText().toString().length();

                        int remainingCount = MAX_COUNT - currLength;
                        binding.tvCharCountDetail.setText(String.format("%d", remainingCount));

                        binding.btnRTextDetail.setEnabled(remainingCount < 0 ? false: true);
                        binding.btnRTextDetail.setTextColor(ContextCompat.getColor(getApplicationContext(), remainingCount < 0 ? android.R.color.darker_gray : android.R.color.holo_blue_light));

                        binding.tvCharCountDetail.setTextColor(ContextCompat.getColor(getApplicationContext(), remainingCount < 0 ? android.R.color.holo_red_dark : android.R.color.darker_gray));



                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (binding.etReply.getText().length() == 0) {
                            binding.etReply.removeTextChangedListener(this);
                            binding.etReply.setText(replyStr);
                            binding.etReply.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                            binding.etReply.addTextChangedListener(this);
                        }

                    }

                });

            }
        });
    }

    private void setUpReplyText() {
        binding.etReply.setText(screenName);
        binding.etReply.setSelection(screenName.length());
        binding.etReply.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
        binding.tvCharCountDetail.setText(String.format("%d", MAX_COUNT-screenName.length()));
        binding.tvCharCountDetail.setVisibility(View.VISIBLE);
        binding.btnRTextDetail.setVisibility(View.VISIBLE);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    private void populateViews() {
        Tweet tweetDetail = retweetedStatus == null ? tweet : retweetedStatus;
        String origUserName;
        // Load images
        Glide.with(this)
                .load(tweetDetail.getUser().getProfileImageUrl())
                .bitmapTransform(new RoundedCornersTransformation(this, PROFILE_IMG_ROUND, 0))
                .placeholder(R.drawable.tweet_social)
                .crossFade()
                .into(binding.ivProfileDetailImage);

        // Check if multimedia image is available
        Media media = tweetDetail.getMedia();
        if (media != null) {
            binding.ivDetailMultiMedia.setVisibility(View.VISIBLE);
            Glide
                    .with(this)
                    .load(media.getImageUrl())
                    .bitmapTransform(new RoundedCornersTransformation(this, MEDIA_IMG_ROUND, 0))
                    .placeholder(R.drawable.tweet_social)
                    .crossFade()
                    .into(binding.ivDetailMultiMedia);
            if (media.getType().equals(Constants.VIDEO_STR)) {
                Log.d("DEBUG", String.format("Got Video Url for tweet: %s", media.getVideoUrl()));
            }
        } else {
            binding.ivDetailMultiMedia.setVisibility(View.GONE);
        }

        if (retweetedStatus != null) {
            binding.ivRetweetStatusDetail.setVisibility(View.VISIBLE);
            binding.tvOrigUserNameDetail.setVisibility(View.VISIBLE);
            origUserName = tweet.getUser().getName();
            if (origUserName.equals(User.getCurrentUser().getName())) {
                origUserName = Constants.YOU;
            }
            binding.tvOrigUserNameDetail.setText(String.format("%s Retweeted", origUserName));
        }

        binding.tvDetailUserName.setText(tweetDetail.getUser().getName());
        binding.tvDetailScreenName.setText(String.format("%s%s", Constants.ATRATE, tweetDetail.getUser().getScreenName()));
        binding.tvDetailBody.setText(tweetDetail.getBody());
        new PatternEditableBuilder().
                addPattern(Pattern.compile("\\@(\\w+)"),
                        ContextCompat.getColor(getContext(), R.color.twitter_blue),
                        new PatternEditableBuilder.SpannableClickedListener() {
                            @Override
                            public void onSpanClicked(String text) {
                                client.getUserInfo(text, new JsonHttpResponseHandler() {
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                        intent.putExtra("user", Parcels.wrap(new User(jsonObject)));
                                        Bundle animationBundle =
                                                ActivityOptions.makeCustomAnimation(getContext(), R.anim.slide_from_left,R.anim.slide_to_left).toBundle();
                                        startActivity(intent, animationBundle);
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
                                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                                intent.putExtra("query", text);
                                Bundle animationBundle =
                                        ActivityOptions.makeCustomAnimation(getContext(), R.anim.slide_from_left,R.anim.slide_to_left).toBundle();
                                startActivity(intent, animationBundle);
                            }
                        }).into(binding.tvDetailBody);

        if (tweetDetail.isFavorited()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.btnLikeDetail.setBackground(getDrawable(R.drawable.like_selected));
            }
        }

        binding.tvLikes.setText(FormatUtil.buildSpan(FormatUtil.format(tweetDetail.getFavoriteCount()), LIKES));
        binding.tvRetweets.setText(FormatUtil.buildSpan(FormatUtil.format(tweetDetail.getRetweetCount()), RETWEEETS));
        if (tweetDetail.isRetweeted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.btnRetweetDetail.setBackground(getDrawable(R.drawable.retweet_selected));
            }
        }
        binding.tvDetailDate.setText(DateUtil.getDateTimeInFormat(tweetDetail.getCreatedAt(), "dd MMM yy"));
        binding.tvDetailTime.setText(DateUtil.getDateTimeInFormat(tweetDetail.getCreatedAt(), "h:mm a"));
        binding.tvCharCountDetail.setText(String.format("%d", MAX_COUNT));

        if (isReply) {
            setUpReplyText();
        } else {
            binding.etReply.setText(replyStr);
        }
    }

    private void processIntent() {
        Intent intent = getIntent();
        tweet = Parcels.unwrap(intent.getParcelableExtra("tweet"));
        retweetedStatus = tweet.getRetweetedStatus();
        StringBuilder screenNamePH = new StringBuilder();
        if (retweetedStatus != null) {
            screenNamePH.append(
                    String.format("%s%s ", Constants.ATRATE, retweetedStatus.getUser().getScreenName()));
        }

        screenNamePH.append(String.format("%s%s ", Constants.ATRATE, tweet.getUser().getScreenName()));
        screenName = screenNamePH.toString();

        StringBuilder replyPlaceHolder = new StringBuilder();
        replyPlaceHolder.append(REPLY_TO);
        if (retweetedStatus != null) {
            replyPlaceHolder.append(String.format("%s and ", retweetedStatus.getUser().getName()));
        }
        replyPlaceHolder.append(tweet.getUser().getName());
        replyStr = replyPlaceHolder.toString();

        isReply = intent.getBooleanExtra("is_reply", false);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void createTweet(Tweet tweet) {
        postTweet(tweet);
    }

    private void postTweet(Tweet tweet) {
        client.postTweet(tweet.getBody(), this.tweet.getIdStr(), new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                Tweet.saveTweet(jsonObject);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(ERROR, "Error creating tweet: " + errorResponse.toString());

            }
        });
    }
}
