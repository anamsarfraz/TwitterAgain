package com.codepath.apps.twitter.models;


import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;


@Parcel(analyze={Follow.class})
public class Follow extends BaseModel {

    // Define table fields
    @PrimaryKey
    @Column
    long uid;

    @Column
    String idStr;

    @Column
    String name;

    @Column
    String screenName;

    @Column
    String profileImageUrl;

    @Column
    String tagline;

    @Column
    boolean following;

    // Getters and setters
    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getTagline() {
        return tagline;
    }

    public void setText(String tagline) {
        this.tagline = tagline;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public Follow() {
        super();
    }

    // Parse model from JSON
    public Follow(JSONObject jsonObject){
        super();

        try {
            this.uid = jsonObject.getLong("id");
            this.idStr = jsonObject.getString("id_str");
            this.name = jsonObject.getString("name");
            this.screenName = jsonObject.getString("screen_name");
            this.profileImageUrl = jsonObject.getString("profile_image_url");
            this.tagline = jsonObject.getString("description");
            this.following = jsonObject.getBoolean("following");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Follow> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Follow> follows = new ArrayList<>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject followJson = null;
            try {
                followJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            Follow message = new Follow(followJson);
            follows.add(message);
        }

        return follows;
    }

}
