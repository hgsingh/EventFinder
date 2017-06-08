package com.harsukh.yelpapi;

import com.google.gson.annotations.SerializedName;

/**
 * Created by harsukh on 6/8/17.
 */

public class Authorization {
    @SerializedName("access_token")
    String access_token;
    @SerializedName("token_type")
    String token_type;
    @SerializedName("expires_in")
    int expires_in;
}
