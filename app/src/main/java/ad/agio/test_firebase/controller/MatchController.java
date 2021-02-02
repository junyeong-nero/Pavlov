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

    public boolean isMatching;

    public MatchController() {
        isMatching = false;
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
                        .child("matcher")
                        .addValueEventListener(receiveListener));
    }

    public void removeData() {
        childDatabase.removeEventListener(receiveListener);
        childDatabase.removeValue();
    }

    // db/matcher/uid/matcher 을 확인하는 listener, 이곳에 상대방이 생성한 chatId가 들어온다.
    private final ValueEventListener receiveListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String value = snapshot.getValue(String.class);
            if (snapshot.exists() && !value.equals("")) {
                preReceive(value);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private Chat mChat;
    private Consumer<ArrayList<User>> matchConsumer = list -> LOGGING(list.toString());

    public void startMatching(Predicate<User> condition, Consumer<ArrayList<User>> consumer) {
        matchConsumer = consumer;
        findUserBy(condition, list -> { // 일단 matchers 중에서 조건을 만족하는 사람을 찾아본다.
            if (list == null || list.isEmpty()) { // 만족하는 사람이 없으면
                startReceiving(); // receiver 작동
            } else {
                // 만족하는 사람이 있으면, consumer 실행
                LOGGING("사람이 있어요");
                isMatching = false;
                matchConsumer.accept(list); // receiver 위해서 messaging 하는거 여기서 구현해야됨.
            }
        });
    }

    public void match(User user) {
        pauseReceiving();
        String chatId = Utils.randomWord();
        mDatabase.child(user.getUid()).child("match").setValue(chatId); // 내꺼다 찜하기

        mChat = new Chat();
        mChat.chatId = chatId;
        mChat.chatName = currentUser.getUserName() + "와 " + user.getUserName() + "의 채팅방";
        mChat.users.put(currentUser.getUid(), currentUser); // 자기 데이터 추가

        ChatController chatController = new ChatController(chatId);
        chatController.writeChat(mChat);
        chatController.addConfirmListener(result -> {
            matchResult(result);
            chatController.removeConfirmListener(); // 삭 - 제
        }); // receiver 이 허락을 하는지 체크.
    }

    public void matchResult(String result) {
        if (result.equals("success")) {
            LOGGING("matchResult: success");
            pauseReceiving();
            // TODO 성공시 매칭장소, 시간, 견종등을 나타내는 액티비티로 이동
            // TODO chatId 저장.
        } else if (result.equals("fail")) {
            // chatId 삭제 및 listener 삭제.
            LOGGING("matchResult: fail");
            FirebaseDatabase.getInstance().getReference()
                    .child("chat")
                    .child(mChat.chatId)
                    .removeValue();
            pauseReceiving();
        } else {
            LOGGING("what the type?");
        }
    }

    public void preReceive(String chatId) {
        if(!isMatching)
            throw new IllegalArgumentException("why!");
        ChatController chatController = new ChatController(chatId);
        chatController.readChat(chat -> mChat = chat);
        chatController.readOtherUsers(users -> matchConsumer.accept(users));
    }

    public void receive(User user) {
        pauseReceiving();
        ChatController chatController = new ChatController(mChat.chatId);
        chatController.writeUser(currentUser);
        chatController.sendMatchResult("success");
        // TODO 여기에 무언가 약속장소나, 시간을 db/chat/chatID에 저장하는 그런 기능이 들어가야 한다.
    }

    public void startReceiving() {
        isMatching = true;
        addDataWithListener();
    }

    public void pauseReceiving() {
        isMatching = false;
        removeData();
    }

    public void findUserBy(Predicate<User> condition, Consumer<ArrayList<User>> consumer) {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ArrayList<User> list = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User post = snapshot.getValue(User.class);
                        if (condition.test(post))
                            list.add(post);
                    }
                    consumer.accept(list);
                } else {
                    consumer.accept(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                LOGGING(error.getMessage());
            }
        });
//        mDatabase.get()
//                .addOnSuccessListener(dataSnapshot -> {
//                    ArrayList<User> list = new ArrayList<>();
//                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        User post = snapshot.getValue(User.class);
//                        if (condition.test(post))
//                            list.add(post);
//                    }
//                    consumer.accept(list);
//                })
//                .addOnFailureListener(Throwable::printStackTrace);
    }
}