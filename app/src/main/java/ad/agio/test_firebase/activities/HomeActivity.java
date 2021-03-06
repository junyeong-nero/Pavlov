package ad.agio.test_firebase.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AppointController;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.MatchController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivityHomeBinding;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.fragments.ChatFragment;
import ad.agio.test_firebase.fragments.HomeFragment;
import ad.agio.test_firebase.fragments.NoInternetFragment;
import ad.agio.test_firebase.fragments.ProfileFragment;
import ad.agio.test_firebase.fragments.SearchFragment;
import ad.agio.test_firebase.services.AppointService;
import ad.agio.test_firebase.utils.Codes;
import ad.agio.test_firebase.utils.Utils;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private void log(String string) {
        Log.e(this.getClass().getSimpleName(), string);
    }
    private HashMap<Integer, Consumer<String>> consumers;

    static public UserController userController = null;
    static public AuthController authController = null;

    @SuppressLint("StaticFieldLeak")
    static public MatchController matchController = null;

    @SuppressLint("StaticFieldLeak")
    static public AppointController appointController = null;
    static public User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        consumers = new HashMap<>();
        consumers.put(R.id.navigation_home, t -> getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new HomeFragment(), "HomeFragment")
                .commit());

        consumers.put(R.id.navigation_search, t -> getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new SearchFragment(), "SearchFragment")
                .commit());

        consumers.put(R.id.navigation_profile, t -> getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new ProfileFragment(), "ProfileFragment")
                .commit());

        consumers.put(R.id.navigation_chat, t -> getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new ChatFragment(), "ChatFragment")
                .commit());

        serviceStart();

        matchController.setContext(this);
        appointController.setContext(this);
        userController.readMe(me -> currentUser = me);
        binding.buttonMenu.setOnClickListener(v -> startActivityForResult(
                new Intent(this, MenuActivity.class), Codes.MENU));

        // Goto Home
        menuControl(R.id.navigation_home);
        binding.toolbarTitle.setText(getString(R.string.title_home));
        if(Utils.checkInternet(this))
            consumers.get(R.id.navigation_home).accept(getString(R.string.title_home));
        else
            noInternet();

        setUpNavigation();
    }

    public void selectItem(@NonNull MenuItem item) {
        log("onNavigationItemSelected");

        menuControl(item.getItemId());
        binding.toolbarTitle.setText(item.getTitle().toString());

        if(Utils.checkInternet(this))
            consumers.get(item.getItemId()).accept(item.getTitle().toString());
        else
            noInternet();
    }

    public void setUpNavigation() {
        binding.navView.setOnNavigationItemSelectedListener(item -> {
            selectItem(item);
            return true;
        });
    }

    private void noInternet() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NoInternetFragment(), "NoInternetFragment")
                .commit();
    }

    private void menuControl(int id) {
        log("menuControl");
        HashMap<Integer, List<ImageButton>> map = new HashMap<>();
        map.put(R.id.navigation_profile, Collections.singletonList(binding.buttonMenu));
        // Arrays.asList

        for (int key : map.keySet()) {
            if(key == id) {
                for (ImageButton b : map.get(key)) {
                    b.setVisibility(View.VISIBLE);
                }
            } else {
                for (ImageButton b : map.get(key)) {
                    b.setVisibility(View.GONE);
                }
            }
        }
    }

    private void serviceStart() {
        log("serviceStart");
        if(authController.isAuth()) {
            Intent intent = new Intent(this, AppointService.class);
            startService(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        log("onStart");
        appointController.setContext(this);
        matchController.setContext(this);
    }

    @Override
    public void finishAndRemoveTask() {
        super.finishAndRemoveTask();
        log("finishAndRemoveTask");
        appointController.pauseReceive();
        matchController.pauseReceive();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        log("onActivityResult");
        if (resultCode == Codes.LOGOUT && requestCode == Codes.MENU) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
