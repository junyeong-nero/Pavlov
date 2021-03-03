package ad.agio.test_firebase.controller;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.function.Consumer;

import ad.agio.test_firebase.domain.Chat;
import ad.agio.test_firebase.domain.Meeting;
import ad.agio.test_firebase.domain.Time;
import ad.agio.test_firebase.domain.User;

import static ad.agio.test_firebase.activities.HomeActivity.currentUser;

public class UserController {

    private void log(String s) {
        Log.e(this.getClass().getSimpleName(), s);
    }

    private final FirebaseFirestore db;
    private final AuthController authController;

    public UserController() {
        this.db = FirebaseFirestore.getInstance();
        this.authController = new AuthController();
    }

    /**
     * 유저정보를 추가합니다.
     * @param user user
     */
    public void writeNewUser(User user) {
        log("writeNewUser : " + user.toString());
        db.collection("users")
                .document(user.getUid()).set(user);
    }

    /**
     * Firestore 에 업로드 되어있고, type 이 public 인 사용자들을 읽습니다.
     * Firestore rule 에 의해서 type 이 public 이거나 자신의 프로필만 읽을 수 있다.
     * @param consumer
     */

    // TODO 모든 사람들이 두번씩 읽히는 버그.
    public void readAllUsers(Consumer<User> consumer) {
        log("readAllUsers");
        db.collection("users")
                .whereEqualTo("type", "public")
                .get()
                .addOnCompleteListener(snapshot -> {
                    if (snapshot.isSuccessful()) {
                        for (QueryDocumentSnapshot post : snapshot.getResult()) {
                            consumer.accept(post.toObject(User.class));
                        }
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }


    /**
     * 데이터베이스에 업로드 되어있는 자신의 프로필을 읽습니다.
     * @param consumer
     */
    public void readMe(Consumer<User> consumer) {
        if(authController.isAuth())
            readUser(authController.getUid(), me -> {
                currentUser = me;
                consumer.accept(me);
            });
        else
            log("it is not authenticated");
    }

    /**
     * 다른 사용자의 프로필을 읽습니다.
     * @param uid 다른 사용자의 uid
     * @param consumer 사용자를 컨트롤할 consumer
     */
    public void readUser(String uid, Consumer<User> consumer) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(snapshot -> {
                    consumer.accept(snapshot.getResult().toObject(User.class));
                })
                .addOnFailureListener(snapshot -> {
                    consumer.accept(null);
                });
    }

    /**
     * 사용자의 프로필 데이터를 업데이트 합니다.
     * @param tag 사용자 프로필 태그
     * @param value 업데이트할 값.
     */
    public void updateUser(String tag, Object value) {
        db.collection("users")
                .document(authController.getUid()).update(tag, value);
    }

    /**
     * 사용자의 프로필을 업데이트 합니다.
     * @param user 사용자 프로필
     */
    public void updateUser(User user) {
        db.collection("users")
                .document(user.getUid())
                .set(user);
    }

    /**
     * 프로필 이미지를 업로드 합니다.
     * @param path
     * @throws FileNotFoundException
     */
    public void writeProfileImage(String path) throws FileNotFoundException {
        if(authController.isAuth()) {
            FirebaseStorage.getInstance().getReference()
                    .child("profile_images")
                    .child(authController.getUid())
                    .child("profile")
                    .putStream(new FileInputStream(new File(path)));
        }
    }

    public void readProfileImage(Consumer<byte[]> consumer) {
        readProfileImage(authController.getUid(), consumer);
    }

    /**
     * 프로필 이미지의 경로를 읽어옵니다.
     * @return
     */
    public void readProfileImage(String uid, Consumer<byte[]> consumer) {
        if (authController.isAuth()) {
            FirebaseStorage.getInstance().getReference()
                    .child("profile_images")
                    .child(uid)
                    .child("profile")
                    .getBytes(Long.MAX_VALUE / (2048))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            consumer.accept(task.getResult());
                        }
                    });

        } else {
            log("is not authenticated");
        }
    }

    public void writeChat(Chat chat) {
        if(!currentUser.getArrayChatId().contains(chat.chatId)) {
            String temp = currentUser.getArrayChatId() + chat.chatId + "|";
            updateUser("arrayChatId", temp);
            currentUser.setArrayChatId(temp);
        }
    }

    public void readChat(Consumer<String> consumer) {
        String[] split = currentUser.getArrayChatId().split("\\|");
        for (String chatId : split) {
            if(!chatId.equals(""))
                consumer.accept(chatId);
        }
    }

    public ArrayList<String> readChat() {
        String[] split = currentUser.getArrayChatId().split("\\|");
        return new ArrayList<>(Arrays.asList(split));
    }

    public void removeChat(String chatId) {
        if(currentUser.getArrayChatId().contains(chatId)) {
            ArrayList<String> arr = readChat();
            arr.remove(chatId);
            updateUser("arrayChatId", TextUtils.join("|", arr));
        }
    }

    public Meeting makeMatchMeeting(User user1, User user2) {
        Meeting result = new Meeting();

        Time time = new Time(Calendar.getInstance());
        time.minute += 15; // 15분 뒤에 보자!
        // TODO 이렇게 하면 61분 같은 대참사가 발생한다.
        result.time = time;
        result.place = user1.getNeighbor(); // 일단 동네로 설정
        result.address = user1.getNeighbor(); // user1 기준으로 되어잇는 것도 문제.
        // TODO google place 사용해서 장소선정 해야함.
        return result;
    }

    public Meeting makeAppointMeeting(User user1, User user2) {
        Meeting meeting = new Meeting();

        Time time = new Time(Calendar.getInstance());
        time.minute += 15; // 15분 뒤에 보자!

        meeting.time = time;
        meeting.place = user1.getNeighbor(); // 일단 동네로 설정
        meeting.address = user1.getNeighbor(); // user1 기준으로 되어잇는 것도 문제.
        return meeting;
    }
}
