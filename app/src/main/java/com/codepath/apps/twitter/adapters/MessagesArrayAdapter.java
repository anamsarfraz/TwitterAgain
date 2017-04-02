package com.codepath.apps.twitter.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.models.Message;
import com.codepath.apps.twitter.models.Trend;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Constants;
import com.codepath.apps.twitter.util.DateUtil;
import com.codepath.apps.twitter.util.FormatUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.codepath.apps.twitter.R.string.tweet;


public class MessagesArrayAdapter extends RecyclerView.Adapter<MessagesArrayAdapter.ViewHolder> {


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
        @BindView(R.id.ivProfileImageMessage) ImageView ivProfileImageMessage;
        @BindView(R.id.tvUserNameMessage) TextView tvUserNameMessage;
        @BindView(R.id.tvScreenNameMessage) TextView tvScreenNameMessage;
        @BindView(R.id.tvCreatedTimeMessage) TextView tvCreatedTimeMessage;
        @BindView(R.id.tvTextMessage) TextView tvTextMessage;

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

    // Store a member variable for the Messages
    private List<Message> mMessages;
    // Store the context for easy access
    private Context mContext;

    // Pass in the Message array into the constructor
    public MessagesArrayAdapter(Context context, List<Message> messages) {
        mMessages = messages;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }


    @Override
    public MessagesArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom Trend view layout
        View trendView = inflater.inflate(R.layout.item_message, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(trendView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Get the data model based on position
        Message message = mMessages.get(position);

        ImageView imgView = holder.ivProfileImageMessage;
        Glide.with(mContext)
                .load(message.getProfileImageUrl())
                .bitmapTransform(new CropCircleTransformation(mContext))
                .placeholder(R.drawable.tweet_social)
                .crossFade()
                .into(imgView);

        holder.tvUserNameMessage.setText(message.getSenderName());
        holder.tvScreenNameMessage.setText(
                String.format("%s%s", Constants.ATRATE, message.getSenderScreenName()));
        holder.tvCreatedTimeMessage.setText(DateUtil.getRelativeTimeAgo(message.getCreatedAt()));
        holder.tvTextMessage.setText(message.getText());

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public Message getItem(int position) {
        return mMessages.get(position);
    }

    public void clearItems() {
        int currSize = mMessages.size();
        mMessages.clear();
        notifyItemRangeRemoved(0, currSize);
    }

}