package ad.agio.test_firebase.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import ad.agio.test_firebase.controller.NotificationController;
import ad.agio.test_firebase.databinding.ActivityHomeBinding;
import ad.agio.test_firebase.domain.Notification;
import ad.agio.test_firebase.domain.User;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonProfile.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        binding.buttonFloating.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SearchActivity.class));
            Snackbar.make(v, "floating button", 500).show();
        });


//        BiConsumer<String, String> consumer = (title, content) -> {
//            binding.textTitle.setText(title);
//            binding.textContent.setText(content);
//        };

        Consumer<Notification> consumer = notification -> {
            binding.textTitle.setText(notification.getTitle());
            binding.textContent.setText(notification.getContent());
        };

        NotificationController controller = new NotificationController(consumer);
    }
}