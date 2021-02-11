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

    public void _log(String text) {
        Log.d(this.getClass().getSimpleName(), text);
    }

    private DatabaseReference mDatabase;
    private DatabaseReference childDatabase;
    private UserController userController;
    private AuthController authController;
    private ChatController chatController;

    private Chat mChat;
    private User currentUser;
    private Consumer<ArrayList<User>> matchConsumer = list -> _log(list.toString());
    public Consumer<Chat> matchListener;

    public boolean isMatching;
    public boolean isPreparing = false;

    public MatchController() {
        isMatching = false;
        userController = new UserController();
        authController = new AuthController();
        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("matches");
        if(authController.isAuth())
            prepare();
    }

    public Chat getChat() {
        return mChat;
    }

    public void prepare() {
        isPreparing = true;
        userController.readMe(me -> currentUser = me);
        childDatabase = mDatabase.child(authController.getUid());
    }

    public void addOnlyData(String chatId) {
        currentUser.setMatcher(chatId); // 데이터만 올리는경우, matcher child에 chatId 삽입.
        childDatabase
                .setValue(currentUser)
                .addOnSuccessListener(task -> _log("addOnlyData: success"));
    }

    public void addDataWithListener() {
        childDatabase
                .setValue(currentUser)
                .addOnSuccessListener(task -> childDatabase
                        .child("matcher")
                        .addValueEventListener(receiveListener));
    }

    public void removeData() {
        if(childDatabase != null) {
            childDatabase.removeEventListener(receiveListener);
            childDatabase.removeValue();
        }
    }

    private String previousValue = "";

    // TODO 왜 여러번 listener 가 작동할까? previousValue 이용해서 일단 막아놓기는 했는데 해결이 필요하다.
    // db/matcher/uid/matcher 을 확인하는 listener, 이곳에 상대방이 생성한 chatId가 들어온다.
    private final ValueEventListener receiveListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String value = snapshot.getValue(String.class);
            if (snapshot.exists() && !value.equals(previousValue)) {
                previousValue = value;
                receive(value);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    public void startMatching(Predicate<User> condition, Consumer<ArrayList<User>> consumer) {
        matchConsumer = consumer;
        findUserBy(condition, list -> { // 일단 matchers 중에서 조건을 만족하는 사람을 찾아본다.
            if (list == null || list.isEmpty()) { // 만족하는 사람이 없으면
                startReceiving(); // receiver 작동
            } else {
                // 만족하는 사람이 있으면, consumer 실행
                isMatching = false;
                matchConsumer.accept(list); // receiver 위해서 messaging 하는거 여기서 구현해야됨.
            }
        });
    }

    public void match(User user) {
        String chatId = Utils.randomWord();
        mDatabase.child(user.getUid()).child("matcher").setValue(chatId); // 내꺼다 찜하기

        mChat = new Chat();
        mChat.chatId = chatId;
        mChat.chatName = currentUser.getUserName() + "와 " + user.getUserName() + "의 채팅방";
        mChat.users.put(currentUser.getUid(), currentUser); // 자기 데이터 추가

        chatController = new ChatController(chatId);
        chatController.addChat(mChat);
        chatController.addConfirmListener(result -> {
            matchResult(result);
            chatController.removeConfirmListener(); // 삭 - 제
        }); // receiver 이 허락을 하는지 체크.
    }

    public void matchResult(String result) {
        pauseReceiving();
        if (result.equals("success")) {
            _log("matchResult: success");
            callMatchListener();
            // TODO 성공시 매칭장소, 시간, 견종등을 나타내는 액티비티로 이동
            // TODO chatId 저장.
        } else if (result.equals("fail")) {
            // chatId 삭제 및 listener 삭제.
            _log("matchResult: fail");
            chatController.removeChat();
        } else {
            _log("what the type?");
        }
    }

    public void receive(String chatId) {
        if(!isMatching)
            _log("preReceive");
        chatController = new ChatController(chatId);
        chatController.readChat(chat -> mChat = chat);
        chatController.readOtherUsers(users -> matchConsumer.accept(users));
    }

    public void setChatController(String chatId) {
        chatController = new ChatController(chatId);
        chatController.readChat(chat -> mChat = chat);
    }

    public void receiveResult(User user) {
        pauseReceiving();
        chatController.writeUser(currentUser);
        chatController.sendMatchResult("success");
        callMatchListener();
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

    public void callMatchListener() {
        chatController.readChat(chat -> {
            mChat = chat;
            matchListener.accept(mChat);
        });
    }

    public void findUserBy(Predicate<User> condition, Consumer<ArrayList<User>> consumer) {
        mDatabase.get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        ArrayList<User> list = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            assert snapshot != null;
                            User post = snapshot.getValue(User.class);
                            if (post != null && condition.test(post)
                                    && !post.getUid().equals(currentUser.getUid())
                                    && !post.getUserName().equals("GHOST")
                                    && !post.getUserName().equals("")) {
                                list.add(post);
                            }
                        }
                        consumer.accept(list);
                    } else {
                        consumer.accept(null);
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }
}
