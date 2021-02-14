package ad.agio.test_firebase.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Optional;
import java.util.function.Predicate;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AppointController;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivitySearchBinding;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.utils.RequestCodes;

public class SearchActivity extends AppCompatActivity {

    public void _log(String content) {
        Log.e(this.getClass().getSimpleName(), content);
    }

    private ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(v -> {
            finish();
        });

        Predicate<User> condition = user -> user.getUserName()
                .contains(binding.editQuery.getText().toString());

        binding.button.setOnClickListener(v -> {
            // 20 ~ 40대를 위한 lambda expression
            search(condition);
        });

        binding.editQuery.setOnKeyListener((v, actionId, event) -> {
            if (actionId == KeyEvent.KEYCODE_ENTER) {
                search(condition);
            }
            return false;
        });
    }

    public void search(Predicate<User> condition) {
        binding.textviewLog.removeAllViews();
        UserController controller = new UserController();
        AuthController authController = new AuthController();
        controller.readAllUsers(user -> {
            if (user != null && condition.test(user)) {
                LayoutInflater layoutInflater = getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.inflate_profile, null);
                TextView nick = view.findViewById(R.id.text_nickname);
                nick.setText(user.getUserName());

                _log(user.toString());

                ImageButton button = view.findViewById(R.id.button_chat);
                button.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    Optional<User> opt = Optional.of(user);
                    opt.ifPresent(value -> {
                        intent.putExtra("type", "search");
                        intent.putExtra("isReceiving", true); // is not receiving
                        intent.putExtra("user", value.toString());
                        intent.putExtra("chatId", value.getChatId());
                    });
                    startActivityForResult(intent, RequestCodes.SEARCH_ACTIVITY);
                    finish();
                });
                binding.textviewLog.addView(view);
            }
        });
    }
}