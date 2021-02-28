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

import ad.agio.test_firebase.domain.Chat;
import ad.agio.test_firebase.domain.Meeting;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.utils.Utils;

public class AppointController {

    private void log(String text) {
        Log.e(this.getClass().getSimpleName(), text);
    }

    private DatabaseReference db;
    private final AuthController authController;
    private final UserController userController;

    private User currentUser, otherUser;
    private Chat mChat;

    public Context getContext() {
        return context;
    }
    public void setContext(Context context) {
        this.context = context;
    }

    private Context context;

    private final ValueEventListener receiveListener;
    private Consumer<ArrayList<User>> receiveConsumer;
    public Consumer<Chat> successListener;
    public Consumer<Chat> failureListener;

    public AppointController() {
        this.authController = new AuthController();
        this.userController = new UserController();
        this.userController.readMe(me -> currentUser = me);
        if(authController.isAuth()) {
            this.db = FirebaseDatabase.getInstance().getReference()
                    .child("appoint")
                    .child(authController.getUid());
        }
        receiveListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<User> list = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (data.exists())
                        list.add(data.getValue(User.class));
                }
                receiveConsumer.accept(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    /**
     * 다른사람이 나에게 요청하는 것을 확인하는 리스너를 실행함.
     * @param consumer
     */
    public void startReceive(Consumer<ArrayList<User>> consumer) {
        this.receiveConsumer = consumer;
        if (authController.isAuth()) {
            log("startReceive");
            db.setValue("empty")
                    .addOnSuccessListener(task -> db.addValueEventListener(receiveListener));
        }
    }

    /**
     * 다른사람이 나에게 요청하는 것을 확인하는 리스너를 삭제함.
     */
    public void removeReceive() {
        if (receiveListener != null) {
            db.removeEventListener(receiveListener);
        }
    }

    /**
     * 다른 사람이 보낸 요청을 수락함.
     * @param chatId chatId
     */
    public void appoint(String chatId) {
        chatController = new ChatController(chatId);
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
     * 다른 사람이 보낸 요청을 거절함.
     * @param chatId chatId
     */
    public void reject(String chatId) {
        ChatController chatController = new ChatController(chatId);
        chatController.sendMatchResult("failure"); // request 가 실패함을 알림.
        failureListener.accept(null);
    }

    private ChatController chatController;

    /**
     * 다른 사람에게 요청을 보냄.
     * @param otherUser
     */
    public void request(User otherUser) {
        this.otherUser = otherUser;
        userController.readMe(me -> {

            mChat = new Chat();
            mChat.chatId = Utils.randomWord();
            mChat.chatName = currentUser.getUserName() + "와 " + otherUser.getUserName() + "의 채팅방";
            mChat.users.put(currentUser.getUid(), currentUser); // 자신의 데이터를 채팅방에 추가.

            chatController = new ChatController(mChat.chatId);
            chatController.writeChat(mChat);
            chatController.addConfirmListener(result -> {
                requestResult(result); // 요청의 결과가 나오는 것을 확인하는 리스너
                chatController.removeConfirmListener();
            });

            me.setChatId(mChat.chatId);
            FirebaseDatabase.getInstance().getReference()
                    .child("appoint")
                    .child(otherUser.getUid()) // 다른 사람의 db 에
                    .child(authController.getUid()) // 자신의 uid child 를 생성하고
                    .setValue(me); // 자신의 프로필을 추가함.
        });
    }

    /**
     * 요청의 결과를 처리함.
     * @param result
     */
    private void requestResult(String result) {
        FirebaseDatabase.getInstance().getReference()
                .child("appoint")
                .child(otherUser.getUid())
                .child(authController.getUid())
                .removeValue(); // 다른사람의 db에서 자신의 프로피을 지움.
        if (result.equals("success")) {
            if(successListener != null) {
                addChat(mChat);
                successListener.accept(mChat);
            }
        } else {
            chatController.removeChat();
            if(failureListener != null)
                failureListener.accept(null);
        }
    }

    private void addChat(Chat chat) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        log(gson.toJson(mChat));

        if(!currentUser.getArrayChatId().contains(chat.chatId)) {
            String temp = currentUser.getArrayChatId() + chat.chatId + "|";
            userController.updateUser("arrayChatId", temp);
            currentUser.setArrayChatId(temp);
        }
    }

    private void addMeeting(Chat chat) {
        chat.readOtherUsers(list -> {
            Meeting r = userController.makeAppointMeeting(currentUser, list.get(0));
            chatController.writeMeeting(r);
        });
    }
}
