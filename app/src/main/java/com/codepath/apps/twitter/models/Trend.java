package com.codepath.apps.twitter.models;

import com.codepath.apps.twitter.databases.TwitterDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Table(database = TwitterDatabase.class)
@Parcel(analyze={Trend.class})
public class Trend extends BaseModel {
    @PrimaryKey(autoincrement = true)
    @Column
    int uid;

    @Column
    String name;

    @Column
    long tweetVolume;


    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTweetVolume() {
        return tweetVolume;
    }

    public void setTweetVolume(int tweetVolume) {
        this.tweetVolume = tweetVolume;
    }

    public Trend() {
        super();
    }

    // Parse model from JSON
    public Trend(JSONObject jsonObject) {
        super();

        try {
            this.name = jsonObject.getString("name");
            this.tweetVolume = jsonObject.optLong("tweet_volume", -1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Trend> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Trend> trends = new ArrayList<>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject trendJson = null;
            try {
                trendJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            Trend trend = new Trend(trendJson);
            trend.save();
            trends.add(trend);
        }

        return trends;
    }
    public static List<Trend> getTrends() {
        return new Select()
                .from(Trend.class)
                .orderBy(Trend_Table.uid, true)
                .queryList();
    }

}
