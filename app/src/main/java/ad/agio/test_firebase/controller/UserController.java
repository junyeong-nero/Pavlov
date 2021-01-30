package ad.agio.test_firebase.controller;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Objects;
import java.util.function.Consumer;

import ad.agio.test_firebase.domain.User;

public class UserController {

    static public String TAG = "UserController";

    private final FirebaseFirestore mFirestore;
    private AuthController authController;
    private String UID;

    public UserController() {
        this.mFirestore = FirebaseFirestore.getInstance();
        this.authController = new AuthController();
        this.UID = authController.getUid();
    }

    public void writeNewUser(User user) {
        mFirestore.collection("users")
                .document(user.getUid()).set(user);
    }

    public void readAllUsers(Consumer<User> consumer) {
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

    public void readMe(Consumer<User> consumer) {
        readUser(UID, consumer);
    }

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

    public void readUser(Consumer<User> consumer) {
        authController.checkValidUser();
        readUser(UID, consumer);
    }

    public void updateUser(String tag, Object value) {
        mFirestore.collection("users")
                .document(authController.getUid()).update(tag, value);
    }

    public void updateUser(User user) {
        mFirestore.collection("users")
                .document(user.getUid())
                .set(user);
    }
}
