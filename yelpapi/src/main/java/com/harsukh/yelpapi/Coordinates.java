package com.harsukh.yelpapi;

import com.google.gson.annotations.SerializedName;

/**
 * Created by harsukh on 6/8/17.
 */

public class Coordinates {

    @SerializedName("longitude")
    public double longitude;
    @SerializedName("latitude")
    public double latitude;
}
