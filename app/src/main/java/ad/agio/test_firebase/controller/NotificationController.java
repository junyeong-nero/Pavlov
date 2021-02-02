package ad.agio.test_firebase.controller;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import ad.agio.test_firebase.domain.Notification;
import ad.agio.test_firebase.domain.User;

public class NotificationController {

    final static public String TAG = "NotificationController";
    public void LOGGING(String text) {
        Log.d(TAG, text);
    }

    private final DatabaseReference mDatabase;

    public NotificationController() {
        this.mDatabase = FirebaseDatabase.getInstance().getReference().child("notification");
    }

    public void readNotification(Consumer<Notification> consumer) {
        mDatabase.get()
                .addOnSuccessListener(dataSnapshot -> consumer.accept(dataSnapshot
                        .getValue(Notification.class)))
                .addOnFailureListener(e -> LOGGING("readNotification: read failed"));
    }
}
