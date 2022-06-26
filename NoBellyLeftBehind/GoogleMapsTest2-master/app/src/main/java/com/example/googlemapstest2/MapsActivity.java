package com.example.googlemapstest2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // map-related stuff
    private GoogleMap mMap;
    private LatLng pos;
    private LatLng currentLocation;
    private LatLng centerOfRadius;
    private Marker blueActiveMarker;
    private MarkerOptions blueActiveMarkerOptions;
    private FusedLocationProviderClient fusedLocationClient;

    // front-end stuff
    private EditText editText;
    private Button findMeButton;
    private Button filtersButton;
    private Button doneButton;
    private PopupWindow popupWindow;
    private LinearLayout linearLayout;
    private PopupWindow infoPopupPopupWindow;
    private Button infoPopupDoneButton;

    // data structures
    private List<FoodLocation> locations;
    private Map<MarkerOptions, Integer> map;
    private FilterParameters filterParameters;

    // switches
    private boolean markerActive;
    private boolean filterWindowActive;

    // final variables
    private final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editText = (EditText) findViewById(R.id.editText);
        findMeButton = (Button) findViewById(R.id.button);
        filtersButton = (Button) findViewById(R.id.button2);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        doneButton = (Button) findViewById(R.id.doneButton);
        infoPopupDoneButton = (Button) findViewById(R.id.infoPopupDoneButton);

        locations = new ArrayList<>();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        markerActive = false;
        filterWindowActive = false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // on click listener for locating self
        findMeButton.setOnClickListener(v -> {
            showLocation();
        });

        // on click listener for pressing enter on the keyboard
        editText.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                showLocation();
                return true;
            }
            return false;
        });

        // on click listener for the filters button
        filtersButton.setOnClickListener(v -> {
            mMap.getUiSettings().setScrollGesturesEnabled(false);
            filtersButton.setEnabled(false);
            filterWindowActive = true;
            LayoutInflater layoutInflater = (LayoutInflater) MapsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customView = layoutInflater.inflate(R.layout.popup,null);

            doneButton = customView.findViewById(R.id.doneButton);

            //instantiate popup window
            popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            //display the popup window
            popupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);

            // on done button, get all of the required data and close the popup
            doneButton.setOnClickListener(v1 -> {

                // get references to each input field
                SeekBar seekBar = customView.findViewById(R.id.seekBar);
                CheckBox Su = customView.findViewById(R.id.checkBox);
                CheckBox M = customView.findViewById(R.id.checkBox2);
                CheckBox Tu = customView.findViewById(R.id.checkBox3);
                CheckBox W = customView.findViewById(R.id.checkBox4);
                CheckBox Th = customView.findViewById(R.id.checkBox5);
                CheckBox F = customView.findViewById(R.id.checkBox6);
                CheckBox Sa = customView.findViewById(R.id.checkBox7);

                // make boolean array for days open
                boolean[] daysOpen = {
                        Su.isChecked(),
                        M.isChecked(),
                        Tu.isChecked(),
                        W.isChecked(),
                        Th.isChecked(),
                        F.isChecked(),
                        Sa.isChecked()
                };

                // get references for more input fields
                CheckBox youth = customView.findViewById(R.id.checkBox8);
                CheckBox gp = customView.findViewById(R.id.checkBox9);
                CheckBox olderAdults = customView.findViewById(R.id.checkBox10);
                CheckBox seattleSchools = customView.findViewById(R.id.checkBox11);
                CheckBox contact = customView.findViewById(R.id.checkBox12);
                CheckBox other = customView.findViewById(R.id.checkBox13);

                // add appropriate strings to denote who should be served
                List<String> serve = new ArrayList<>();
                if (youth.isChecked()) {
                    serve.add("Youth and Young Adults");
                }
                if (gp.isChecked()) {
                    serve.add("General Public");
                    serve.add("any adult in need");
                }
                if (olderAdults.isChecked()) {
                    serve.add("Older Adults 60+");
                }
                if (seattleSchools.isChecked()) {
                    serve.add("Seattle Public School Students");
                }
                if (contact.isChecked()) {
                    serve.add("Contact Agency");
                }
                if (other.isChecked()) {
                    serve.add("OTHER");
                }

                // get proximity (last input field)
                int proximity = seekBar.getProgress();

                // create or update filterParameters
                if (filterParameters == null) {
                    filterParameters = new FilterParameters(daysOpen, serve, proximity);
                } else {
                    filterParameters.update(daysOpen, serve, proximity);
                }

                // get a new list based on the filterParameters and then populate the map with that list
                List<FoodLocation> list = filterParameters.filter(locations, centerOfRadius);
                populateMap(list);

                // close the popup window
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                filtersButton.setEnabled(true);
                filterWindowActive = false;
                popupWindow.dismiss();
            });
        });

        // on click listener for each marker
        mMap.setOnMarkerClickListener(marker -> {

            if (markerActive || filterWindowActive) return true;

            markerActive = true;
            mMap.getUiSettings().setScrollGesturesEnabled(false);
            filtersButton.setEnabled(false);


            // get the tag of the marker
            System.out.println("TAG: " + marker.getTag());
            int id = (Integer) marker.getTag();
            FoodLocation cur = null;

            // find the corresponding FoodLocation
            for (FoodLocation fl : locations) {
                if (fl.getId() == id) {
                    cur = fl;
                    break;
                }
            }

            LayoutInflater layoutInflater = (LayoutInflater) MapsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customView = layoutInflater.inflate(R.layout.info_popup_2,null);

            infoPopupDoneButton = customView.findViewById(R.id.infoPopupDoneButton);

            //instantiate popup window
            infoPopupPopupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            //display the popup window
            infoPopupPopupWindow.showAtLocation(linearLayout, Gravity.BOTTOM, 0, 0);

            // fill info
            TextView title = customView.findViewById(R.id.titleView);
            TextView address = customView.findViewById(R.id.addressView);
            TextView daysAndHours = customView.findViewById(R.id.daysAndHoursView);
            TextView distribution = customView.findViewById(R.id.distributionView);
            TextView foodResource = customView.findViewById(R.id.foodResourceTypeView);
            TextView operationalNotes = customView.findViewById(R.id.operationalNotesView);
            TextView operationalStatus = customView.findViewById(R.id.operationalStatusView);
            TextView phoneNumber = customView.findViewById(R.id.phoneNumberView);
            TextView website = customView.findViewById(R.id.websiteView);
            TextView whoTheyServe = customView.findViewById(R.id.whoTheyServeView);

            title.setText("Name: " + (cur.getTitle() != null ? cur.getTitle() : ""));
            address.setText("Address:" + (cur.getAddress() != null ? cur.getAddress() : ""));
            daysAndHours.setText("Hours of Operation: " + (cur.getDayHours() != null ? cur.getDayHours() : ""));
            distribution.setText("Distribution: " + (cur.getDistribution() != null ? cur.getDistribution() : ""));
            foodResource.setText("Food Resource Type: " + (cur.getFoodResourceType() != null ? cur.getFoodResourceType() : ""));
            operationalNotes.setText("Notes: " + (cur.getOperationalNotes() != null ? cur.getOperationalNotes() : ""));
            operationalStatus.setText("Status: " + (cur.getOperationalStatus() != null ? cur.getOperationalStatus() : ""));
            phoneNumber.setText("Phone Number: " + (cur.getPhoneNumber() != null ? cur.getPhoneNumber() : ""));
            website.setText("Website: " + (cur.getWebsite() != null ? cur.getWebsite() : ""));
            whoTheyServe.setText("Who They Serve: " + (cur.getWhoTheyServe() != null ? cur.getWebsite() : ""));

            //close the popup window on button click
            infoPopupDoneButton.setOnClickListener(v1 ->
            {
                infoPopupPopupWindow.dismiss();
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                markerActive = false;
                filtersButton.setEnabled(true);
            });

            return true;
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13.0f));
                        centerOfRadius = currentLocation;
                    }
                });

        // get the data from FireBase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("data");


        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // initial map population with markers
                collectLocations(snapshot);
                populateMap(locations);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapsActivity.this, "Error Fetching Data", Toast.LENGTH_LONG).show();
            }
        });
    }

    // populate the map with the given FoodLocations, make sure the blue marker remains (if one is active)
    private void populateMap(List<FoodLocation> locs) {
        mMap.clear();
        for (FoodLocation loc : locs) {
            Marker marker;
            if (loc.getLatLng() != null) {
                marker = mMap.addMarker(new MarkerOptions().position(loc.getLatLng()).title(loc.getTitle()));
                marker.setTag(loc.getId());
            } else if (!loc.getAddress().isEmpty()) {
                LatLng newMarker = getLocationFromAddress(loc.getAddress());
                if (newMarker != null) {
                    marker = mMap.addMarker(new MarkerOptions().position(newMarker).title(loc.getTitle()));
                    marker.setTag(loc.getId());
                }
            }
        }
        if (blueActiveMarkerOptions != null) {
            mMap.addMarker(blueActiveMarkerOptions);
        }
    }

    // shows the location of the inputted text
    private void showLocation() {
        closeKeyBoard();
        String input = editText.getText().toString();
        Geocoder geocoder = new Geocoder(getBaseContext());
        List<Address> list;
        try {
            list = geocoder.getFromLocationName(input, 1);
            Address current = list.get(0);
            pos = new LatLng(current.getLatitude(), current.getLongitude());
            if (blueActiveMarker != null) {
                blueActiveMarker.remove();
            }
            blueActiveMarkerOptions = new MarkerOptions().draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).alpha(1.0f).position(pos).title("Selected Location").zIndex(1);
            blueActiveMarker = mMap.addMarker(blueActiveMarkerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 10.0f));
            centerOfRadius = pos;
        } catch (Exception e) {
            System.out.println(e);
            Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Invalid Location!", BaseTransientBottomBar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    // closes the keyboard
    private void closeKeyBoard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // stores Location objects
    private void collectLocations(DataSnapshot snapshot) {
        for (DataSnapshot dsp : snapshot.getChildren()) {
            FoodLocation loc = new FoodLocation(dsp, getBaseContext());
            locations.add(loc);
        }
    }

    // input is an address, output is the corresponding LatLng
    private LatLng getLocationFromAddress(String address) {
        Geocoder geocoder = new Geocoder(getBaseContext());
        List<android.location.Address> list;
        try {
            list = geocoder.getFromLocationName(address, 1);
            Address current = list.get(0);
            return new LatLng(current.getLatitude(), current.getLongitude());
        } catch (Exception e){
            System.out.println(e);
            return null;
        }
    }



}