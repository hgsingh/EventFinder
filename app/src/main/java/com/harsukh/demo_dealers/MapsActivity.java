package com.harsukh.demo_dealers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.harsukh.yelpapi.Business;
import com.harsukh.yelpapi.Search;
import com.harsukh.yelpapi.Yelp;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by harsukh on 6/8/17.
 */

public class MapsActivity extends AppCompatActivity implements LocationListener, ISpeechObserver {
    private static Location current_location = null;
    private Yelp yelp = null;
    private EditText editText;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private MapView map;
    private SpeechRecognizerWrapper speechRecognizer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Context ctx = getApplicationContext();
        setContentView(R.layout.activity_maps);
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = (MapView) findViewById(R.id.MapView);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        yelp = Yelp.get(getApplicationContext());
        editText = (EditText) findViewById(R.id.search_text_maps);
        editText.setText("");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000l, 10.0f, this);
        //setMarkers(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        speechRecognizer = new SpeechRecognizerWrapper();
        speechRecognizer.initializeSpeechService(this, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Initialized location updated");
        current_location = location;
        IMapController mapController = map.getController();
        mapController.setZoom(19);
        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapController.setCenter(startPoint);
        //Picasso.with(this).load(constructInitialUri()).into((ImageView) findViewById(R.id.MapView));
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
        final String search_term = editText.getText().toString();
        if (!TextUtils.isEmpty(search_term) && current_location != null) {
            yelp.initiateAuth(new Yelp.OnAuthComplete() {
                @Override
                public void startFunction() {
                    yelp.search(search_term, current_location.getLatitude(), current_location.getLongitude()).enqueue(new Callback<Search>() {
                        @Override
                        public void onResponse(Call<Search> call, Response<Search> response) {
                            setMarkers(response.body());
                        }

                        @Override
                        public void onFailure(Call<Search> call, Throwable t) {
                            Log.e(TAG, "unable to initiate search", t);
                        }
                    });
                }
            });
        }
    }

    private void setMarkers(Search search) {
        //your items
        ArrayList<OverlayItem> items = new ArrayList<>();

        for (Business business : search.businesses) {
            items.add(new OverlayItem(business.name, business.phone, new GeoPoint(business.coordinates.latitude, business.coordinates.longitude))); // Lat/Lon decimal degrees
        }

        items.add(new OverlayItem("RenCen", "GM Renaissance Center, Detroit", new GeoPoint(42.329, -83.0399))); // Lat/Lon decimal degrees

        ItemizedOverlay<OverlayItem> markers = new ItemizedIconOverlay<OverlayItem>(items,
                getDrawable(R.drawable.marker),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                }, this);

        map.getOverlays().add(markers);
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

    @Override
    protected void onResume() {
        super.onResume();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        goImmersiveMode(2);
    }

    protected void goImmersiveMode(int ch) {

        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int newUiOptions = getWindow().getDecorView().getSystemUiVisibility();

        // Navigation bar hiding: Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            newUiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            newUiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && ch == 1) {
            newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && ch == 2) {
            newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }


    @Override
    public void setText(Iterator<String> matches) {
        StringBuilder listString = new StringBuilder();
        int i = 0;
        while (matches.hasNext()) {
            String current = matches.next();
            listString.append(current + " ");
            if (i == 1) {
                editText.append(current);
            }
            ++i;
        }
    }

    @Override
    public void endOfSpeech() {
        speechRecognizer.endOfSpeech();
    }

    @Override
    public void restart() {
        speechRecognizer.endOfSpeech();
        speechRecognizer.startOfSpeech(this);
    }

    public void startVoice(View view) {
        speechRecognizer.startOfSpeech(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        speechRecognizer.stopListeningSpeechService();
    }
}
