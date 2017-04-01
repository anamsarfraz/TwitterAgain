package com.codepath.apps.twitter.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.models.Trend;
import com.codepath.apps.twitter.util.FormatUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TrendsArrayAdapter extends RecyclerView.Adapter<TrendsArrayAdapter.ViewHolder> {

    // Define class constants

    // Define listener member variable
    private OnItemClickListener listener;
    private static String TWEETS = "TWEETS";

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvTrendId) TextView tvTrendId;
        @BindView(R.id.tvTrendName) TextView tvTrendName;
        @BindView(R.id.tvTweetVol) TextView tvTweetVol;

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

    // Store a member variable for the Drafts
    private List<Trend> mTrends;
    // Store the context for easy access
    private Context mContext;

    // Pass in the Draft array into the constructor
    public TrendsArrayAdapter(Context context, List<Trend> trends) {
        mTrends = trends;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }


    @Override
    public TrendsArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom Trend view layout
        View trendView = inflater.inflate(R.layout.item_trend, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(trendView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Get the data model based on position
        Trend trend = mTrends.get(position);

        holder.tvTrendId.setText(String.valueOf(position+1));
        holder.tvTrendName.setText(trend.getName());
        long tweetVol = trend.getTweetVolume();
        if (tweetVol >= 0) {
            holder.tvTweetVol.setVisibility(View.VISIBLE);
            holder.tvTweetVol.setText(FormatUtil.buildSpan(FormatUtil.format(trend.getTweetVolume()), TWEETS));
        } else {
            holder.tvTweetVol.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mTrends.size();
    }

    public Trend getItem(int position) {
        return mTrends.get(position);
    }

    public void clearItems() {
        int currSize = mTrends.size();
        mTrends.clear();
        notifyItemRangeRemoved(0, currSize);
    }

}