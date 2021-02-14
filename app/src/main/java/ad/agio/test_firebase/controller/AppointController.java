package ad.agio.test_firebase.controller;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Optional;
import java.util.function.Consumer;

import ad.agio.test_firebase.activities.ChatActivity;
import ad.agio.test_firebase.activities.HomeActivity;
import ad.agio.test_firebase.domain.Chat;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.utils.Utils;

public class AppointController {

    private DatabaseReference db;
    private AuthController authController;
    private UserController userController;
    private ValueEventListener listener;
    private User currentUser;
    private Consumer<ArrayList<User>> consumer;

    public AppointController() {
        this.authController = new AuthController();
        this.userController = new UserController();
        this.userController.readMe(me -> currentUser = me);
        this.db = FirebaseDatabase.getInstance().getReference()
                .child("appoint")
                .child(authController.getUid());
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<User> list = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (data.exists())
                        list.add(data.getValue(User.class));
                }
                consumer.accept(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    public void makeReceiver(Consumer<ArrayList<User>> consumer) {
        this.consumer = consumer;
        if (authController.isAuth()) {
            db.setValue("empty")
                    .addOnSuccessListener(task -> db.addValueEventListener(listener));
        }
    }

    private Chat mChat;
    public Consumer<Chat> appointmentListener;

    public void appoint(String chatId) {
        ChatController chatController = new ChatController(chatId);
        chatController.writeUser(currentUser);
        chatController.readChat(chat -> {
            mChat = chat;
            if(appointmentListener != null)
                appointmentListener.accept(mChat);
        });
    }

    public void request(String uid) {
        DatabaseReference want = FirebaseDatabase.getInstance().getReference()
                .child("appoint")
                .child(uid);
        userController.readMe(me -> {
            me.setChatId(Utils.randomWord()); // TODO 정말 희박한 가능성이지만 chatId가 중복될 가능성이 있다.
            want.child(authController.getUid()).setValue(me);
        });
    }
}
