package com.example.googlemapsapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    private Button near;




    private EditText editText;
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
        Log.i(TAG,"now it should searc for place");
        //searchForPlace();
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
    //    closeKeyboard();
    }
//    private void searchForPlace(){
//        Log.i(TAG,"beginning of searching");
//        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int actionid, KeyEvent keyEvent) {
//                //Log.i(TAG,"inside the listenr");
//                if(actionid== EditorInfo.IME_ACTION_DONE || actionid==EditorInfo.IME_ACTION_SEARCH || keyEvent.getAction()==KeyEvent.ACTION_DOWN || keyEvent.getAction()==KeyEvent.KEYCODE_ENTER){
//                  //function for searching the place
//                  //  Log.i(TAG,"trueee");
//                    geoLocate();
//                    closeKeyboard();
//                    return true;
//                }
//                return false;
//            }
//        });
//
//    }
//    public void geoLocate(){
//        String searchString=editText.getText().toString();
//        Geocoder geocoder=new Geocoder(MapsActivity.this);
//        List<Address> results=new ArrayList<>();
//        try{
//               results=geocoder.getFromLocationName(searchString,1);
//
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//        if(results.size()>0){
//            Address adress= results.get(0);
//         //   Toast.makeText(MapsActivity.this,"hehe "+ adress.getCountryName(),Toast.LENGTH_LONG).show();
//            String title=adress.getAddressLine(0);
//            moveCamera(new LatLng(adress.getLatitude(),adress.getLongitude()),DEFAULT_ZOOM,title);
//            if (editText.length() > 0) {
//                editText.getText().clear();
//            }
//        }else{
//            Toast.makeText(MapsActivity.this,"The adress you are searching for does not exist",Toast.LENGTH_LONG).show();
//        }
//
//    }
//    public void closeKeyboard(){
//        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
//    }


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
//                if(place.getAddressComponents().asList().get(0).getTypes().get(0).equalsIgnoreCase("route")){
//                    binding.textViewLocation.setText(place.getAddress()); //Works well
//                    location = place.getAddress();

                final LatLng lll=place.getLatLng();

                Log.i(TAG, "Place: " + lll.longitude + ", " + lll.latitude);
                moveCamera(lll,DEFAULT_ZOOM,place.getName());
              //  }else{ //If user does not choose a specific place.
                   // AndroidUtils.vibratePhone(getApplication(), 200);
                   //// TastyToast.makeText(getApplicationContext(),
                        //    getString(R.string.choose_an_address), TastyToast.DEFAULT, TastyToast.CONFUSING);
//                }
//
//                Log.i(TAG, "Place: " + place.getAddressComponents().asList().get(0).getTypes().get(0) + ", " + place.getId() + ", " + place.getAddress());
            }



            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }
}
