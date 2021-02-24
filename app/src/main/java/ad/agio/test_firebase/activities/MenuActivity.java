package ad.agio.test_firebase.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.databinding.ActivityMenuBinding;
import ad.agio.test_firebase.utils.RequestCodes;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(v -> finish());

        HashMap<String, Consumer<Void>> hashMap = new HashMap<>();
        HashMap<String, Integer> iconMap = new HashMap<>();

        iconMap.put("로그아웃", R.drawable.ic_person);
        hashMap.put("로그아웃", (v) -> {
            AuthController authController = new AuthController();
            authController.signOut();
            setResult(RequestCodes.LOGOUT);
            finish();
        });

        for (String key : hashMap.keySet()) {
            View view = getLayoutInflater().inflate(R.layout.menu_inflater, null);
            ImageView imageView = view.findViewById(R.id.icon);
            imageView.setImageResource(Objects.requireNonNull(iconMap.get(key)));
            imageView.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.colorPrimary)));

            Button button = view.findViewById(R.id.title);
            button.setText(key);
            button.setBackground(ContextCompat.getDrawable(this, R.drawable.ripple_rect));
            button.setOnClickListener(v -> Objects.requireNonNull(hashMap.get(key))
                    .accept(null));

            binding.layoutMenu.addView(view);
        }
    }
}