package com.example.wroguide;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationListener;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
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
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, Serializable {

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

    private Polyline route;

    private Marker lastClickedMarker;

    LocationManager mLocationManager;

    ArrayList oldPoints;

    private Timer t;

    private Marker userLocationMarker;

    private MarkerOptions userLocationMarkerOptions;

    static final int PICK_DIRECTIONS_REQUEST = 1;

    private Map<Marker, String> allMarkersMap = new HashMap<Marker, String>();

    private double distance;

    private double time;


    //Handles if user chose directions or close button

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                //String result=data.getStringExtra("result");
                drawRoute(lastClickedMarker.getPosition());
                Intent i = new Intent(getApplicationContext(), CurrentRouteActivityWindow.class);
                Bundle bundle = new Bundle();

                bundle.putDouble("distance", distance);
                bundle.putDouble("time", time);

                i.putExtras(bundle);
                startActivityForResult(i, 11);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                Log.i("directions", "CLOSE");
            }
        }

        if (requestCode == 11) {
            if(route != null) {
                route.remove();
                route = null;
            }
        }
    }


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
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                MapsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        changeLocationIcon();
                    }
                });
            }
        },0,500);



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

                        String id = c+locationSnapshot.child("id").getValue().toString();
                        String name = locationSnapshot.child("name").getValue().toString();
                        String latitudeTemp = locationSnapshot.child("latitude").getValue().toString();
                        String longitudeTemp = locationSnapshot.child("longitude").getValue().toString();
                        String description = locationSnapshot.child("description").getValue().toString();

                        double latitude = Double.parseDouble(latitudeTemp);
                        double longitude = Double.parseDouble(longitudeTemp);

                        listOfLocations.add(new Location(id, name, category, latitude, longitude, description));
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


    //gets icon from drawables

    private int getIcon(Category category) {

        switch(category) {

            case FOOD: return(R.drawable.ic_local_dining_black_24dp);

            case SHOPPING: return(R.drawable.ic_local_grocery_store_black_24dp);

            case ART: return(R.drawable.ic_local_activity_black_24dp);

            case HISTORICAL: return(R.drawable.ic_access_time_black_24dp);

            case ARCHITECTURAL: return(R.drawable.ic_store_mall_directory_black_24dp);

        }
        return 0;
    }

    //changes vector to bitmap

    private BitmapDescriptor vectorToBitmap(Category category, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), getIcon(category), null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }



    private void drawMarkers(ArrayList<Location> locations, ArrayList<Location> markers, Category chosenCategory){

        mMap.clear();
        allMarkersMap.clear();
        markers.clear();

        BitmapDescriptor icon = vectorToBitmap(chosenCategory, Color.parseColor("#0033AA"));

        for (Location l : locations) {
            if(l.getCategory().equals(chosenCategory)) {

                Marker tempMarker = mMap.addMarker(new MarkerOptions().position(
                        new LatLng(l.getLatitude(), l.getLongitude())).title(l.getName()).snippet(l.getDescription()).icon(icon));
                allMarkersMap.put(tempMarker, l.getId());
                markers.add(l);
            }
        }

        userLocationMarker = mMap.addMarker(userLocationMarkerOptions);
    }

    private void drawAllMarkers(ArrayList<Location> locations, ArrayList<Location> markers) {

        mMap.clear();
        allMarkersMap.clear();
        markers.clear();

        BitmapDescriptor icon;

        for (Location l : locations) {
            icon = vectorToBitmap(l.getCategory(), Color.parseColor("#0033AA"));
            Marker tempMarker = mMap.addMarker(new MarkerOptions().position(
                    new LatLng(l.getLatitude(), l.getLongitude())).title(l.getName()).snippet(l.getDescription()).icon(icon));
            allMarkersMap.put(tempMarker, l.getId());
            markers.add(l);
        }
        userLocationMarker = mMap.addMarker(userLocationMarkerOptions);
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
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
                            LatLng loc = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            Bitmap icon = getBitmap(R.drawable.dark_marker);
                            userLocationMarkerOptions = new MarkerOptions()
                                    .zIndex(1)
                                    .position(loc)
                                    .icon(BitmapDescriptorFactory.fromBitmap(icon));
                            userLocationMarker = mMap.addMarker(userLocationMarkerOptions);
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
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {


                if (marker.equals(userLocationMarker)) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                    return true;
                }

                String id = allMarkersMap.get(marker);
                String title = marker.getTitle();
                String snippet = marker.getSnippet();
                distance = distance(marker.getPosition().latitude,
                        marker.getPosition().longitude, mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude());
                time = 1.4 * distance;

                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));


                Intent i = new Intent(getApplicationContext(), MarkerInfoActivityWindow.class);

                Bundle bundle = new Bundle();

                bundle.putString("id", id);
                bundle.putString("title", title);
                bundle.putString("snippet", snippet);

                i.putExtras(bundle);

                lastClickedMarker = marker;

                startActivityForResult(i, PICK_DIRECTIONS_REQUEST, bundle);





                return true;
            }

        });
        /*mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                drawRoute(marker);
                lastClickedMarker = marker;
            }
        });
        mMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {

                if(route != null) {
                    route.remove();
                    route = null;
                }
                lastClickedMarker = null;
            }
        }); */

    }

    private void changeLocationIcon() {
        if(userLocationMarker != null) {
            if(userLocationMarker.getAlpha() == 0.5f) {
                userLocationMarker.setAlpha(1.0f);
            } else {
                userLocationMarker.setAlpha(0.5f);
            }
        }
    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
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
            mLastKnownLocation = location;
            Bitmap icon = getBitmap(R.drawable.dark_marker);
            userLocationMarkerOptions = new MarkerOptions()
                    .zIndex(1)
                    .position(currentLocation)
                    .icon(BitmapDescriptorFactory.fromBitmap(icon));
            if(userLocationMarker != null) {
                userLocationMarker.setPosition(currentLocation);
            } else {
                userLocationMarker = mMap.addMarker(new MarkerOptions()
                    .zIndex(1)
                    .position(currentLocation)
                    .icon(BitmapDescriptorFactory.fromBitmap(icon)));
            }

            if (!listOfLocationMarkers.isEmpty()) {
                checkCloseLocations(location.getLatitude(), location.getLongitude(), listOfLocationMarkers);
            }

            if(route != null) {
                route.remove();
                drawRoute(lastClickedMarker.getPosition());
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
            if (smallestDistance < 20) {
                showAlert(currentLatitude, currentLongitude, markers.get(smallestIndex), markers);
                if(route != null && lastClickedMarker.equals(markers.get(smallestIndex))) {
                    route.remove();
                    route = null;
                }
            }

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

            if (polylineOptions != null) {
                oldPoints = points;
                route = mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }


        }
    }

    public void drawRoute(LatLng destination) {
        LatLng origin = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        String url = "";
        if(destination != null) {
            url = getRequestUrl(origin, destination);
        }

        TaskRequestDirections requestDirectionsTask = new TaskRequestDirections();

        requestDirectionsTask.execute(url + "&mode=walking");
    }

    public void setLastClickedMarker(Marker lastClickedMarker) {
        this.lastClickedMarker = lastClickedMarker;
    }
}
