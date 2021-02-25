package ad.agio.test_firebase.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.ChatController;
import ad.agio.test_firebase.databinding.ActivityChatBinding;
import ad.agio.test_firebase.domain.Chat;
import ad.agio.test_firebase.domain.User;

import static ad.agio.test_firebase.activities.HomeActivity.authController;
import static ad.agio.test_firebase.activities.HomeActivity.currentUser;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatController chatController;
    private Chat mChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        String chatId = intent.getStringExtra("chatId");

        chatController = new ChatController(chatId);
        chatController.readChat(chat -> {
            mChat = chat;
            binding.toolbarTitle.setText(mChat.chatName);
        });
        chatController.addTextListener(text -> binding.text.setText(text)); // text Listener

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.editText.setOnKeyListener((v, actionId, event) -> {
            if (actionId == KeyEvent.KEYCODE_ENTER) {
                send();
            }
            return false;
        });

        binding.buttonSend.setOnClickListener(v -> send());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatController.removeConfirmListener();
        chatController.removeTextListener();
    }

    public void send() {
        assert chatController != null;
        chatController.sendText(currentUser, binding.editText.getText().toString());

        binding.editText.setText("");
        binding.scroll.post(() -> binding.scroll.fullScroll(View.FOCUS_DOWN)); // 가장 아래로 스크롤 내리기
    }
}