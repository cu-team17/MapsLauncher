package com.team17.mapslauncher;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    final private String REQUESTING_LOCATION_UPDATES_KEY = "theKey";
    private boolean mRequestingLocationUpdates = true;
    private GoogleMap mMap;
    private Location loc = new Location("Default");
    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("MapsStuff", "Started activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);
        updateValuesFromBundle(savedInstanceState);

        // Default location (Engineering Center at CU)
        loc = new Location("Default");
        loc.setLatitude(40.007121);
        loc.setLongitude(-105.263742);
        loc.setTime(new Date().getTime());

        fm = getSupportFragmentManager();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);

        super.onSaveInstanceState(outState);
    }

    private void updateUI() {
        String cityName = "unknown";
        LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
        try {
            List<Address> addressList = new Geocoder(getApplicationContext(), Locale.getDefault())
                    .getFromLocation(ll.latitude, ll.longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                cityName = addressList.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(ll)
                .title("Marker in " + cityName));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
        mMap.setMinZoomPreference(20.0f);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.v("MapsStuff", "Loc =" + loc.toString());
        updateUI();

    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        // Update the value of mRequestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
        }

        // Update UI to match restored state
        updateUI();
    }
}
