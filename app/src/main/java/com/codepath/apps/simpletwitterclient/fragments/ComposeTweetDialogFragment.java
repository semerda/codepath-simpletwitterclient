package com.codepath.apps.simpletwitterclient.fragments;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.simpletwitterclient.R;
import com.codepath.apps.simpletwitterclient.TwitterApplication;
import com.codepath.apps.simpletwitterclient.activities.TimelineActivity;
import com.codepath.apps.simpletwitterclient.models.Tweet;
import com.codepath.apps.simpletwitterclient.net.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cz.msebera.android.httpclient.Header;

/**
 * Created by ernest on 8/4/16.
 */
public class ComposeTweetDialogFragment extends DialogFragment {

    TwitterClient client = TwitterApplication.getRestClient();

    // Ref: https://github.com/codepath/android_guides/wiki/Working-with-the-EditText
    @BindView(R.id.btnClose)
    Button btnClose;

    @BindView(R.id.tvAccountUserName)
    TextView tvAccountUserName;
    @BindView(R.id.tvAccountScreenName)
    TextView tvAccountScreenName;
    @BindView(R.id.ivAccountProfileImage)
    ImageView ivAccountProfileImage;

    @BindView(R.id.etStatus)
    EditText etStatus;
    @BindView(R.id.btnTweet)
    Button btnTweet;

    int characterCounter;

    private Unbinder unbinder;

    public ComposeTweetDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static ComposeTweetDialogFragment newInstance(String title, Tweet tweet) {
        ComposeTweetDialogFragment frag = new ComposeTweetDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", title);
        if (tweet != null) {
            args.putString("userName", tweet.getUser().getName());
            args.putString("screenName", tweet.getUser().getScreenName());
            args.putString("profileImageUrl", tweet.getUser().getProfileImageUrl());
        }
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_compose_tweet, container);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().getAttributes().windowAnimations = R.style.MyDialogAnimation_Window;
        return dialog;
    }

    public void setupAuthenticatedUser(View view, String name, String screenName, String url) {
        tvAccountUserName.setText("\n\n" + name);
        tvAccountScreenName.setText("@" + screenName);

        Glide.with(view.getContext())
                .load(url)
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_autorenew_black_48dp)
                .error(R.drawable.ic_block_red_48dp)
                .into(new BitmapImageViewTarget(ivAccountProfileImage) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(view.getContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ivAccountProfileImage.setImageDrawable(circularBitmapDrawable);
                    }
                });

        // User must be replying
        if (getArguments().containsKey("screenName")) {
            etStatus.setText(String.format("@%s ", getArguments().getString("screenName")));
            etStatus.setSelection(etStatus.getText().length());
        }
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments().containsKey("screenName")) {
            setupAuthenticatedUser(view,
                    getArguments().getString("userName"),
                    String.format("%s ", getArguments().getString("screenName")),
                    getArguments().getString("profileImageUrl")
            );
        } else {
            // Test data
            //setupAuthenticatedUser(view, "Ernest Semerda", "ernestsemerda", "https://pbs.twimg.com/profile_images/72457982/ernest_semerda_400x400.jpg");

            // Real data
            client.getAccountCredentials(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    //Log.d("DEBUG-2", response.toString());
                    try {
                        setupAuthenticatedUser(view,
                                response.getString("name"),
                                response.getString("screen_name"),
                                response.getString("profile_image_url_https").replace("_normal", "_bigger"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d("DEBUG", throwable.toString());
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    Log.d("DEBUG", errorResponse.toString());
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG", errorResponse.toString());
                }
            });
        }

        //String title = getArguments().getString("title");
        //getDialog().setTitle(title);

        //saveButton.setOnClickListener(v -> Toast.makeText(getContext(), "Search Settings Saved", Toast.LENGTH_LONG).show());

        // When too many characters are entered disable the Tweet (submit) button
        etStatus.addTextChangedListener(mTextEditorWatcher);

        // Tweet button is disabled by default
        btnTweet.setEnabled(false);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private final TextWatcher mTextEditorWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            btnTweet.setEnabled((s.length() > 140) ? false : true);
        }
    };

    @OnClick(R.id.btnTweet)
    public void onClickTweetStatus(Button button) {
        String status = etStatus.getText().toString();

        client.composeTweet(status, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //Log.d("DEBUG", response.toString());
                try {
                    TimelineActivity timelineActivity = (TimelineActivity) getActivity();
                    timelineActivity.populateTimeline(0); // Refresh the timeline
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getDialog().dismiss();

                // Refresh parent activity -- has to be here otherwise dialog has context
                TimelineActivity callingActivity = (TimelineActivity) getActivity();
                callingActivity.sendRefreshRequest();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG", errorResponse.toString());
            }
        });
    }

    @OnClick(R.id.btnClose)
    public void onClickClose(Button button) {
        getDialog().dismiss();
    }

    // When binding a fragment in onCreateView, set the views to null in onDestroyView.
    // ButterKnife returns an Unbinder on the initial binding that has an unbind method to do this automatically.
    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
