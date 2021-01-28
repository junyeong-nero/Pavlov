package ad.agio.test_firebase.controller;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.function.Consumer;

import ad.agio.test_firebase.domain.User;

public class UserController {

    static public String TAG = "UserController";

    private final FirebaseFirestore mFirestore;
    private final FirebaseAuth mAuth;
    private String UID;

    public UserController() {
        this.mFirestore = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.UID = mAuth.getCurrentUser().getUid();
    }

    public String getUID() {
        checkValidUser();
        return mAuth.getCurrentUser().getUid();
    }

    public void checkValidUser() {
        try {
            if (mAuth.getCurrentUser() == null)
                throw new IllegalAccessException("checkValidUser: it is not valid user");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * write new user to realtime database
     * @param user
     */
    public void writeNewUser(User user) {
        mFirestore.collection("users")
                .document(user.getId()).set(user);
    }

    public void readAllUsers(Consumer<User> consumer) {
        mFirestore
                .collection("users")
                .get()
                .addOnCompleteListener(snapshot -> {
                    if (snapshot.isSuccessful()) {
                        for (QueryDocumentSnapshot post : snapshot.getResult()) {
                            consumer.accept(post.toObject(User.class));
                        }
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    public void readUser(String uid, Consumer<User> consumer) {
        mFirestore
                .collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(snapshot -> {
                    User user = snapshot.getResult().toObject(User.class);
                    if (user.getId().equals(uid)) {
                        consumer.accept(user);
                    }
                })
                .addOnFailureListener(snapshot -> {
                    consumer.accept(null);
                });
    }

    public void readUser(Consumer<User> consumer) {
        checkValidUser();
        readUser(UID, consumer);
    }

    public void updateUser(String uid, String tag, String value) {
        mFirestore.collection("users")
                .document(uid).update(tag, value);
    }

    public void updateUser(User user) {
        mFirestore.collection("users")
                .document(user.getId())
                .set(user);
    }
}
