package com.example.wroguide;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationListener;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DrawerLayout drawerLayout;

    private NavigationView navigation;

    private static final String TAG = "MainActivity";

    private final ArrayList<Location> listOfLocations = new ArrayList<Location>();

    private ArrayList<Location> listOfLocationMarkers = new ArrayList<Location>();

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private android.location.Location mLastKnownLocation;

    private boolean mLocationPermissionGranted;

    private LatLng wroclawMainSquare = new LatLng(51.1098994, 17.0321699);

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private Marker mLastSelectedMarker;

    private TextView mTopText;

    private SeekBar mRotationBar;

    private CheckBox mFlatBox;

    private RadioGroup mOptions;

    LocationManager mLocationManager;

    ArrayList oldPoints;


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
        mapFragment.setHasOptionsMenu(true);


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
                        String description = locationSnapshot.child("description").getValue().toString();

                        double latitude = Double.parseDouble(latitudeTemp);
                        double longitude = Double.parseDouble(longitudeTemp);

                        listOfLocations.add(new Location(name, category, latitude, longitude, description));
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
                        drawAllMarkers(listOfLocations, listOfLocationMarkers);
                        Toast.makeText(MapsActivity.this, "Clicked All Locations button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_food:
                        //Do some thing here
                        // add navigation drawer item onclick method here

                        drawMarkers(listOfLocations, listOfLocationMarkers, Category.FOOD);
                        Toast.makeText(MapsActivity.this, "Clicked Food button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_museums:
                        //Do some thing here
                        // add navigation drawer item onclick method here

                        drawMarkers(listOfLocations, listOfLocationMarkers, Category.ART);
                        Toast.makeText(MapsActivity.this, "Clicked Art button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_architecture:
                        //Do some thing here
                        // add navigation drawer item onclick method here
                        drawMarkers(listOfLocations, listOfLocationMarkers, Category.ARCHITECTURAL);
                        Toast.makeText(MapsActivity.this, "Clicked Architecture button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_shopping:
                        //Do some thing here
                        // add navigation drawer item onclick method here

                        drawMarkers(listOfLocations, listOfLocationMarkers, Category.SHOPPING);
                        Toast.makeText(MapsActivity.this, "Clicked Shopping button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_history:
                        //Do some thing here
                        // add navigation drawer item onclick method here
                        drawMarkers(listOfLocations, listOfLocationMarkers, Category.HISTORICAL);
                        Toast.makeText(MapsActivity.this, "Clicked Historical Objects button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                }
                return false;
            }
        });



        //Checking the location

        requestLocation();

    }


    //Pop up message / alert box for close locations

    private void showAlert(double currentLatitude, double currentLongitude, final Location location, final ArrayList<Location> markers) {



        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Close Location:");
        alertBuilder.setMessage(location.getName());
        alertBuilder.setCancelable(true);




        alertBuilder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        markers.remove(location);
                        dialog.cancel();
                    }
                });


        AlertDialog alert = alertBuilder.create();
        alert.show();
    }


    private void drawMarkers(ArrayList<Location> locations, ArrayList<Location> markers, Category chosenCategory){

        mMap.clear();
        markers.clear();


        for (Location l : locations) {
            if(l.getCategory().equals(chosenCategory)) {
                mMap.addMarker(new MarkerOptions().position(
                        new LatLng(l.getLatitude(), l.getLongitude())).title(l.getName()).snippet(l.getDescription()));
                markers.add(l);
            }
        }
    }

    private void drawAllMarkers(ArrayList<Location> locations, ArrayList<Location> markers) {

        mMap.clear();
        markers.clear();


        for (Location l : locations) {
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(l.getLatitude(), l.getLongitude())).title(l.getName()).snippet(l.getDescription()));
            markers.add(l);
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
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
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
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());


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


    private void requestLocation() {

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);

        mLocationManager = (LocationManager)  this.getSystemService(this.LOCATION_SERVICE);
        String provider = mLocationManager.getBestProvider(criteria, true);

        mLocationManager.requestLocationUpdates(provider, 5000, 2, (android.location.LocationListener) locationListener);
    }




    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {

            final LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

            if (!listOfLocationMarkers.isEmpty()) {
                checkCloseLocations(location.getLatitude(), location.getLongitude(), listOfLocationMarkers);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };



    private void checkCloseLocations(double currentLatitude, double currentLongitude, ArrayList<Location> markers) {

        Double smallestDistance = Double.MAX_VALUE;
        int index = 0;
        int smallestIndex = 0;
        for (Location m : markers) {
            Double tempDistance1 = distance(currentLatitude, currentLongitude, m.getLatitude(), m.getLongitude());

            if (smallestDistance > tempDistance1) {

                smallestDistance = tempDistance1;
                smallestIndex = index;
            }

            index++;
        }
            if (smallestDistance < 20) { showAlert(currentLatitude, currentLongitude, markers.get(smallestIndex), markers); }

    }



    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515 * 1609.344;
            return (dist);
        }
    }



    /** Demonstrates customizing the info window and/or its contents. */
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            mWindow.setClickable(false);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mWindow);
            mWindow.setClickable(false);
            return mWindow;
        }

        private void render(Marker marker, View view) {

            String title = marker.getTitle();
            TextView titleUi = view.findViewById(R.id.title);
            if (title != null) {
                titleUi.setText(title);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = view.findViewById(R.id.snippet);
            if (snippet != null) {
                snippetUi.setText(snippet);
            } else {
                snippetUi.setText("");
            }



        }



    }


    private String getRequestUrl(LatLng origin, LatLng dest) {
        //Value of origin
        String str_org = "origin=" + origin.latitude +","+origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude+","+dest.longitude;
        //Set value enable the sensor
        String sensor = "sensor=false";
        //Mode for find direction
        String mode = "mode=walking";
        //Build the full param
        String param = str_org +"&" + str_dest  +  "&" +sensor+"&" +mode + "&key=" + getString(R.string.google_maps_key);
        //Output format
        String output = "json";

        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        return url;
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map



            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat,lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions!=null) {
                oldPoints = points;
                mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }


        }
    }


}
