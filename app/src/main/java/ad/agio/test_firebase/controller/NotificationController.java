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

    private DatabaseReference mDatabase;

    public NotificationController(Consumer<Notification> consumer) {
        this.mDatabase = FirebaseDatabase.getInstance().getReference().child("notification");
        this.mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Notification notification = dataSnapshot.getValue(Notification.class);
                if(notification != null){
                    consumer.accept(notification);
                } else {
                    Log.d(TAG, "데이터가 없습니다.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }
}
