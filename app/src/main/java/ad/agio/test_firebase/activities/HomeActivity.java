package ad.agio.test_firebase.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.databinding.ActivityHomeBinding;
import ad.agio.test_firebase.fragments.ChatFragment;
import ad.agio.test_firebase.fragments.HomeFragment;
import ad.agio.test_firebase.fragments.ProfileFragment;
import ad.agio.test_firebase.fragments.SearchFragment;
import ad.agio.test_firebase.services.AppointService;
import ad.agio.test_firebase.utils.GraphicComponents;
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

        HashMap<String, Consumer<String>> map = new HashMap<>();
        HashMap<String, Integer> icon = new HashMap<>();

        icon.put("홈", R.drawable.ic_home);
        map.put("홈", t -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment(), "HomeFragment")
                    .commit();
        });

        icon.put("탐색", R.drawable.ic_search);
        map.put("탐색", t -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SearchFragment(), "SearchFragment")
                    .commit();
        });

        icon.put("프로필", R.drawable.ic_person);
        map.put("프로필", t -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment(), "ProfileFragment")
                    .commit();
        });

        icon.put("채팅", R.drawable.ic_chat);
        map.put("채팅", t -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatFragment(), "ProfileFragment")
                    .commit();
        });

        GraphicComponents g = new GraphicComponents(this);

        List<String> arr = Arrays.asList("홈", "탐색", "채팅", "프로필");
        for (String key : arr) {
            View view = getLayoutInflater().inflate(R.layout.home_button_inflater, null);
            ImageButton button = view.findViewById(R.id.button);
            button.setImageResource(icon.get(key));
            button.setOnClickListener(v -> {
                menuControl(key);
                binding.toolbarTitle.setText(key);
                map.get(key).accept(key);
            });
            TextView textView = view.findViewById(R.id.text);
            textView.setText(key);
            binding.layout.addView(view, g.getScreenWidth() / arr.size(), g.dp(56));
        }

        binding.buttonMenu.setOnClickListener(v -> startActivityForResult(new Intent(this, MenuActivity.class), RequestCodes.MENU_ACTIVITY));
        authController = new AuthController();
        serviceStart();
    }

    private void menuControl(String fragment) {
        HashMap<String, List<ImageButton>> map = new HashMap<>();
        map.put("프로필", Collections.singletonList(binding.buttonMenu));
        for (String key : map.keySet()) {
            if(key.equals(fragment)) {
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
        if(authController.isAuth())
            startService(new Intent(this, AppointService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        log("onActivityResult");
        if (resultCode == RequestCodes.LOGOUT && requestCode == RequestCodes.MENU_ACTIVITY) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}