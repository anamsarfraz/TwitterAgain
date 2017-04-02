package com.codepath.apps.twitter.adapters;


import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.models.Follow;
import com.codepath.apps.twitter.models.Message;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Constants;
import com.codepath.apps.twitter.util.DateUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static android.R.id.message;


public class FollowArrayAdapter extends RecyclerView.Adapter<FollowArrayAdapter.ViewHolder> {


    private static final int PROFILE_IMG_ROUND = 6;
    // Define listener member variable
    private OnItemClickListener listener;

    // Define the listener interface
    public interface OnItemClickListener {
        void onImageClick(View itemView, int position);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivProfileImageFollow) ImageView ivProfileImageFollow;
        @BindView(R.id.tvUserNameFollow) TextView tvUserNameFollow;
        @BindView(R.id.tvScreenNameFollow) TextView tvScreenNameFollow;
        @BindView(R.id.tvTaglineFollow) TextView tvTaglineFollow;
        @BindView(R.id.ivFollow) ImageView ivFollow;

        public ViewHolder(final View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);


            ivFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onImageClick(itemView, position);
                        }
                    }
                }
            });
        }

    }

    // Store a member variable for the Messages
    private List<Follow> mFollows;
    // Store the context for easy access
    private Context mContext;

    // Pass in the Message array into the constructor
    public FollowArrayAdapter(Context context, List<Follow> follows) {
        mFollows = follows;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }


    @Override
    public FollowArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom Trend view layout
        View trendView = inflater.inflate(R.layout.item_follow, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(trendView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Get the data model based on position
        Follow follow = mFollows.get(position);

        ImageView imgView = holder.ivProfileImageFollow;
        Glide.with(mContext)
                .load(follow.getProfileImageUrl())
                .bitmapTransform(new RoundedCornersTransformation(mContext, PROFILE_IMG_ROUND, 0))
                .placeholder(R.drawable.tweet_social)
                .crossFade()
                .into(imgView);

        holder.tvUserNameFollow.setText(follow.getName());
        holder.tvScreenNameFollow.setText(
                String.format("%s%s", Constants.ATRATE, follow.getScreenName()));

        if (follow.getTagline().length() > 0 ) {
            holder.tvTaglineFollow.setText(follow.getTagline());
            holder.tvTaglineFollow.setVisibility(View.VISIBLE);
        } else {
            holder.tvTaglineFollow.setVisibility(View.GONE);
        }


        holder.ivFollow.setImageResource(0);
        holder.ivFollow.setVisibility(
                User.getCurrentUser().getUid() == follow.getUid() ? View.GONE : View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.ivFollow.setBackground(mContext.getDrawable(follow.isFollowing() ? R.drawable.ic_following : R.drawable.ic_follow));
        }


    }

    @Override
    public int getItemCount() {
        return mFollows.size();
    }

    public Follow getItem(int position) {
        return mFollows.get(position);
    }

    public void clearItems() {
        int currSize = mFollows.size();
        mFollows.clear();
        notifyItemRangeRemoved(0, currSize);
    }

}