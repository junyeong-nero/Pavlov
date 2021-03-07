package ad.agio.test_firebase.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.databinding.ActivitySearchPlaceBinding;
import ad.agio.test_firebase.domain.WalkPoint;
import ad.agio.test_firebase.utils.Codes;

public class SearchPlaceActivity extends AppCompatActivity {

    private ActivitySearchPlaceBinding binding;
    private void log(String t) {
        Log.e(this.getClass().getSimpleName(), t);
    }
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private AutocompleteSupportFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchPlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationPermissionGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if(!locationPermissionGranted)
            finish();

        getDeviceLocation();

        // Initialize the AutocompleteSupportFragment.
        fragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        if (fragment != null) {

            fragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));
            fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NotNull Place place) {

                    WalkPoint wp = new WalkPoint(place);
                    Intent intent = new Intent();
                    intent.putExtra("walk_point", new Gson().toJson(wp));
                    log(new Gson().toJson(wp));
                    setResult(Codes.SEARCH_PLACE, intent);
                    finish();
                }

                @Override
                public void onError(@NotNull Status status) {
                    log("An error occurred: " + status);
                }
            });
        }
        // Set up a PlaceSelectionListener to handle the response.
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            log(lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude());
                            float bound = 0.2F;
                            fragment.setLocationRestriction(RectangularBounds.newInstance(
                                    new LatLng(lastKnownLocation.getLatitude() - bound, lastKnownLocation.getLongitude() - bound),
                                    new LatLng(lastKnownLocation.getLatitude() + bound, lastKnownLocation.getLongitude() + bound)));
                        }
                    } else {
                        log("Current location is null. Using defaults.");
                    }
                });
            }
        } catch (SecurityException e) {
            log(e.getMessage());
        }
    }
}