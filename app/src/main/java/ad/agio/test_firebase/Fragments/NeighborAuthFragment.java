package ad.agio.test_firebase.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.FragmentNeighborAuthBinding;
import ad.agio.test_firebase.databinding.FragmentProfileBinding;
import ad.agio.test_firebase.domain.User;
import gun0912.tedbottompicker.TedBottomPicker;

public class NeighborAuthFragment extends Fragment implements OnMapReadyCallback {

    private void _log(String text) {
        Log.d(NeighborAuthFragment.class.getSimpleName(), text);
    }

    private FragmentNeighborAuthBinding binding;
    private AuthController authController;
    private UserController userController;
    private User currentUser;

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 17;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNeighborAuthBinding.inflate(inflater, container, false);

        authController = new AuthController();
        userController = new UserController();
        userController.readMe(me -> currentUser = me);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        final SupportMapFragment mapFragment = (SupportMapFragment) requireActivity().getSupportFragmentManager()
                .findFragmentByTag("NeighborAuthFragment")
                .getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationPermissionGranted = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        binding.buttonCheck.setOnClickListener(v -> {
            UserController userController = new UserController();
            userController.updateUser("neighbor",
                    getAddress(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));

            FragmentManager supportFragmentManager = requireActivity().getSupportFragmentManager();
            Fragment fragment = supportFragmentManager.findFragmentByTag("NeighborAuthFragment");
            if (fragment != null) {
                supportFragmentManager.beginTransaction().remove(fragment);
            }
        });

        return binding.getRoot();
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
            _log(e.getLocalizedMessage());
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
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            _log(lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude());
                        }
                    } else {
                        _log("Current location is null. Using defaults.");
                        map.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            _log(e.getMessage());
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
            Geocoder geocoder = new Geocoder(requireContext(), Locale.KOREA);
            List<Address> fromLocation = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = fromLocation.get(0);
            StringBuilder stringBuilder = new StringBuilder(); // 대구시 수성구 사월동
            stringBuilder.append(address.getLocality()).append(" "); //시
            stringBuilder.append(address.getSubLocality()).append(" "); //구
            stringBuilder.append(address.getThoroughfare()); //동
            //return stringBuilder.toString();
            return address.getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
            return "no address";
        }
    }
}