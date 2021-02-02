package ad.agio.test_firebase.controller;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.function.Consumer;

import ad.agio.test_firebase.domain.Chat;
import ad.agio.test_firebase.domain.User;

public class ChatController {
    public String chatId;
    private DatabaseReference db;

    public ChatController(String chatId) {
        this.chatId = chatId;
        this.db = FirebaseDatabase.getInstance().getReference()
                .child("chat")
                .child(chatId);
    }

    public void writeChat(Chat chat) {
        db.setValue(chat);
    }

    public void readChat(Consumer<Chat> consumer) {
        db.get()
                .addOnSuccessListener(dataSnapshot -> consumer.accept(dataSnapshot.getValue(Chat.class)))
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void writeUser(User user) {
        db.child("users")
                .child(user.getUid())
                .setValue(user);
    }

    public void sendMatchResult(String result) {
        db.child("result")
                .setValue(result);
    }

    private ValueEventListener confirmListener;

    public void addConfirmListener(Consumer<String> consumer) {
        // db/chat/chatId/match 를 확인하는 listener, receiver가 동의했을 때 success 문자열이 들어오는 것을 확인
        confirmListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String post = snapshot.getValue(String.class);
                if (post != null)
                    consumer.accept(post);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        db.child("match") // Check chat's match child
                .addValueEventListener(confirmListener);
    }
    public void removeConfirmListener() {
        db.child("match")
                .removeEventListener(confirmListener);
    }

    public void readAllUsers(Consumer<ArrayList<User>> consumer) {
        db.get()
                .addOnSuccessListener(dataSnapshot -> {
                    ArrayList<User> list = new ArrayList<>();
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    chat.users.forEach( (key, user) -> list.add(user));
                    consumer.accept(list);
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void readOtherUsers(Consumer<ArrayList<User>> consumer) {
        AuthController auth = new AuthController();
        db.get()
                .addOnSuccessListener(dataSnapshot -> {
                    ArrayList<User> list = new ArrayList<>();
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    chat.users.forEach( (key, user) -> {
                        if(!user.getUid().equals(auth.getUid())) // 내가 아닌것들만 add
                            list.add(user);
                    });
                    consumer.accept(list);
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

}
