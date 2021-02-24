package ad.agio.test_firebase.controller;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import ad.agio.test_firebase.domain.User;

public class UserController {

    private void log(String s) {
        Log.d(this.getClass().getSimpleName(), s);
    }

    private final FirebaseFirestore mFirestore;
    private AuthController authController;

    public UserController() { // TODO 사용자 사진 데이터베이스 업로드
        this.mFirestore = FirebaseFirestore.getInstance();
        this.authController = new AuthController();
    }

    /**
     * 유저정보를 추가합니다.
     * @param user user
     */
    public void writeNewUser(User user) {
        log("writeNewUser : " + user.toString());
        mFirestore.collection("users")
                .document(user.getUid()).set(user);
    }

    /**
     * Firestore 에 업로드 되어있고, type 이 public 인 사용자들을 읽습니다.
     * Firestore rule 에 의해서 type 이 public 이거나 자신의 프로필만 읽을 수 있다.
     * @param consumer
     */
    public void readAllUsers(Consumer<User> consumer) {
        log("readAllUsers");
        mFirestore
                .collection("users")
                .whereEqualTo("type", "public")
                .get()
                .addOnCompleteListener(snapshot -> {
                    if (snapshot.isSuccessful()) {
                        for (QueryDocumentSnapshot post : Objects.requireNonNull(snapshot.getResult())) {
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
            readUser(authController.getUid(), consumer);
        else
            log("it is not authenticated");
    }

    /**
     * 다른 사용자의 프로필을 읽습니다.
     * @param uid 다른 사용자의 uid
     * @param consumer 사용자를 컨트롤할 consumer
     */
    public void readUser(String uid, Consumer<User> consumer) {
        mFirestore
                .collection("users")
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
        mFirestore.collection("users")
                .document(authController.getUid()).update(tag, value);
    }

    /**
     * 사용자의 프로필을 업데이트 합니다.
     * @param user 사용자 프로필
     */
    public void updateUser(User user) {
        mFirestore.collection("users")
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
            StorageReference sr = FirebaseStorage.getInstance().getReference();
            sr.child("profile_images")
                    .child(authController.getUid())
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
            StorageReference sr = FirebaseStorage.getInstance().getReference();
            sr.child("profile_images")
                    .child(uid)
                    .getBytes(Long.MAX_VALUE)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            consumer.accept(task.getResult());
                        }
                    });

        } else {
            log("is not authenticated");
        }
    }
}
