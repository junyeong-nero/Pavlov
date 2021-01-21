package ad.agio.test_firebase.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

import ad.agio.test_firebase.domain.User;

public class UserController {

    private Context ctx;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    static public String TAG = "UserController";

    public UserController(Context context) {
        this.ctx = context;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        this.mAuth = FirebaseAuth.getInstance();
    }

    /**
     * write new user to realtime database
     * @param user
     */
    public void writeNewUser(User user) {
        mDatabase.child("users").child(user.getId()).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    // Write was successful!
                    Toast.makeText(ctx, "저장을 완료했습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "저장을 완료했습니다.");
                })
                .addOnFailureListener(e -> {
                    // Write failed
                    Toast.makeText(ctx, "저장을 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "저장을 실패했습니다.");
                });

    }

    /**
     * read userData from realtime database.
     * @param id
     */
    public void readUser(String id){
        mDatabase.child("users").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                if(dataSnapshot.getValue(User.class) != null){
                    User post = dataSnapshot.getValue(User.class);
                    loginListener.success(post);
                } else {
                    Log.d("writeNewUser", "데이터가 없습니다.");
                    loginListener.failure(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("FireBaseData", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    public void editUserData(User user, String tag, String value){
        mDatabase.child("users").child(user.getId()).child(tag).setValue(value);
    }

    public void editUserData(String id, String tag, String value){
        mDatabase.child("users").child(id).child(tag).setValue(value);
    }

    private LoginListener loginListener;

    public void setLoginListener(LoginListener loginListener) {
        this.loginListener = loginListener;
    }

    public static class LoginListener {
        public LoginListener() { }
        public void success(User find) { }
        public void failure(User find) { }
    }
}
