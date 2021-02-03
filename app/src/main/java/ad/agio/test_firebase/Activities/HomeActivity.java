package ad.agio.test_firebase.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.MatchController;
import ad.agio.test_firebase.controller.NotificationController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivityHomeBinding;
import ad.agio.test_firebase.domain.User;

public class HomeActivity extends AppCompatActivity {

    String TAG = "HomeActivity";
    public void LOGGING(String text) {
        Log.d(TAG, text);
    }

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonProfile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        binding.buttonFloating.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchActivity.class)));

        NotificationController controller = new NotificationController();
        controller.readNotification(notification -> {
            binding.textTitle.setText(notification.getTitle());
            binding.textContent.setText(notification.getContent());
        });

        authController = new AuthController();
        if(authController.isAuth())
            setting();
    }

    private User currentUser;
    private AuthController authController;
    private UserController userController;
    private MatchController matchController;

    public void setting() {
        userController = new UserController();
        userController.readMe(user -> {
            currentUser = user;
            binding.buttonMain.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(getApplicationContext(), R.color.primary)
            ));
        });

        matchController = new MatchController();
        binding.buttonMain.setOnClickListener(v -> {
            if(matchController == null)
                matchController = new MatchController();
            if (currentUser != null) {
                if(!matchController.isMatching) {
                    LOGGING("match: start");
                    binding.textIndicator.setText("매칭중..");
                    matchController.startMatching(
                            user -> true, // condition
                            list -> {
                                CharSequence[] items = new CharSequence[list.size()];
                                for (int i = 0; i < items.length; i++)
                                    items[i] = list.get(i).getUserName();

                                new AlertDialog.Builder(HomeActivity.this)
                                        .setTitle("매칭성공")
                                        .setItems(items, (dialog, which) -> {
                                            if(matchController.isMatching) // receiving 하는 중
                                                matchController.receiveResult(list.get(which));
                                            else // matching 하는 중
                                                matchController.match(list.get(which));
                                        })
                                        .setNegativeButton("안할래용", (dialog, which) -> {
                                            binding.textIndicator.setText("매칭하려면 밑의 버튼을 눌러주세요");
                                            matchController.pauseReceiving();
                                        })
                                        .setOnDismissListener(dialog -> {
                                            binding.textIndicator.setText("매칭하려면 밑의 버튼을 눌러주세요");
                                            matchController.pauseReceiving();
                                        })
                                        .show();
                            });
                } else {
                    LOGGING("match: finish");
                    binding.textIndicator.setText("매칭하려면 밑의 버튼을 눌러주세요");
                    matchController.pauseReceiving();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(matchController != null)
            matchController.pauseReceiving();
        matchController = null;
        userController = null;
    }

    @Override
    public void onStop() {
        if(matchController != null)
            matchController.pauseReceiving();
        matchController = null;
        userController = null;
        super.onStop();
    }
}