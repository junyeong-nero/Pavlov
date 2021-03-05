package ad.agio.test_firebase.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import ad.agio.test_firebase.fragments.NoInternetFragment;
import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AppointController;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.MatchController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivityHomeBinding;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.fragments.ChatFragment;
import ad.agio.test_firebase.fragments.HomeFragment;
import ad.agio.test_firebase.fragments.ProfileFragment;
import ad.agio.test_firebase.fragments.SearchFragment;
import ad.agio.test_firebase.services.AppointService;
import ad.agio.test_firebase.utils.GraphicComponents;
import ad.agio.test_firebase.utils.Codes;
import ad.agio.test_firebase.utils.Utils;

public class HomeActivity extends AppCompatActivity {

    private void log(String text) {
        Log.e(this.getClass().getSimpleName(), text);
    }
    private ActivityHomeBinding binding;
    static public UserController userController = new UserController();
    static public AuthController authController = new AuthController();
    static public MatchController matchController = new MatchController();
    static public AppointController appointController = new AppointController();
    static public User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        serviceStart();

        matchController.setContext(this);
        appointController.setContext(this);
        userController.readMe(me -> currentUser = me);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment(), "HomeFragment")
                .commit();

        HashMap<String, Consumer<String>> map = new HashMap<>();
        HashMap<String, Integer> icon = new HashMap<>();

        icon.put("홈", R.drawable.ic_home);
        map.put("홈", t -> getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment(), "HomeFragment")
                .commit());

        icon.put("탐색", R.drawable.ic_search);
        map.put("탐색", t -> getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SearchFragment(), "SearchFragment")
                .commit());

        icon.put("프로필", R.drawable.ic_person);
        map.put("프로필", t -> getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment(), "ProfileFragment")
                .commit());

        icon.put("채팅", R.drawable.ic_chat);
        map.put("채팅", t -> getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ChatFragment(), "ChatFragment")
                .commit());

        GraphicComponents g = new GraphicComponents(this);

        List<String> arr = Arrays.asList("홈", "탐색", "채팅", "프로필");
        for (String key : arr) {
            View view = getLayoutInflater().inflate(R.layout.inflater_home_button, null);
            ImageButton button = view.findViewById(R.id.button);
            button.setImageResource(icon.get(key));
            button.setOnClickListener(v -> {

                menuControl(key);
                binding.toolbarTitle.setText(key);
                if(Utils.checkInternet(this))
                    map.get(key).accept(key);
                else
                    noInternet();
            });
            TextView textView = view.findViewById(R.id.text);
            textView.setText(key);
            binding.layout.addView(view, g.getScreenWidth() / arr.size(), g.dp(56));
        }

        binding.buttonMenu.setOnClickListener(v -> startActivityForResult(
                new Intent(this, MenuActivity.class), Codes.MENU_ACTIVITY));

    }

    private void noInternet() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NoInternetFragment(), "NoInternetFragment")
                .commit();
    }

    private void menuControl(String fragment) {
        HashMap<String, List<ImageButton>> map = new HashMap<>();
        map.put("프로필", Arrays.asList(binding.buttonMenu));

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
        if(authController.isAuth()) {
            Intent intent = new Intent(this, AppointService.class);
            startService(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        appointController.setContext(this);
        matchController.setContext(this);
    }

    @Override
    public void finishAndRemoveTask() {
        super.finishAndRemoveTask();
        appointController.pauseReceive();
        matchController.pauseReceive();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        log("onActivityResult");
        if (resultCode == Codes.LOGOUT && requestCode == Codes.MENU_ACTIVITY) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}