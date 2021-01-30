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

import ad.agio.test_firebase.domain.User;

public class MatchController {

    static public String TAG = "MatchController";
    public void LOGGING(String text) {
        Log.d(TAG, text);
    }

    private DatabaseReference mDatabase;
    private UserController userController;
    private AuthController authController;
    private Consumer<ArrayList<User>> receiveConsumer;


    public MatchController() {
        userController = new UserController();
        authController = new AuthController();
        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("matches");
    }

    public void addMatcher() {
        userController.readMe(me -> mDatabase.child(authController.getUid())
                .setValue(me)
                .addOnSuccessListener(task -> mDatabase.child(authController.getUid())
                        .addValueEventListener(listener)));
    }

    public void removeMatcher() {
        mDatabase.child(authController.getUid()).removeValue();
    }

    private ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String value = snapshot.child("matcher").getValue(String.class);
            if(value != null && !value.equals(""))
                LOGGING(value); // receiveConsumer.accept();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private boolean isMatching = false;

    public void startMatching(Predicate<User> condition, Consumer<ArrayList<User>> consumer) {
        findUserBy(condition, list -> { // 일단 matchers 중에서 조건을 만족하는 사람을 찾아본다.
            if(list.isEmpty()) {
                // 만족하는 사람이 없으면,
                isMatching = true;
                receiveConsumer = consumer;
                addMatcher();
            } else {
                // 만족하는 사람이 있으면, consumer 실행
                isMatching = false;
                consumer.accept(list); // receiver 위해서 messaging 하는거 여기서 구현해야됨.
            }
        });
    }

    public void pauseMatching() {
        if(isMatching) {
            mDatabase.child(authController.getUid()).removeEventListener(listener);
        }
    }

    public void stopMatching() {
        pauseMatching();
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
                        if(condition.test(post))
                            list.add(post);
                    }
                    consumer.accept(list);
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }
}
