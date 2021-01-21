package ad.agio.test_firebase.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.function.Function;
import java.util.function.Predicate;

import ad.agio.test_firebase.databinding.ActivitySearchBinding;
import ad.agio.test_firebase.domain.User;

public class SearchActivity extends AppCompatActivity {

    final static public String TAG = "SearchActivity";
    private ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(v -> {
            finish();
        });

        binding.button.setOnClickListener(v -> {
            // 20 ~ 40대를 위한 lambda expression
            Predicate<User> condition = user -> user.getAge() >= 20 && user.getAge() <= 40;
            search(condition);
        });
    }

    private DatabaseReference mDatabase;

    public void search(Predicate<User> condition) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("users").orderByChild("type");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        // TODO: handle the post
                        if(postSnapshot.getValue(User.class) != null) {
                            User post = postSnapshot.getValue(User.class);
                            if (condition.test(post)) {
                                Log.d(TAG, post.toString());
                                binding.textviewLog.setText(post.toString());
                            }
                        }
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }
}