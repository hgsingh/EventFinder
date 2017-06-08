package com.harsukh.yelpapi;

import com.google.gson.annotations.SerializedName;

/**
 * Created by harsukh on 6/8/17.
 */

public class Business {

    @SerializedName("name")
    public String name;
    @SerializedName("distance")
    public double distance;
    @SerializedName("phone")
    public String phone;
    @SerializedName("coordinates")
    public Coordinates coordinates;
}
