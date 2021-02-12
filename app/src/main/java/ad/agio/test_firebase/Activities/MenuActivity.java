package ad.agio.test_firebase.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.databinding.ActivityMenuBinding;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(v -> finish());

        HashMap<String, Consumer<Void>> hashMap = new HashMap<>();
        hashMap.put("로그아웃", (v) -> {
            AuthController authController = new AuthController();
            authController.signOut();
            finish();
        });

        for (String key : hashMap.keySet()) {
            Button button = new Button(this);
            button.setText(key);
            button.setGravity(Gravity.CENTER);
            button.setOnClickListener(v -> hashMap.get(key).accept(null));
            binding.layoutMenu.addView(button, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}