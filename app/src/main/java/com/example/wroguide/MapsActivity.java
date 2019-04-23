package com.example.wroguide;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DrawerLayout drawerLayout;

    private NavigationView navigation;

    private static final String TAG = "MainActivity";

    private final ArrayList<Location> listOfLocations = new ArrayList<Location>();

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private android.location.Location mLastKnownLocation;

    private boolean mLocationPermissionGranted;

    private LatLng wroclawMainSquare = new LatLng(51.1098994,17.0321699);

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FirebaseApp.initializeApp(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("category");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionbar.setTitle("WroGuide");

        drawerLayout = findViewById(R.id.drawer_layout);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    Category category;
                    String c = categorySnapshot.getKey();
                    switch (c) {
                        case "food":
                            category = Category.FOOD;
                            break;
                        case "shopping":
                            category = Category.SHOPPING;
                            break;
                        case "museum":
                            category = Category.ART;
                            break;
                        case "historical":
                            category = Category.HISTORICAL;
                            break;
                        case "architecture":
                            category = Category.ARCHITECTURAL;
                            break;
                        default:
                            category = Category.FOOD;
                    }
                    for (DataSnapshot locationSnapshot : categorySnapshot.getChildren()) {

                        String name = locationSnapshot.child("name").getValue().toString();
                        String latitudeTemp = locationSnapshot.child("latitude").getValue().toString();
                        String longitudeTemp = locationSnapshot.child("longitude").getValue().toString();

                        double latitude = Double.parseDouble(latitudeTemp);
                        double longitude = Double.parseDouble(longitudeTemp);

                        listOfLocations.add(new Location(name, category, latitude, longitude));
                        System.out.println("NAME: " + name);
                    }

                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        navigation = (NavigationView) findViewById(R.id.nav_view);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.nav_all:
                        drawAllMarkers(listOfLocations);
                        Toast.makeText(MapsActivity.this, "Clicked All Locations button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_food:
                        //Do some thing here
                        // add navigation drawer item onclick method here

                        drawMarkers(listOfLocations, Category.FOOD);
                        Toast.makeText(MapsActivity.this, "Clicked Food button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_museums:
                        //Do some thing here
                        // add navigation drawer item onclick method here

                        drawMarkers(listOfLocations, Category.ART);
                        Toast.makeText(MapsActivity.this, "Clicked Art button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_architecture:
                        //Do some thing here
                        // add navigation drawer item onclick method here
                        drawMarkers(listOfLocations, Category.ARCHITECTURAL);
                        Toast.makeText(MapsActivity.this, "Clicked Architecture button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_shopping:
                        //Do some thing here
                        // add navigation drawer item onclick method here

                        drawMarkers(listOfLocations, Category.SHOPPING);
                        Toast.makeText(MapsActivity.this, "Clicked Shopping button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_history:
                        //Do some thing here
                        // add navigation drawer item onclick method here
                        drawMarkers(listOfLocations, Category.HISTORICAL);
                        Toast.makeText(MapsActivity.this, "Clicked Historical Objects button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                }
                return false;
            }
        });

    }

    private void drawMarkers(ArrayList<Location> locations, Category chosenCategory){

        mMap.clear();
        for (Location l : locations) {
            if(l.getCategory().equals(chosenCategory)) {
                mMap.addMarker(new MarkerOptions().position(
                        new LatLng(l.getLatitude(), l.getLongitude())).title(l.getName()));
            }
        }
    }

    private void drawAllMarkers(ArrayList<Location> locations) {
        mMap.clear();
        for (Location l : locations) {
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(l.getLatitude(), l.getLongitude())).title(l.getName()));
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(wroclawMainSquare, 15));

                        if (task.isSuccessful()) {
                            mLastKnownLocation = (android.location.Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), 15));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(wroclawMainSquare, 15));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

   @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }
}
