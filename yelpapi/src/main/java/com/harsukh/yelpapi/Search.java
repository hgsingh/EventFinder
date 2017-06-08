package com.harsukh.yelpapi;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by harsukh on 6/8/17.
 */

public class Search {
    @SerializedName("total")
    public int total;
    @SerializedName("businesses")
    public List<Business> businesses;
    @SerializedName("region")
    public Region region;
}
