package com.codepath.apps.simpletwitterclient.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.simpletwitterclient.AssetsClass;
import com.codepath.apps.simpletwitterclient.R;
import com.codepath.apps.simpletwitterclient.TwitterApplication;
import com.codepath.apps.simpletwitterclient.adapters.TweetsAdapter;
import com.codepath.apps.simpletwitterclient.fragments.ComposeTweetDialogFragment;
import com.codepath.apps.simpletwitterclient.listeners.EndlessRecyclerViewScrollListener;
import com.codepath.apps.simpletwitterclient.models.Tweet;
import com.codepath.apps.simpletwitterclient.net.NetworkClass;
import com.codepath.apps.simpletwitterclient.net.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    private ArrayList<Tweet> tweets;
    private TweetsAdapter tweetsAdapter;
    private RecyclerView rvTweets;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setLogo(R.drawable.twitter_logo_bluebg_50px);
        myToolbar.setTitle(R.string.BLANK);
        setSupportActionBar(myToolbar);

        if (!NetworkClass.isNetworkAvailable(this) && !NetworkClass.isInternetConnected()) {
            //Toast.makeText(this, "Internet appears to be down. This app needs internet to function.", Toast.LENGTH_SHORT).show();
            Snackbar.make(findViewById(R.id.content), "Internet appears to be down. This app needs internet to function.", Snackbar.LENGTH_LONG).
                    setAction("Action", null).show();
        } else {
            setupView();
        }
    }

    public void setupView() {
        // Create the arraylist (data source)
        tweets = new ArrayList<>();
        // Construct the adapter from data source
        tweetsAdapter = new TweetsAdapter(this, tweets);
        // Connect adapter to list view
        rvTweets = (RecyclerView) findViewById(R.id.rvTweets);
        rvTweets.setAdapter(tweetsAdapter);
        // Set layout manager to position the items
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setItemAnimator(new SlideInUpAnimator());
        // http://guides.codepath.com/android/Using-the-RecyclerView#performance
        //rvTweets.setHasFixedSize(true);

        // Endless scrolling
        // Ref: https://github.com/codepath/android_guides/wiki/Endless-Scrolling-with-AdapterViews-and-RecyclerView
        rvTweets.addOnScrollListener(new EndlessRecyclerViewScrollListener(new LinearLayoutManager(this)) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Toast.makeText(TimelineActivity.this, "Loading page " + page, Toast.LENGTH_SHORT).show();
                try {
                    populateTimeline(page);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Clear the existing list
                tweetsAdapter.clearData();
                // Refresh the list from API call
                try {
                    populateTimeline(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        /*
        // Make sure we can click on each item to see more detail about the article
        rvTweets.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, float x, float y) {
                // Get the article to display
                Tweet tweet = tweets.get(position);
                //Log.d("DEBUG", String.valueOf(tweet));

                FragmentManager fm = getSupportFragmentManager();
                DetailTweetDialogFragment detailTweetDialogFragment = DetailTweetDialogFragment.newInstance("", tweet);
                detailTweetDialogFragment.show(fm, "fragment_detail_tweet");
            }
        }));
        */

        // Compose a Tweet
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabComposeTweet);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                FragmentManager fm = getSupportFragmentManager();
                ComposeTweetDialogFragment composeTweetDialogFragment = ComposeTweetDialogFragment.newInstance(getString(R.string.compose_a_tweet), null);
                composeTweetDialogFragment.show(fm, "fragment_compose_tweet");
            }
        });

        // Get the client using Singleton client
        client = TwitterApplication.getRestClient();
        // Show timeline on device
        try {
            populateTimeline(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendRefreshRequest() {
        // Clear the existing list
        tweetsAdapter.clearData();
        try {
            // Refresh the list from API call
            populateTimeline(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Send an API request to get the timeline json
    // Fill the listview by creating the tweet object from the json
    public void populateTimeline(int page) throws JSONException {
        boolean loadFromLocalStore = false;
        if (loadFromLocalStore) {
            // Kept on hitting Twitter's rate limiter so this is a temporary solution
            // while I finish off the rest of the app and mess with the XML UI.
            AssetsClass assets = new AssetsClass(this);
            JSONArray json = assets.loadJSONFromAsset();

            Integer start_index = tweets.size();
            tweets.addAll(Tweet.fromJSONArray(json));
            tweetsAdapter.notifyItemRangeInserted(start_index, tweets.size());

            // Now we call setRefreshing(false) to signal refresh has finished
            swipeContainer.setRefreshing(false);

            Log.d("DEBUG", String.valueOf(Tweet.getCountOfTweets()));
        } else {
            client.getHomeTimeline(page, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                    Log.d("DEBUG", json.toString());

                    Integer start_index = tweets.size();
                    tweets.addAll(Tweet.fromJSONArray(json));
                    tweetsAdapter.notifyItemRangeInserted(start_index, tweets.size());
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG", errorResponse.toString());
                }
            });
        }
    }
}
