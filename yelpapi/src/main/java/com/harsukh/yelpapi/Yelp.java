package com.harsukh.yelpapi;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;

import static com.harsukh.yelpapi.YelpOAuth.*;

public class Yelp {

    private static String CLIENT_CREDENTIALS = "CLIENT_PREFERENCES";
    private static final String AUTH_KEY = "AUTH_KEY";
    private static final String EXPIRY_KEY = "EXPIRY";
    private static final String TOKEN_TYPE = "TYPE";
    private Context context;
    private static IYelp yelpService;

    private static Yelp yelp = null;

    /**
     * Setup the Yelp API OAuth credentials.
     */
    private Yelp(Context context) {
        this.context = context;
    }

    public static final Yelp get(Context context) {
        if (yelp == null) {
            yelp = new Yelp(context);
            yelpService = createService(IYelp.class);
            return yelp;
        } else return yelp;
    }

    public void initiateAuth(final OnAuthComplete onAuthComplete) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(CLIENT_CREDENTIALS, Context.MODE_PRIVATE);
        final String authToken = sharedPreferences.getString(AUTH_KEY, null);
        if (TextUtils.isEmpty(authToken)) {
            final SharedPreferences.Editor editor = sharedPreferences.edit();

            yelpService.getOAuthCredentials(GRANT_TYPE, CLIENT_ID, CLIENT_SECRET).enqueue(new Callback<Authorization>() {
                @Override
                public void onResponse(Call<Authorization> call, Response<Authorization> response) {
                    Authorization authorization = response.body();
                    Log.i("Yelp", authorization.access_token);
                    editor.putString(AUTH_KEY, authorization.access_token);
                    editor.putString(TOKEN_TYPE, authorization.token_type);
                    editor.putLong(EXPIRY_KEY, authorization.expires_in * 1000 + System.currentTimeMillis());
                    editor.apply();
                    onAuthComplete.startFunction();
                }

                @Override
                public void onFailure(Call<Authorization> call, Throwable t) {
                    Log.e("Yelp", "Unable to authorize", t);
                }
            });
        } else {
            onAuthComplete.startFunction();

        }
    }

    /**
     * Search with term and location.
     *
     * @param term      Search term
     * @param latitude  Latitude
     * @param longitude Longitude
     * @return JSON string response
     */
    public Call<Search> search(final String term, final double latitude, final double longitude) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(CLIENT_CREDENTIALS, Context.MODE_PRIVATE);
        final String authToken = sharedPreferences.getString(AUTH_KEY, null);
        final String authType = sharedPreferences.getString(TOKEN_TYPE, null);
        return yelpService.getSearchResults(authType + " " + authToken, term, latitude, longitude);
    }

    /**
     * Search with term string location.
     *
     * @param term Search term
     * @return JSON string response
     */
    public Observable<Search> search(final String term, final String location) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(CLIENT_CREDENTIALS, Context.MODE_PRIVATE);
        final String authToken = sharedPreferences.getString(AUTH_KEY, null);
        final String authType = sharedPreferences.getString(TOKEN_TYPE, null);
        return yelpService.getSearchResults(authType + " " + authToken, term, location);
    }

    public interface OnAuthComplete {
        void startFunction();
    }
}
