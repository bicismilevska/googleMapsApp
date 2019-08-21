package com.example.googlemapsapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;


import java.util.Arrays;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG="MapsActivity";
    private static final String FINE_LOCATION= Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION=Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String INTERNET=Manifest.permission.INTERNET;
    private static final int LOCATION_PERMISSION_REQUEST_CODE=333;
    private static final float DEFAULT_ZOOM =15f ;
    public FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted=false;
    private AutocompleteSupportFragment autocompleteFragment;

    private GoogleMap mapGoogle;
    private View mapview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
      //  editText=findViewById(R.id.edittext);
        checkForPermission();
//

    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapGoogle=googleMap;
         findCurrentLocation();
            mapGoogle.setMyLocationEnabled(true);
            mapGoogle.getUiSettings().setMyLocationButtonEnabled(true);
        initGooglePlacesApi();


        if (mapview != null &&
                mapview.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapview.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 190);
        }



    }
    public void initializeMap(){
        Log.i(TAG,"Initializing Map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapview=mapFragment.getView();
        mapFragment.getMapAsync(this);

    }

    private void checkForPermission()
    {
        String [] permissions={FINE_LOCATION,COARSE_LOCATION,INTERNET};
       if(ContextCompat.checkSelfPermission(MapsActivity.this,INTERNET)==PackageManager.PERMISSION_GRANTED) {
           if (ContextCompat.checkSelfPermission(MapsActivity.this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
               if (ContextCompat.checkSelfPermission(MapsActivity.this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                   locationPermissionGranted = true;
                   initializeMap();
               } else {
                   ActivityCompat.requestPermissions(MapsActivity.this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
               }
           } else {
               ActivityCompat.requestPermissions(MapsActivity.this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
           }
       }else{
           ActivityCompat.requestPermissions(MapsActivity.this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
       }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG,"on request permission result");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted=false;
        if(LOCATION_PERMISSION_REQUEST_CODE==requestCode){
            for(int i=0;i<grantResults.length;i++){
                Log.i(TAG,"on request permission result inside the loop");
                if(grantResults[i]!= PackageManager.PERMISSION_GRANTED)
                    locationPermissionGranted=false;
            }
            locationPermissionGranted=true;
            initializeMap();
        }
        Log.i(TAG,"on request permission result:::"+ locationPermissionGranted);
    }
    public void findCurrentLocation(){


    fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(MapsActivity.this);
    try{
        if(locationPermissionGranted){
            Log.i(TAG,"getting the current location");
            Task location=fusedLocationProviderClient.getLastLocation();
            Log.i(TAG,"starting the task");
             location.addOnCompleteListener(new OnCompleteListener() {
                 @Override
                 public void onComplete(@NonNull Task task) {
                     if(task.isSuccessful()){
                         Log.i(TAG,"checking if the task is successful");
                         Location currentlocation=(Location)task.getResult();
                         Log.i(TAG,"current latitude and longitude "+ currentlocation.getLatitude()+"    "+ currentlocation.getLongitude());
                         moveCamera(new LatLng(currentlocation.getLatitude(),currentlocation.getLongitude()),DEFAULT_ZOOM,"My Location");


                     }
                     else{
                         Log.i(TAG,"Current location not found");
                         Toast.makeText(MapsActivity.this,"Current location not found",Toast.LENGTH_LONG).show();
                     }
                 }
             });
        }
    }
    catch(SecurityException e){
        e.printStackTrace();
    }
    }
    private void moveCamera(LatLng ll,float zoom,String title){
        Log.i(TAG,"moving camera to the "+ ll.latitude + "and "+ ll.longitude);
        mapGoogle.moveCamera(CameraUpdateFactory.newLatLngZoom(ll,zoom));
        MarkerOptions options=new MarkerOptions()
                .position(ll)
                .title(title);
        mapGoogle.addMarker(options);

    }



    private void initGooglePlacesApi() {
        // Initialize Places.
       Places.initialize(getApplicationContext(), "API_KEY");
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(getApplicationContext());

        // Initialize the AutocompleteSupportFragment.
         autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setHint("Search..");
//

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG,Place.Field.NAME));



        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {


                final LatLng lll=place.getLatLng();

                Log.i(TAG, "Place: " + lll.longitude + ", " + lll.latitude);
                moveCamera(lll,DEFAULT_ZOOM,place.getName());
            }



            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }
}
