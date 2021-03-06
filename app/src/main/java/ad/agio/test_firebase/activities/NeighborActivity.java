package ad.agio.test_firebase.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.databinding.ActivityNeighborBinding;
import ad.agio.test_firebase.utils.Codes;

import static ad.agio.test_firebase.activities.HomeActivity.currentUser;

public class NeighborActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityNeighborBinding binding;
    private void log(String s) {
        Log.e(this.getClass().getSimpleName(), s);
    }

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 17;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private PlacesClient placesClient;

    private static final int M_MAX_ENTRIES = 5;
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNeighborBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.textNeighbor.setText(String.format("현재 %s님의 위치는", currentUser.getUserName()));
        binding.textNeighbor2.setText(String.format("'%s' 입니다", currentUser.getNeighbor()));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        binding.buttonCheck.setOnClickListener(v -> {
            Snackbar.make(binding.buttonCheck, "동네인증 완료!", 500).show();

            Intent intent = new Intent();
            intent.putExtra("neighbor",
                    getAddress(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
            setResult(Codes.NEIGHBOR, intent);
            finish();
        });

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);

        locationPermissionGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        updateLocationUI();
        getDeviceLocation();
        showCurrentPlace();
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        } try {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true); //내 위치로 돌아가는 버튼 활성화
        } catch (SecurityException e) {
            log(e.getLocalizedMessage());
        }
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
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            binding.textNeighbor2.setText(getString(
                                    R.string.neighbor_activity_address_explain,
                                    getAddress(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude())));
//                            binding.textNeighbor2.setText(
//                                    "'" +
//                                    getAddress(lastKnownLocation.getLatitude(),
//                                            lastKnownLocation.getLongitude())
//                                    + "' 입니다");
                        }
                    } else {
                        log("Current location is null. Using defaults.");
                        map.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            log(e.getMessage());
        }
    }


    // TODO Fix this
    private void showCurrentPlace() {
        if (map == null) {
            return;
        }

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final
            Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener (task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    FindCurrentPlaceResponse likelyPlaces = task.getResult();

                    // Set the count, handling cases where less than 5 entries are returned.
                    int count = Math.min(likelyPlaces.getPlaceLikelihoods().size(), M_MAX_ENTRIES);

                    int i = 0;
                    likelyPlaceNames = new String[count];
                    likelyPlaceAddresses = new String[count];
                    likelyPlaceAttributions = new List[count];
                    likelyPlaceLatLngs = new LatLng[count];

                    for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                        // Build a list of likely places to show the user.
                        log(placeLikelihood.getPlace().getName());
                        likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                        likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                        likelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                .getAttributions();
                        likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                        i++;
                        if (i > (count - 1)) {
                            break;
                        }
                    }

                    // Show a dialog offering the user the list of likely places, and add a
                    // marker at the selected place.
                    // openPlacesDialog();
                }
            });
        } else {
            // The user has not granted permission.
            log("The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            map.addMarker(new MarkerOptions()
                    .title("title")
                    .position(defaultLocation)
                    .snippet("snippet"));

            // Prompt the user for permission.
        }
    }

    /**
     * getAdress from latitude and longitude
     * @param latitude 위도
     * @param longitude 경도
     * @return 지역의 '동' 주소
     */
    private String getAddress(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.KOREA);
            List<Address> fromLocation = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = fromLocation.get(0);
            StringBuilder stringBuilder = new StringBuilder(); // 대구시 수성구 사월동
            stringBuilder.append(address.getLocality()).append(" "); //시
            stringBuilder.append(address.getSubLocality()).append(" "); //구
            stringBuilder.append(address.getThoroughfare()); //동
            return address.getThoroughfare();
//            return address.getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
            return "no address";
        }
    }
}