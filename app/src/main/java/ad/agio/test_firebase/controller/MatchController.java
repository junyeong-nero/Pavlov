package ad.agio.test_firebase.controller;

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

    private DatabaseReference sendingDatabase;
    private UserController userController;
    private AuthController authController;

    public MatchController() {
        userController = new UserController();
        authController = new AuthController();
        sendingDatabase = FirebaseDatabase.getInstance().getReference()
                .child("matches");
    }

    public void addMatcher(User user) {
        sendingDatabase.child(user.getUid())
                .setValue(user)
                .addOnSuccessListener(o -> {
                    // success
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void removeMatcher(User user) {
        sendingDatabase.child(user.getUid()).removeValue();
    }

    private ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<User> list = new ArrayList<>();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                User post = snapshot.getValue(User.class);
                if(post != null && !post.getUid().equals(authController.getUID()))
                    list.add(post);
            }
            sendListener.send(list);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private EventListener sendListener;
    public void setSendListener(EventListener listener) {
        this.sendListener = listener;
    }

    public abstract static class EventListener {
        protected abstract void send(ArrayList<User> list);
    }

    public void startMatching() {
        sendingDatabase.addValueEventListener(listener);
    }
    public void pauseMatching() {
        sendingDatabase.removeEventListener(listener);
    }

    public void matched() {
        pauseMatching();
        sendingDatabase = null;
    }

    public void findAll(Consumer<ArrayList<User>> consumer) {
        findUserBy(user -> true, consumer);
    }

    public void findUserByName(String name, Consumer<ArrayList<User>> consumer) {
        findUserBy(user -> user.getUserName().equals(name), consumer);
    }

    public void findUserByUid(String name, Consumer<ArrayList<User>> consumer) {
        findUserBy(user -> user.getUid().equals(name), consumer);
    }

    public void findUserBy(Predicate<User> condition, Consumer<ArrayList<User>> consumer) {
        sendingDatabase.get()
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
