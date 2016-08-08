package com.codepath.apps.simpletwitterclient.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by ernest on 8/1/16.
 *
 * Source of JSON
 * https://dev.twitter.com/rest/reference/get/account/verify_credentials
 *
 * ActiveAndroid
 * http://guides.codepath.com/android/ActiveAndroid-Guide
 */

@Table(name = "Users")
public class User extends Model implements Parcelable {

    // This is a regular field
    @Column(name = "Name")
    private String name;

    @Column(name = "UId")
    private long uid;

    @Column(name = "ScreenName")
    private String screenName;

    @Column(name = "ProfileImageUrl")
    private String profileImageUrl;

    // Make sure to have a default constructor for every ActiveAndroid model
    public User(){
        super();
    }

    public User(String name, long uid, String screenName, String profileImageUrl){
        super();
        this.name = name;
        this.uid = uid;
        this.screenName = screenName;
        this.profileImageUrl = profileImageUrl;
    }

    // Used to return items from another table based on the foreign key
    public List<Tweet> items() {
        return getMany(Tweet.class, "User");
    }

    public String getName() {
        return name;
    } // actual user name eg. Ernest Semerda

    public long getUid() {
        return uid;
    }

    public String getScreenName() {
        return screenName;
    } // username or handle eg. ernestsemerda

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // deserialize the user object => User
    public static User fromJSON(JSONObject json) {
        User user = new User();
        // Extract and fill the values
        try {
            user.name = json.getString("name");
            user.uid = json.getLong("id");
            user.screenName = json.getString("screen_name");
            // Note: using bigger version of profile images - clearer
            // Ref: https://dev.twitter.com/overview/general/user-profile-images-and-banners
            user.profileImageUrl = json.getString("profile_image_url_https").replace("_normal", "_bigger");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Return a user
        return user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeLong(this.uid);
        dest.writeString(this.screenName);
        dest.writeString(this.profileImageUrl);
    }

    protected User(Parcel in) {
        this.name = in.readString();
        this.uid = in.readLong();
        this.screenName = in.readString();
        this.profileImageUrl = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
