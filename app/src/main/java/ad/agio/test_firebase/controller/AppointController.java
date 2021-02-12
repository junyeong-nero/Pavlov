package ad.agio.test_firebase.controller;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AppointController {

    private DatabaseReference db;

    public AppointController() {
        this.db = FirebaseDatabase.getInstance().getReference().child("appoint");
    }
}
