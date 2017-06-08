package com.harsukh.demo_dealers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.harsukh.yelpapi.Business;
import com.harsukh.yelpapi.Search;
import com.harsukh.yelpapi.Yelp;
import com.squareup.picasso.Picasso;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by harsukh on 6/8/17.
 */

public class MapsActivity extends AppCompatActivity implements LocationListener {
    private static Location current_location = null;
    private Yelp yelp = null;
    private EditText editText;
    private static final String TAG = MapsActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        yelp = Yelp.get(getApplicationContext());
        setContentView(R.layout.activity_maps);
        editText = (EditText) findViewById(R.id.search_text_maps);
        editText.setText("");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000l, 10.0f, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        current_location = location;
        Picasso.with(this).load(constructInitialUri()).into((ImageView) findViewById(R.id.MapView));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void geoLocate(View v) {
        String search_term = editText.getText().toString();
        if (!TextUtils.isEmpty(search_term)) {
            yelp.search(search_term, current_location.getLatitude(), current_location.getLongitude()).
                    subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Search>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "Error when calling", e);
                }

                @Override
                public void onNext(Search search) {
                    setMarkers(search);
                }
            });
        }
    }

    private void setMarkers(Search search) {
        Uri uri = constructUri(search);
        Picasso.with(this).load(uri).into((ImageView) findViewById(R.id.MapView));
    }

    private Uri constructUri(Search search) {
        Uri.Builder builder = new Uri.Builder().scheme("https").authority("maps.googleapis.com")
                .appendPath("maps").appendPath("api").appendPath("staticmap").
                appendQueryParameter("center", "" + current_location.getLatitude() + "," +
                        current_location.getLongitude()).appendQueryParameter("zoom", "10")
                .appendQueryParameter("size", "300x100").appendQueryParameter("scale", "2");
        StringBuilder locations = new StringBuilder();
        for (Business business : search.businesses) {
            locations.append('|');
            locations.append(business.coordinates.latitude);
            locations.append(',');
            locations.append(business.coordinates.longitude);
        }
        builder.appendQueryParameter("marker", "color:blue" + locations.toString());
        builder.appendQueryParameter("key", getString(R.string.MAP_KEY));
        Uri uri = builder.build();
        Log.i(TAG, uri.toString());
        return uri;
    }

    private Uri constructInitialUri() {
        Uri uri = new Uri.Builder().scheme("https").authority("maps.googleapis.com")
                .appendPath("maps").appendPath("api").appendPath("staticmap").
                appendQueryParameter("center", "" + current_location.getLatitude() + "," +
                        current_location.getLongitude()).appendQueryParameter("zoom", "10")
                .appendQueryParameter("size", "300x100").appendQueryParameter("scale", "2").
                        appendQueryParameter("key", getString(R.string.MAP_KEY)).build();
        Log.i(TAG, uri.toString());
        return uri;
    }

}
