package com.codepath.apps.simpletwitterclient.adapters;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.simpletwitterclient.BR;
import com.codepath.apps.simpletwitterclient.R;
import com.codepath.apps.simpletwitterclient.TimeClass;
import com.codepath.apps.simpletwitterclient.handlers.TweetHandler;
import com.codepath.apps.simpletwitterclient.models.Tweet;

import java.text.ParseException;
import java.util.List;

/**
 * Created by ernest on 8/3/16.
 */
public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.BindingHolder> {

    // Store a member variable for the contacts
    private List<Tweet> mTweets;
    // Store the context for easy access
    private Context mContext;

    // Pass in the contact array into the constructor
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        mTweets = tweets;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding binding;

        public BindingHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tweet, parent, false);
        BindingHolder holder = new BindingHolder(v);

        // This allows me to bind TweetHandlers to manage onClick from XML layout
        holder.getBinding().setVariable(BR.handler, new TweetHandler(mContext));

        return holder;
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        final Tweet tweet = mTweets.get(position);
        holder.getBinding().setVariable(BR.tweet, tweet);
        holder.getBinding().executePendingBindings();

        Log.d("DEBUG", "binding: " + holder + "at position: " + position);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    @BindingAdapter({"bind:bImageUrl"})
    public static void loadImage(ImageView view, String url) {
        // Android doesn't have a way to load images so we use Glide instead of Picasso
        // because it's better with memory: https://inthecheesefactory.com/blog/get-to-know-glide-recommended-by-google/en
        // Ref: https://futurestud.io/blog/glide-image-resizing-scaling
        Glide.with(view.getContext())
                .load(url)
                //.override(300, 300) // resizes the image to these dimensions (in pixel)
                .centerCrop() // this cropping technique scales the image so that it fills the requested bounds and then crops the extra.
                .placeholder(R.drawable.ic_autorenew_black_48dp)
                .error(R.drawable.ic_block_red_48dp)
                .into(view);
        view.setAlpha((float) 1.0);
    }

    @BindingAdapter({"bind:bCreatedAt"})
    public static void loadTimeAgo(TextView view, String createdAt) {
        String timeAgoString = "Not Available";
        if (createdAt != null) {
            try {
                TimeClass timeClass = new TimeClass();
                timeAgoString = timeClass.getTimeAgoUsingStringDate(createdAt, "", view.getContext());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        view.setText(timeAgoString);
    }

    public void clearData() {
        int sizeOfList = mTweets.size();
        // clear list
        mTweets.clear();
        // let the adapter know about the changes and reload view.
        //this.notifyDataSetChanged();
        this.notifyItemRangeRemoved(0, sizeOfList);
    }
}
