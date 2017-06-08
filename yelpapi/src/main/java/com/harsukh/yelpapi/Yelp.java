package com.harsukh.yelpapi;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.harsukh.yelpapi.YelpOAuth.*;

public class Yelp {

    private static String CLIENT_CREDENTIALS = "CLIENT_PREFERENCES";
    private static final String AUTH_KEY = "AUTH_KEY";
    private static final String EXPIRY_KEY = "EXPIRY";
    private Context context;

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
            return yelp;
        } else return yelp;
    }


    /**
     * Search with term and location.
     *
     * @param term      Search term
     * @param latitude  Latitude
     * @param longitude Longitude
     * @return JSON string response
     */
    public Observable<Search> search(final String term, final double latitude, final double longitude) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(CLIENT_CREDENTIALS, Context.MODE_PRIVATE);
        final String authToken = sharedPreferences.getString(AUTH_KEY, null);
        final IYelp yelp = createService(IYelp.class);
        if (TextUtils.isEmpty(authToken)) {
            return yelp.getOAuthCredentials(GRANT_TYPE, CLIENT_ID, CLIENT_SECRET).
                    subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).
                    flatMap(new Func1<Authorization, Observable<Search>>() {
                        @Override
                        public Observable<Search> call(Authorization authorization) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(AUTH_KEY, authorization.access_token);
                            editor.putLong(EXPIRY_KEY, authorization.expires_in * 1000 + System.currentTimeMillis());
                            editor.apply();
                            return yelp.getSearchResults(authorization.access_token, term, latitude, longitude);
                        }
                    }).asObservable();
        } else {
            return yelp.getSearchResults(authToken, term, latitude, longitude);
        }
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
        final IYelp yelp = createService(IYelp.class);
        if (TextUtils.isEmpty(authToken)) {
            return yelp.getOAuthCredentials(GRANT_TYPE, CLIENT_ID, CLIENT_SECRET).
                    subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).
                    flatMap(new Func1<Authorization, Observable<Search>>() {
                        @Override
                        public Observable<Search> call(Authorization authorization) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(AUTH_KEY, authorization.access_token);
                            editor.putLong(EXPIRY_KEY, authorization.expires_in * 1000 + System.currentTimeMillis());
                            editor.apply();
                            return yelp.getSearchResults(authorization.access_token, term, location);
                        }
                    }).asObservable();
        } else {
            return yelp.getSearchResults(authToken, term, location);
        }
    }
}
