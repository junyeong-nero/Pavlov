package ad.agio.test_firebase.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.databinding.ActivityMenuBinding;
import ad.agio.test_firebase.utils.Codes;

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
            setResult(Codes.LOGOUT);
            finish();
        });

        iconMap.put("공지사항", R.drawable.ic_create);
        hashMap.put("공지사항", (v) -> {
            startActivity(new Intent(MenuActivity.this, NoticeActivity.class));
        });

        List<String> keys = Arrays.asList("로그아웃", "공지사항");
        for (String key : keys) {
            View view = getLayoutInflater().inflate(R.layout.inflater_menu, null);
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