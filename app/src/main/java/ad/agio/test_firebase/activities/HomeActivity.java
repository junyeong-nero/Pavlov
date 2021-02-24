package ad.agio.test_firebase.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Optional;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.MatchController;
import ad.agio.test_firebase.controller.NotificationController;
import ad.agio.test_firebase.databinding.ActivityHomeBinding;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.fragments.HomeFragment;
import ad.agio.test_firebase.fragments.ProfileFragment;
import ad.agio.test_firebase.fragments.SearchFragment;
import ad.agio.test_firebase.services.AppointService;
import ad.agio.test_firebase.utils.RequestCodes;

public class HomeActivity extends AppCompatActivity {

    private void log(String text) {
        Log.e(this.getClass().getSimpleName(), text);
    }
    private ActivityHomeBinding binding;
    private AuthController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment(), "HomeFragment")
                .commit();

        binding.buttonHome.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment(), "HomeFragment")
                    .commit();
            binding.toolbarTitle.setText("홈");
            binding.buttonMenu.setVisibility(View.GONE);
        });

        binding.buttonSearch.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SearchFragment(), "SearchFragment")
                    .commit();
            binding.toolbarTitle.setText("검색");
            binding.buttonMenu.setVisibility(View.GONE);
        });

        binding.buttonProfile.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment(), "ProfileFragment")
                    .commit();
            binding.toolbarTitle.setText("프로필");
            binding.buttonMenu.setVisibility(View.VISIBLE);
        });

        binding.buttonMenu.setOnClickListener(v -> {
            startActivity(new Intent(this, MenuActivity.class));
        });

//        binding.button1.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
//        binding.button2.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchActivity.class)));

        authController = new AuthController();
        serviceStart();
    }

    private void serviceStart() {
        if(authController.isAuth())
            startService(new Intent(this, AppointService.class));
    }

    private void serviceStop() {
        stopService(new Intent(this, AppointService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RequestCodes.LOGOUT) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}