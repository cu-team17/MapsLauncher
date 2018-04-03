package com.team17.mapslauncher;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    final private String REQUESTING_LOCATION_UPDATES_KEY = "theKey";
    final private String MAP_LOG_TAG = "MapsLauncher";
    private boolean mRequestingLocationUpdates = true;
    private GoogleMap mMap;
    private Location loc = new Location("Default");
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;

    LinearLayout weatherWidget;
    LinearLayout musicWidget;
    LinearLayout speedWidget;

    AppWidgetManager mAppWidgetManager;
    AppWidgetHost mAppWidgetHost;

    int REQUEST_BIND_WIDGET = 111;

    AppWidgetProviderInfo weatherWidgetInfo;
    AppWidgetProviderInfo spotifyWidgetInfo;
    AppWidgetProviderInfo speedometerWidgetInfo;

    int weatherWidgetId;
    int spotifyWidgetId;
    int speedometerWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);
        updateValuesFromBundle(savedInstanceState);

        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new AppWidgetHost(this, R.id.APPWIDGET_HOST_ID);

        weatherWidget = (LinearLayout) findViewById(R.id.weather_widget);
        musicWidget = (LinearLayout) findViewById(R.id.music_widget);
        speedWidget = (LinearLayout) findViewById(R.id.speed_widget);

        weatherWidgetInfo = new AppWidgetProviderInfo();
        spotifyWidgetInfo = new AppWidgetProviderInfo();
        speedometerWidgetInfo = new AppWidgetProviderInfo();

        weatherWidgetId = mAppWidgetHost.allocateAppWidgetId();
        spotifyWidgetId = mAppWidgetHost.allocateAppWidgetId();
        speedometerWidgetId = mAppWidgetHost.allocateAppWidgetId();

        List<AppWidgetProviderInfo> appWidgetInfos = new ArrayList<AppWidgetProviderInfo>();
        appWidgetInfos = mAppWidgetManager.getInstalledProviders();

        //Getting the appInfo for each widget
        for(int j = 0; j < appWidgetInfos.size(); j++)
        {
//            Log.d("WidgetInfo", "appWidgetInfo Size = " + appWidgetInfos.size());
//            Log.d("WidgetInfo", "WidgetProviderInfo = " + appWidgetInfos);

            //Gets the app info for the speedometer widget
            if (appWidgetInfos.get(j).provider.getPackageName().equals("it.opbyte.odometerwidget") && appWidgetInfos.get(j).provider.getClassName().equals("it.opbyte.odometerwidget.OdometerWidget_2x2"))
            {
                speedometerWidgetInfo = appWidgetInfos.get(j);
                createSpeedometerWidget(speedometerWidgetId);
            }

            //Gets the app info for the Spotify widget
            if (appWidgetInfos.get(j).provider.getPackageName().equals("com.spotify.music") && appWidgetInfos.get(j).provider.getClassName().equals("com.spotify.music.spotlets.widget.SpotifyWidget"))
            {
                spotifyWidgetInfo = appWidgetInfos.get(j);
                createSpotifyWidget(spotifyWidgetId);
            }

            //Gets the app info for the weather widget
            if (appWidgetInfos.get(j).provider.getPackageName().equals("com.graph.weather.forecast.channel") && appWidgetInfos.get(j).provider.getClassName().equals("com.graph.weather.forecast.channel.widgets.WidgetProvider"))
            {
                weatherWidgetInfo = appWidgetInfos.get(j);
                createWeatherWidget(weatherWidgetId);
            }
        }

        // Default location (Engineering Center at CU)
        loc = new Location("Default");
        loc.setLatitude(40.007121);
        loc.setLongitude(-105.263742);
        loc.setTime(new Date().getTime());

        FragmentManager fm = getSupportFragmentManager();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                getLastKnownLocation();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this, 1);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                // Update UI with location data
                loc = locationResult.getLocations().get(0);
                updateUI();
            }
        };
    }

    //Creates the speedometer widget
    public void createSpeedometerWidget(int speedometerWidgetId) {
        AppWidgetHostView hostView = mAppWidgetHost.createView(this, speedometerWidgetId, speedometerWidgetInfo);
        hostView.setAppWidget(speedometerWidgetId, speedometerWidgetInfo);
        speedWidget.addView(hostView);
    }

    //Creates the Spotify widget
    public void createSpotifyWidget(int spotifyWidgetId) {
        AppWidgetHostView hostView = mAppWidgetHost.createView(this, spotifyWidgetId, spotifyWidgetInfo);
        hostView.setAppWidget(spotifyWidgetId, spotifyWidgetInfo);
        musicWidget.addView(hostView);
    }

    //Creates the weather widget
    public void createWeatherWidget(int weatherWidgetId) {
        AppWidgetHostView hostView = mAppWidgetHost.createView(this, weatherWidgetId, weatherWidgetInfo);
        hostView.setAppWidget(weatherWidgetId, weatherWidgetInfo);
        weatherWidget.addView(hostView);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAppWidgetHost.startListening();

        //Creates the intent for the weather widget to allow it to update
        boolean weatherPermission = false;
        weatherPermission = mAppWidgetManager.bindAppWidgetIdIfAllowed(weatherWidgetId, weatherWidgetInfo.provider); //Gives weather widget permission to access app info
        if (!weatherPermission) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, weatherWidgetId);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, weatherWidgetInfo.provider);
            startActivityForResult(intent, REQUEST_BIND_WIDGET);
        }

        //Creates the intent for the spotify widget to allow it to update
        boolean spotifyPermission = false;
        spotifyPermission = mAppWidgetManager.bindAppWidgetIdIfAllowed(spotifyWidgetId, spotifyWidgetInfo.provider); //Gives spotify widget permission to access app info
        if (!spotifyPermission) {
            Intent intent2 = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
            intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, spotifyWidgetId);
            intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, spotifyWidgetInfo.provider);
            startActivityForResult(intent2, REQUEST_BIND_WIDGET);
        }

        //Creates the intent for the speedometer widget to allow it to update
        boolean speedometerPermission = false;
        speedometerPermission = mAppWidgetManager.bindAppWidgetIdIfAllowed(speedometerWidgetId, speedometerWidgetInfo.provider); //Gives speedometer widget permission to access app info
        if (!speedometerPermission) {
            Intent intent3 = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
            intent3.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, speedometerWidgetId);
            intent3.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, speedometerWidgetInfo.provider);
            startActivityForResult(intent3, REQUEST_BIND_WIDGET);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAppWidgetHost.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        } else {
            buildGoogleApiClient();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);

        super.onSaveInstanceState(outState);
    }

    private void updateUI() {
        String cityName = "unknown";
        Log.i(MAP_LOG_TAG, "Updating UI with new location" + loc.toString());
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
        updateUI();

    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                loc = location;
                                updateUI();
                            }
                        }
                    });
        } else {
            Log.w(MAP_LOG_TAG, "Failed permissions in getLastKnownLocation");
            requestLocationPermissions();
        }
    }

    protected void requestLocationPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, 1);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        createLocationRequest();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        } else {
            Log.w(MAP_LOG_TAG, "Failed permissions in startLocationUpdates");
            requestLocationPermissions();
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
