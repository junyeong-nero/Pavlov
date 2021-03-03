package ad.agio.test_firebase.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Optional;

import ad.agio.test_firebase.activities.ChatActivity;
import ad.agio.test_firebase.activities.OtherProfileActivity;
import ad.agio.test_firebase.controller.NotificationController;
import ad.agio.test_firebase.databinding.FragmentHomeBinding;
import ad.agio.test_firebase.domain.User;

import static ad.agio.test_firebase.activities.HomeActivity.matchController;
import static ad.agio.test_firebase.activities.HomeActivity.authController;

public class HomeFragment extends Fragment {

    private void log(String text) {
        Log.e(this.getClass().getSimpleName(), text);
    }
    private FragmentHomeBinding binding;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NotificationController controller = new NotificationController();
        controller.readNotification(notification -> {
            binding.textTitle.setText(notification.getTitle());
            binding.textContent.setText(notification.getContent());
        });

        if(authController.isAuth())
            matchController.prepare();

        setting();
    }

    public void setting() {

        matchController.successListener = chat -> { // match is finished!
            matchFinish();
            Intent intent = new Intent(matchController.getContext(), ChatActivity.class);
            intent.putExtra("chatId", chat.chatId);
            startActivity(intent);
        };

        binding.buttonMain.setOnClickListener(v -> {
            if (authController.isAuth()) {

                if(!matchController.isPreparing)
                    matchController.prepare();

                if(!matchController.isReceiving) {
                    log("match: start");
                    binding.textIndicator.setText("매칭중..");
                    matchController.startMatching(
                            user -> true, // condition
                            list -> {
                                Optional<User> user = list.stream().findAny();
                                log(user.toString());

                                user.ifPresent(value -> {

                                    Intent intent = new Intent(matchController.getContext(),
                                            OtherProfileActivity.class);
                                    intent.putExtra("type", "match");
                                    intent.putExtra("isReceiving", matchController.isReceiving);
                                    // request => false, receive => true
                                    intent.putExtra("user", value.toString());

                                    if (matchController.getChat() != null) {
                                        intent.putExtra("chatId", matchController.getChat().chatId);
                                        // receive 하는 경우 chatId가 존재함.
                                    } else {
                                        intent.putExtra("chatId", "fake");
                                        // request 하는 경우 chatId가 없음.
                                    }
                                    startActivity(intent);
                                    matchFinish(); // 자동으로 매칭종료.
                                });
                            });
                } else {
                    matchFinish();
                }
            }
        });
    }

    private void matchFinish() {
        log("match: finish");
        binding.textIndicator.setText("매칭하려면 밑의 버튼을 눌러주세요");
        matchController.pauseReceive();
    }

    @Deprecated
    private void showDialog(ArrayList<User> list) { // for select user to match
        CharSequence[] items = new CharSequence[list.size()];
        for (int i = 0; i < items.length; i++)
            items[i] = list.get(i).getUserName();

        new AlertDialog.Builder(requireContext())
                .setTitle("매칭성공")
                .setItems(items, (dialog, which) -> {
                    Intent intent = new Intent(requireContext(), OtherProfileActivity.class);
                    intent.putExtra("type", "home");
                    intent.putExtra("isReceiving", matchController.isReceiving);
                    intent.putExtra("user", list.get(which).toString());

                    if (matchController.getChat() != null)
                        intent.putExtra("chatId", matchController.getChat().chatId);
                    else
                        intent.putExtra("chatId", "fake");

                    log(list.get(which).toString());
                    startActivity(intent);
                })
                .setNegativeButton("안할래용", (dialog, which) -> {
                    binding.textIndicator.setText("매칭하려면 밑의 버튼을 눌러주세요");
                    matchController.pauseReceive();
                })
                .setOnDismissListener(dialog -> {
                    binding.textIndicator.setText("매칭하려면 밑의 버튼을 눌러주세요");
                    matchController.pauseReceive();
                })
                .show();
    }


}