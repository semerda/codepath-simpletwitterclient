package com.codepath.apps.simpletwitterclient.net;

import android.content.Context;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

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
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "STHDRqJx3Q8HDCSZNc4irQoN5";       // Change this
	public static final String REST_CONSUMER_SECRET = "CytArPIjDE8ef9nXO3RWFMXd12pn2oHUsHUvLAlRMDn1zKuiJd"; // Change this
	public static final String REST_CALLBACK_URL = "oauth://cpsimpletwitterclient"; // Change this (here and in manifest)

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

    // HomeTimeLine - Gets us the home timeline
    // GET statuses/home_timeline.json
    //      count=25
    //      since_id=1 (most recent tweets)
    public void getHomeTimeline(int page, AsyncHttpResponseHandler handler) {
        // [] = JSONArray (ROOT)
        // {} = JSONObject
        // Ref response: https://dev.twitter.com/rest/reference/get/statuses/home_timeline
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        // Specify the params
        RequestParams params = new RequestParams();
        params.put("count", "25");
        params.put("since_id", "1");
        // Execute the request
        getClient().get(apiUrl, params, handler);
    }

    // ComposeTweet
    public void composeTweet(String status, AsyncHttpResponseHandler handler) {
        // {} = JSONObject (ROOT)
        // Ref response: https://dev.twitter.com/rest/reference/post/statuses/update
        // Eg. https://api.twitter.com/1.1/statuses/update.json?status=ABC
        String apiUrl = getApiUrl("statuses/update.json");
        // Specify the params
        RequestParams params = new RequestParams();
        params.put("status", status);
        // Execute the request
        getClient().post(apiUrl, params, handler);
    }

    // Get Logged in User Account
    public void getAccountCredentials(AsyncHttpResponseHandler handler) {
        // {} = JSONObject (ROOT)
        // Ref response: https://dev.twitter.com/rest/reference/get/account/verify_credentials
        // Eg. https://api.twitter.com/1.1/account/verify_credentials.json
        String apiUrl = getApiUrl("account/verify_credentials.json");
        // Execute the request
        getClient().get(apiUrl, null, handler);
    }

    public void retweetStatus(long tweetId, AsyncHttpResponseHandler handler) {
        // https://dev.twitter.com/rest/reference/post/statuses/retweet/%3Aid
        String apiUrl = getApiUrl(String.format("statuses/retweet/%s.json", tweetId));
        // Specify the params
        RequestParams params = new RequestParams();
        params.put("id", tweetId);
        // Execute the request
        getClient().post(apiUrl, params, handler);
    }

    public void favoriteStatus(long tweetId, AsyncHttpResponseHandler handler) {
        // https://dev.twitter.com/rest/reference/post/favorites/create
        String apiUrl = getApiUrl(String.format("favorites/create.json?id=%s", tweetId));
        // Specify the params
        RequestParams params = new RequestParams();
        params.put("id", tweetId);
        // Execute the request
        getClient().post(apiUrl, params, handler);
    }

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */
}