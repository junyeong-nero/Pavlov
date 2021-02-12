package ad.agio.test_firebase.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.google.firebase.database.DatabaseReference;

import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.ChatController;
import ad.agio.test_firebase.databinding.ActivityChatBinding;
import ad.agio.test_firebase.domain.Chat;
import ad.agio.test_firebase.domain.User;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private DatabaseReference chatDatabase;
    private ChatController chatController;
    private AuthController authController;
    private Chat mChat;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        String chatId = intent.getStringExtra("chatId");

        authController = new AuthController();
        chatController = new ChatController(chatId);
        chatController.readChat(chat -> {
            mChat = chat;
            currentUser = mChat.users.get(authController.getUid());
        });

        chatController.addTextListener(text -> binding.text.setText(text)); // text Listener

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