package com.codepath.apps.twitter.models;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.codepath.apps.twitter.databases.TwitterDatabase;
import com.codepath.apps.twitter.util.Constants;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;
import org.parceler.Transient;

import java.util.ArrayList;
import java.util.List;

import static com.codepath.apps.twitter.R.string.tweet;
import static com.codepath.apps.twitter.models.User.currentUser;
import static com.codepath.apps.twitter.models.User.pref;
import static com.codepath.apps.twitter.models.User_Table.tagLine;
import static com.raizlabs.android.dbflow.config.FlowManager.getContext;


@Table(database = TwitterDatabase.class)
@Parcel(analyze={Message.class})
public class Message extends BaseModel {

    // Define table fields
    @PrimaryKey
    @Column
    long uid;

    @Column
    String idStr;

    @Column
    String senderName;

    @Column
    String senderScreenName;

    @Column
    String profileImageUrl;

    @Column
    String text;

    @Column
    String createdAt;

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

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String name) {
        this.senderName = name;
    }

    public String getSenderScreenName() {
        return senderScreenName;
    }

    public void setSenderScreenName(String screenName) {
        this.senderScreenName = screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Message() {
        super();
    }

    // Parse model from JSON
    public Message(JSONObject jsonObject){
        super();

        try {
            this.uid = jsonObject.getLong("id");
            this.idStr = jsonObject.getString("id_str");
            this.senderName = jsonObject.getJSONObject("sender").getString("name");
            this.senderScreenName = jsonObject.getJSONObject("sender").getString("screen_name");
            this.profileImageUrl = jsonObject.getJSONObject("sender").getString("profile_image_url");
            this.text = jsonObject.getString("text");
            this.createdAt = jsonObject.getString("created_at");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Message> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Message> messages = new ArrayList<>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject messageJson = null;
            try {
                messageJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            Message message = new Message(messageJson);
            message.save();
            messages.add(message);
        }

        return messages;
    }

    public static void saveMessage(JSONObject jsonObject) {
        new Message(jsonObject).save();
    }

    // Record Finders
    public static Message byId(long uid) {
        return new Select().from(Message.class).where(Message_Table.uid.eq(uid)).querySingle();
    }

    public static List<Message> recentItems(long maxId) {
        Condition condition = maxId <= 0 ? Message_Table.uid.greaterThan(maxId) : Message_Table.uid.lessThanOrEq(maxId);
        return new Select()
                .from(Message.class)
                .where(condition)
                .orderBy(Message_Table.uid, false)
                .limit(Constants.MAX_TWEET_COUNT)
                .queryList();
    }
}
