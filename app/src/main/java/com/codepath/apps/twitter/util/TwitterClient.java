package com.codepath.apps.twitter.util;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
	public static final String REST_URL = "https://api.twitter.com/1.1";
	public static final String REST_CONSUMER_KEY = "4MYbq0IOFXCJg4lHMlcu23nfO";
	public static final String REST_CONSUMER_SECRET = "dFIDL9hl35cqdt7WeDANX45h7ythP0X9MIcHVYoEdrvBxuYGMS";
	public static final String REST_CALLBACK_URL = "oauth://cptweetclient.android";

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	public void getLoggedInUser(AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("account/verify_credentials.json");
		getClient().get(apiUrl, handler);

	}
	public void getHomeTimeline(long maxId, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		RequestParams params = new RequestParams();
        params.put("count", Constants.MAX_TWEET_COUNT);
		if (maxId > 0) {
			params.put("max_id", maxId);
		}

		getClient().get(apiUrl, params, handler);
	}

	public void postTweet(String body, String replyId, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		params.put("status", body);
		if (replyId != null) {
			params.put("in_reply_to_status_id", replyId);
		}
		getClient().post(apiUrl, params, handler);
	}

    public void getMentionsTimeline(long maxId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/mentions_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", Constants.MAX_TWEET_COUNT);
		if (maxId > 0) {
			params.put("max_id", maxId);
		}

		getClient().get(apiUrl, params, handler);
	}

	public void getUserTimeline(String screenName, long maxId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/user_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", Constants.MAX_TWEET_COUNT);
		params.put("screen_name", screenName);
		if (maxId > 0) {
			params.put("max_id", maxId);
		}

		getClient().get(apiUrl, params, handler);
	}

}
