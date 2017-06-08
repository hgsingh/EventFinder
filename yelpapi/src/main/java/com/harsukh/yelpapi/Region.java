package com.harsukh.yelpapi;

import com.google.gson.annotations.SerializedName;

/**
 * Created by harsukh on 6/8/17.
 */

public class Region {
    @SerializedName("center")
    public Center center;

    public static final class Center {
        @SerializedName("latitude")
        public double latitude;
        @SerializedName("longitude")
        public double longitude;
    }
}
