package com.harsukh.yelpapi;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;


public class YelpOAuth {

    public static final String CLIENT_SECRET = "yKckQF64GdHNTGYJjTZsalQteY4jxMOBEIU2ph4qiQwoRoObez0gjOPQiSwPHUc2";
    public static final String CLIENT_ID = "qDLX-y33HXhcxsKJdJeBjQ";
    public static final String GRANT_TYPE = "client_credentials";


    public static final String BASE_URL = "https://api.yelp.com/";

    private static Retrofit retrofit = null;

    public static <S> S createService(Class<S> serviceClass) {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor);
        Retrofit.Builder builder;
        if (retrofit == null) {
            builder = new Retrofit.Builder().baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()).
                            addCallAdapterFactory(RxJavaCallAdapterFactory.create());
            retrofit = builder.client(clientBuilder.build()).build();
        }
        return retrofit.create(serviceClass);
    }


    public interface IYelp {
        //defines the http method we want to use using retrofit's handy syntax
        @POST("/oauth2/token")
        Call<Authorization> getOAuthCredentials(@Query("grant_type") String grantType,
                                                @Query("client_id") String clientId,
                                                @Query("client_secret") String clientSecret);

        @GET("v3/businesses/search")
        Call<Search> getSearchResults(@Header("Authorization") String access_token,
                                      @Query("term") String term, @Query("latitude") double latitude,
                                      @Query("longitude") double longitude);

        @GET("v3/businesses/search")
        Observable<Search> getSearchResults(@Header("Authorization") String access_token,
                                            @Query("term") String term,
                                            @Query("location") String location);
    }
}
