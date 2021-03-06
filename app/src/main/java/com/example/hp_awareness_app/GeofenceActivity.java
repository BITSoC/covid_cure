package com.example.hp_awareness_app;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GeofenceActivity extends AppCompatActivity
    implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        //  GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        ResultCallback<Status> {

  private static final String TAG = GeofenceActivity.class.getSimpleName();

  private GoogleMap map;
  private GoogleApiClient googleApiClient;
  private Location lastLocation;

  private TextView textLat, textLong;
  SharedPreferences sharedPrefs;
  SharedPreferences.Editor editor;

  private MapFragment mapFragment;
  Boolean check;

  public double forNextLat, forNextLong;
  DatabaseReference reference;

  private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";

  // Create a Intent send by the notification
  public static Intent makeNotificationIntent(Context context, String msg) {
    Intent intent = new Intent(context, GeofenceActivity.class);
    intent.putExtra(NOTIFICATION_MSG, msg);
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);
    textLat = (TextView) findViewById(R.id.lat);
    textLong = (TextView) findViewById(R.id.lon);

    sharedPrefs = getSharedPreferences("App", MODE_PRIVATE);
    editor = sharedPrefs.edit();

    // initialize GoogleMaps
    initGMaps();

    // create GoogleApiClient
    createGoogleApi();
  }

  // Create GoogleApiClient instance
  private void createGoogleApi() {
    Log.d(TAG, "createGoogleApi()");
    if (googleApiClient == null) {
      googleApiClient =
          new GoogleApiClient.Builder(this)
              .addConnectionCallbacks(this)
              .addOnConnectionFailedListener(this)
              .addApi(LocationServices.API)
              .build();
    }
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Call GoogleApiClient connection when starting the Activity
    googleApiClient.connect();
  }

  @Override
  protected void onStop() {
    super.onStop();

    // Disconnect GoogleApiClient when stopping Activity
    googleApiClient.disconnect();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.geofence:
        {
          getCurrentLocation();
          item.setEnabled(false);

          //   startGeofence();

          return true;
        }
      case R.id.clear:
        {
          clearGeofence();
          return true;
        }
    }
    return super.onOptionsItemSelected(item);
  }

  private final int REQ_PERMISSION = 999;

  // Check for permission to access Location
  private boolean checkPermission() {
    Log.d(TAG, "checkPermission()");
    // Ask for permission if it wasn't granted yet
    if (Build.VERSION.SDK_INT >= 21) {
      check =
          (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
              == PackageManager.PERMISSION_GRANTED);
    }
    return check;
  }

  // Asks for permission
  private void askPermission() {
    Log.d(TAG, "askPermission()");
    ActivityCompat.requestPermissions(
        this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION);
  }

  // Verify user's response of the permission requested
  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    Log.d(TAG, "onRequestPermissionsResult()");
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case REQ_PERMISSION:
        {
          if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
            getLastKnownLocation();

          } else {
            // Permission denied
            permissionsDenied();
          }
          break;
        }
    }
  }

  // App cannot work without the permissions
  private void permissionsDenied() {
    Log.w(TAG, "permissionsDenied()");
    // TODO close app and warn user
  }

  // Initialize GoogleMaps
  private void initGMaps() {
    mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }

  // Callback called when Map is ready
  @Override
  public void onMapReady(GoogleMap googleMap) {
    Log.d(TAG, "onMapReady()");
    map = googleMap;
    //  map.setOnMapClickListener(this);
    map.setOnMarkerClickListener(this);
  }

  //    @Override
  //    public void onMapClick(LatLng latLng) {
  //        Log.d(TAG, "onMapClick(" + latLng + ")");
  //        markerForGeofence(latLng);
  //    }

  @Override
  public boolean onMarkerClick(Marker marker) {
    Log.d(TAG, "onMarkerClickListener: " + marker.getPosition());
    return false;
  }

  private LocationRequest locationRequest;
  // Defined in mili seconds.
  // This number in extremely low, and should be used only for debug
  private final int UPDATE_INTERVAL = 1000;
  private final int FASTEST_INTERVAL = 900;

  // Start location Updates
  private void startLocationUpdates() {
    Log.i(TAG, "startLocationUpdates()");
    locationRequest =
        LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_INTERVAL);

    if (checkPermission())
      LocationServices.FusedLocationApi.requestLocationUpdates(
          googleApiClient, locationRequest, this);
  }

  @Override
  public void onLocationChanged(Location location) {
    Log.d(TAG, "onLocationChanged [" + location + "]");
    lastLocation = location;
    writeActualLocation(location);
  }

  // GoogleApiClient.ConnectionCallbacks connected
  @Override
  public void onConnected(@Nullable Bundle bundle) {
    Log.i(TAG, "onConnected()");
    getLastKnownLocation();
    recoverGeofenceMarker();
  }

  // GoogleApiClient.ConnectionCallbacks suspended
  @Override
  public void onConnectionSuspended(int i) {
    Log.w(TAG, "onConnectionSuspended()");
  }

  // GoogleApiClient.OnConnectionFailedListener fail
  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.w(TAG, "onConnectionFailed()");
  }

  // Get last known location
  private void getLastKnownLocation() {
    Log.d(TAG, "getLastKnownLocation()");
    if (checkPermission()) {
      lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
      if (lastLocation != null) {
        Log.i(
            TAG,
            "LasKnown location. "
                + "Long: "
                + lastLocation.getLongitude()
                + " | Lat: "
                + lastLocation.getLatitude());
        writeLastLocation();
        startLocationUpdates();

      } else {
        Log.w(TAG, "No location retrieved yet");
        getCurrentLocation();
        startLocationUpdates();
      }
    } else askPermission();
  }

  private void writeActualLocation(Location location) {
    textLat.setText("Lat: " + location.getLatitude());
    textLong.setText("Long: " + location.getLongitude());

    markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
  }

  private void writeLastLocation() {
    writeActualLocation(lastLocation);
  }

  private Marker locationMarker;

  private void markerLocation(LatLng latLng) {
    Log.i(TAG, "markerLocation(" + latLng + ")");
    String title = latLng.latitude + ", " + latLng.longitude;
    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
    if (map != null) {
      if (locationMarker != null) locationMarker.remove();
      locationMarker = map.addMarker(markerOptions);
      float zoom = 15f;
      CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
      map.animateCamera(cameraUpdate);
    }
  }

  private double latitude, longitude;
  LatLng l1;

  public void getCurrentLocation() {

    final LocationRequest locationRequest = new LocationRequest();
    locationRequest.setInterval(10000);
    locationRequest.setFastestInterval(3000);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    LocationServices.getFusedLocationProviderClient(GeofenceActivity.this)
        .requestLocationUpdates(
            locationRequest,
            new LocationCallback() {

              @Override
              public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LocationServices.getFusedLocationProviderClient(GeofenceActivity.this)
                    .removeLocationUpdates(this);
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                  int latestLoc = locationResult.getLocations().size() - 1;

                  latitude = locationResult.getLocations().get(latestLoc).getLatitude();
                  longitude = locationResult.getLocations().get(latestLoc).getLongitude();

                  Geocoder geocoder = new Geocoder(GeofenceActivity.this, Locale.getDefault());
                  try {
                    List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                    String address1 = addressList.get(0).getAddressLine(0);
                    String area = addressList.get(0).getLocality();
                    String city = addressList.get(0).getAdminArea();
                    String country = addressList.get(0).getCountryName();
                    String postalCode = addressList.get(0).getPostalCode();
                    String fullAddress =
                        address1 + ", " + area + ", " + city + ", " + country + ", " + postalCode;

                    editor.putString("Geo Location", fullAddress);
                    editor.commit();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = user.getUid();
                    reference =
                        FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Geofencing")
                            .child(uid);

                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("Address", fullAddress);
                    reference.updateChildren(userMap);

                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                }
                Handler handler = new Handler();
                handler.postDelayed(
                    new Runnable() {
                      @Override
                      public void run() {
                        l1 = new LatLng(latitude, longitude);

                        markerForGeofence(l1);
                      }
                    },
                    10000);
              }
            },
            Looper.getMainLooper());
  }

  private Marker geoFenceMarker;

  private void markerForGeofence(LatLng latLng) {
    Log.i(TAG, "markerForGeofence(" + latLng + ")");
    String title = latLng.latitude + ", " + latLng.longitude;
    // Define marker options
    MarkerOptions markerOptions =
        new MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            .title(title);
    if (map != null) {
      // Remove last geoFenceMarker
      if (geoFenceMarker != null) // ye h taakk jb change ho to pichla remove ho jaae
      geoFenceMarker.remove();

      geoFenceMarker = map.addMarker(markerOptions);
    }
    startGeofence();
  }

  // Start Geofence creation process
  private void startGeofence() {
    Log.i(TAG, "startGeofence()");
    if (geoFenceMarker != null) {
      Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
      GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
      addGeofence(geofenceRequest);

    } else {
      Log.e(TAG, "Geofence marker is null");
    }
  }

  private static final long GEO_DURATION = 60 * 60 * 1000;
  private static final String GEOFENCE_REQ_ID = "My Geofence";
  private static final float GEOFENCE_RADIUS = 100.0f; // in meters

  // Create a Geofence
  private Geofence createGeofence(LatLng latLng, float radius) {
    Log.d(TAG, "createGeofence");
    return new Geofence.Builder()
        .setRequestId(GEOFENCE_REQ_ID)
        .setCircularRegion(latLng.latitude, latLng.longitude, radius)
        .setExpirationDuration(GEO_DURATION)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
        .build();
  }

  // Create a Geofence Request
  private GeofencingRequest createGeofenceRequest(Geofence geofence) {
    Log.d(TAG, "createGeofenceRequest");
    return new GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofence(geofence)
        .build();
  }

  private PendingIntent geoFencePendingIntent;
  private final int GEOFENCE_REQ_CODE = 0;

  private PendingIntent createGeofencePendingIntent() {
    Log.d(TAG, "createGeofencePendingIntent");
    if (geoFencePendingIntent != null) return geoFencePendingIntent;

    Intent intent = new Intent(this, GeofenceTransitionService.class);
    return PendingIntent.getService(
        this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  // Add the created GeofenceRequest to the device's monitoring list
  private void addGeofence(GeofencingRequest request) {
    Log.d(TAG, "addGeofence");
    if (checkPermission())
      LocationServices.GeofencingApi.addGeofences(
              googleApiClient, request, createGeofencePendingIntent())
          .setResultCallback(this);
  }

  @Override
  public void onResult(@NonNull Status status) {
    Log.i(TAG, "onResult: " + status);
    if (status.isSuccess()) {
      saveGeofence();
      drawGeofence();
    } else {
      // inform about fail
    }
  }

  // Draw Geofence circle on GoogleMap
  private Circle geoFenceLimits;

  private void drawGeofence() {
    Log.d(TAG, "drawGeofence()");

    if (geoFenceLimits != null) geoFenceLimits.remove();

    CircleOptions circleOptions =
        new CircleOptions()
            .center(geoFenceMarker.getPosition())
            .strokeColor(Color.argb(50, 70, 70, 70))
            .fillColor(Color.argb(100, 150, 150, 150))
            .radius(GEOFENCE_RADIUS);
    geoFenceLimits = map.addCircle(circleOptions);
  }

  private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
  private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";

  // Saving GeoFence marker with prefs mng
  private void saveGeofence() {
    Log.d(TAG, "saveGeofence()");
    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();

    editor.putLong(
        KEY_GEOFENCE_LAT, Double.doubleToRawLongBits(geoFenceMarker.getPosition().latitude));
    editor.putLong(
        KEY_GEOFENCE_LON, Double.doubleToRawLongBits(geoFenceMarker.getPosition().longitude));
    editor.apply();
  }

  // Recovering last Geofence marker
  private void recoverGeofenceMarker() {
    Log.d(TAG, "recoverGeofenceMarker");
    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

    if (sharedPref.contains(KEY_GEOFENCE_LAT) && sharedPref.contains(KEY_GEOFENCE_LON)) {
      double lat = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LAT, -1));
      double lon = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LON, -1));
      LatLng latLng = new LatLng(lat, lon);
      markerForGeofence(latLng);
      drawGeofence();
    }
  }

  // Clear Geofence
  private void clearGeofence() {
    Log.d(TAG, "clearGeofence()");
    LocationServices.GeofencingApi.removeGeofences(googleApiClient, createGeofencePendingIntent())
        .setResultCallback(
            new ResultCallback<Status>() {
              @Override
              public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                  // remove drawing
                  removeGeofenceDraw();
                }
              }
            });
  }

  private void removeGeofenceDraw() {
    Log.d(TAG, "removeGeofenceDraw()");
    if (geoFenceMarker != null) geoFenceMarker.remove();
    if (geoFenceLimits != null) geoFenceLimits.remove();
  }
}
