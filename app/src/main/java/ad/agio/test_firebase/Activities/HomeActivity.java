package ad.agio.test_firebase.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;

import ad.agio.test_firebase.Fragments.OtherProfileFragment;
import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.MatchController;
import ad.agio.test_firebase.controller.NotificationController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivityHomeBinding;
import ad.agio.test_firebase.domain.User;

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

        setting();
    }

    private User currentUser;
    private AuthController authController;
    private UserController userController;
    private MatchController matchController;

    public void setting() {
        binding.buttonMain.setOnClickListener(v -> {

            if (authController.isAuth()) {
                matchController.prepare();

                userController.readMe(user -> {
                    currentUser = user;
                    binding.buttonMain.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(getApplicationContext(), R.color.primary)
                    ));
                });

                matchController.matchListener = chat -> {
                    Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
                    intent.putExtra("chatId", chat.chatId);
                    startActivity(intent);
                };

                if(!matchController.isMatching) {
                    _log("match: start");
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
                                            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);

                                            if(matchController.getChat() != null)
                                                intent.putExtra("chatId", matchController.getChat().chatId);
                                            else
                                                intent.putExtra("chatId", "fake");

                                            intent.putExtra("isMatching", matchController.isMatching);
                                            intent.putExtra("user", list.get(which).toString());
                                            _log(list.get(which).toString());
                                            startActivity(intent);

//                                            if(matchController.isMatching) // receiving 하는 중
//                                                matchController.receiveResult(list.get(which));
//                                            else // matching 하는 중
//                                                matchController.match(list.get(which));
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
                    _log("match: finish");
                    binding.textIndicator.setText("매칭하려면 밑의 버튼을 눌러주세요");
                    matchController.pauseReceiving();
                }
            }
        });
    }
}