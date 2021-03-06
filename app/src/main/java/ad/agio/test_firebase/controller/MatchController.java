package ad.agio.test_firebase.controller;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import ad.agio.test_firebase.domain.Chat;
import ad.agio.test_firebase.domain.Meeting;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.utils.Utils;

public class MatchController {

    public void log(String text) {
        Log.d(this.getClass().getSimpleName(), text);
    }

    private DatabaseReference childDatabase;
    private ChatController chatController;
    private final DatabaseReference mDatabase;
    private final UserController userController;
    private final AuthController authController;

    private Chat mChat;
    private User currentUser, otherUser;
    private Consumer<ArrayList<User>> otherProfileConsumer = list -> log(list.toString());
    public Consumer<Chat> successListener;
    public Consumer<Chat> failureListener;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private Context context;

    public boolean isReceiving;
    public boolean isPreparing = false;

    public MatchController() {
        isReceiving = false;
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

    /**
     * 매칭전 자신의 프로필과 db에 연결합니다.
     */
    public void prepare() {
        if(authController.isAuth()) {
            isPreparing = true;
            userController.readMe(me -> currentUser = me);
            childDatabase = mDatabase.child(authController.getUid());
        } else {
            log("it is not authenticated");
        }
    }

    /**
     * db에 자신의 프로필을 업로드하고
     * 요청을 받는 리스너를 추가합니다.
     */
    private void addDataWithListener() {
        childDatabase
                .setValue(currentUser)
                .addOnSuccessListener(task -> childDatabase
                        .child("chatId")
                        .addValueEventListener(receiveListener));
    }

    /**
     * db에 업로드한 자신의 프로필을 지웁니다.
     */
    private void removeData() {
        if(childDatabase != null) {
            childDatabase.removeEventListener(receiveListener);
            childDatabase.removeValue();
        }
    }

    private String previousValue = "";

    /**
     * db/matches/uid/chatId 을 확인하는 listener
     * 상대방이 생성한 chatId를 확인하고 receive 함수로 넘겨줌.
     */
    private final ValueEventListener receiveListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String value = snapshot.getValue(String.class);
            if (snapshot.exists()) {
                assert value != null;
                if (!value.equals(previousValue)) { // 똑같은 정보를 여러번 받는 버그가 있어서 추가함.
                    previousValue = value;
                    receive(value);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    /**
     * 매칭을 시작함. receive 할지 혹은 request 할지 여기서 나뉨.
     * 매칭을 하는 조건과, 매칭시 어떤 행동을 해야하는지 지정해야 함.
     * @param condition 매칭조건, 성별, 나이, 거리 등
     * @param consumer 매칭되었을 때 수행될 것
     */
    public void startMatching(Predicate<User> condition, Consumer<ArrayList<User>> consumer) {
        otherProfileConsumer = consumer;
        isReceiving = true; // 일단 receiving 을 true 로 만들자.
        findUserBy(condition, list -> { // 일단 matchers 중에서 조건을 만족하는 사람을 찾아본다.
            if (list == null || list.isEmpty()) { // 만족하는 사람이 없으면
                startReceive(); // receiver 작동
            } else {
                // 만족하는 사람이 있으면, consumer 실행
                isReceiving = false;
                otherProfileConsumer.accept(list); // receiver 위해서 messaging 하는거 여기서 구현해야됨.
            }
        });
    }

    /**
     * 매칭을 요청함.
     * @param otherUser 상대방
     */
    public void request(User otherUser) {
        this.otherUser = otherUser;
        String chatId = Utils.randomWord();
        mDatabase.child(otherUser.getUid()).child("chatId").setValue(chatId);
        // 상대방의 프로필의 chatId에 생성한 chatId를 전송합니다.

        mChat = new Chat();
        mChat.chatId = chatId;
        mChat.chatName = currentUser.getUserName() + "와 " + otherUser.getUserName() + "의 채팅방";
        mChat.users.put(currentUser.getUid(), currentUser); // 자신의 프로필을 채팅방에 추가

        chatController = new ChatController(chatId);
        chatController.writeChat(mChat);
        chatController.addConfirmListener(result -> {
            requestResult(result);
            chatController.removeConfirmListener(); // 수락을 확인하는 컨트롤러를 삭제합니다.
        }); // receiver 가 요청을 수락하는지 체크합니다.
    }

    /**
     * 매칭 요청이후, 상대방이 수락여부를 컨트롤 합니다.
     * @param result requestResult, success or fail
     */
    public void requestResult(String result) {
        pauseReceive();
        if (result.equals("success")) {
            log("matchResult: success");
            chatController.readChat(chat -> {
                mChat = chat;
                addChat(mChat);
                if(successListener != null) {
                    successListener.accept(mChat); // 채팅방과 연결
                }
            });
        } else {
            // chatId 삭제 및 listener 삭제.
            log("matchResult: fail");
            chatController.removeChat();
            failureListener.accept(null);
        }
    }

    /**
     * 상대방의 요청을 받음. startReceiving 와 연결되어 있습니다.
     * matchers/myUid/chatId 리스터에 의해서 작동합니다.
     * @param chatId chatId
     */
    public void receive(String chatId) {
        if(!isReceiving)
            log("is not Receiving");
        chatController = new ChatController(chatId);
        chatController.readChat(chat -> mChat = chat);
        chatController.readOtherUsers(users -> otherProfileConsumer.accept(users));
    }

    /**
     * 상대방의 요청을 수락함.
     * @param otherUser 상대방
     */
    public void receiveResult(User otherUser) {
        log("receiveResult\n" + otherUser.toString());
        this.otherUser = otherUser;
        pauseReceive();
        chatController.writeUserOnComplete(currentUser, task -> {
            chatController.sendMatchResult("success"); // request 가 성공함을 알림.
        }); // 프로필을 채팅창에 작성한 이후에 성공을 알린다.
        chatController.readChat(chat -> {
            mChat = chat;
            mChat.writeUser(currentUser);
            addChat(mChat);
            addMeeting(mChat);
            if(successListener != null) {
                successListener.accept(mChat); // 채팅방과 연결
            }
        });
    }

    /**
     * 상대방의 요청을 수락함.
     * @param otherUser 상대방
     */
    public void reject(User otherUser) {
        log("reject\n" + otherUser.toString());
        this.otherUser = otherUser;

        pauseReceive();
        chatController.sendMatchResult("success");
        if(failureListener != null)
            failureListener.accept(null);
    }


    /**
     * 채팅 컨트롤러를 연결함.
     * @param chatId chatId
     */
    public void setChatController(String chatId) {
        chatController = new ChatController(chatId);
        chatController.readChat(chat -> mChat = chat);
    }

    /**
     * 요청을 확인하는 리스너를 실행함.
     */
    public void startReceive() {
        isReceiving = true;
        addDataWithListener();
    }

    /**
     * 요청을 확인하는 리스너를 중단함.
     */
    public void pauseReceive() {
        isReceiving = false;
        removeData();
    }

    private void addChat(Chat chat) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        log(gson.toJson(mChat));
        userController.writeChatId(chat);
    }

    private void addMeeting(Chat chat) {
        chat.readOtherUsers(list -> {
            Meeting r = userController.makeMatchMeeting(currentUser, list.get(0));
            chatController.writeMeeting(r);
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
                                // root 가 없어져서 client is offline 에러가 발생하지 않도록 GHOST 데이터를 추가해놓았음.
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
