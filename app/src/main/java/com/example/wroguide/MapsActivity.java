package com.example.wroguide;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        final ArrayList<String> categories = new ArrayList<String>();
        final ArrayList<ArrayList<Location>> listOfLocations = new ArrayList<ArrayList<Location>>();

        FirebaseApp.initializeApp(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("category");



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

                for(DataSnapshot categorySnapshot : dataSnapshot.getChildren()){
                    categories.add(categorySnapshot.getKey());


                    listOfLocations.add(new ArrayList<Location>());
                    //System.out.println("Wielkosc: " + listOfLocations.size() + categories.size());
                    for(DataSnapshot locationSnapshot: categorySnapshot.getChildren()) {
                        //System.out.println(locationSnapshot.child("name").getValue());

                        String name = locationSnapshot.child("name").getValue().toString();
                        String category = categories.get(categories.size()-1);
                        Long longitude = (Long)locationSnapshot.child("longitude").getValue();
                        Long latitude = (Long)locationSnapshot.child("latitude").getValue();



                        listOfLocations.get(categories.size()-1).add(new Location(name, category, latitude, longitude));
                        System.out.println(name + category + longitude + latitude);



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
                    case R.id.nav_food:
                        //Do some thing here
                        // add navigation drawer item onclick method here
                        Toast.makeText(MapsActivity.this, "Clicked Food button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                    case R.id.nav_museums:
                        //Do some thing here
                        // add navigation drawer item onclick method here
                        Toast.makeText(MapsActivity.this, "Clicked Art button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_architecture:
                        //Do some thing here
                        // add navigation drawer item onclick method here
                        Toast.makeText(MapsActivity.this, "Clicked Architecture button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_shopping:
                        //Do some thing here
                        // add navigation drawer item onclick method here
                        Toast.makeText(MapsActivity.this, "Clicked Shopping button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_history:
                        //Do some thing here
                        // add navigation drawer item onclick method here
                        Toast.makeText(MapsActivity.this, "Clicked Historical Objects button!",
                                Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                }
                return false;
            }
        });

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

        // Add a marker in Sydney and move the camera
        LatLng wroclawMainSquare = new LatLng(51.1098994,17.0321699);
        mMap.addMarker(new MarkerOptions().position(wroclawMainSquare).title("Marker in Main Square"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(wroclawMainSquare, 17));
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




}
