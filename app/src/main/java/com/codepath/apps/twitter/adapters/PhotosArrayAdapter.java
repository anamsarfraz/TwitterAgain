package com.codepath.apps.twitter.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;


import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PhotosArrayAdapter extends RecyclerView.Adapter<PhotosArrayAdapter.ViewHolder> {

    // Define class constants

    // Define listener member variable
    private static OnItemClickListener listener;

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivUserPhoto)
        ImageView ivUserPhoto;

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
    private List<String> mPhotos;
    // Store the context for easy access
    private Context mContext;

    // Pass in the Draft array into the constructor
    public PhotosArrayAdapter(Context context, List<String> photos) {
        mPhotos = photos;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }


    @Override
    public PhotosArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom Draft view layout
        View draftView = inflater.inflate(R.layout.item_photo, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(draftView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Get the data model based on position
        String photoUrl = mPhotos.get(position);
        holder.ivUserPhoto.setImageResource(0);
        Log.d("DEBUG", "Coming in bind view");
        Glide.with(mContext)
                .load(photoUrl)
                .placeholder(R.drawable.tweet_social)
                .crossFade()
                .into(holder.ivUserPhoto);
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    public String getItem(int position) {
        return mPhotos.get(position);
    }

    public void clearItems() {
        int currSize = mPhotos.size();
        mPhotos.clear();
        notifyItemRangeRemoved(0, currSize);
    }

}