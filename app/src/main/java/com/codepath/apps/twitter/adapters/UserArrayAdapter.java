package com.codepath.apps.twitter.adapters;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.models.Message;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Constants;
import com.codepath.apps.twitter.util.DateUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static android.R.id.message;


public class UserArrayAdapter extends RecyclerView.Adapter<UserArrayAdapter.ViewHolder> {


    // Define listener member variable
    private OnItemClickListener listener;

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivProfileImageUser) ImageView ivProfileImageUser;
        @BindView(R.id.tvNameUser) TextView tvNameUser;
        @BindView(R.id.tvScreenNameUser) TextView tvScreenNameUser;


        public ViewHolder(final View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });
        }

    }

    // Store a member variable for the users
    private List<User> mUsers;
    // Store the context for easy access
    private Context mContext;

    // Pass in the user array into the constructor
    public UserArrayAdapter(Context context, List<User> users) {
        mUsers = users;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }


    @Override
    public UserArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom Trend view layout
        View trendView = inflater.inflate(R.layout.item_user, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(trendView);
        return viewHolder;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        User user = mUsers.get(position);

        ImageView imgView = holder.ivProfileImageUser;
        Glide.with(mContext)
                .load(user.getProfileImageUrl())
                .bitmapTransform(new CropCircleTransformation(mContext))
                .placeholder(R.drawable.tweet_social)
                .crossFade()
                .into(imgView);

        holder.tvNameUser.setText(user.getName());
        // set the verified image view
        Drawable drawable =  mContext.getDrawable(R.drawable.verified_user);
        if (user.getVerified()) {
            holder.tvNameUser.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
        } else {
            holder.tvNameUser.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        holder.tvScreenNameUser.setText(
                String.format("%s%s", Constants.ATRATE, user.getScreenName()));

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public User getItem(int position) {
        return mUsers.get(position);
    }

    public void clearItems() {
        int currSize = mUsers.size();
        mUsers.clear();
        notifyItemRangeRemoved(0, currSize);
    }

}