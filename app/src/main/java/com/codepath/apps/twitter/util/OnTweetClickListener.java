package com.codepath.apps.twitter.util;

import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;


public interface OnTweetClickListener {
    public void onItemClick(Tweet tweet);
    public void onViewClick(User user);
    public void onHashTagClick(String hashTag);
    public void onReplyClick(Tweet tweet);
    public void onMessageClick(Tweet tweet);
}