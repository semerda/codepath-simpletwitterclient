package com.codepath.apps.simpletwitterclient.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ernest on 8/1/16.
 *
 * Source of JSON
 * https://dev.twitter.com/rest/reference/get/statuses/home_timeline
 *
 * API Rate Limits
 * https://dev.twitter.com/rest/public/rate-limiting
 */

// Parse the JSON + Store the data, encapsulate state logic or display logic
@Table(name = "Tweets")
public class Tweet extends Model implements Parcelable {

    public static Creator<Tweet> getCREATOR() {
        return CREATOR;
    }

    // This is a regular field
    @Column(name = "TweetId")
    private long tweetId;

    @Column(name = "Body")
    private String body;

    @Column(name = "MediaType")
    private String mediaType;

    @Column(name = "MediaUrl")
    private String mediaUrl;

    @Column(name = "UId")
    private long uid; // unique id for the tweet
    // This is an association to another activeandroid model

    @Column(name = "User", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    private User user; // store embedded user object

    @Column(name = "CreatedAt")
    private String createdAt;

    @Column(name = "FavoriteCount")
    private long favoriteCount;

    @Column(name = "RetweetCount")
    private long retweetCount;

    // Make sure to have a default constructor for every ActiveAndroid model
    public Tweet(){
        super();
    }

    public Tweet(long tweetId, String body, String mediaType, String mediaUrl, long uid, User user, long favoriteCount, long retweetCount, String createdAt){
        super();
        this.tweetId = tweetId;
        this.body = body;
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl;
        this.uid = uid;
        this.user = user;
        this.favoriteCount = favoriteCount;
        this.retweetCount = retweetCount;
        this.createdAt = createdAt;
    }

    public static List<Tweet> getAll(User user) {
        // This is how you execute a query
        return new Select()
                .from(Tweet.class)
                .where("User = ?", user.getId())
                .orderBy("Name ASC")
                .execute();
    }

    public long getTweetId() {
        return tweetId;
    }

    public String getBody() {
        return body;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public long getUid() {
        return uid;
    }

    public User getUser() {
        return user;
    }

    public String getFavoriteCount() { return String.valueOf(favoriteCount); }

    public String getRetweetCount() { return String.valueOf(retweetCount); }

    public String getCreatedAt() {
        return createdAt;
    }

    // Deserialize the JSON and build Tweet objects
    // Tweet.fromJSON("{...}") => <Tweet>
    public static Tweet fromJSON(JSONObject jsonObject) {
        Tweet tweet = new Tweet();
        // Extract the values from the json, store them
        try {
            tweet.tweetId = jsonObject.getLong("id");

            tweet.body = jsonObject.getString("text");

            tweet.mediaUrl = null;
            //JSONObject jsonObjectMedia = jsonObject.getJSONObject("extended_entities");
            JSONObject jsonObjectMedia = jsonObject.getJSONObject("entities");
            if (jsonObjectMedia.has("media")) {
                JSONArray jsonArrayMedia = jsonObjectMedia.getJSONArray("media");
                tweet.mediaType = jsonArrayMedia.getJSONObject(0).getString("type");
                tweet.mediaUrl = jsonArrayMedia.getJSONObject(0).getString("media_url_https");

                /*
                Log.d("DEBUG", tweet.mediaUrl);
                if (!tweet.mediaType.equals("photo")) {
                    JSONArray jsonArrayVariants = jsonArrayMedia.getJSONObject(0).getJSONObject("video_info").getJSONArray("variants");
                    String url = jsonArrayVariants.getJSONObject(0).getString("url");
                    Log.d("DEBUG", url);

                    tweet.mediaUrl = url;
                }
                */
            }

            tweet.uid = jsonObject.getLong("id");
            tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
            tweet.favoriteCount = jsonObject.getLong("favorite_count");
            tweet.retweetCount = jsonObject.getLong("retweet_count");
            tweet.createdAt = jsonObject.getString("created_at");

            tweet.save();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Return the tweet object
        return tweet;
    }

    // Tweet.fromJSONArray([ { ... }, { ... } ] => List<Tweet>
    public static ArrayList<Tweet> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<>();

        ActiveAndroid.beginTransaction();
        try {

            // Iterate the json array and create tweets
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject tweetJson = jsonArray.getJSONObject(i);
                    Tweet tweet = Tweet.fromJSON(tweetJson);
                    if (tweet != null) {
                        tweets.add(tweet);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue; // even if 1 tweet fails continue to process the other tweets
                }
            }

            ActiveAndroid.setTransactionSuccessful();
        }
        finally {
            ActiveAndroid.endTransaction();
        }

        // Return the finished list
        return tweets;
    }

    public static int getCountOfTweets() {
        return new Select().from(Tweet.class).count();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.tweetId);
        dest.writeString(this.body);
        dest.writeString(this.mediaType);
        dest.writeString(this.mediaUrl);
        dest.writeLong(this.uid);
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.createdAt);
        dest.writeLong(this.favoriteCount);
        dest.writeLong(this.retweetCount);
    }

    protected Tweet(Parcel in) {
        this.tweetId = in.readLong();
        this.body = in.readString();
        this.mediaType = in.readString();
        this.mediaUrl = in.readString();
        this.uid = in.readLong();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.createdAt = in.readString();
        this.favoriteCount = in.readLong();
        this.retweetCount = in.readLong();
    }

    public static final Creator<Tweet> CREATOR = new Creator<Tweet>() {
        @Override
        public Tweet createFromParcel(Parcel source) {
            return new Tweet(source);
        }

        @Override
        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };
}
