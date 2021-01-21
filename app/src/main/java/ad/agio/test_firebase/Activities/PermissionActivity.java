package ad.agio.test_firebase.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ad.agio.test_firebase.databinding.ActivityPermissionBinding;

public class PermissionActivity extends AppCompatActivity {

    private ActivityPermissionBinding binding;
    final static public int PERMISSIONS_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayList<String> arr = new ArrayList<>();
        arr.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        arr.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        arr.add(Manifest.permission.INTERNET);
        requestPermissions(arr);
    }

    /**
     * Prompts the us
     */
    private void requestPermissions(ArrayList<String> permissions) {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions.get(0)) ==
                PackageManager.PERMISSION_GRANTED) { // already granted
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
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
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                }
            }
        }
    }
}