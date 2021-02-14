package ad.agio.test_firebase.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Optional;

import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.MatchController;
import ad.agio.test_firebase.controller.NotificationController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivityHomeBinding;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.services.AppointService;

public class HomeActivity extends AppCompatActivity {

    final private String _tag = "HomeActivity";
    private void _log(String text) {
        Log.d(_tag, text);
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

        userController = new UserController();
        authController = new AuthController();
        matchController = new MatchController();

        if(authController.isAuth())
            matchController.prepare();

        serviceStart();
        setting();
    }

    private void serviceStart() {
        startService(new Intent(this, AppointService.class));
    }

    private void serviceStop() {
        stopService(new Intent(this, AppointService.class));
    }

    private AuthController authController;
    private UserController userController;
    private MatchController matchController;

    public void setting() {

        matchController.matchListener = chat -> { // match is finished!
            Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
            intent.putExtra("chatId", chat.chatId);
            startActivity(intent);
        };

        binding.buttonMain.setOnClickListener(v -> {
            if (authController.isAuth()) {

                if(!matchController.isPreparing)
                    matchController.prepare();

                if(!matchController.isReceiving) {
                    _log("match: start");
                    binding.textIndicator.setText("매칭중..");
                    matchController.startMatching(
                            user -> true, // condition
                            list -> {
                                Optional<User> user = list.stream().findAny();
                                _log(user.toString());

                                user.ifPresent(value -> {
                                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                                    intent.putExtra("type", "home");
                                    intent.putExtra("isMatching", matchController.isReceiving);
                                    intent.putExtra("user", value.toString());

                                    if (matchController.getChat() != null)
                                        intent.putExtra("chatId", matchController.getChat().chatId);
                                    else
                                        intent.putExtra("chatId", "fake");

                                    _log(value.toString());
                                    startActivity(intent);
                                });
                            });
                } else {
                    matchFinish();
                }
            }
        });
    }

    private void matchFinish() {
        _log("match: finish");
        binding.textIndicator.setText("매칭하려면 밑의 버튼을 눌러주세요");
        matchController.pauseReceiving();
    }

    private void showDialog(ArrayList<User> list) { // for select user to match
        CharSequence[] items = new CharSequence[list.size()];
        for (int i = 0; i < items.length; i++)
            items[i] = list.get(i).getUserName();

        new AlertDialog.Builder(HomeActivity.this)
                .setTitle("매칭성공")
                .setItems(items, (dialog, which) -> {
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    intent.putExtra("type", "home");
                    intent.putExtra("isMatching", matchController.isReceiving);
                    intent.putExtra("user", list.get(which).toString());

                    if (matchController.getChat() != null)
                        intent.putExtra("chatId", matchController.getChat().chatId);
                    else
                        intent.putExtra("chatId", "fake");

                    _log(list.get(which).toString());
                    startActivity(intent);
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
    }
}