package ad.agio.test_firebase.controller;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import ad.agio.test_firebase.domain.User;

public class MatchController {

    static public String TAG = "MatchController";

    private DatabaseReference mDatabase;

    public MatchController() {
        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("matches");
    }

    public void addMatcher(User user) {
        mDatabase.child(user.getId())
                .setValue(user)
                .addOnSuccessListener(o -> {
                    // success
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void removeMatcher(User user) {
        mDatabase.child(user.getId()).removeValue();
    }

    private ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<User> list = new ArrayList<>();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                User post = snapshot.getValue(User.class);
                if(post != null)
                    list.add(post);
            }
            changeListener.change(list);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private EventListener changeListener;
    public void setChangeListener(EventListener listener) {
        this.changeListener = listener;
    }

    public abstract static class EventListener {
        protected abstract void change(ArrayList<User> list);
    }

    public void startMatching() {
        mDatabase.addValueEventListener(listener);
    }

    public void pauseMatching() {
        mDatabase.removeEventListener(listener);
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
