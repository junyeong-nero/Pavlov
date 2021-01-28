package ad.agio.test_firebase.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.function.Predicate;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivitySearchBinding;
import ad.agio.test_firebase.domain.User;

public class SearchActivity extends AppCompatActivity {

    final static public String TAG = "SearchActivity";
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

        // TODO enterkey 를 통한 search에서 두번 수행되는 버그.
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
        controller.readAllUsers(user -> {
            if (user != null && condition.test(user)) {
                LayoutInflater layoutInflater = getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.inflate_profile, null);
                TextView nick = view.findViewById(R.id.text_nickname);
                nick.setText(user.getUserName());

                Log.d(TAG, user.getUserName());

                ImageButton button = view.findViewById(R.id.button_chat);
                button.setOnClickListener(v -> {
                    Intent chat = new Intent(getApplicationContext(), ChatActivity.class);
                    chat.putExtra("receiver", user.getId());
                    chat.putExtra("sender", controller.getUID());
                    startActivity(chat);
                });
                binding.textviewLog.addView(view);
            }
        });
    }
}