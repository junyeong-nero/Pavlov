package ad.agio.test_firebase.activities;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;

import ad.agio.test_firebase.controller.AppointController;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.MatchController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivityPermissionBinding;

import static ad.agio.test_firebase.activities.HomeActivity.appointController;
import static ad.agio.test_firebase.activities.HomeActivity.authController;
import static ad.agio.test_firebase.activities.HomeActivity.matchController;
import static ad.agio.test_firebase.activities.HomeActivity.userController;

public class PermissionActivity extends AppCompatActivity {

    private ActivityPermissionBinding binding;
    final static public int PERMISSIONS_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // NO Status bar
        binding = ActivityPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayList<String> arr = new ArrayList<>();
        arr.add(Manifest.permission.INTERNET);
        arr.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        arr.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        arr.add(Manifest.permission.ACCESS_NETWORK_STATE);
        arr.add(Manifest.permission.ACCESS_FINE_LOCATION);
        arr.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        requestPermissions(arr);
    }

    /**
     * Prompts the us
     */
    private void requestPermissions(ArrayList<String> permissions) {
        boolean b = permissions.stream().allMatch(str -> ContextCompat.checkSelfPermission(
                this.getApplicationContext(), str) == PackageManager.PERMISSION_GRANTED);
        if (b) { // already granted
            init();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[0]),
                    PERMISSIONS_REQUEST);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            boolean b = Arrays.stream(permissions).allMatch(str -> ContextCompat.checkSelfPermission(
                    this.getApplicationContext(), str) == PackageManager.PERMISSION_GRANTED);
            if (grantResults.length > 0 && b) {
                init();
            }
        }
    }

    public void init() {
        userController = new UserController();
        authController = new AuthController();
        matchController = new MatchController();
        appointController = new AppointController();
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (authController.isAuth()) {
                startActivity(
                        new Intent(PermissionActivity.this, HomeActivity.class));
            } else {
                startActivity(
                        new Intent(PermissionActivity.this, LoginActivity.class));
            }
            finish();
        }).start();
    }
}