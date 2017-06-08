package com.harsukh.demo_dealers;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.harsukh.yelpapi.Business;
import com.harsukh.yelpapi.Search;
import com.harsukh.yelpapi.Yelp;

import java.util.ArrayList;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private EditText mEditText;
    private Location current_location = null;
    private Yelp yelp;
    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (google_serv_check()) {
            setContentView(R.layout.activity_main);
            initMaps();
            mEditText = (EditText) findViewById(R.id.search_text);
            mEditText.setText("");
            yelp = Yelp.get(this);
        }
    }

    private void initMaps() //setting the map fragment and loading it
    {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.MapFragment); //find the fragment by id
        mapFragment.getMapAsync(this); //loads maps asynchronously then calls the onMapsReady
    }

    public boolean google_serv_check() //checks if service is available
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int isAvailable = apiAvailability.isGooglePlayServicesAvailable(getApplicationContext());
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        }
        if (apiAvailability.isUserResolvableError(isAvailable)) {
            Dialog dialog = apiAvailability.getErrorDialog(MainActivity.this, isAvailable, 0);
            dialog.show();
        }
        return false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (mGoogleMap != null) {
            mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    MainActivity.this.setMarker(latLng.toString(), latLng.latitude, latLng.longitude);
                }
            });

            //custom window designed in here too
            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.info_window, null); //retrieves the info_window xml layout
                    //the root in the function is set to null because no root is needed
                    TextView tvLocality = (TextView) v.findViewById(R.id.tv_locality);
                    TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
                    TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
                    TextView dist = (TextView) v.findViewById(R.id.tv_dist);
                    //setting the textViews  use the marker to retrieve this data
                    LatLng ll = marker.getPosition();
                    tvLocality.setText(marker.getTitle());
                    tvLat.setText("Latitude: " + ll.latitude);
                    tvLng.setText("Longitude: " + ll.longitude);
                    if (current_location != null) {
                        Location set_ll = new Location(marker.getTitle());
                        set_ll.setLatitude(ll.latitude);
                        set_ll.setLongitude(ll.longitude);
                        float distance = current_location.distanceTo(set_ll);
                        dist.setText("Distance to: " + distance);
                    } else {
                        dist.setText("Distance to: ????");
                    }
                    return v;
                }
            });
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API) //adds the api for location services
                .addConnectionCallbacks(this) // callback method for connection
                .addOnConnectionFailedListener(this) //checks if connection failed
                .build();
        mGoogleApiClient.connect();
    }

    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private ArrayList<Polyline> lines = new ArrayList<Polyline>();
    static final int POLYGON_POINTS = 100;

    private void setMarker(String locality, double x, double y) {
        //removing polygon if already drawn
        if (markers.size() == POLYGON_POINTS) {
            removeEverything();
        }
        //creating marker options
        MarkerOptions options = new MarkerOptions()
                .title(locality)
                .draggable(false) //drag a marker
                .position(new LatLng(x, y)); //get position of marker
        if (locality.equals("current location")) {
            markers.add(0, mGoogleMap.addMarker(options));
        } else {
            markers.add(mGoogleMap.addMarker(options));
        }
        if (markers.size() > 1) {
            drawline(markers.get(0), markers.get(markers.size() - 1));
        }
    }

    private void removeEverything() {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
        for (Polyline line : lines) {
            line.remove();
        }
        lines.clear();
    }

    private void drawline(Marker marker1, Marker marker2) {
        Location locationA = new Location("point A");

        locationA.setLatitude(marker1.getPosition().latitude);
        locationA.setLongitude(marker1.getPosition().longitude);

        Location locationB = new Location("point B");

        locationB.setLatitude(marker2.getPosition().latitude);
        locationB.setLongitude(marker2.getPosition().longitude);

        float distance = locationA.distanceTo(locationB);
        PolylineOptions options = new PolylineOptions()
                .add(marker1.getPosition())
                .add(marker2.getPosition())
                .color(Color.BLUE)
                .width(3);
        lines.add(mGoogleMap.addPolyline(options));
        Toast.makeText(MainActivity.this, "Distance to: " + distance, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connection success!");
        mLocationRequest = LocationRequest.create(); //creates location request object
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //sets priority of request with high location accuract
        mLocationRequest.setInterval(1000); //location retrieved every second
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this); //updates location and listens for the location
        //calls  onLocationChanged();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "failed to connect to location service " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "failed to connect to location service " + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) //gets location based on updated position
    {
        Log.i(TAG, "Location Changed called");
        if (location == null) {
            Toast.makeText(getApplicationContext(), "Can't get location", Toast.LENGTH_LONG).show();
        } else {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());


            if (current_location == null) {
                current_location = location;
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
                mGoogleMap.animateCamera(update);
                setMarker("current location", location.getLatitude(), location.getLongitude());
            } else {
                float distance = current_location.distanceTo(location);
                if (Math.round(distance) > 100) {
                    current_location = location;
                    if (markers.get(0) != null) {
                        markers.remove(0);
                    }
                    setMarker("current location", location.getLatitude(), location.getLongitude());
                }
            }
        }
    }

    public void geoLocate(View v) {
        String search_term = mEditText.getText().toString();
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

    public void setMarkers(Search search) {
        for (Business business : search.businesses) {
            setMarker(business.name, business.coordinates.latitude, business.coordinates.longitude);
        }
    }
}
