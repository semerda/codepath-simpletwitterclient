package com.codepath.apps.simpletwitterclient;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ernest on 8/4/16.
 */

// Let's use this to preload the Twitter tweets JSON from local store to avoid rate limiter.
// Helpful when mucking around with the funky XML layouts.
public class AssetsClass {

    private Context context;

    public AssetsClass(Context context) {
        this.context = context;
    }

    public JSONArray loadJSONFromAsset() throws JSONException {
        String json = null;
        try {
            InputStream is = this.context.getAssets().open("tweets.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        JSONArray jsonArray = new JSONArray(json);

        return jsonArray;
    }
}
