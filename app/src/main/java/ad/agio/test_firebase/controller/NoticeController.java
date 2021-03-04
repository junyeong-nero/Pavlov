package ad.agio.test_firebase.controller;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.function.Consumer;

import ad.agio.test_firebase.domain.Notice;

public class NoticeController {

    final static public String TAG = "NotificationController";
    public void log(String text) {
        Log.d(TAG, text);
    }

    private final DatabaseReference mDatabase;

    public NoticeController() {
        this.mDatabase = FirebaseDatabase.getInstance().getReference().child("notification");
    }

    /**
     * 공지정보를 받아옵니다.
     * @param consumer Notification consumer
     */
    public void readNotice(Consumer<ArrayList<Notice>> consumer) {
        mDatabase.get()
                .addOnSuccessListener(dataSnapshot -> {
                    ArrayList<Notice> arr = new ArrayList<>();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            arr.add(d.getValue(Notice.class));
                        }
                    }
                    consumer.accept(arr);
                })
                .addOnFailureListener(e -> log("readNotification: read failed"));
    }
}
