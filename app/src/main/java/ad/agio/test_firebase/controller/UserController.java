package ad.agio.test_firebase.controller;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.function.Consumer;

import ad.agio.test_firebase.domain.Chat;
import ad.agio.test_firebase.domain.Meeting;
import ad.agio.test_firebase.domain.Time;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.domain.WalkPoint;

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
     * @param consumer consumer
     */
    public void readAllUsers(Consumer<User> consumer) {
        log("readAllUsers");
        db.collection("users")
                .whereEqualTo("type", "public")
                .get()
                .addOnCompleteListener(snapshot -> {
                    if (snapshot.isSuccessful()) {
                        for (QueryDocumentSnapshot post : snapshot.getResult()) {
                            User user = post.toObject(User.class);
                            consumer.accept(user);
                        }
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }


    /**
     * 데이터베이스에 업로드 되어있는 자신의 프로필을 읽습니다.
     * @param consumer consumer
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
                .addOnCompleteListener(snapshot -> consumer.accept(snapshot.getResult()
                        .toObject(User.class)))
                .addOnFailureListener(snapshot -> consumer.accept(null));
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
        currentUser = user;
    }

    public void addWalkPoint(WalkPoint walkPoint) {
        ArrayList<WalkPoint> walkPoints = currentUser.getWalkPoints();
        walkPoints.add(walkPoint);
        updateUser("walkPoints", walkPoints);
    }

    public void removeWalkPoint(WalkPoint walkPoint) {
        ArrayList<WalkPoint> walkPoints = currentUser.getWalkPoints();
        walkPoints.remove(walkPoint);
        updateUser("walkPoints", walkPoints);
    }

    public void removeWalkPoint(int index) {
        ArrayList<WalkPoint> walkPoints = currentUser.getWalkPoints();
        walkPoints.remove(index);
        updateUser("walkPoints", walkPoints);
    }

    public void setStatus(int status) {
        updateUser("status", status);
        currentUser.setStatus(status);
    }

    /**
     * 프로필 이미지를 업로드 합니다.
     * @param path 파일경로
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

    public void writeChatId(Chat chat) {
        ArrayList<String> list = readChatId();
        if(!list.contains(chat.chatId)) {
            list.add(chat.chatId);
            String temp = TextUtils.join("|", list);
            updateUser("arrayChatId", temp);
            currentUser.setArrayChatId(temp);
        }
    }

    public void readChatId(Consumer<String> consumer) {
        ArrayList<String> split = readChatId();
        for (String chatId : split) {
            if(!chatId.equals(""))
                consumer.accept(chatId);
        }
    }

    public ArrayList<String> readChatId() {
        String[] split = currentUser.getArrayChatId().split("\\|");
        return new ArrayList<>(Arrays.asList(split));
    }

    public void removeChat(String chatId) {
        if(currentUser.getArrayChatId().contains(chatId)) {
            ArrayList<String> arr = readChatId();
            arr.remove(chatId);
            String temp = TextUtils.join("|", arr);
            currentUser.setArrayChatId(temp);
            updateUser("arrayChatId", temp);
        }
    }

    public Meeting makeMatchMeeting(User user1, User user2) {
        Meeting meeting = new Meeting();

        Time time = new Time(Calendar.getInstance());
        time.add(Calendar.MINUTE, 15); // 15분 뒤에 보자.
        meeting.time = time;

        ArrayList<WalkPoint> walkPoints1 = user1.getWalkPoints();
        ArrayList<WalkPoint> walkPoints2 = user2.getWalkPoints();
        if(walkPoints1.size() != 0)
            meeting.place = user1.getWalkPoints().get(0); // user1의 산책장소로 설정
        else if(walkPoints2.size() != 0)
            meeting.place = user1.getWalkPoints().get(0); // user1의 산책장소가 없다면, user2 사용

        return meeting;
    }

    public Meeting makeAppointMeeting(User user1, User user2) {
        Meeting meeting = new Meeting();

        Time time = new Time(Calendar.getInstance());
        time.add(Calendar.MINUTE, 15); // 15분 뒤에 보자.
        meeting.time = time;

        ArrayList<WalkPoint> walkPoints1 = user1.getWalkPoints();
        ArrayList<WalkPoint> walkPoints2 = user2.getWalkPoints();
        if(walkPoints1.size() != 0)
            meeting.place = user1.getWalkPoints().get(0); // user1의 산책장소로 설정
        else if(walkPoints2.size() != 0)
            meeting.place = user1.getWalkPoints().get(0); // user1의 산책장소가 없다면, user2 사용

        return meeting;
    }
}
