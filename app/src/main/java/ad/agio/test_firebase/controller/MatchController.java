package ad.agio.test_firebase.controller;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import ad.agio.test_firebase.domain.Chat;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.utils.Utils;

public class MatchController {

    static public String TAG = "MatchController";

    public void LOGGING(String text) {
        Log.d(TAG, text);
    }

    private DatabaseReference mDatabase;
    private DatabaseReference childDatabase;
    private UserController userController;
    private AuthController authController;
    private User currentUser;

    public MatchController() {
        userController = new UserController();
        authController = new AuthController();
        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("matches");
        childDatabase = mDatabase.child(authController.getUid());
        userController.readMe(me -> currentUser = me);
    }

    public void addOnlyData(String chatId) {
        currentUser.setMatcher(chatId); // 데이터만 올리는경우, matcher child에 chatId 삽입.
        childDatabase
                .setValue(currentUser)
                .addOnSuccessListener(task -> LOGGING("addOnlyData: success"));
    }

    public void addDataWithListener() {
        childDatabase
                .setValue(currentUser)
                .addOnSuccessListener(task -> childDatabase
                        .addValueEventListener(listener));
    }

    public void removeData() {
        childDatabase.removeEventListener(listener);
        childDatabase.removeValue();
    }

    // receive
    private final ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String value = snapshot.child("matcher").getValue(String.class);
            if (value != null && !value.equals("")) {
                preReceive(value);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    // confirm
    private final ValueEventListener confirmListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String post = snapshot.getValue(String.class);
            assert post != null;
            if (post.equals("success")) {
                LOGGING("match: 상대방이 수락함");
            } else if (post.equals("fail")) {
                LOGGING("match: 상대방이 거절함");
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private Chat mChat;
    private boolean isMatching = false;
    private Consumer<ArrayList<User>> matchConsumer = list -> LOGGING(list.toString());

    public void startMatching(Predicate<User> condition, Consumer<ArrayList<User>> consumer) {
        matchConsumer = consumer;
        findUserBy(condition, list -> { // 일단 matchers 중에서 조건을 만족하는 사람을 찾아본다.
            if (list.isEmpty()) {
                // 만족하는 사람이 없으면,
                isMatching = true;
                startReceiving();
            } else {
                // 만족하는 사람이 있으면, consumer 실행
                isMatching = false;
                matchConsumer.accept(list); // receiver 위해서 messaging 하는거 여기서 구현해야됨.
            }
        });
    }

    public void match(User user) {
        // stopReceiving();
        String chatId = Utils.randomWord(); // TODO 랜덤 chatId generator
        mDatabase.child(user.getUid()).child("match").setValue(chatId); // 내꺼다 찜하기

        mChat = new Chat();
        mChat.chatId = chatId;
        mChat.chatName = currentUser.getUserName() + "와 " + user.getUserName() + "의 채팅방";
        mChat.users.put(currentUser.getUid(), currentUser); // 자기 데이터 추가

        DatabaseReference db = FirebaseDatabase.getInstance().getReference()
                .child("chat")
                .child(mChat.chatId);
        db.setValue(mChat);

        // Check chat's match child
        db.child("match").addValueEventListener(confirmListener);
    }

    public void preReceive(String chatId) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference()
                .child("chat");

        db.child(chatId)
                .get()
                .addOnSuccessListener(dataSnapshot -> mChat = dataSnapshot.getValue(Chat.class));

        ArrayList<User> list = new ArrayList<>();
        for (String key : mChat.users.keySet())
            if (!key.equals(authController.getUid()))
                list.add(mChat.users.get(key));

        matchConsumer.accept(list);
    }


    public void receive(String chatId) {
        stopReceiving();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference()
                .child("chat")
                .child(chatId);
        db.child("match").setValue("success"); // 성공 메세지 입력
        db.child("users").child(currentUser.getUid()).setValue(currentUser); // 사용자 데이터 입력
    }

    public void startReceiving() {
        addDataWithListener();
    }

    public void pauseReceiving() {
        if (isMatching) {
            removeData();
        }
    }

    public void stopReceiving() {
        removeData();
        isMatching = false;
        mDatabase = null;
    }

    public void findAll(Consumer<ArrayList<User>> consumer) {
        findUserBy(user -> true, consumer);
    }

    public void findUserBy(Predicate<User> condition, Consumer<ArrayList<User>> consumer) {
        mDatabase.get()
                .addOnSuccessListener(dataSnapshot -> {
                    ArrayList<User> list = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User post = snapshot.getValue(User.class);
                        if (condition.test(post))
                            list.add(post);
                    }
                    consumer.accept(list);
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }
}
