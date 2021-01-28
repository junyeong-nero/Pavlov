package ad.agio.test_firebase.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivityChatBinding;
import ad.agio.test_firebase.databinding.ActivitySearchBinding;
import ad.agio.test_firebase.domain.Chat;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private DatabaseReference chatDatabase;
    private Chat mChat;
    private String senderName;

    private final ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Chat post = snapshot.getValue(Chat.class);
            if (post == null) {
                chatDatabase.setValue(mChat);
            } else {
                mChat = post;
                update();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        String receiver = intent.getStringExtra("receiver");
        String sender = intent.getStringExtra("sender");

        mChat = new Chat();
        mChat.setReceiverId(receiver);
        mChat.setSenderId(sender);
        mChat.setChatId("hello");

        UserController controller = new UserController();
        controller.readUser(mChat.getSenderId(), user -> {
            senderName = user.getUserName();
        });

        DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                .child("chat");

        chatDatabase = database.child(mChat.getChatId());
        chatDatabase.addValueEventListener(listener);

//        database.child(mChat.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Chat post = snapshot.getValue(Chat.class);
//                if (post != null) {
//                    check = true;
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//        database.child(mChat.getReverseChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Chat post = snapshot.getValue(Chat.class);
//                if (post != null) {
//                    check2 = true;
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//        if (check) { // 기존 데이터 베이스 체크
//            chatDatabase = database.child(mChat.getChatId());
//            chatDatabase.addValueEventListener(listener);
//        } else if (check2) {
//            // 내가 받은적은 없었는지 체크
//            chatDatabase = database.child(mChat.getReverseChatId());
//            chatDatabase.addValueEventListener(listener);
//        } else {
//            chatDatabase = database.child(mChat.getChatId()); // 없으면 생성하기.
//            chatDatabase.setValue(mChat).addOnCompleteListener(task -> {
//                chatDatabase.addValueEventListener(listener); // 성공후 listener 연결
//            });
//        }

        binding.editText.setOnKeyListener((v, actionId, event) -> {
            if (actionId == KeyEvent.KEYCODE_ENTER) {
                String temp = mChat.getText() + "[" + senderName + "]:" + binding.editText.getText() + "\n";
                mChat.setText(temp);
                chatDatabase.setValue(mChat);

                binding.editText.setText("");
                binding.scroll.post(() -> binding.scroll.fullScroll(View.FOCUS_DOWN));
            }
            return false;
        });

        binding.buttonSend.setOnClickListener(v -> {
            String temp = mChat.getText();
            temp += "\n" + "[" + senderName + "]:" + binding.editText.getText();
            binding.editText.setText("");
            mChat.setText(temp);
            chatDatabase.setValue(mChat);
        });
    }

    private void update() {
        binding.text.setText(mChat.getText());
    }
}