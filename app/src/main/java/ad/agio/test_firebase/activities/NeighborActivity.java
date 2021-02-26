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
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivityNeighborBinding;
import ad.agio.test_firebase.utils.Codes;

public class NeighborActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityNeighborBinding binding;
    private UserController userController;
    private void log(String s) {
        Log.e(this.getClass().getSimpleName(), s);
    }

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 17;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNeighborBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(v -> finish());
        userController = new UserController();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationPermissionGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        binding.buttonCheck.setOnClickListener(v -> {
            Snackbar.make(binding.buttonCheck, "동네인증 완료!", 500).show();

            Intent intent = new Intent();
            intent.putExtra("neighbor",
                    getAddress(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
            setResult(Codes.NEIGHBOR_ACTIVITY, intent);
            finish();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        updateLocationUI();
        getDeviceLocation();
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
                            binding.textNeighbor.setText(
                                    getAddress(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
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

    /**
     * getAdress from latitude and longitude
     * @param latitude
     * @param longitude
     * @return
     * @throws IOException
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